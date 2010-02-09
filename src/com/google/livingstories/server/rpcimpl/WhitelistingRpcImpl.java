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

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.livingstories.client.WhitelistedUser;
import com.google.livingstories.client.WhitelistingService;
import com.google.livingstories.server.WhitelistRestrictionEntity;
import com.google.livingstories.server.WhitelistedUserEntity;
import com.google.livingstories.server.dataservices.impl.PMF;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.jdo.Extent;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

/**
 * RPC Service for getting the data for the preview launch whitelisting of external users.
 */
public class WhitelistingRpcImpl extends RemoteServiceServlet implements WhitelistingService {
  private static final Comparator<WhitelistedUser> COMPARATOR = new Comparator<WhitelistedUser>() {
    @Override
    public int compare(WhitelistedUser lhs, WhitelistedUser rhs) {
      return lhs.getEmail().compareTo(rhs.getEmail());
    }
  };

  
  @Override
  public synchronized WhitelistedUser addWhitelistedUser(String email) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    
    try {
      WhitelistedUserEntity entity = new WhitelistedUserEntity(email);
      pm.makePersistent(entity);
      return entity.toClientObject();
    } finally {
      pm.close();
    }
  }
  
  @Override
  public synchronized void deleteWhitelistedUser(Long id) {
    PersistenceManager pm = PMF.get().getPersistenceManager();

    try {
      WhitelistedUserEntity entity = pm.getObjectById(WhitelistedUserEntity.class, id);
      pm.deletePersistent(entity);
    } finally {
      pm.close();
    }
  }
  
  @Override
  public synchronized List<WhitelistedUser> getAllWhitelistedUsers() {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    
    try {
      Extent<WhitelistedUserEntity> entities = pm.getExtent(WhitelistedUserEntity.class);
      List<WhitelistedUser> whitelistedUsers = new ArrayList<WhitelistedUser>(); 
      for (WhitelistedUserEntity entity : entities) {
        whitelistedUsers.add(entity.toClientObject());
      }
      Collections.sort(whitelistedUsers, COMPARATOR);
      return whitelistedUsers;
    } finally {
      pm.close();
    }
  }
  
  public synchronized boolean isUserWhitelisted(String emailParam) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query query = pm.newQuery(WhitelistedUserEntity.class);
    query.setFilter("email == emailParam");
    query.declareParameters("java.lang.String emailParam");

    try {
      @SuppressWarnings("unchecked")
      List<WhitelistedUserEntity> results = (List<WhitelistedUserEntity>) query.execute(emailParam);
      return !results.isEmpty();
    } finally {
      query.closeAll();
      pm.close();
    }
  }
  
  @Override
  public synchronized Boolean isRestrictedToWhitelist() {
    Boolean restrictedToWhitelist = Caches.getIsRestrictedToWhitelist();
    if (restrictedToWhitelist != null) {
      return restrictedToWhitelist;
    }
  
    PersistenceManager pm = PMF.get().getPersistenceManager();
    
    try {
      Extent<WhitelistRestrictionEntity> entities = pm.getExtent(WhitelistRestrictionEntity.class);
      for (WhitelistRestrictionEntity entity : entities) {
        restrictedToWhitelist = entity.getRestrictedToWhitelist();
        break;
      }
      if (restrictedToWhitelist == null) {
        restrictedToWhitelist = true;
      }
      Caches.setRestrictedToWhitelist(restrictedToWhitelist);
      return restrictedToWhitelist;
    } finally {
      pm.close();
    }
  }
  
  @Override
  public synchronized Boolean setRestrictedToWhitelist(Boolean restrictedToWhitelist) {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    
    try {
      Extent<WhitelistRestrictionEntity> entities = pm.getExtent(WhitelistRestrictionEntity.class);
      for (WhitelistRestrictionEntity entity : entities) {
        entity.setRestrictedToWhitelist(restrictedToWhitelist);
        pm.makePersistent(entity);
        Caches.setRestrictedToWhitelist(restrictedToWhitelist);
        return restrictedToWhitelist;
      }
      WhitelistRestrictionEntity entity = new WhitelistRestrictionEntity(restrictedToWhitelist);
      pm.makePersistent(entity);
      Caches.setRestrictedToWhitelist(restrictedToWhitelist);
      return restrictedToWhitelist;
    } finally {
      pm.close();
    }
  }
}
