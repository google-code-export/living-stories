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

package com.google.livingstories.server.dataservices.impl;

import com.google.appengine.repackaged.com.google.common.base.Function;
import com.google.livingstories.client.AtomType;
import com.google.livingstories.client.BaseAtom;
import com.google.livingstories.client.Importance;
import com.google.livingstories.client.PublishState;
import com.google.livingstories.server.BaseContentEntity;
import com.google.livingstories.server.dataservices.ContentDataService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;

/**
 * Implementation of the content data service using JDO. This implementation references
 * the database on every call and does not handle caching, etc.
 */
public class ContentDataServiceImpl implements ContentDataService {

  @Override
  public synchronized BaseAtom save(BaseAtom baseContent) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Transaction tx = null;
    BaseContentEntity contentEntity;

    try {
      if (baseContent.getId() == null) {
        contentEntity = BaseContentEntity.fromClientObject(baseContent);
      } else {
        contentEntity = pm.getObjectById(BaseContentEntity.class, baseContent.getId());
        contentEntity.copyFields(baseContent);
      }
      tx = pm.currentTransaction();
      tx.begin();
      pm.makePersistent(contentEntity);
      tx.commit();
      return contentEntity.toClientObject();
    }  finally {
      if (tx != null && tx.isActive()) {
        tx.rollback();
      }
      pm.close();
    }
  }

  @Override
  public synchronized void delete(final Long id) {
    PersistenceManager pm = PMF.get().getPersistenceManager();

    try {
      BaseContentEntity contentEntity = pm.getObjectById(BaseContentEntity.class, id);
      // First remove the object being deleted from the linked fields of other objects
      updateAtomReferencesHelper(pm, "linkedAtomIds", id,
          new Function<BaseContentEntity, Void>() {
            public Void apply(BaseContentEntity atom) { 
              atom.removeLinkedAtomId(id); 
              return null;
            }
          });
      // If deleting a player, remove it from contributorIds of other objects that may contain it
      if (contentEntity.getAtomType() == AtomType.PLAYER) {
        updateAtomReferencesHelper(pm, "contributorIds", id,
            new Function<BaseContentEntity, Void>() {
              public Void apply(BaseContentEntity atom) {
                atom.removeContributorId(id); 
                return null;
              }
            });
      }
      pm.deletePersistent(contentEntity);
    } finally {
      pm.close();
    }
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

  @Override
  public synchronized void deleteContentForLivingStory(Long livingStoryId) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query query = pm.newQuery(BaseContentEntity.class);
    query.setFilter("livingStoryId == livingStoryIdParam");
    query.declareParameters("java.lang.Long livingStoryIdParam");
    
    try {
      @SuppressWarnings("unchecked")
      List<BaseContentEntity> entities = (List<BaseContentEntity>) query.execute(livingStoryId);
      pm.deletePersistentAll(entities);
    } finally {
      query.closeAll();
      pm.close();
    }
  }
  
  @Override
  public synchronized void removeTheme(Long themeId) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query query = pm.newQuery(BaseContentEntity.class);
    // Checks to see if the collection has angleIdParam in angleIds somewhere, not
    // strictly for equality:
    query.setFilter("angleIds == angleIdParam");
    query.declareParameters("java.lang.Long angleIdParam");
    
    try {
      @SuppressWarnings("unchecked")
      List<BaseContentEntity> results = (List<BaseContentEntity>) query.execute(themeId);
      for (BaseContentEntity contentEntity : results) {
        contentEntity.removeThemeId(themeId);
      }
      pm.makePersistentAll(results);
    } finally {
      query.closeAll();
      pm.close();
    }
  }

  

  @Override
  public synchronized BaseAtom retrieveById(Long id, boolean populateLinkedEntities) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    
    try {
      BaseAtom content = pm.getObjectById(BaseContentEntity.class, id).toClientObject();
      if (populateLinkedEntities) {
        content.setLinkedAtoms(retrieveByIds(content.getLinkedAtomIds()));
      }
      return content;
    } catch (JDOObjectNotFoundException e) {
      return null;
    } finally {
      pm.close();
    }
  }

  @Override
  public synchronized List<BaseAtom> retrieveByIds(Collection<Long> ids) {
    if (ids.isEmpty()) {
      return new ArrayList<BaseAtom>();
    }
    
    PersistenceManager pm = PMF.get().getPersistenceManager();
    List<Object> oids = new ArrayList<Object>(ids.size());
    for (Long id : ids) {
      oids.add(pm.newObjectIdInstance(BaseContentEntity.class, id));
    }
    
    try {
      @SuppressWarnings("unchecked")
      Collection contentEntities = pm.getObjectsById(oids);
      List<BaseAtom> results = new ArrayList<BaseAtom>(contentEntities.size());
      for (Object contentEntity : contentEntities) {
        results.add(((BaseContentEntity)contentEntity).toClientObject());
      }
      return results;
    } finally {
      pm.close();
    }
  }

  @Override
  public synchronized List<BaseAtom> retrieveByLivingStory(Long livingStoryId, 
      PublishState publishState) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query query = pm.newQuery(BaseContentEntity.class);
    query.setFilter("livingStoryId == livingStoryIdParam"
        + (publishState == null ? "" : " && publishState == '" + publishState.name() + "'"));
    query.setOrdering("timestamp desc");
    query.declareParameters("java.lang.Long livingStoryIdParam");
    return executeQuery(pm, query, livingStoryId);
  }

  @Override
  public synchronized List<BaseAtom> retrieveEntitiesContributedBy(Long contributorId) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query query = pm.newQuery(BaseContentEntity.class);
    query.setFilter("contributorIds == entityIdParam"
        + " && publishState == '" + PublishState.PUBLISHED.name() + "'");
    query.setOrdering("timestamp desc");
    query.declareParameters("java.lang.Long entityIdParam");
    return executeQuery(pm, query, contributorId);
  }

  @Override
  public synchronized List<BaseAtom> retrieveEntitiesThatLinkTo(Long entityId) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query query = pm.newQuery(BaseContentEntity.class);
    query.setFilter("linkedAtomIds == entityIdParam"
        + " && publishState == '" + PublishState.PUBLISHED.name() + "'");
    query.setOrdering("timestamp desc");
    query.declareParameters("java.lang.Long entityIdParam");
    return executeQuery(pm, query, entityId); 
  }
  
  private List<BaseAtom> executeQuery(PersistenceManager pm, Query query, Long param) {
    try {
      List<BaseAtom> contentList = new ArrayList<BaseAtom>();
      @SuppressWarnings("unchecked")
      List<BaseContentEntity> contentEntities = (List<BaseContentEntity>) query.execute(
          param);
      for (BaseContentEntity contentEntity : contentEntities) {
        BaseAtom content = contentEntity.toClientObject();
        contentList.add(content);
      }
      return contentList;
    } finally {
      query.closeAll();
      pm.close();
    }
  }

  @Override
  public synchronized List<BaseAtom> search(Long livingStoryId, AtomType atomType, Date afterDate,
      Date beforeDate, Importance importance, PublishState publishState) {
    StringBuilder queryFilters = new StringBuilder("livingStoryId == " + livingStoryId);
    if (atomType != null) {
      queryFilters.append(" && atomType == '" + atomType.name() + "'");
    }
    if (afterDate != null) {
      queryFilters.append(" && timestamp >= afterDateParam");
    }
    if (beforeDate != null) {
      queryFilters.append(" && timestamp <= beforeDateParam");
    }
    if (importance != null) {
      queryFilters.append(" && importance == '" + importance.name() + "'");
    }
    if (publishState != null) {
      queryFilters.append(" && publishState == '" + publishState.name() + "'");
    }
  
    
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query query = pm.newQuery(BaseContentEntity.class);
    query.setFilter(queryFilters.toString());
    query.declareParameters("java.util.Date afterDateParam, java.util.Date beforeDateParam");
    query.setOrdering("timestamp desc");

    try {
      @SuppressWarnings("unchecked")
      List<BaseContentEntity> contentEntities = (List<BaseContentEntity>) query.execute(afterDate,
          beforeDate);
      List<BaseAtom> contentList = new ArrayList<BaseAtom>();
      for (BaseContentEntity contentEntity : contentEntities) {
        BaseAtom content = contentEntity.toClientObject();
        contentList.add(content);
      }
      return contentList;
    } finally {
      query.closeAll();
      pm.close();
    }
  }
  
  @Override
  public synchronized Integer getNumberOfEntitiesUpdatedSinceTime(Long livingStoryId, 
      AtomType entityType, Date afterDate) throws IllegalArgumentException {
    if (livingStoryId == null || entityType == null || afterDate == null) {
      throw new IllegalArgumentException("Arguments cannot be null.");
    }
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query query = pm.newQuery(BaseContentEntity.class);
    query.setFilter("livingStoryId == livingStoryIdParam " +
        "&& publishState == '" + PublishState.PUBLISHED.name() + "' " +
        "&& atomType == '" + entityType.name() + "' " +
        "&& timestamp > timeParam");
    query.declareParameters("java.lang.Long livingStoryIdParam, java.util.Date timeParam");
    query.setResult("count(id)");
    
    try {
      return (Integer) query.execute(livingStoryId, afterDate);
    } finally {
      query.closeAll();
      pm.close();
    }
  }
}
