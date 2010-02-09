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
import com.google.livingstories.client.Publisher;
import com.google.livingstories.server.AdminUserEntity;
import com.google.livingstories.server.UserEntity;
import com.google.livingstories.server.UserLspEntity;
import com.google.livingstories.server.dataservices.UserDataService;

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
    UserLspEntity entity = retrieveUserLspEntity(userId, livingStoryId);
    return entity == null ? null : entity.getLastVisitedTime();
  }
  
  @Override
  public synchronized Map<Long, Date> getAllLastVisitTimes(String userId) {
    UserEntity userEntity = retrieveUserEntity(userId);
    Map<Long, Date> visitTimesMap = new HashMap<Long, Date>();
    if (userEntity != null) {
      List<UserLspEntity> storyDataList = userEntity.getLspDataList();
      if (storyDataList != null) {
        for (UserLspEntity userLspEntity : storyDataList) {
          visitTimesMap.put(userLspEntity.getLspId(), userLspEntity.getLastVisitedTime());
        }
      }
    }
    return visitTimesMap;
  }

  @Override
  public synchronized boolean isUserSubscribedToEmails(String userId, Long livingStoryId) {
    UserLspEntity entity = retrieveUserLspEntity(userId, livingStoryId);
    return entity == null ? false : entity.isSubscribedToEmails();
  }
  
  @Override
  public synchronized int getVisitCountForStory(String userId, Long livingStoryId) {
    UserLspEntity entity = retrieveUserLspEntity(userId, livingStoryId);
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
  public synchronized void updateVisitDataForStory(String userId, Long livingStoryId) {
    UserEntity userEntity = retrieveUserEntity(userId);
    if (userEntity == null) {
      userEntity = createNewUserEntity(userId);
    }

    UserLspEntity userLspEntity = userEntity.getUserDataPerLsp(livingStoryId);
    if (userLspEntity == null) {
      // This means the user has not visited this LSP before. A new row needs to be added.
      createNewUserLspEntity(userId, livingStoryId, false);
    } else {
      // This means the user has visited this LSP before and the timestamp needs to be
      // updated in place.
      PersistenceManager pm = PMF.get().getPersistenceManager();
      
      try { 
        // This object needs to be queried using its primary key from the same
        // PeristentManager object as the one that is going to persist it
        userLspEntity = pm.getObjectById(UserLspEntity.class, userLspEntity.getId());
        userLspEntity.setLastVisitedTime(new Date());
        userLspEntity.incrementVisitCount();
        pm.makePersistent(userLspEntity);
      } finally {
        pm.close();
      }
    }
  }
  
  @Override
  public synchronized void setEmailSubscription(String userId, Long livingStoryId, 
      boolean subscribe) {
    UserEntity userEntity = retrieveUserEntity(userId);
    if (userEntity == null) {
      userEntity = createNewUserEntity(userId);
    }

    UserLspEntity userLspEntity = userEntity.getUserDataPerLsp(livingStoryId);
    if (userLspEntity == null) {
      // This means the user has not visited this LSP before. A new row needs to be added.
      createNewUserLspEntity(userId, livingStoryId, subscribe);
    } else {
      // This means the user has visited this LSP before and only the subscription needs to be 
      // updated.
      PersistenceManager pm = PMF.get().getPersistenceManager();
      try { 
        // This object needs to be queried using its primary key from the same
        // PersistentManager object as the one that is going to persist it
        userLspEntity = pm.getObjectById(UserLspEntity.class, userLspEntity.getId());
        userLspEntity.setSubscribedToEmails(subscribe);
        pm.makePersistent(userLspEntity);
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
      userInfo.setGoogleAccountId(createKey(userId));
      userInfo.setDefaultLspView(defaultView.getFilterParams());
      pm.makePersistent(userInfo);
    } finally {
      pm.close();
    }
  }

  @Override
  public synchronized void deleteVisitTimesForStory(Long livingStoryId) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query query = pm.newQuery(UserLspEntity.class);
    query.setFilter("lspId == lspIdParam");
    query.declareParameters("java.lang.Long lspIdParam");
    
    try {
      @SuppressWarnings("unchecked")
      List<UserLspEntity> userEntities = (List<UserLspEntity>) query.execute(livingStoryId);
      pm.deletePersistentAll(userEntities);
    } finally {
      query.closeAll();
      pm.close();
    }
  }
  
  @Override
  public synchronized Publisher getPublisherForAdminUser(String adminUserId) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query query = pm.newQuery(AdminUserEntity.class);
    query.setFilter("email == emailParam");
    query.declareParameters("String emailParam");

    try {
      @SuppressWarnings("unchecked")
      List<AdminUserEntity> results = (List<AdminUserEntity>) query.execute(adminUserId);
      if (results.isEmpty()) {
        return null;
      } else {
        return results.get(0).getPublisher();
      }
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
  
  private UserLspEntity retrieveUserLspEntity(String userEmail, Long livingStoryId) {
    UserEntity userEntity = retrieveUserEntity(userEmail);
    return userEntity == null ? null : userEntity.getUserDataPerLsp(livingStoryId);
  }
  
  /**
   * Return a unique key that can be created for the {@link UserEntity} class from the
   * email address of a Google Account {@link com.google.appengine.api.users.User} object.
   */
  private Key createKey(String userEmail) {
    return KeyFactory.createKey(UserEntity.class.getSimpleName(), userEmail);
  }
  
  /**
   * Create a new {@link UserEntity} data object for a user and the given lsp, and
   * persist it to the database. 
   */
  private UserEntity createNewUserEntity(String userEmail) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    try {
      UserEntity userInfo = new UserEntity();
      userInfo.setGoogleAccountId(createKey(userEmail));
      pm.makePersistent(userInfo);
      return userInfo;
    } finally {
      pm.close();
    }
  }
  
  /**
   * Save information for an existing user, but a new LSP.
   */
  private UserLspEntity createNewUserLspEntity(String userEmail, Long lspId, 
      boolean subscribedToEmails) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    
    try { 
      UserEntity userInfo = pm.getObjectById(UserEntity.class, createKey(userEmail));
      UserLspEntity userLspEntity = userInfo.addNewLspTimestamp(lspId);
      userLspEntity.setSubscribedToEmails(subscribedToEmails);
      pm.makePersistent(userInfo);
      return userLspEntity;
    } finally {
      pm.close();
    }
  }
}
