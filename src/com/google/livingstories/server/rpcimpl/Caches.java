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

import com.google.appengine.api.memcache.InvalidValueException;
import com.google.appengine.api.memcache.MemcacheServiceException;
import com.google.appengine.api.memcache.stdimpl.GCacheException;
import com.google.appengine.api.memcache.stdimpl.GCacheFactory;
import com.google.appengine.repackaged.com.google.common.base.Joiner;
import com.google.livingstories.client.AtomTypesBundle;
import com.google.livingstories.client.BaseAtom;
import com.google.livingstories.client.DisplayAtomBundle;
import com.google.livingstories.client.FilterSpec;
import com.google.livingstories.client.LivingStory;
import com.google.livingstories.client.PlayerAtom;
import com.google.livingstories.client.StartPageBundle;
import com.google.livingstories.client.Theme;
import com.google.livingstories.server.util.LRUCache;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheFactory;
import javax.cache.CacheManager;

/**
 * Class that stores references to different cache instances in the app.
 */
public class Caches {
  // Use a no-expiration memcache to store the most commonly used things.
  private static final Cache noExpirationCache = getCache(0);

  @SuppressWarnings("unchecked")
  private static <T> T get(String key) {
    try {
      return (T) noExpirationCache.get(key);
    } catch (InvalidValueException ex) {
      return null;
    } catch (MemcacheServiceException ex) {
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  private static <T> void put(String key, T value) {
    try {
      noExpirationCache.put(key, value);
    } catch (MemcacheServiceException ex) {
      remove(key);
    } catch (GCacheException e) {
      remove(key);
    }
  }
  
  private static void remove(String key) {
    noExpirationCache.remove(key);
  }

  public static void clearAll() {
    noExpirationCache.clear();
  }


  /** Living story cache methods */

  public static List<LivingStory> getLivingStories() {
    return get(getLivingStoryCacheKey());
  }

  public static synchronized void setLivingStories(List<LivingStory> livingStories) {
    put(getLivingStoryCacheKey(), livingStories);
  }

  public static void clearLivingStories() {
    remove(getLivingStoryCacheKey());
  }

  private static String getLivingStoryCacheKey() {
    return "allLivingStories";
  }


  /** Atoms for livingStory cache methods **/

  public static List<BaseAtom> getLivingStoryAtoms(Long livingStoryId, boolean onlyPublished) {
    return get(getLivingStoryAtomsCacheKey(livingStoryId, onlyPublished));
  }

  public static void setLivingStoryAtoms(
      Long livingStoryId, boolean onlyPublished, List<BaseAtom> livingStoryAtoms) {
    put(getLivingStoryAtomsCacheKey(livingStoryId, onlyPublished), livingStoryAtoms);
  }

  public static void clearLivingStoryAtoms(Long livingStoryId) {
    remove(getLivingStoryAtomsCacheKey(livingStoryId, true));
    remove(getLivingStoryAtomsCacheKey(livingStoryId, false));
    remove(getDisplayAtomBundleCacheKey(livingStoryId));
    remove(getContributorsForLivingStoryCacheKey(livingStoryId));
    // also, in case any non-living-story-specific information was changed here; e.g., authorship
    remove(getDisplayAtomBundleCacheKey(null));
  }

  private static String getLivingStoryAtomsCacheKey(Long livingStoryId, boolean onlyPublished) {
    return "livingStoryAtoms:" + livingStoryId + ":" + onlyPublished;
  }


  /** Theme cache methods **/

  public static List<Theme> getLivingStoryThemes(Long livingStoryId) {
    return get(getLivingStoryThemesCacheKey(livingStoryId));
  }

  public static void setLivingStoryThemes(Long livingStoryId, List<Theme> livingStoryThemes) {
    put(getLivingStoryThemesCacheKey(livingStoryId), livingStoryThemes);
  }

  public static void clearLivingStoryThemes(Long livingStoryId) {
    remove(getLivingStoryThemesCacheKey(livingStoryId));
  }

  private static String getLivingStoryThemesCacheKey(Long livingStoryId) {
    return "angles:" + String.valueOf(livingStoryId);
  }

  public static Map<Long, AtomTypesBundle> getLivingStoryThemeInfo(Long livingStoryId) {
    return get(getLivingStoryThemeInfoCacheKey(livingStoryId));
  }
  
  public static void setLivingStoryThemeInfo(
      Long livingStoryId, Map<Long, AtomTypesBundle> themeInfo) {
    put(getLivingStoryThemeInfoCacheKey(livingStoryId), themeInfo);
  }
  
  public static void clearLivingStoryThemeInfo(Long livingStoryId) {
    remove(getLivingStoryThemeInfoCacheKey(livingStoryId));
  }

  private static String getLivingStoryThemeInfoCacheKey(Long livingStoryId) {
    return "angleinfo:" + String.valueOf(livingStoryId);
  }
  
  /** Contributor cache methods **/
  
  public static Map<Long, PlayerAtom> getContributorsForLivingStory(Long livingStoryId) {
    return get(getContributorsForLivingStoryCacheKey(livingStoryId));
  }
  
  public static void setContributorsForLivingStory(
      Long livingStoryId, Map<Long, PlayerAtom> contributors) {
    put(getContributorsForLivingStoryCacheKey(livingStoryId), contributors);
  }
  
  public static void clearContributorsForLivingStory(Long livingStoryId) {
    remove(getContributorsForLivingStoryCacheKey(livingStoryId));
  }
  
  private static String getContributorsForLivingStoryCacheKey(Long livingStoryId) {
    return "contributors:" + livingStoryId;
  }
  
  /** Display Atom Bundle cache methods **/
  
  private static final int DISPLAY_ATOM_BUNDLE_CACHE_SIZE = 5;
  
  public static DisplayAtomBundle getDisplayAtomBundle(Long livingStoryId,
      FilterSpec filter, Long focusedAtomId, Date cutoff) {
    LRUCache<String, DisplayAtomBundle> cache = get(getDisplayAtomBundleCacheKey(livingStoryId));
    if (cache != null) {
      return cache.get(getDisplayAtomBundleMapKey(filter, focusedAtomId, cutoff));
    } else {
      return null;
    }
  }

  public static void setDisplayAtomBundle(Long livingStoryId,
      FilterSpec filter, Long focusedAtomId, Date cutoff, DisplayAtomBundle bundle) {
    String cacheKey = getDisplayAtomBundleCacheKey(livingStoryId);
    LRUCache<String, DisplayAtomBundle> cache = get(cacheKey);
    if (cache == null) {
      cache = new LRUCache<String, DisplayAtomBundle>(DISPLAY_ATOM_BUNDLE_CACHE_SIZE);
    }
    cache.put(getDisplayAtomBundleMapKey(filter, focusedAtomId, cutoff), bundle);
    put(cacheKey, cache);
  }

  public static void clearDisplayAtomBundles(Long livingStoryId) {
    remove(getDisplayAtomBundleCacheKey(livingStoryId));
  }

  private static String getDisplayAtomBundleCacheKey(Long livingStoryId) {
    return "displayAtomBundle:" + String.valueOf(livingStoryId);
  }  

  private static String getDisplayAtomBundleMapKey(FilterSpec filter,
      Long focusedAtomId, Date cutoff) {
    return Joiner.on(":").useForNull("null").join(filter.getMapKeyString(),
        focusedAtomId, (cutoff == null ? null : cutoff.getTime()));
  }
  
  /** Start page cache methods **/
  
  public static StartPageBundle getStartPageBundle() {
    return get(getStartPageBundleCacheKey());
  }
  
  public static void setStartPageBundle(StartPageBundle bundle) {
    put(getStartPageBundleCacheKey(), bundle);
  }
  
  public static void clearStartPageBundle() {
    remove(getStartPageBundleCacheKey());
  }
  
  private static String getStartPageBundleCacheKey() {
    return "startpage:";
  }

  /**
   * Configures a cache instance with an expiration of expirationSeconds.
   * If expirationSeconds is 0, the cache will not expire.
   */
  @SuppressWarnings("unchecked")
  private static Cache getCache(int expirationSeconds) {
    Cache memcache;

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

    return memcache;
  }
}
