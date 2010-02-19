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

import com.google.appengine.api.memcache.InvalidValueException;
import com.google.appengine.api.memcache.MemcacheServiceException;
import com.google.appengine.api.memcache.stdimpl.GCacheException;
import com.google.appengine.api.memcache.stdimpl.GCacheFactory;
import com.google.livingstories.server.dataservices.ServerCache;

import java.util.HashMap;
import java.util.Map;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheFactory;
import javax.cache.CacheManager;

/**
 * A server cache implementation backed by app engine's memcache.
 */
public class AppEngineCacheImpl implements ServerCache {
  private Cache memcache;

  /**
   * Configures a cache instance with an expiration of expirationSeconds.
   * If expirationSeconds is 0, the cache will not expire.
   */
  @SuppressWarnings("unchecked")
  public AppEngineCacheImpl(int expirationSeconds) {
    Map properties = new HashMap();
    if (expirationSeconds > 0) {
      properties.put(GCacheFactory.EXPIRATION_DELTA, expirationSeconds);
    }
    
    try {
      CacheFactory cacheFactory = CacheManager.getInstance().getCacheFactory();
      memcache = cacheFactory.createCache(properties);
    } catch (CacheException ex) {
      throw new RuntimeException(ex);
    }
  }

  
  @SuppressWarnings("unchecked")
  public <T> T get(String key) {
    try {
      return (T) memcache.get(key);
    } catch (InvalidValueException ex) {
      return null;
    } catch (MemcacheServiceException ex) {
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  public <T> void put(String key, T value) {
    try {
      memcache.put(key, value);
    } catch (MemcacheServiceException ex) {
      remove(key);
    } catch (GCacheException e) {
      remove(key);
    }
  }
  
  public void remove(String key) {
    memcache.remove(key);
  }

  public void clear() {
    memcache.clear();
  }
}
