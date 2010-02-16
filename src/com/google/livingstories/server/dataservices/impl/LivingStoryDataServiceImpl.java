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

import com.google.livingstories.client.LivingStory;
import com.google.livingstories.client.PublishState;
import com.google.livingstories.server.dataservices.LivingStoryDataService;
import com.google.livingstories.server.dataservices.entities.LivingStoryEntity;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;

/**
 * Implementation of the Living Story data interface using JDO. This implementation references
 * the database on every call and does not handle caching, etc.
 */
public class LivingStoryDataServiceImpl implements LivingStoryDataService {
  
  @Override
  public synchronized LivingStory save(Long id, String urlName, String title, 
      PublishState publishState, String summary) throws IllegalArgumentException {
    
    // If a new story is being created, first make sure another story with the same URL doesn't
    // already exist.
    if (id == null) {
      LivingStory existingStoryWithSameUrl = retrieveByUrlName(urlName, true);
      if (existingStoryWithSameUrl != null) {
        throw new IllegalArgumentException("Story with the same URL '" + urlName 
            + "' already exists.");    
      }
    }
    
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Transaction tx = null;
    
    try {
      LivingStoryEntity entity = null;
      if (id == null) {
        entity = new LivingStoryEntity(urlName, title, publishState, summary);
      } else {
        entity = pm.getObjectById(LivingStoryEntity.class, id);
        entity.setUrl(urlName);
        entity.setTitle(title);
        entity.setPublishState(publishState);
        String previousLatestRevision = entity.getSummary();
        if (!previousLatestRevision.equals(summary)) {
          entity.addSummaryRevision(summary);
        }
      }
      tx = pm.currentTransaction();
      tx.begin();
      pm.makePersistent(entity);
      tx.commit();
      return entity.toClientObject(false);
    } finally {
      if (tx != null && tx.isActive()) {
        tx.rollback();
      }
      pm.close();
    }
  }

  @Override
  public synchronized void delete(Long id) {
    // Delete any user data associated with this living story first
    DataImplFactory.getUserDataService().deleteVisitTimesForStory(id);
    // Then delete all the Content entities that are part of this living story
    DataImplFactory.getContentService().deleteContentForLivingStory(id);
    // Then delete the themes - all the content objects that might have been referencing these
    // themes have already been deleted above
    DataImplFactory.getThemeService().deleteThemesForLivingStory(id);
    
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      // Finally delete the story entity itself
      LivingStoryEntity livingStory = pm.getObjectById(LivingStoryEntity.class, id);
      pm.deletePersistent(livingStory);
    } finally {
      pm.close();
    }
  }
  
  @Override
  public synchronized LivingStory retrieveById(Long id, boolean latestRevisionsOnly) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      return pm.getObjectById(LivingStoryEntity.class, id).toClientObject(latestRevisionsOnly);
    } catch (JDOObjectNotFoundException notFound) {
      return null;
    } finally {
      pm.close();
    }
  }

  @Override
  public synchronized LivingStory retrieveByUrlName(String urlName, boolean latestRevisionsOnly) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query query = pm.newQuery(LivingStoryEntity.class);
    query.setFilter("url == urlParam");
    query.declareParameters("java.lang.String urlParam");
    
    try {
      @SuppressWarnings("unchecked")
      List<LivingStoryEntity> entities = (List<LivingStoryEntity>) query.execute(urlName);
      if (entities.isEmpty()) {
        return null;
      } else {
        return entities.get(0).toClientObject(latestRevisionsOnly);
      }
    } finally {
      query.closeAll();
      pm.close();
    }
  }
  
  @Override
  public synchronized List<LivingStory> retrieveAll(PublishState publishState, 
      boolean latestRevisionsOnly) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query query = pm.newQuery(LivingStoryEntity.class);
    if (publishState != null) {
      query.setFilter("publishState == '" + publishState.name() + "'");
    }
    
    try {
      @SuppressWarnings("unchecked")
      List<LivingStoryEntity> entities = (List<LivingStoryEntity>) query.execute();
      List<LivingStory> results = new ArrayList<LivingStory>();
      for (LivingStoryEntity entity : entities) {
        results.add(entity.toClientObject(latestRevisionsOnly));
      }
      return results;
    } finally {
      query.closeAll();
      pm.close();
    }
  }
}
