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

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.livingstories.client.FilterSpec;
import com.google.livingstories.server.dataservices.UserDataService;
import com.google.livingstories.server.dataservices.entities.UserEntity;
import com.google.livingstories.server.dataservices.entities.UserLivingStoryEntity;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

/**
 * Implementation of the user data interface using JDO.
 */
public class UserDataServiceImpl implements UserDataService {

  @Override
  public synchronized Date getLastVisitTimeForStory(String userId, Long livingStoryId) {
    UserLivingStoryEntity entity = retrieveUserLivingStoryEntity(userId, livingStoryId);
    return entity == null ? null : entity.getLastVisitedTime();
  }
  
  @Override
  public synchronized Map<Long, Date> getAllLastVisitTimes(String userEmail) {
    Map<Long, Date> visitTimesMap = new HashMap<Long, Date>();

    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query query = pm.newQuery(UserLivingStoryEntity.class);
    query.setFilter("parentEmailAddress == emailParam");
    query.declareParameters("java.lang.String emailParam");
    try {
      @SuppressWarnings("unchecked")
      List<UserLivingStoryEntity> userLivingStoryEntities =
          (List<UserLivingStoryEntity>) query.execute(userEmail);
      for (UserLivingStoryEntity entity : userLivingStoryEntities) {
        visitTimesMap.put(entity.getLivingStoryId(), entity.getLastVisitedTime());
      }
      return visitTimesMap;
    } finally {
      query.closeAll();
      pm.close();
    }
  }

  @Override
  public synchronized boolean isUserSubscribedToEmails(String userId, Long livingStoryId) {
    UserLivingStoryEntity entity = retrieveUserLivingStoryEntity(userId, livingStoryId);
    return entity == null ? false : entity.isSubscribedToEmails();
  }
  
  @Override
  public synchronized int getVisitCountForStory(String userId, Long livingStoryId) {
    UserLivingStoryEntity entity = retrieveUserLivingStoryEntity(userId, livingStoryId);
    return entity == null ? 0 : entity.getVisitCount();
  }

  @Override
  public synchronized FilterSpec getDefaultStoryView(String userId) {
    UserEntity userEntity = retrieveUserEntity(userId);
    if (userEntity == null) {
      return null;
    } else {
      String defaultStoryView = userEntity.getDefaultLspView();
      return defaultStoryView == null ? null : new FilterSpec(defaultStoryView);
    }
  }

  @Override
  public synchronized void updateVisitDataForStory(String userEmail, Long livingStoryId) {
    UserLivingStoryEntity userLivingStoryEntity =
        retrieveUserLivingStoryEntity(userEmail, livingStoryId);
    if (userLivingStoryEntity == null) {
      // This means the user has not visited this living story before. A new row needs to be added.
      // Check if a userEntity exists for this user.  If not, we need to create that first.
      if (retrieveUserEntity(userEmail) == null) {
        createNewUserEntity(userEmail);
      }
      createNewUserLivingStoryEntity(userEmail, livingStoryId, false, null);
    } else {
      // This means the user has visited this living story before and the timestamp needs to be
      // updated in place.
      PersistenceManager pm = PMF.get().getPersistenceManager();
      
      try { 
        // This object needs to be queried using its primary key from the same
        // PeristentManager object as the one that is going to persist it
        userLivingStoryEntity = pm.getObjectById(
            UserLivingStoryEntity.class, userLivingStoryEntity.getId());
        userLivingStoryEntity.setLastVisitedTime(new Date());
        userLivingStoryEntity.incrementVisitCount();
        pm.makePersistent(userLivingStoryEntity);
      } finally {
        pm.close();
      }
    }
  }
  
  @Override
  public synchronized void setEmailSubscription(String userEmail, Long livingStoryId, 
      boolean subscribe, String localeId) {
    UserLivingStoryEntity userLivingStoryEntity =
        retrieveUserLivingStoryEntity(userEmail, livingStoryId);
    if (userLivingStoryEntity == null) {
      // This means the user has not visited this living story before. A new row needs to be added.
      // Check if a userEntity exists for this user.  If not, we need to create that first.
      if (retrieveUserEntity(userEmail) == null) {
        createNewUserEntity(userEmail);
      }
      createNewUserLivingStoryEntity(userEmail, livingStoryId, subscribe, localeId);
    } else {
      // This means the user has visited this living story before and only the subscription needs
      // to be updated.
      PersistenceManager pm = PMF.get().getPersistenceManager();
      try { 
        // This object needs to be queried using its primary key from the same
        // PersistentManager object as the one that is going to persist it
        userLivingStoryEntity = pm.getObjectById(
            UserLivingStoryEntity.class, userLivingStoryEntity.getId());
        userLivingStoryEntity.setSubscribedToEmails(subscribe);
        userLivingStoryEntity.setSubscriptionLocale(localeId);
        pm.makePersistent(userLivingStoryEntity);
      } finally {
        pm.close();
      }
    }
  }

  @Override
  public synchronized void setDefaultStoryView(String userId, FilterSpec defaultView) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    
    try { 
      UserEntity userInfo = pm.getObjectById(UserEntity.class, createKey(userId));
      userInfo.setDefaultLspView(defaultView.getFilterParams());
      pm.makePersistent(userInfo);
    } catch (JDOObjectNotFoundException e) {
      UserEntity userInfo = new UserEntity();
      userInfo.setEmailAddress(userId);
      userInfo.setDefaultLspView(defaultView.getFilterParams());
      pm.makePersistent(userInfo);
    } finally {
      pm.close();
    }
  }

  @Override
  public synchronized void deleteVisitTimesForStory(Long livingStoryId) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query query = pm.newQuery(UserLivingStoryEntity.class);
    query.setFilter("livingStoryId == livingStoryIdParam");
    query.declareParameters("java.lang.Long livingStoryIdParam");
    
    try {
      @SuppressWarnings("unchecked")
      List<UserLivingStoryEntity> userEntities =
          (List<UserLivingStoryEntity>) query.execute(livingStoryId);
      pm.deletePersistentAll(userEntities);
    } finally {
      query.closeAll();
      pm.close();
    }
  }
  
  /**
   * Query a {@link UserEntity} object from the datastore given its key and load
   * the data for all its child objects so that it can be accessed.
   */
  private UserEntity retrieveUserEntity(String userEmail) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      UserEntity userInfo = pm.getObjectById(UserEntity.class, createKey(userEmail));
      // This step is needed to fetch the data for the child objects
      pm.retrieve(userInfo);
      return userInfo;
    } catch (JDOObjectNotFoundException e) {
      return null;
    } finally {
      pm.close();
    }
  }
  
  private UserLivingStoryEntity retrieveUserLivingStoryEntity(
      String userEmail, Long livingStoryId) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query query = pm.newQuery(UserLivingStoryEntity.class);
    query.setFilter("parentEmailAddress == emailParam && livingStoryId == livingStoryIdParam");
    query.declareParameters("java.lang.String emailParam, java.lang.Long livingStoryIdParam");
    
    try {
      @SuppressWarnings("unchecked")
      List<UserLivingStoryEntity> userLivingStoryEntities =
          (List<UserLivingStoryEntity>) query.execute(userEmail, livingStoryId);
      return userLivingStoryEntities.isEmpty() ? null : userLivingStoryEntities.iterator().next();
    } finally {
      query.closeAll();
      pm.close();
    }
  }
  
  /**
   * Return a unique key that can be created for the {@link UserEntity} class from the
   * email address of a Google Account {@link com.google.appengine.api.users.User} object.
   */
  private Key createKey(String userEmail) {
    return KeyFactory.createKey(UserEntity.class.getSimpleName(), userEmail);
  }
  
  /**
   * Create a new {@link UserEntity} data object for a user and the given living story, and
   * persist it to the database. 
   */
  private UserEntity createNewUserEntity(String userEmail) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      UserEntity userInfo = new UserEntity();
      userInfo.setEmailAddress(userEmail);
      pm.makePersistent(userInfo);
      return userInfo;
    } finally {
      pm.close();
    }
  }
  
  /**
   * Save information for an existing user, but a new living story.
   */
  private UserLivingStoryEntity createNewUserLivingStoryEntity(String userEmail,
      Long livingStoryId, boolean subscribedToEmails, String localeId) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try { 
      UserLivingStoryEntity userLivingStoryEntity =
          new UserLivingStoryEntity(userEmail, livingStoryId, new Date());
      userLivingStoryEntity.setSubscribedToEmails(subscribedToEmails);
      if (localeId != null) {
        userLivingStoryEntity.setSubscriptionLocale(localeId);
      }
      pm.makePersistent(userLivingStoryEntity);
      return userLivingStoryEntity;
    } finally {
      pm.close();
    }
  }
}
