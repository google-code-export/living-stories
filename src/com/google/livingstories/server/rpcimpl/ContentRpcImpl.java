/**
 * Copyright 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS-IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.livingstories.server.rpcimpl;

import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.repackaged.com.google.common.base.Function;
import com.google.appengine.repackaged.com.google.common.collect.ImmutableList;
import com.google.appengine.repackaged.com.google.common.collect.Lists;
import com.google.appengine.repackaged.com.google.common.collect.Maps;
import com.google.appengine.repackaged.com.google.common.collect.Sets;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.livingstories.client.AssetAtom;
import com.google.livingstories.client.AtomType;
import com.google.livingstories.client.BackgroundAtom;
import com.google.livingstories.client.BaseAtom;
import com.google.livingstories.client.ContentRpcService;
import com.google.livingstories.client.DisplayAtomBundle;
import com.google.livingstories.client.EventAtom;
import com.google.livingstories.client.FilterSpec;
import com.google.livingstories.client.Importance;
import com.google.livingstories.client.NarrativeAtom;
import com.google.livingstories.client.PlayerAtom;
import com.google.livingstories.client.PublishState;
import com.google.livingstories.client.contentmanager.SearchTerms;
import com.google.livingstories.client.util.GlobalUtil;
import com.google.livingstories.client.util.SnippetUtil;
import com.google.livingstories.client.util.dom.JavaNodeAdapter;
import com.google.livingstories.server.BaseContentEntity;
import com.google.livingstories.server.LivingStoryEntity;
import com.google.livingstories.server.UserLivingStoryEntity;
import com.google.livingstories.server.dataservices.impl.PMF;
import com.google.livingstories.server.util.AlertSender;
import com.google.livingstories.server.util.StringUtil;
import com.google.livingstories.servlet.ExternalServiceKeyChain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;
import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpServletRequest;

/**
 * Implementation of the RPC service that is used for reading and writing {@link BaseContentEntity}
 * objects to the AppEngine datastore. This service converts the {@link BaseContentEntity} data
 * objects to {@link BaseAtom} for the client use.
 */
public class ContentRpcImpl extends RemoteServiceServlet implements ContentRpcService {
  public static final int ATOM_COUNT_LIMIT = 20;  
  public static final int JUMP_TO_ATOM_CONTEXT_COUNT = 3;
  private static final int EMAIL_ALERT_SNIPPET_LENGTH = 500;
  
  private static final Logger logger =
      Logger.getLogger(ContentRpcImpl.class.getCanonicalName());
  
  private InternetAddress fromAddress = null;

  @Override
  public synchronized BaseAtom createOrChangeAtom(BaseAtom clientAtom) {
    // Get the list of atoms to link within the content first so that if there is an exception with
    // the queries, it doesn't affect the saving of the atom. Except for unassigned atoms and
    // player atoms because we don't auto-link from their content. Or if the atom doesn't have
    // any content.
    boolean runAutoLink = clientAtom.getLivingStoryId() != null 
        && clientAtom.getAtomType() != AtomType.PLAYER
        && !GlobalUtil.isContentEmpty(clientAtom.getContent());
    List<PlayerAtom> playerAtoms = null;
    List<BackgroundAtom> concepts = null;
    
    try {
      if (runAutoLink) {
        playerAtoms = getPlayers(clientAtom.getLivingStoryId());
        concepts = getConcepts(clientAtom.getLivingStoryId());
      }
    } catch (Exception e) {
      logger.warning("Skipping auto-linking. Error with retrieving players or concepts."
          + e.getMessage());
      runAutoLink = false;
    }
    
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Transaction tx = null;
    BaseContentEntity atomEntity;
    PublishState oldPublishState = null;
    
    Set<Long> newLinkedAtomSuggestions = null;
    
    try {
      if (clientAtom.getId() != null) {
        atomEntity = pm.getObjectById(BaseContentEntity.class, clientAtom.getId());
        oldPublishState = atomEntity.getPublishState();
        atomEntity.copyFields(clientAtom);
      } else {
        atomEntity = BaseContentEntity.fromClientObject(clientAtom);
      }
      
      if (runAutoLink) {
        newLinkedAtomSuggestions = 
            AutoLinkEntitiesInContent.createLinks(atomEntity, playerAtoms, concepts);
      }

      tx = pm.currentTransaction();
      tx.begin();
      pm.makePersistent(atomEntity);
      tx.commit();
      
      // If this was an event or a narrative and had a linked narrative, then the 'standalone'
      // field on the narrative atom needs to be updated to 'false'.
      // Note: this doesn't handle the case of unlinking a previously linked narrative atom.
      // That would require checking the linked atoms of every single other event atom to make
      // sure it's not linked to from anywhere else, which would be an expensive operation.
      Set<Long> linkedAtomIds = atomEntity.getLinkedAtomIds();
      AtomType atomType = atomEntity.getAtomType();
      if ((atomType == AtomType.EVENT || atomType == AtomType.NARRATIVE)
          && !linkedAtomIds.isEmpty()) {
        List<Object> oids = new ArrayList<Object>(linkedAtomIds.size());
        for (Long id : linkedAtomIds) {
          oids.add(pm.newObjectIdInstance(BaseContentEntity.class, id));
        }

        @SuppressWarnings("unchecked")
        Collection<BaseContentEntity> linkedAtoms = pm.getObjectsById(oids);
        for (BaseContentEntity linkedAtom : linkedAtoms) {
          if (linkedAtom.getAtomType() == AtomType.NARRATIVE) {
            linkedAtom.setIsStandalone(false);
          }
        }
      }

      // TODO(ericzhang): may also want to invalidate linked atoms if they changed
      // and aren't from the same living story.
      invalidateCache(clientAtom.getLivingStoryId());
    } finally {
      if (tx != null && tx.isActive()) {
        tx.rollback();
      }
      pm.close();
    }
    
    // Send email alerts if an event atom was changed from 'Draft' to 'Published'
    if (atomEntity.getAtomType() == AtomType.EVENT
        && atomEntity.getPublishState() == PublishState.PUBLISHED
        && oldPublishState != null && oldPublishState == PublishState.DRAFT) {
      sendEmailAlerts((EventAtom)clientAtom);
    }

    // We pass suggested new linked atoms back to the client by adding their ids to the
    // client object before returning it. It's the client's responsibility to check
    // the linked atom ids it passed in with those that came back, and to present appropriate
    // UI for processing the suggestions. Note that we shouldn't add the suggestions directly
    // to atomEntity! This will persist them to the datastore prematurely.
    BaseAtom ret = atomEntity.toClientObject();
    
    if (newLinkedAtomSuggestions != null) {
      ret.addAllLinkedAtomIds(newLinkedAtomSuggestions);
    }
    
    return ret;
  }
  
  @Override
  public List<PlayerAtom> getUnassignedPlayers() {
    return getPlayers(null);
  }
  
  private List<PlayerAtom> getPlayers(Long livingStoryId) {
    List<BaseContentEntity> playerEntities =
        getPublishedAtomsByType(livingStoryId, AtomType.PLAYER);
    List<PlayerAtom> playerAtoms = Lists.newArrayList();
    for (BaseContentEntity playerEntity : playerEntities) {
      playerAtoms.add((PlayerAtom)(playerEntity.toClientObject()));
    }
    return playerAtoms;
  }
  
  private List<BackgroundAtom> getConcepts(Long livingStoryId) {
    List<BaseContentEntity> backgroundEntities = 
        getPublishedAtomsByType(livingStoryId, AtomType.BACKGROUND);
    List<BackgroundAtom> backgroundAtoms = Lists.newArrayList();
    for (BaseContentEntity backgroundEntity : backgroundEntities) {
      if (!GlobalUtil.isContentEmpty(backgroundEntity.getName())) {
        backgroundAtoms.add((BackgroundAtom)(backgroundEntity.toClientObject()));
      }
    }
    return backgroundAtoms;
  }
  
  private List<BaseContentEntity> getPublishedAtomsByType(Long livingStoryId, AtomType atomType) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    
    Query query = pm.newQuery(BaseContentEntity.class); 
    query.setFilter("livingStoryId == livingStoryIdParam " +
        "&& publishState == com.google.livingstories.client.PublishState.PUBLISHED " +
        "&& atomType == '" + atomType.name() + "'");
    query.declareParameters("java.lang.Long livingStoryIdParam");
    
    try {
      @SuppressWarnings("unchecked")
      List<BaseContentEntity> entities = (List<BaseContentEntity>) query.execute(livingStoryId);
      pm.retrieveAll(entities);
      return entities;
    } finally {
      query.closeAll();
      pm.close();
    }
  }
  
  private void sendEmailAlerts(EventAtom eventAtom) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    
    // Get list of users
    Query query = pm.newQuery(UserLivingStoryEntity.class); 
    query.setFilter("livingStoryId == livingStoryIdParam && subscribedToEmails == true");
    query.declareParameters("long livingStoryIdParam");
    
    try {
      @SuppressWarnings("unchecked")
      List<UserLivingStoryEntity> userLivingStoryEntities =
          (List<UserLivingStoryEntity>) query.execute(eventAtom.getLivingStoryId());
      List<String> users = new ArrayList<String>();
      for (UserLivingStoryEntity entity : userLivingStoryEntities) {
        users.add(entity.getId().getParent().getName());
      }
      if (!users.isEmpty()) {
        LivingStoryEntity livingStory = pm.getObjectById(LivingStoryEntity.class,
            eventAtom.getLivingStoryId());
        String baseLspUrl = getBaseServerUrl() + "/lsps/" + livingStory.getUrl();
        
        StringBuilder emailContent = new StringBuilder("<b>");
        emailContent.append(eventAtom.getEventUpdate()).append("</b>");
        emailContent.append("<span style=\"color: #777;\">&nbsp;-&nbsp;");
        emailContent.append(livingStory.getPublisher().toString());
        emailContent.append("</span>");
        String eventSummary = eventAtom.getEventSummary();
        String eventDetails = eventAtom.getContent();
        if (GlobalUtil.isContentEmpty(eventSummary) 
            && !GlobalUtil.isContentEmpty(eventDetails)) {
          eventSummary = SnippetUtil.createSnippet(JavaNodeAdapter.fromHtml(eventDetails), 
                  EMAIL_ALERT_SNIPPET_LENGTH);
        }
        if (eventSummary != null && !eventSummary.isEmpty()) {
          emailContent.append("<br><br>").append(StringUtil.stripForExternalSites(eventSummary));
        }
        emailContent.append("<br><a href=\"")
            .append(baseLspUrl)
            .append("#OVERVIEW:false,false,false,n,n,n:")
            .append(eventAtom.getId())
            .append(";\">Read more</a>")
            .append("<br><br>-----<br>")
            .append("<span style=\"font-size:small\">This is an automated alert. ")
            .append("To unsubscribe, click the 'unsubscribe' link on the top right of ")
            .append("<a href=\"")
            .append(UserServiceFactory.getUserService().createLoginURL(baseLspUrl))
            .append("\">this page</a>.</span>");

        // getServletContext() doesn't return a valid result at construction-time, so
        // we initialize fromAddress lazily.
        if (fromAddress == null) {
          fromAddress = new ExternalServiceKeyChain(getServletContext()).getFromAddress();
        }

        AlertSender.sendEmail(fromAddress, users,
            "Update: " + livingStory.getTitle(), emailContent.toString());
        
      }
    } finally {
      query.closeAll();
      pm.close();
    }

  }
  
  private String getBaseServerUrl() {
    HttpServletRequest request = super.getThreadLocalRequest();
    StringBuffer url = request.getRequestURL();
    return url.substring(0, url.length() - request.getRequestURI().length());
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public synchronized List<BaseAtom> getAtomsForLivingStory(
      Long livingStoryId, boolean onlyPublished) {
    List<BaseAtom> atoms = Caches.getLivingStoryAtoms(livingStoryId, onlyPublished);
    if
    (atoms != null) {
      return atoms;
    }
 
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query query = pm.newQuery(BaseContentEntity.class);
    query.setFilter("livingStoryId == livingStoryIdParam"
        + (onlyPublished ? "&& publishState == '" + PublishState.PUBLISHED.name() + "'" : ""));
    query.setOrdering("timestamp desc");
    query.declareParameters("java.lang.Long livingStoryIdParam");

    try {
      List<BaseAtom> clientAtoms = new ArrayList<BaseAtom>();
      
      @SuppressWarnings("unchecked")
      List<BaseContentEntity> results = (List<BaseContentEntity>) query.execute(livingStoryId);
      for (BaseContentEntity result : results) {
        BaseAtom atom = result.toClientObject();
        clientAtoms.add(atom);
      }
      Caches.setLivingStoryAtoms(livingStoryId, onlyPublished, clientAtoms);
      return clientAtoms;
    } finally {
      query.closeAll();
      pm.close();
    }
  }
    
  /**
   * Gets the eventBundle for a given date range within a living story. 
   * @param livingStoryId the relevant story's id.
   * @param filterSpec a specification of how to filter the results
   * @param focusedAtomId An optional atom that should be included in the returned
   *    list.  Specifying this parameter causes the method to return all atoms
   *    from cutoff up until 3 atoms after the focused atom.  Otherwise, the
   *    method just returns the first 20 atoms after cutoff.
   * @param cutoff Do not return atoms that sort earlier/later than this date (exclusive)
   *    (Depends on order specified in filterSpec). Null if there is no bound.
   * @return an appropriate DisplayAtomBundle
   */
  @Override
  public synchronized DisplayAtomBundle getDisplayAtomBundle(Long livingStoryId,
      FilterSpec filterSpec, Long focusedAtomId, Date cutoff) {
    if (filterSpec.contributorId != null || filterSpec.playerId != null) {
      throw new IllegalArgumentException(
          "filterSpec.contributorId and filterSpec.playerId should not be set by remote callers."
          + " contributorId = " + filterSpec.contributorId + " playerId = "+ filterSpec.playerId);
    }
    DisplayAtomBundle result = Caches.getDisplayAtomBundle(
        livingStoryId, filterSpec, focusedAtomId, cutoff);
    if (result != null) {
      return result;
    }
    
    FilterSpec localFilterSpec = new FilterSpec(filterSpec);
    
    BaseAtom focusedAtom = null;
    if (focusedAtomId != null) {
      focusedAtom = getAtom(focusedAtomId, false);
      if (focusedAtom != null) {
        if (adjustFilterSpecForAtom(localFilterSpec, focusedAtom)) {
          // If we had to adjust the filter spec to accommodate the focused atom,
          // we'll be switching filter views, so we want to clear the start date
          // and reload the list from the beginning.
          cutoff = null;
        }
      }
    }
    
    // Some preliminaries. Note that the present implementation just filters all atoms for
    // a story, which could be a bit expensive if there's a cache miss. By and large, though,
    // we'd expect a lot more cache hits than cache misses, unlike the case with, say,
    // a twitter "following" feed, which is more likely to be unique to that user.
    List<BaseAtom> allAtoms = getAtomsForLivingStory(livingStoryId, true);
    
    Map<Long, BaseAtom> idToAtomMap = Maps.newHashMap();
    List<BaseAtom> relevantAtoms = Lists.newArrayList();
    for (BaseAtom atom : allAtoms) {
      idToAtomMap.put(atom.getId(), atom);
      
      Date sortKey = atom.getDateSortKey();
      boolean matchesStartDate = (cutoff == null) ||
          (localFilterSpec.oldestFirst ? !sortKey.before(cutoff) : !sortKey.after(cutoff));

      if (matchesStartDate && localFilterSpec.doesAtomMatch(atom)) {
        relevantAtoms.add(atom);
      }
    }
    sortBaseAtomList(relevantAtoms, localFilterSpec);

    // Need to get the focused atom from the idToAtomMap instead of using the object directly.
    // This is because we use indexOf() to find the location of the focused atom in the list,
    // and the original atom isn't the same object instance.
    List<BaseAtom> coreAtoms = getSublist(relevantAtoms,
        focusedAtom == null ? null : idToAtomMap.get(focusedAtomId), cutoff);
    Set<Long> linkedAtomIds = Sets.newHashSet();
    
    for (BaseAtom atom : coreAtoms) {
      if (atom.displayTopLevel()) {
        // If an atom isn't a top-level display atom, we can get away without returning its
        // linked atoms.
        linkedAtomIds.addAll(atom.getLinkedAtomIds());
      }
    }

    Set<BaseAtom> linkedAtoms = Sets.newHashSet();
    for (Long id : linkedAtomIds) {
      BaseAtom linkedAtom = idToAtomMap.get(id);
      if (linkedAtom == null) {
        System.err.println("Linked atom with id " + id + " is not found.");
      } else {
        linkedAtoms.add(linkedAtom);
        // For linked narratives, we want to get their own linked atoms as well
        if (linkedAtom.getAtomType() == AtomType.NARRATIVE) {
          for (Long linkedToLinkedAtomId : linkedAtom.getLinkedAtomIds()) {
            BaseAtom linkedToLinkedAtom = idToAtomMap.get(linkedToLinkedAtomId);
            if (linkedToLinkedAtom != null) {
              linkedAtoms.add(linkedToLinkedAtom);
            }
          }
        }
      }
    }
    
    Date nextDateInSequence = getNextDateInSequence(coreAtoms, relevantAtoms);

    result = new DisplayAtomBundle(coreAtoms, linkedAtoms, nextDateInSequence, localFilterSpec);
    Caches.setDisplayAtomBundle(livingStoryId, filterSpec, focusedAtomId, cutoff, result);
    return result;
  }

  /**
   * Check if the atom matches the filterSpec.  If not, this method adjusts the filter
   * spec so that the atom will match.
   * @return whether or not the filterSpec was adjusted.
   */
  private boolean adjustFilterSpecForAtom(FilterSpec filterSpec, BaseAtom atom) {
    if (filterSpec.doesAtomMatch(atom)) {
      return false;
    }
    if (filterSpec.themeId != null && !atom.getThemeIds().contains(filterSpec.themeId)) {
      filterSpec.themeId = null;
    }
    if (filterSpec.importantOnly && atom.getImportance() != Importance.HIGH) {
      filterSpec.importantOnly = false;
    }
    if (filterSpec.atomType != atom.getAtomType()) {
      filterSpec.atomType = null;
    } else if (atom.getAtomType() == AtomType.ASSET
        && filterSpec.assetType != ((AssetAtom) atom).getAssetType()) {
      filterSpec.atomType = null;
      filterSpec.assetType = null;
    }
    if (filterSpec.opinion && (atom.getAtomType() != AtomType.NARRATIVE
        || !((NarrativeAtom) atom).isOpinion())) {
      filterSpec.opinion = false;
    }
    return true;
  }
  
  private void sortBaseAtomList(List<BaseAtom> atoms, FilterSpec filterSpec) {
    Collections.sort(
        atoms, filterSpec.oldestFirst ? BaseAtom.COMPARATOR : BaseAtom.REVERSE_COMPARATOR);
  }
  
  private List<BaseAtom> getSublist(List<BaseAtom> allAtoms, BaseAtom focusedAtom, Date cutoff) {
    int atomLimit;
    if (focusedAtom == null) {
      atomLimit = ATOM_COUNT_LIMIT;
    } else {
      atomLimit = allAtoms.indexOf(focusedAtom) + 1 + JUMP_TO_ATOM_CONTEXT_COUNT;
      // If we are not appending atoms and there are less than 20 results because of a focussed
      // atom, bump the limit up to 20
      if (cutoff == null && atomLimit < ATOM_COUNT_LIMIT) {
        atomLimit = ATOM_COUNT_LIMIT;
      }
    }
    atomLimit = Math.min(allAtoms.size(), atomLimit);

    while (atomLimit < allAtoms.size() - 1) {
      Date thisAtomDate = allAtoms.get(atomLimit).getDateSortKey();
      Date nextAtomDate = allAtoms.get(atomLimit + 1).getDateSortKey();
      if (!thisAtomDate.equals(nextAtomDate)) {
        break;
      }
      atomLimit++;
    }
    
    // Copy the sublist into a new ArrayList since the sublist() method returns
    // a view backed by the original list, which includes a bunch of atoms we don't
    // care about.
    return new ArrayList<BaseAtom>(allAtoms.subList(0, atomLimit));
  }

  /**
   * We return the date of the atom after the last core atom returned
   * as the 'next date in sequence', which we will use as the startDate in this method
   * on the next call, when the user wants more atoms.
   * Very rare corner case:
   * If the user loads up the page, an atom is added whose date falls between 
   * the date of the last atom returned and the next date in sequence, and then
   * the user clicks 'view more', we'll miss displaying that new atom.
   * We don't really care about this corner case though, since it will almost
   * never happen.
   */
  private Date getNextDateInSequence(List<BaseAtom> coreAtoms, List<BaseAtom> relevantAtoms) {
    return coreAtoms.size() < relevantAtoms.size()
        ? relevantAtoms.get(coreAtoms.size()).getDateSortKey() : null;
  }
  
  @Override
  public synchronized BaseAtom getAtom(Long id, boolean getLinkedAtoms) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    
    try {
      BaseAtom atom = pm.getObjectById(BaseContentEntity.class, id).toClientObject();
      if (getLinkedAtoms) {
        atom.setLinkedAtoms(getAtoms(atom.getLinkedAtomIds()));
      }
      return atom;
    } catch (JDOObjectNotFoundException e) {
      return null;
    } finally {
      pm.close();
    }
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public synchronized List<BaseAtom> getAtoms(Collection<Long> ids) {
    if (ids.isEmpty()) {
      return new ArrayList<BaseAtom>();
    }
    
    PersistenceManager pm = PMF.get().getPersistenceManager();
    List<Object> oids = new ArrayList<Object>(ids.size());
    for (Long id : ids) {
      oids.add(pm.newObjectIdInstance(BaseContentEntity.class, id));
    }
    
    try {
      Collection results = pm.getObjectsById(oids);
      List<BaseAtom> atoms = new ArrayList<BaseAtom>(results.size());
      for (Object result : results) {
        atoms.add(((BaseContentEntity)result).toClientObject());
      }
      return atoms;
    } finally {
      pm.close();
    }
  }

  @Override
  public synchronized DisplayAtomBundle getRelatedAtoms(
      Long atomId, boolean byContribution, Date cutoff) {
    // translate atomId and byContribution into an appropriate FilterSpec, which we use
    // to respond from cache instead of by making fresh queries.
    FilterSpec filterSpec = new FilterSpec();
    if (byContribution) {
      filterSpec.contributorId = atomId;
    } else {
      filterSpec.playerId = atomId;
    }
    DisplayAtomBundle result = Caches.getDisplayAtomBundle(null, filterSpec, null, cutoff);
    if (result != null) {
      return result;
    }
    
    PersistenceManager pm = PMF.get().getPersistenceManager();

    Query query = pm.newQuery(BaseContentEntity.class);
    String atomIdClause =
      byContribution ? "contributorIds == atomIdParam" : "linkedAtomIds == atomIdParam";
    query.setFilter(atomIdClause
        + " && publishState == '" + PublishState.PUBLISHED.name() + "'");
    // no need to explicitly set ordering, as we resort by display order.
    query.declareParameters("java.lang.Long atomIdParam");

    try {
      List<BaseAtom> relevantAtoms = new ArrayList<BaseAtom>();
      
      @SuppressWarnings("unchecked")
      List<BaseContentEntity> atomEntities = (List<BaseContentEntity>) query.execute(atomId);
      for (BaseContentEntity atomEntity : atomEntities) {
        BaseAtom atom = atomEntity.toClientObject();
        if (cutoff == null || !atom.getDateSortKey().after(cutoff)) {
          relevantAtoms.add(atom);
        }
      }
      
      // sort and put a window on the list, get the next date in the sequence
      sortBaseAtomList(relevantAtoms, filterSpec);
      List<BaseAtom> coreAtoms = getSublist(relevantAtoms, null, cutoff); 
      Date nextDateInSequence = getNextDateInSequence(coreAtoms, relevantAtoms);
      
      result = new DisplayAtomBundle(coreAtoms, Collections.<BaseAtom>emptySet(),
          nextDateInSequence, filterSpec);
      Caches.setDisplayAtomBundle(null, filterSpec, null, cutoff, result);
      return result;
    } finally {
      query.closeAll();
      pm.close();
    }
  }
  
  /**
   * Performs an atom query given a set of search filter terms.
   * 
   * For all search combinations to be successful, we require the existence
   * of several indexes:
   * - LivingStoryId/PublishState/Timestamp (minimum, required fields)
   * - LivingStoryId/PublishState/Timestamp/AtomType
   * - LivingStoryId/PublishState/Timestamp/AtomType/PlayerType
   * - LivingStoryId/PublishState/Timestamp/AtomType/AssetType
   * - LivingStoryId/PublishState/Timestamp/AtomType/NarrativeType
   * - LivingStoryId/PublishState/Timestamp/Importance/AtomType
   * - LivingStoryId/PublishState/Timestamp/Importance/AtomType/PlayerType
   * - LivingStoryId/PublishState/Timestamp/Importance/AtomType/AssetType
   * - LivingStoryId/PublishState/Timestamp/Importance/AtomType/NarrativeType
   */
  @Override
  public List<BaseAtom> executeSearch(SearchTerms searchTerms) {
    PersistenceManager pm = PMF.get().getPersistenceManager();

    Query query = pm.newQuery(BaseContentEntity.class);
    
    StringBuilder queryFilters = new StringBuilder(
        "livingStoryId == " + String.valueOf(searchTerms.livingStoryId)
        + " && publishState == '" + searchTerms.publishState.name() + "'");
    
    // Optional filter: Date
    if (searchTerms.beforeDate != null) {
      queryFilters.append(" && timestamp < beforeDateParam");
    }
    if (searchTerms.afterDate != null) {
      queryFilters.append(" && timestamp >= afterDateParam");
    }
    
    // Optional filter: Importance
    if (searchTerms.importance != null) {
      queryFilters.append(" && importance == '" + searchTerms.importance.name() + "'");
    }
    
    // Optional filter: atom type
    if (searchTerms.atomType != null) {
      queryFilters.append( "&& atomType == '" + searchTerms.atomType.name() + "'");
    }
    
    // Optional filter: atom subtype
    if (searchTerms.atomType == AtomType.PLAYER && searchTerms.playerType != null) {
      queryFilters.append(" && playerType == '" + searchTerms.playerType.name() + "'");
    } else if (searchTerms.atomType == AtomType.ASSET && searchTerms.assetType != null) {
      queryFilters.append(" && assetType == '" + searchTerms.assetType.name() + "'");
    } else if (searchTerms.atomType == AtomType.NARRATIVE && searchTerms.narrativeType != null) {
      queryFilters.append(" && narrativeType == '" + searchTerms.narrativeType.name() + "'");
    }
    
    query.setFilter(queryFilters.toString());
    query.declareParameters("java.util.Date beforeDateParam, java.util.Date afterDateParam");
    query.setOrdering("timestamp desc");

    try {
      List<BaseAtom> clientAtoms = new ArrayList<BaseAtom>();
      
      @SuppressWarnings("unchecked")
      List<BaseContentEntity> results =
          (List<BaseContentEntity>) query.execute(searchTerms.beforeDate, searchTerms.afterDate);
      for (BaseContentEntity result : results) {
        BaseAtom atom = result.toClientObject();
        clientAtoms.add(atom);
      }
      return clientAtoms;
    } finally {
      query.closeAll();
      pm.close();
    }
  }
  

  
  @Override
  public synchronized void deleteAtom(final Long id) {
    PersistenceManager pm = PMF.get().getPersistenceManager();

    try {
      BaseContentEntity atomEntity = pm.getObjectById(BaseContentEntity.class, id);

      updateAtomReferencesHelper(pm, "linkedAtomIds", id,
          new Function<BaseContentEntity, Void>() {
            public Void apply(BaseContentEntity atom) { 
              atom.removeLinkedAtomId(id); return null;
            }
          });

      // If deleting a contributor as well, update relevant contributor ids too.
      if (atomEntity.getAtomType() == AtomType.PLAYER) {
        updateAtomReferencesHelper(pm, "contributorIds", id,
            new Function<BaseContentEntity, Void>() {
              public Void apply(BaseContentEntity atom) {
                atom.removeContributorId(id); return null;
              }
            });
      }
      
      invalidateCache(atomEntity.getLivingStoryId());
      pm.deletePersistent(atomEntity);
    } finally {
      pm.close();
    }
  }
  
  private void invalidateCache(Long livingStoryId) {
    Caches.clearLivingStoryAtoms(livingStoryId);
    Caches.clearLivingStoryThemeInfo(livingStoryId);
    Caches.clearStartPageBundle();
  }
  
  /**
   * Helper method that updates atoms that refer to an atom to-be-deleted. 
   * @param pm the persistence manager
   * @param relevantField relevant field name for the query
   * @param removeFunc a Function to apply to the results of the query
   * @param id the id of the to-be-deleted atom
   */
  private void updateAtomReferencesHelper(PersistenceManager pm, String relevantField, Long id,
      Function<BaseContentEntity, Void> removeFunc) {
    Query query = pm.newQuery(BaseContentEntity.class);
    query.setFilter(relevantField + " == atomIdParam");
    query.declareParameters("java.lang.Long atomIdParam");
    try {
      @SuppressWarnings("unchecked")
      List<BaseContentEntity> results = (List<BaseContentEntity>) query.execute(id);
      for (BaseContentEntity atom : results) {
        removeFunc.apply(atom);
      }
      pm.makePersistentAll(results);
    } finally {
      query.closeAll();
    }
  }
  
  private List<Query> getUpdateQueries(PersistenceManager pm, Date timeParam, int range) {
    String commonQueryFilter = "livingStoryId == livingStoryIdParam "
        + "&& publishState == com.google.livingstories.client.PublishState.PUBLISHED "
        + (timeParam == null ? "" : "&& timestamp > timeParam ");
    
    Query eventsQuery = pm.newQuery(BaseContentEntity.class);
    eventsQuery.setFilter(commonQueryFilter +
        "&& atomType == com.google.livingstories.client.AtomType.EVENT");
    eventsQuery.setOrdering("timestamp desc");
    if (range != 0) {
      eventsQuery.setRange(0, range);
    }
    eventsQuery.declareParameters("Long livingStoryIdParam" 
        + (timeParam == null ? "" : ", java.util.Date timeParam"));
    
    Query narrativesQuery = pm.newQuery(BaseContentEntity.class);
    narrativesQuery.setFilter(commonQueryFilter +
        "&& atomType == com.google.livingstories.client.AtomType.NARRATIVE " +
        "&& isStandalone == true");
    narrativesQuery.setOrdering("timestamp desc");
    if (range != 0) {
      narrativesQuery.setRange(0, range);
    }
    narrativesQuery.declareParameters("Long livingStoryIdParam" 
        + (timeParam == null ? "" : ", java.util.Date timeParam"));
    
    return ImmutableList.of(eventsQuery, narrativesQuery);
  }
  
  @Override
  public synchronized Integer getUpdateCountSinceTime(Long livingStoryId, Date time) {
    PersistenceManager pm = PMF.get().getPersistenceManager();

    List<Query> updateQueries = getUpdateQueries(pm, time, 0);
    
    try {
      int result = 0;
      for (Query query : updateQueries) {
        query.setResult("count(id)");
        result += (Integer) query.execute(livingStoryId, time);
      }
      return result;
    } finally {
      for (Query query : updateQueries) {
        query.closeAll();
      }
      pm.close();
    }
  }
  
  @Override
  public List<BaseAtom> getUpdatesSinceTime(Long livingStoryId, Date time) {
    PersistenceManager pm = PMF.get().getPersistenceManager();

    List<Query> updateQueries = getUpdateQueries(pm, time, 0);
    
    try {
      List<BaseAtom> updates = new ArrayList<BaseAtom>();
      for (Query query : updateQueries) {
        @SuppressWarnings("unchecked")
        List<BaseContentEntity> results = 
            (List<BaseContentEntity>) query.execute(livingStoryId, time);
        for (BaseContentEntity result : results) {
          BaseAtom atom = result.toClientObject();
          updates.add(atom);
        }
      }
      return updates;
    } finally {
      for (Query query : updateQueries) {
        query.closeAll();
      }
      pm.close();
    }
  }
  
  /**
   * Return the latest 3 updates on top-level display items for a story, sorted in reverse-
   * chronological order.
   */
  public List<BaseAtom> getUpdatesForStartPage(Long livingStoryId) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    List<Query> updateQueries = getUpdateQueries(pm, null, 3);
    try {
      List<BaseAtom> updates = new ArrayList<BaseAtom>();
      // Get the latest 3 events and latest 3 narratives and then return the latest 3 items
      // from those 6 because there is no way to do one appengine query for that
      for (Query query : updateQueries) {
        @SuppressWarnings("unchecked")
        List<BaseContentEntity> results = (List<BaseContentEntity>) query.execute(livingStoryId);
        for (BaseContentEntity result : results) {
          BaseAtom atom = result.toClientObject();
          updates.add(atom);
        }
      }
      Collections.sort(updates, BaseAtom.REVERSE_COMPARATOR);
      // Just return the latest 3 updates
      return new ArrayList<BaseAtom>(updates.subList(0, Math.min(3, updates.size())));
    } finally {
      for (Query query : updateQueries) {
        query.closeAll();
      }
      pm.close();
    }
  }
  
  @Override
  public List<EventAtom> getImportantEventsForLivingStory(Long livingStoryId) {
    PersistenceManager pm = PMF.get().getPersistenceManager();

    Query query = pm.newQuery(BaseContentEntity.class);
    query.setFilter("livingStoryId == livingStoryIdParam " +
        "&& atomType == com.google.livingstories.client.AtomType.EVENT " +
        "&& importance == com.google.livingstories.client.Importance.HIGH " +
        "&& publishState == com.google.livingstories.client.PublishState.PUBLISHED");
    query.declareParameters("Long livingStoryIdParam");
    
    try {
      @SuppressWarnings("unchecked")
      List<BaseContentEntity> results = 
          (List<BaseContentEntity>) query.execute(livingStoryId);
      List<EventAtom> events = new ArrayList<EventAtom>();
      for (BaseContentEntity result : results) {
        EventAtom event = (EventAtom) result.toClientObject();
        events.add(event);
      }
      Collections.sort(events, BaseAtom.REVERSE_COMPARATOR);
      return events;
    } finally {
      query.closeAll();
      pm.close();
    }
  }
  
  /**
   * This method will return a list of all the players in the living story,
   * sorted by importance.  Our importance ranking is currently based solely
   * on the number of atoms in the living story that are linked to each player. 
   */
  @Override
  public List<PlayerAtom> getImportantPlayersForLivingStory(Long livingStoryId) {
    PersistenceManager pm = PMF.get().getPersistenceManager();

    Query query = pm.newQuery(BaseContentEntity.class);
    query.setFilter("livingStoryId == livingStoryIdParam " +
        "&& atomType == com.google.livingstories.client.AtomType.PLAYER " +
        "&& importance == com.google.livingstories.client.Importance.HIGH " +
        "&& publishState == com.google.livingstories.client.PublishState.PUBLISHED");
    query.declareParameters("Long livingStoryIdParam");
    
    List<PlayerAtom> players = Lists.newArrayList();
    try {
      @SuppressWarnings("unchecked")
      List<BaseContentEntity> results = 
          (List<BaseContentEntity>) query.execute(livingStoryId);
      for (BaseContentEntity result : results) {
        players.add((PlayerAtom) result.toClientObject());
      }
    } finally {
      query.closeAll();
      pm.close();
    }
    return players;
  }
  
  /**
   * Returns all the contributors for this living story.
   */
  @Override
  public Map<Long, PlayerAtom> getContributorsByIdForLivingStory(Long livingStoryId) {
    Map<Long, PlayerAtom> result = Caches.getContributorsForLivingStory(livingStoryId);
    if (result != null) {
      return result;
    }
    
    List<BaseAtom> allAtoms = getAtomsForLivingStory(livingStoryId, true);
    
    Set<Long> allContributorIds = new HashSet<Long>();
    for (BaseAtom atom : allAtoms) {
      allContributorIds.addAll(atom.getContributorIds());
    }
    
    List<BaseAtom> contributors = getAtoms(allContributorIds);
    
    result = new HashMap<Long, PlayerAtom>();
    for (BaseAtom contributor : contributors) {
      if (contributor.getAtomType() == AtomType.PLAYER) {
        result.put(contributor.getId(), (PlayerAtom) contributor);
      } else {
        logger.warning("Contributor id " + contributor.getId() + " does not map to a player");
      }
    }

    Caches.setContributorsForLivingStory(livingStoryId, result);
    return result;
  }
}
