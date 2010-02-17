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
import com.google.common.base.Joiner;
import com.google.livingstories.client.BaseContentItem;
import com.google.livingstories.client.ContentItemTypesBundle;
import com.google.livingstories.client.DisplayContentItemBundle;
import com.google.livingstories.client.FilterSpec;
import com.google.livingstories.client.LivingStory;
import com.google.livingstories.client.PlayerContentItem;
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


  /** ContentItems for livingStory cache methods **/

  public static List<BaseContentItem> getLivingStoryContentItems(Long livingStoryId,
      boolean onlyPublished) {
    return get(getLivingStoryContentItemsCacheKey(livingStoryId, onlyPublished));
  }

  public static void setLivingStoryContentItems(
      Long livingStoryId, boolean onlyPublished, List<BaseContentItem> livingStoryContentItems) {
    put(getLivingStoryContentItemsCacheKey(livingStoryId, onlyPublished), livingStoryContentItems);
  }

  public static void clearLivingStoryContentItems(Long livingStoryId) {
    remove(getLivingStoryContentItemsCacheKey(livingStoryId, true));
    remove(getLivingStoryContentItemsCacheKey(livingStoryId, false));
    remove(getDisplayContentItemBundleCacheKey(livingStoryId));
    remove(getContributorsForLivingStoryCacheKey(livingStoryId));
    // also, in case any non-living-story-specific information was changed here; e.g., authorship
    remove(getDisplayContentItemBundleCacheKey(null));
  }

  private static String getLivingStoryContentItemsCacheKey(Long livingStoryId,
      boolean onlyPublished) {
    return "livingStoryContentItems:" + livingStoryId + ":" + onlyPublished;
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
    return "themes:" + String.valueOf(livingStoryId);
  }

  public static Map<Long, ContentItemTypesBundle> getLivingStoryThemeInfo(Long livingStoryId) {
    return get(getLivingStoryThemeInfoCacheKey(livingStoryId));
  }
  
  public static void setLivingStoryThemeInfo(
      Long livingStoryId, Map<Long, ContentItemTypesBundle> themeInfo) {
    put(getLivingStoryThemeInfoCacheKey(livingStoryId), themeInfo);
  }
  
  public static void clearLivingStoryThemeInfo(Long livingStoryId) {
    remove(getLivingStoryThemeInfoCacheKey(livingStoryId));
  }

  private static String getLivingStoryThemeInfoCacheKey(Long livingStoryId) {
    return "themeinfo:" + String.valueOf(livingStoryId);
  }
  
  /** Contributor cache methods **/
  
  public static Map<Long, PlayerContentItem> getContributorsForLivingStory(Long livingStoryId) {
    return get(getContributorsForLivingStoryCacheKey(livingStoryId));
  }
  
  public static void setContributorsForLivingStory(
      Long livingStoryId, Map<Long, PlayerContentItem> contributors) {
    put(getContributorsForLivingStoryCacheKey(livingStoryId), contributors);
  }
  
  public static void clearContributorsForLivingStory(Long livingStoryId) {
    remove(getContributorsForLivingStoryCacheKey(livingStoryId));
  }
  
  private static String getContributorsForLivingStoryCacheKey(Long livingStoryId) {
    return "contributors:" + livingStoryId;
  }
  
  /** Display content item bundle cache methods **/
  
  private static final int DISPLAY_CONTENT_ITEM_BUNDLE_CACHE_SIZE = 5;
  
  public static DisplayContentItemBundle getDisplayContentItemBundle(Long livingStoryId,
      FilterSpec filter, Long focusedContentItemId, Date cutoff) {
    LRUCache<String, DisplayContentItemBundle> cache =
        get(getDisplayContentItemBundleCacheKey(livingStoryId));
    if (cache != null) {
      return cache.get(getDisplayContentItemBundleMapKey(filter, focusedContentItemId, cutoff));
    } else {
      return null;
    }
  }

  public static void setDisplayContentItemBundle(Long livingStoryId,
      FilterSpec filter, Long focusedContentItemId, Date cutoff, DisplayContentItemBundle bundle) {
    String cacheKey = getDisplayContentItemBundleCacheKey(livingStoryId);
    LRUCache<String, DisplayContentItemBundle> cache = get(cacheKey);
    if (cache == null) {
      cache = new LRUCache<String, DisplayContentItemBundle>(
          DISPLAY_CONTENT_ITEM_BUNDLE_CACHE_SIZE);
    }
    cache.put(getDisplayContentItemBundleMapKey(filter, focusedContentItemId, cutoff), bundle);
    put(cacheKey, cache);
  }

  public static void clearDisplayContentItemBundles(Long livingStoryId) {
    remove(getDisplayContentItemBundleCacheKey(livingStoryId));
  }

  private static String getDisplayContentItemBundleCacheKey(Long livingStoryId) {
    return "displayContentItemBundle:" + String.valueOf(livingStoryId);
  }  

  private static String getDisplayContentItemBundleMapKey(FilterSpec filter,
      Long focusedContentItemId, Date cutoff) {
    return Joiner.on(":").useForNull("null").join(filter.getMapKeyString(),
        focusedContentItemId, (cutoff == null ? null : cutoff.getTime()));
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
