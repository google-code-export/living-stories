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

import com.google.livingstories.client.Theme;
import com.google.livingstories.server.AngleEntity;
import com.google.livingstories.server.dataservices.ThemeDataService;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;

/**
 * Implementation of the themes service using JDO. This implementation references
 * the database on every call and does not handle caching, etc.
 */
public class ThemeDataServiceImpl implements ThemeDataService {

  @Override
  public synchronized Theme save(Theme theme) throws IllegalArgumentException {
    Long id = theme.getId();
    Long livingStoryId = theme.getLivingStoryId();
    String name = theme.getName();
    // First check that the theme name is not empty
    if (name == null || name.trim().equals("")) {
      throw new IllegalArgumentException("Theme name cannot be empty");
    }
    
    // Then check that another theme with the same name doesn't exist for the same story.
    // We'll just retrieve all the themes for the given story, instead of directly querying
    // for one with a matching name because the number of themes per story will not be too high
    List<Theme> themesForStory = retrieveByLivingStory(livingStoryId);
    for (Theme themeForStory : themesForStory) {
      if (themeForStory.getName().equals(name)) {
        if (themeForStory.getId() == id) {
          // If the id, livingStoryId and name are the same, nothing needs to change
          return theme;
        } else {
          throw new IllegalArgumentException("Theme with name '" + name + "' already exists for "
              + "this story.");
        }
      }
    }
    
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Transaction tx = null;
    
    try {
      AngleEntity entity = null;
      if (id == null) {
        entity = new AngleEntity(name, livingStoryId);
      } else {
        entity = pm.getObjectById(AngleEntity.class, id);
        entity.setName(name);
      }
      tx = pm.currentTransaction();
      tx.begin();
      pm.makePersistent(entity);
      tx.commit();
      return entity.toClientObject();
    } finally {
      if (tx != null && tx.isActive()) {
        tx.rollback();
      }
      pm.close();
    }
  }
  
  @Override
  public synchronized void delete(Long id) {
    // First remove the theme from any content objects it may appear in
    DataImplFactory.getContentService().removeTheme(id);
    // Then delete the entity from the database
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      AngleEntity angleEntity = pm.getObjectById(AngleEntity.class, id);
      pm.deletePersistent(angleEntity);
    } finally {
      pm.close();
    }
  }

  @Override
  public synchronized void deleteThemesForLivingStory(Long livingStoryId) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query query = pm.newQuery(AngleEntity.class);
    query.setFilter("livingStoryId == livingStoryIdParam");
    query.declareParameters("java.lang.Long livingStoryIdParam");

    try {
      @SuppressWarnings("unchecked")
      List<AngleEntity> entities = (List<AngleEntity>) query.execute(livingStoryId);
      pm.deletePersistentAll(entities);
    } finally {
      query.closeAll();
      pm.close();
    }
  }

  @Override
  public synchronized Theme retrieveById(Long id) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      return pm.getObjectById(AngleEntity.class, id).toClientObject();
    } catch (JDOObjectNotFoundException notFound) {
      return null;
    } finally {
      pm.close();
    }
  }

  @Override
  public synchronized List<Theme> retrieveByLivingStory(Long livingStoryId) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query query = pm.newQuery(AngleEntity.class);
    query.setFilter("livingStoryId == livingStoryIdParam");
    query.declareParameters("java.lang.Long livingStoryIdParam");

    try {
      @SuppressWarnings("unchecked")
      List<AngleEntity> entities = (List<AngleEntity>) query.execute(livingStoryId);
      List<Theme> results = new ArrayList<Theme>();
      for (AngleEntity entity : entities) {
        results.add(entity.toClientObject());
      }
      return results;
    } finally {
      query.closeAll();
      pm.close();
    }
  }
}
