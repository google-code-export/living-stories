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
import com.google.livingstories.client.AssetContentItem;
import com.google.livingstories.client.ContentItemType;
import com.google.livingstories.client.BackgroundContentItem;
import com.google.livingstories.client.BaseContentItem;
import com.google.livingstories.client.ContentRpcService;
import com.google.livingstories.client.DisplayContentItemBundle;
import com.google.livingstories.client.EventContentItem;
import com.google.livingstories.client.FilterSpec;
import com.google.livingstories.client.Importance;
import com.google.livingstories.client.NarrativeContentItem;
import com.google.livingstories.client.PlayerContentItem;
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
 * objects to {@link BaseContentItem} for the client use.
 */
public class ContentRpcImpl extends RemoteServiceServlet implements ContentRpcService {
  public static final int CONTENT_ITEM_COUNT_LIMIT = 20;  
  public static final int JUMP_TO_CONTENT_ITEM_CONTEXT_COUNT = 3;
  private static final int EMAIL_ALERT_SNIPPET_LENGTH = 500;
  
  private static final Logger logger =
      Logger.getLogger(ContentRpcImpl.class.getCanonicalName());
  
  private InternetAddress fromAddress = null;
  private String publisherName = null;

  @Override
  public synchronized BaseContentItem createOrChangeContentItem(BaseContentItem contentItem) {
    // Get the list of content items to link within the content first so that if there is an
    // exception with the queries, it doesn't affect the saving of the content entity. Except for
    // unassigned content items and player content items, because we don't auto-link from their
    // content. Or if the content item doesn't have any content.
    boolean runAutoLink = contentItem.getLivingStoryId() != null 
        && contentItem.getContentItemType() != ContentItemType.PLAYER
        && !GlobalUtil.isContentEmpty(contentItem.getContent());
    List<PlayerContentItem> playerContentItems = null;
    List<BackgroundContentItem> concepts = null;
    
    try {
      if (runAutoLink) {
        playerContentItems = getPlayers(contentItem.getLivingStoryId());
        concepts = getConcepts(contentItem.getLivingStoryId());
      }
    } catch (Exception e) {
      logger.warning("Skipping auto-linking. Error with retrieving players or concepts."
          + e.getMessage());
      runAutoLink = false;
    }
    
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Transaction tx = null;
    BaseContentEntity contentEntity;
    PublishState oldPublishState = null;
    
    Set<Long> newLinkedContentItemSuggestions = null;
    
    try {
      if (contentItem.getId() != null) {
        contentEntity = pm.getObjectById(BaseContentEntity.class, contentItem.getId());
        oldPublishState = contentEntity.getPublishState();
        contentEntity.copyFields(contentItem);
      } else {
        contentEntity = BaseContentEntity.fromClientObject(contentItem);
      }
      
      if (runAutoLink) {
        newLinkedContentItemSuggestions = 
            AutoLinkEntitiesInContent.createLinks(contentEntity, playerContentItems, concepts);
      }

      tx = pm.currentTransaction();
      tx.begin();
      pm.makePersistent(contentEntity);
      tx.commit();
      
      // If this was an event or a narrative and had a linked narrative, then the 'standalone'
      // field on the narrative content item needs to be updated to 'false'.
      // Note: this doesn't handle the case of unlinking a previously linked narrative content item.
      // That would require checking the linked content items of every single other event
      // content item to make sure it's not linked to from anywhere else, which would be an
      // expensive operation.
      Set<Long> linkedContentEntityIds = contentEntity.getLinkedContentEntityIds();
      ContentItemType contentItemType = contentEntity.getContentItemType();
      if ((contentItemType == ContentItemType.EVENT || contentItemType == ContentItemType.NARRATIVE)
          && !linkedContentEntityIds.isEmpty()) {
        List<Object> oids = new ArrayList<Object>(linkedContentEntityIds.size());
        for (Long id : linkedContentEntityIds) {
          oids.add(pm.newObjectIdInstance(BaseContentEntity.class, id));
        }

        @SuppressWarnings("unchecked")
        Collection<BaseContentEntity> linkedContentEntities = pm.getObjectsById(oids);
        for (BaseContentEntity linkedContentEntity : linkedContentEntities) {
          if (linkedContentEntity.getContentItemType() == ContentItemType.NARRATIVE) {
            linkedContentEntity.setIsStandalone(false);
          }
        }
      }

      // TODO: may also want to invalidate linked content items if they changed
      // and aren't from the same living story.
      invalidateCache(contentItem.getLivingStoryId());
    } finally {
      if (tx != null && tx.isActive()) {
        tx.rollback();
      }
      pm.close();
    }
    
    // Send email alerts if an event content item was changed from 'Draft' to 'Published'
    if (contentEntity.getContentItemType() == ContentItemType.EVENT
        && contentEntity.getPublishState() == PublishState.PUBLISHED
        && oldPublishState != null && oldPublishState == PublishState.DRAFT) {
      sendEmailAlerts((EventContentItem)contentItem);
    }

    // We pass suggested new linked content items back to the client by adding their ids to the
    // client object before returning it. It's the client's responsibility to check the linked
    // content item ids it passed in with those that came back, and to present appropriate
    // UI for processing the suggestions. Note that we shouldn't add the suggestions directly
    // to contentEntity! This will persist them to the datastore prematurely.
    BaseContentItem ret = contentEntity.toClientObject();
    
    if (newLinkedContentItemSuggestions != null) {
      ret.addAllLinkedContentItemIds(newLinkedContentItemSuggestions);
    }
    
    return ret;
  }
  
  @Override
  public List<PlayerContentItem> getUnassignedPlayers() {
    return getPlayers(null);
  }
  
  private List<PlayerContentItem> getPlayers(Long livingStoryId) {
    List<BaseContentEntity> playerEntities =
        getPublishedContentEntitiesByType(livingStoryId, ContentItemType.PLAYER);
    List<PlayerContentItem> playerContentItems = Lists.newArrayList();
    for (BaseContentEntity playerEntity : playerEntities) {
      playerContentItems.add((PlayerContentItem)(playerEntity.toClientObject()));
    }
    return playerContentItems;
  }
  
  private List<BackgroundContentItem> getConcepts(Long livingStoryId) {
    List<BaseContentEntity> backgroundEntities = 
        getPublishedContentEntitiesByType(livingStoryId, ContentItemType.BACKGROUND);
    List<BackgroundContentItem> backgroundContentItems = Lists.newArrayList();
    for (BaseContentEntity backgroundEntity : backgroundEntities) {
      if (!GlobalUtil.isContentEmpty(backgroundEntity.getName())) {
        backgroundContentItems.add((BackgroundContentItem)(backgroundEntity.toClientObject()));
      }
    }
    return backgroundContentItems;
  }
  
  private List<BaseContentEntity> getPublishedContentEntitiesByType(Long livingStoryId,
      ContentItemType contentItemType) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    
    Query query = pm.newQuery(BaseContentEntity.class); 
    query.setFilter("livingStoryId == livingStoryIdParam " +
        "&& publishState == com.google.livingstories.client.PublishState.PUBLISHED " +
        "&& contentItemType == '" + contentItemType.name() + "'");
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
  
  private void sendEmailAlerts(EventContentItem eventContentItem) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    
    // Get list of users
    Query query = pm.newQuery(UserLivingStoryEntity.class); 
    query.setFilter("livingStoryId == livingStoryIdParam && subscribedToEmails == true");
    query.declareParameters("long livingStoryIdParam");
    
    try {
      @SuppressWarnings("unchecked")
      List<UserLivingStoryEntity> userLivingStoryEntities =
          (List<UserLivingStoryEntity>) query.execute(eventContentItem.getLivingStoryId());
      List<String> users = new ArrayList<String>();
      for (UserLivingStoryEntity entity : userLivingStoryEntities) {
        users.add(entity.getId().getParent().getName());
      }
      if (!users.isEmpty()) {
       // getServletContext() doesn't return a valid result at construction-time, so
       // we initialize the external properties lazily.
       if (fromAddress == null && publisherName == null) {
          ExternalServiceKeyChain externalKeys = new ExternalServiceKeyChain(getServletContext());
          publisherName = externalKeys.getPublisherName();
          fromAddress = externalKeys.getFromAddress();
        }
        
        LivingStoryEntity livingStory = pm.getObjectById(LivingStoryEntity.class,
            eventContentItem.getLivingStoryId());
        String baseLspUrl = getBaseServerUrl() + "/lsps/" + livingStory.getUrl();
        
        StringBuilder emailContent = new StringBuilder("<b>");
        emailContent.append(eventContentItem.getEventUpdate()).append("</b>");
        if (!GlobalUtil.isContentEmpty(publisherName)) {
          emailContent.append("<span style=\"color: #777;\">&nbsp;-&nbsp;");
          emailContent.append(publisherName);
          emailContent.append("</span>");
        }
        String eventSummary = eventContentItem.getEventSummary();
        String eventDetails = eventContentItem.getContent();
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
            .append(eventContentItem.getId())
            .append(";\">Read more</a>")
            .append("<br><br>-----<br>")
            .append("<span style=\"font-size:small\">This is an automated alert. ")
            .append("To unsubscribe, click the 'unsubscribe' link on the top right of ")
            .append("<a href=\"")
            .append(UserServiceFactory.getUserService().createLoginURL(baseLspUrl))
            .append("\">this page</a>.</span>");

        if (fromAddress != null) {
          AlertSender.sendEmail(fromAddress, users,
              "Update: " + livingStory.getTitle(), emailContent.toString());
        }
        
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
  public synchronized List<BaseContentItem> getContentItemsForLivingStory(
      Long livingStoryId, boolean onlyPublished) {
    List<BaseContentItem> contentItems =
        Caches.getLivingStoryContentItems(livingStoryId, onlyPublished);
    if (contentItems != null) {
      return contentItems;
    }
 
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query query = pm.newQuery(BaseContentEntity.class);
    query.setFilter("livingStoryId == livingStoryIdParam"
        + (onlyPublished ? "&& publishState == '" + PublishState.PUBLISHED.name() + "'" : ""));
    query.setOrdering("timestamp desc");
    query.declareParameters("java.lang.Long livingStoryIdParam");

    try {
      List<BaseContentItem> clientContentItems = new ArrayList<BaseContentItem>();
      
      @SuppressWarnings("unchecked")
      List<BaseContentEntity> results = (List<BaseContentEntity>) query.execute(livingStoryId);
      for (BaseContentEntity result : results) {
        clientContentItems.add(result.toClientObject());
      }
      Caches.setLivingStoryContentItems(livingStoryId, onlyPublished, clientContentItems);
      return clientContentItems;
    } finally {
      query.closeAll();
      pm.close();
    }
  }
    
  /**
   * Gets the eventBundle for a given date range within a living story. 
   * @param livingStoryId the relevant story's id.
   * @param filterSpec a specification of how to filter the results
   * @param focusedContentItemId optional; indicates that contentItem with this id should be
   *    included in the returned list.  Specifying this parameter causes the method to return all
   *    content items from cutoff up until 3 items after the focused content item.  Otherwise, the
   *    method just returns the first 20 content items after cutoff.
   * @param cutoff Do not return content items that sort earlier/later than this date (exclusive)
   *    (Depends on order specified in filterSpec). Null if there is no bound.
   * @return an appropriate DisplayContentItemBundle
   */
  @Override
  public synchronized DisplayContentItemBundle getDisplayContentItemBundle(Long livingStoryId,
      FilterSpec filterSpec, Long focusedContentItemId, Date cutoff) {
    if (filterSpec.contributorId != null || filterSpec.playerId != null) {
      throw new IllegalArgumentException(
          "filterSpec.contributorId and filterSpec.playerId should not be set by remote callers."
          + " contributorId = " + filterSpec.contributorId + " playerId = "+ filterSpec.playerId);
    }
    DisplayContentItemBundle result = Caches.getDisplayContentItemBundle(
        livingStoryId, filterSpec, focusedContentItemId, cutoff);
    if (result != null) {
      return result;
    }
    
    FilterSpec localFilterSpec = new FilterSpec(filterSpec);
    
    BaseContentItem focusedContentItem = null;
    if (focusedContentItemId != null) {
      focusedContentItem = getContentItem(focusedContentItemId, false);
      if (focusedContentItem != null) {
        if (adjustFilterSpecForContentItem(localFilterSpec, focusedContentItem)) {
          // If we had to adjust the filter spec to accommodate the focused content item,
          // we'll be switching filter views, so we want to clear the start date
          // and reload the list from the beginning.
          cutoff = null;
        }
      }
    }
    
    // Some preliminaries. Note that the present implementation just filters all content items for
    // a story, which could be a bit expensive if there's a cache miss. By and large, though,
    // we'd expect a lot more cache hits than cache misses, unlike the case with, say,
    // a twitter "following" feed, which is more likely to be unique to that user.
    List<BaseContentItem> allContentItems = getContentItemsForLivingStory(livingStoryId, true);
    
    Map<Long, BaseContentItem> idToContentItemMap = Maps.newHashMap();
    List<BaseContentItem> relevantContentItems = Lists.newArrayList();
    for (BaseContentItem contentItem : allContentItems) {
      idToContentItemMap.put(contentItem.getId(), contentItem);
      
      Date sortKey = contentItem.getDateSortKey();
      boolean matchesStartDate = (cutoff == null) ||
          (localFilterSpec.oldestFirst ? !sortKey.before(cutoff) : !sortKey.after(cutoff));

      if (matchesStartDate && localFilterSpec.doesContentItemMatch(contentItem)) {
        relevantContentItems.add(contentItem);
      }
    }
    sortContentItemList(relevantContentItems, localFilterSpec);

    // Need to get the focused content item from the map instead of using the object directly.
    // This is because we use indexOf() to find the location of the focused content item in the
    // list and the original contentItem isn't the same object instance.
    List<BaseContentItem> coreContentItems = getSublist(relevantContentItems,
        focusedContentItem == null ? null : idToContentItemMap.get(focusedContentItemId), cutoff);
    Set<Long> linkedContentItemIds = Sets.newHashSet();
    
    for (BaseContentItem contentItem : coreContentItems) {
      if (contentItem.displayTopLevel()) {
        // If a content item isn't a top-level display content item, we can get away without
        // returning its linked content items.
        linkedContentItemIds.addAll(contentItem.getLinkedContentItemIds());
      }
    }

    Set<BaseContentItem> linkedContentItems = Sets.newHashSet();
    for (Long id : linkedContentItemIds) {
      BaseContentItem linkedContentItem = idToContentItemMap.get(id);
      if (linkedContentItem == null) {
        System.err.println("Linked content item with id " + id + " is not found.");
      } else {
        linkedContentItems.add(linkedContentItem);
        // For linked narratives, we want to get their own linked content items as well
        if (linkedContentItem.getContentItemType() == ContentItemType.NARRATIVE) {
          for (Long linkedToLinkedContentItemId : linkedContentItem.getLinkedContentItemIds()) {
            BaseContentItem linkedToLinkedContentItem =
                idToContentItemMap.get(linkedToLinkedContentItemId);
            if (linkedToLinkedContentItem != null) {
              linkedContentItems.add(linkedToLinkedContentItem);
            }
          }
        }
      }
    }
    
    Date nextDateInSequence = getNextDateInSequence(coreContentItems, relevantContentItems);

    result = new DisplayContentItemBundle(coreContentItems, linkedContentItems, nextDateInSequence,
        localFilterSpec);
    Caches.setDisplayContentItemBundle(livingStoryId, filterSpec, focusedContentItemId, cutoff,
        result);
    return result;
  }

  /**
   * Check if the contentItem matches the filterSpec.  If not, this method adjusts the filter
   * spec so that the contentItem will match.
   * @return whether or not the filterSpec was adjusted.
   */
  private boolean adjustFilterSpecForContentItem(FilterSpec filterSpec,
      BaseContentItem contentItem) {
    if (filterSpec.doesContentItemMatch(contentItem)) {
      return false;
    }
    if (filterSpec.themeId != null && !contentItem.getThemeIds().contains(filterSpec.themeId)) {
      filterSpec.themeId = null;
    }
    if (filterSpec.importantOnly && contentItem.getImportance() != Importance.HIGH) {
      filterSpec.importantOnly = false;
    }
    if (filterSpec.contentItemType != contentItem.getContentItemType()) {
      filterSpec.contentItemType = null;
    } else if (contentItem.getContentItemType() == ContentItemType.ASSET
        && filterSpec.assetType != ((AssetContentItem) contentItem).getAssetType()) {
      filterSpec.contentItemType = null;
      filterSpec.assetType = null;
    }
    if (filterSpec.opinion && (contentItem.getContentItemType() != ContentItemType.NARRATIVE
        || !((NarrativeContentItem) contentItem).isOpinion())) {
      filterSpec.opinion = false;
    }
    return true;
  }
  
  private void sortContentItemList(List<BaseContentItem> contentItems, FilterSpec filterSpec) {
    Collections.sort(contentItems,
        filterSpec.oldestFirst ? BaseContentItem.COMPARATOR : BaseContentItem.REVERSE_COMPARATOR);
  }
  
  private List<BaseContentItem> getSublist(List<BaseContentItem> allContentItems,
      BaseContentItem focusedContentItem, Date cutoff) {
    int contentItemLimit;
    if (focusedContentItem == null) {
      contentItemLimit = CONTENT_ITEM_COUNT_LIMIT;
    } else {
      contentItemLimit =
          allContentItems.indexOf(focusedContentItem) + 1 + JUMP_TO_CONTENT_ITEM_CONTEXT_COUNT;
      // If we are not appending content items and there are less than 20 results because of a
      // focussed content item, bump the limit up to 20
      if (cutoff == null && contentItemLimit < CONTENT_ITEM_COUNT_LIMIT) {
        contentItemLimit = CONTENT_ITEM_COUNT_LIMIT;
      }
    }
    contentItemLimit = Math.min(allContentItems.size(), contentItemLimit);

    while (contentItemLimit < allContentItems.size() - 1) {
      Date thisContentItemDate = allContentItems.get(contentItemLimit).getDateSortKey();
      Date nextContentItemDate = allContentItems.get(contentItemLimit + 1).getDateSortKey();
      if (!thisContentItemDate.equals(nextContentItemDate)) {
        break;
      }
      contentItemLimit++;
    }
    
    // Copy the sublist into a new ArrayList since the sublist() method returns
    // a view backed by the original list, which includes a bunch of content items we don't
    // care about.
    return new ArrayList<BaseContentItem>(allContentItems.subList(0, contentItemLimit));
  }

  /**
   * We return the date of the content item after the last core content item returned
   * as the 'next date in sequence', which we will use as the startDate in this method
   * on the next call, when the user wants more content items.
   * Very rare corner case:
   * If the user loads up the page, a content item is added whose date falls between 
   * the date of the last content item returned and the next date in sequence, and then
   * the user clicks 'view more', we'll miss displaying that new content item.
   * We don't really care about this corner case though, since it will almost
   * never happen.
   */
  private Date getNextDateInSequence(List<BaseContentItem> coreContentItems,
      List<BaseContentItem> relevantContentItems) {
    return coreContentItems.size() < relevantContentItems.size()
        ? relevantContentItems.get(coreContentItems.size()).getDateSortKey() : null;
  }
  
  @Override
  public synchronized BaseContentItem getContentItem(Long id, boolean getLinkedContentItems) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    
    try {
      BaseContentItem contentItem = pm.getObjectById(BaseContentEntity.class, id).toClientObject();
      if (getLinkedContentItems) {
        contentItem.setLinkedContentItems(getContentItems(contentItem.getLinkedContentItemIds()));
      }
      return contentItem;
    } catch (JDOObjectNotFoundException e) {
      return null;
    } finally {
      pm.close();
    }
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public synchronized List<BaseContentItem> getContentItems(Collection<Long> ids) {
    if (ids.isEmpty()) {
      return new ArrayList<BaseContentItem>();
    }
    
    PersistenceManager pm = PMF.get().getPersistenceManager();
    List<Object> oids = new ArrayList<Object>(ids.size());
    for (Long id : ids) {
      oids.add(pm.newObjectIdInstance(BaseContentEntity.class, id));
    }
    
    try {
      Collection results = pm.getObjectsById(oids);
      List<BaseContentItem> contentItems = new ArrayList<BaseContentItem>(results.size());
      for (Object result : results) {
        contentItems.add(((BaseContentEntity)result).toClientObject());
      }
      return contentItems;
    } finally {
      pm.close();
    }
  }

  @Override
  public synchronized DisplayContentItemBundle getRelatedContentItems(
      Long contentItemId, boolean byContribution, Date cutoff) {
    // translate contentItemId and byContribution into an appropriate FilterSpec, which we use
    // to respond from cache instead of by making fresh queries.
    FilterSpec filterSpec = new FilterSpec();
    if (byContribution) {
      filterSpec.contributorId = contentItemId;
    } else {
      filterSpec.playerId = contentItemId;
    }
    DisplayContentItemBundle result =
        Caches.getDisplayContentItemBundle(null, filterSpec, null, cutoff);
    if (result != null) {
      return result;
    }
    
    PersistenceManager pm = PMF.get().getPersistenceManager();

    Query query = pm.newQuery(BaseContentEntity.class);
    String contentItemIdClause = byContribution
        ? "contributorIds == contentItemIdParam" : "linkedContentItemIds == contentItemIdParam";
    query.setFilter(contentItemIdClause
        + " && publishState == '" + PublishState.PUBLISHED.name() + "'");
    // no need to explicitly set ordering, as we resort by display order.
    query.declareParameters("java.lang.Long contentItemIdParam");

    try {
      List<BaseContentItem> relevantContentItems = new ArrayList<BaseContentItem>();
      
      @SuppressWarnings("unchecked")
      List<BaseContentEntity> contentEntities =
          (List<BaseContentEntity>) query.execute(contentItemId);
      for (BaseContentEntity contentEntity : contentEntities) {
        BaseContentItem contentItem = contentEntity.toClientObject();
        if (cutoff == null || !contentItem.getDateSortKey().after(cutoff)) {
          relevantContentItems.add(contentItem);
        }
      }
      
      // sort and put a window on the list, get the next date in the sequence
      sortContentItemList(relevantContentItems, filterSpec);
      List<BaseContentItem> coreContentItems = getSublist(relevantContentItems, null, cutoff); 
      Date nextDateInSequence = getNextDateInSequence(coreContentItems, relevantContentItems);
      
      result = new DisplayContentItemBundle(coreContentItems,
          Collections.<BaseContentItem>emptySet(), nextDateInSequence, filterSpec);
      Caches.setDisplayContentItemBundle(null, filterSpec, null, cutoff, result);
      return result;
    } finally {
      query.closeAll();
      pm.close();
    }
  }
  
  /**
   * Performs a content entity query given a set of search filter terms.
   * 
   * For all search combinations to be successful, we require the existence
   * of several indexes:
   * - LivingStoryId/PublishState/Timestamp (minimum, required fields)
   * - LivingStoryId/PublishState/Timestamp/ContentItemType
   * - LivingStoryId/PublishState/Timestamp/ContentItemType/PlayerType
   * - LivingStoryId/PublishState/Timestamp/ContentItemType/AssetType
   * - LivingStoryId/PublishState/Timestamp/ContentItemType/NarrativeType
   * - LivingStoryId/PublishState/Timestamp/Importance/ContentItemType
   * - LivingStoryId/PublishState/Timestamp/Importance/ContentItemType/PlayerType
   * - LivingStoryId/PublishState/Timestamp/Importance/ContentItemType/AssetType
   * - LivingStoryId/PublishState/Timestamp/Importance/ContentItemType/NarrativeType
   */
  @Override
  public List<BaseContentItem> executeSearch(SearchTerms searchTerms) {
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
    
    // Optional filter: contentItemType
    if (searchTerms.contentItemType != null) {
      queryFilters.append( "&& contentItemType == '" + searchTerms.contentItemType.name() + "'");
    }
    
    // Optional filter: content item subtype
    if (searchTerms.contentItemType == ContentItemType.PLAYER && searchTerms.playerType != null) {
      queryFilters.append(" && playerType == '" + searchTerms.playerType.name() + "'");
    } else if (searchTerms.contentItemType == ContentItemType.ASSET
        && searchTerms.assetType != null) {
      queryFilters.append(" && assetType == '" + searchTerms.assetType.name() + "'");
    } else if (searchTerms.contentItemType == ContentItemType.NARRATIVE
        && searchTerms.narrativeType != null) {
      queryFilters.append(" && narrativeType == '" + searchTerms.narrativeType.name() + "'");
    }
    
    query.setFilter(queryFilters.toString());
    query.declareParameters("java.util.Date beforeDateParam, java.util.Date afterDateParam");
    query.setOrdering("timestamp desc");

    try {
      List<BaseContentItem> clientContentItems = new ArrayList<BaseContentItem>();
      
      @SuppressWarnings("unchecked")
      List<BaseContentEntity> results =
          (List<BaseContentEntity>) query.execute(searchTerms.beforeDate, searchTerms.afterDate);
      for (BaseContentEntity result : results) {
        clientContentItems.add(result.toClientObject());
      }
      return clientContentItems;
    } finally {
      query.closeAll();
      pm.close();
    }
  }
  

  
  @Override
  public synchronized void deleteContentItem(final Long id) {
    PersistenceManager pm = PMF.get().getPersistenceManager();

    try {
      BaseContentEntity contentEntity = pm.getObjectById(BaseContentEntity.class, id);

      updateContentEntityReferencesHelper(pm, "linkedContentEntityIds", id,
          new Function<BaseContentEntity, Void>() {
            public Void apply(BaseContentEntity contentEntity) { 
              contentEntity.removeLinkedContentEntityId(id); return null;
            }
          });

      // If deleting a contributor as well, update relevant contributor ids too.
      if (contentEntity.getContentItemType() == ContentItemType.PLAYER) {
        updateContentEntityReferencesHelper(pm, "contributorIds", id,
            new Function<BaseContentEntity, Void>() {
              public Void apply(BaseContentEntity contentEntity) {
                contentEntity.removeContributorId(id); return null;
              }
            });
      }
      
      invalidateCache(contentEntity.getLivingStoryId());
      pm.deletePersistent(contentEntity);
    } finally {
      pm.close();
    }
  }
  
  private void invalidateCache(Long livingStoryId) {
    Caches.clearLivingStoryContentItems(livingStoryId);
    Caches.clearLivingStoryThemeInfo(livingStoryId);
    Caches.clearStartPageBundle();
  }
  
  /**
   * Helper method that updates content entities that refer to a content entity soon to be deleted. 
   * @param pm the persistence manager
   * @param relevantField relevant field name for the query
   * @param removeFunc a Function to apply to the results of the query
   * @param id the id of the to-be-deleted content entity
   */
  private void updateContentEntityReferencesHelper(PersistenceManager pm, String relevantField,
      Long id, Function<BaseContentEntity, Void> removeFunc) {
    Query query = pm.newQuery(BaseContentEntity.class);
    query.setFilter(relevantField + " == contentItemIdParam");
    query.declareParameters("java.lang.Long contentItemIdParam");
    try {
      @SuppressWarnings("unchecked")
      List<BaseContentEntity> results = (List<BaseContentEntity>) query.execute(id);
      for (BaseContentEntity result : results) {
        removeFunc.apply(result);
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
        "&& contentItemType == com.google.livingstories.client.ContentItemType.EVENT");
    eventsQuery.setOrdering("timestamp desc");
    if (range != 0) {
      eventsQuery.setRange(0, range);
    }
    eventsQuery.declareParameters("Long livingStoryIdParam" 
        + (timeParam == null ? "" : ", java.util.Date timeParam"));
    
    Query narrativesQuery = pm.newQuery(BaseContentEntity.class);
    narrativesQuery.setFilter(commonQueryFilter +
        "&& contentItemType == com.google.livingstories.client.ContentItemType.NARRATIVE " +
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
  public List<BaseContentItem> getUpdatesSinceTime(Long livingStoryId, Date time) {
    PersistenceManager pm = PMF.get().getPersistenceManager();

    List<Query> updateQueries = getUpdateQueries(pm, time, 0);
    
    try {
      List<BaseContentItem> updates = new ArrayList<BaseContentItem>();
      for (Query query : updateQueries) {
        @SuppressWarnings("unchecked")
        List<BaseContentEntity> results = 
            (List<BaseContentEntity>) query.execute(livingStoryId, time);
        for (BaseContentEntity result : results) {
          updates.add(result.toClientObject());
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
  public List<BaseContentItem> getUpdatesForStartPage(Long livingStoryId) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    List<Query> updateQueries = getUpdateQueries(pm, null, 3);
    try {
      List<BaseContentItem> updates = new ArrayList<BaseContentItem>();
      // Get the latest 3 events and latest 3 narratives and then return the latest 3 items
      // from those 6 because there is no way to do one appengine query for that
      for (Query query : updateQueries) {
        @SuppressWarnings("unchecked")
        List<BaseContentEntity> results = (List<BaseContentEntity>) query.execute(livingStoryId);
        for (BaseContentEntity result : results) {
          updates.add(result.toClientObject());
        }
      }
      Collections.sort(updates, BaseContentItem.REVERSE_COMPARATOR);
      // Just return the latest 3 updates
      return new ArrayList<BaseContentItem>(updates.subList(0, Math.min(3, updates.size())));
    } finally {
      for (Query query : updateQueries) {
        query.closeAll();
      }
      pm.close();
    }
  }
  
  @Override
  public List<EventContentItem> getImportantEventsForLivingStory(Long livingStoryId) {
    PersistenceManager pm = PMF.get().getPersistenceManager();

    Query query = pm.newQuery(BaseContentEntity.class);
    query.setFilter("livingStoryId == livingStoryIdParam " +
        "&& contentItemType == com.google.livingstories.client.ContentItemType.EVENT " +
        "&& importance == com.google.livingstories.client.Importance.HIGH " +
        "&& publishState == com.google.livingstories.client.PublishState.PUBLISHED");
    query.declareParameters("Long livingStoryIdParam");
    
    try {
      @SuppressWarnings("unchecked")
      List<BaseContentEntity> results = 
          (List<BaseContentEntity>) query.execute(livingStoryId);
      List<EventContentItem> events = new ArrayList<EventContentItem>();
      for (BaseContentEntity result : results) {
        EventContentItem event = (EventContentItem) result.toClientObject();
        events.add(event);
      }
      Collections.sort(events, BaseContentItem.REVERSE_COMPARATOR);
      return events;
    } finally {
      query.closeAll();
      pm.close();
    }
  }
  
  /**
   * This method will return a list of all the players in the living story,
   * sorted by importance.  Our importance ranking is currently based solely
   * on the number of content items in the living story that are linked to each player. 
   */
  @Override
  public List<PlayerContentItem> getImportantPlayersForLivingStory(Long livingStoryId) {
    PersistenceManager pm = PMF.get().getPersistenceManager();

    Query query = pm.newQuery(BaseContentEntity.class);
    query.setFilter("livingStoryId == livingStoryIdParam " +
        "&& contentItemType == com.google.livingstories.client.ContentItemType.PLAYER " +
        "&& importance == com.google.livingstories.client.Importance.HIGH " +
        "&& publishState == com.google.livingstories.client.PublishState.PUBLISHED");
    query.declareParameters("Long livingStoryIdParam");
    
    List<PlayerContentItem> players = Lists.newArrayList();
    try {
      @SuppressWarnings("unchecked")
      List<BaseContentEntity> results = 
          (List<BaseContentEntity>) query.execute(livingStoryId);
      for (BaseContentEntity result : results) {
        players.add((PlayerContentItem) result.toClientObject());
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
  public Map<Long, PlayerContentItem> getContributorsByIdForLivingStory(Long livingStoryId) {
    Map<Long, PlayerContentItem> result = Caches.getContributorsForLivingStory(livingStoryId);
    if (result != null) {
      return result;
    }
    
    List<BaseContentItem> allContentItems = getContentItemsForLivingStory(livingStoryId, true);
    
    Set<Long> allContributorIds = new HashSet<Long>();
    for (BaseContentItem contentItem : allContentItems) {
      allContributorIds.addAll(contentItem.getContributorIds());
    }
    
    List<BaseContentItem> contributors = getContentItems(allContributorIds);
    
    result = new HashMap<Long, PlayerContentItem>();
    for (BaseContentItem contributor : contributors) {
      if (contributor.getContentItemType() == ContentItemType.PLAYER) {
        result.put(contributor.getId(), (PlayerContentItem) contributor);
      } else {
        logger.warning("Contributor id " + contributor.getId() + " does not map to a player");
      }
    }

    Caches.setContributorsForLivingStory(livingStoryId, result);
    return result;
  }
}
