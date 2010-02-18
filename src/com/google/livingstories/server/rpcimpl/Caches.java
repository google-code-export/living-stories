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
import java.util.List;
import java.util.Map;

/**
 * Class that stores references to different cache instances in the app.
 */
public class Caches {
  // Use a no-expiration memcache to store the most commonly used things.
  private static final ServerCache noExpirationCache = new AppEngineCacheImpl(0);

  public static void clearAll() {
    noExpirationCache.clear();
  }
  
  /** Living story cache methods */

  public static List<LivingStory> getLivingStories() {
    return noExpirationCache.get(getLivingStoryCacheKey());
  }

  public static synchronized void setLivingStories(List<LivingStory> livingStories) {
    noExpirationCache.put(getLivingStoryCacheKey(), livingStories);
  }

  public static void clearLivingStories() {
    noExpirationCache.remove(getLivingStoryCacheKey());
  }

  private static String getLivingStoryCacheKey() {
    return "allLivingStories";
  }


  /** ContentItems for livingStory cache methods **/

  public static List<BaseContentItem> getLivingStoryContentItems(Long livingStoryId,
      boolean onlyPublished) {
    return noExpirationCache.get(getLivingStoryContentItemsCacheKey(livingStoryId, onlyPublished));
  }

  public static void setLivingStoryContentItems(
      Long livingStoryId, boolean onlyPublished, List<BaseContentItem> livingStoryContentItems) {
    noExpirationCache.put(getLivingStoryContentItemsCacheKey(livingStoryId, onlyPublished),
        livingStoryContentItems);
  }

  public static void clearLivingStoryContentItems(Long livingStoryId) {
    noExpirationCache.remove(getLivingStoryContentItemsCacheKey(livingStoryId, true));
    noExpirationCache.remove(getLivingStoryContentItemsCacheKey(livingStoryId, false));
    noExpirationCache.remove(getDisplayContentItemBundleCacheKey(livingStoryId));
    noExpirationCache.remove(getContributorsForLivingStoryCacheKey(livingStoryId));
    // also, in case any non-living-story-specific information was changed here; e.g., authorship
    noExpirationCache.remove(getDisplayContentItemBundleCacheKey(null));
  }

  private static String getLivingStoryContentItemsCacheKey(Long livingStoryId,
      boolean onlyPublished) {
    return "livingStoryContentItems:" + livingStoryId + ":" + onlyPublished;
  }


  /** Theme cache methods **/

  public static List<Theme> getLivingStoryThemes(Long livingStoryId) {
    return noExpirationCache.get(getLivingStoryThemesCacheKey(livingStoryId));
  }

  public static void setLivingStoryThemes(Long livingStoryId, List<Theme> livingStoryThemes) {
    noExpirationCache.put(getLivingStoryThemesCacheKey(livingStoryId), livingStoryThemes);
  }

  public static void clearLivingStoryThemes(Long livingStoryId) {
    noExpirationCache.remove(getLivingStoryThemesCacheKey(livingStoryId));
  }

  private static String getLivingStoryThemesCacheKey(Long livingStoryId) {
    return "themes:" + String.valueOf(livingStoryId);
  }

  public static Map<Long, ContentItemTypesBundle> getLivingStoryThemeInfo(Long livingStoryId) {
    return noExpirationCache.get(getLivingStoryThemeInfoCacheKey(livingStoryId));
  }
  
  public static void setLivingStoryThemeInfo(
      Long livingStoryId, Map<Long, ContentItemTypesBundle> themeInfo) {
    noExpirationCache.put(getLivingStoryThemeInfoCacheKey(livingStoryId), themeInfo);
  }
  
  public static void clearLivingStoryThemeInfo(Long livingStoryId) {
    noExpirationCache.remove(getLivingStoryThemeInfoCacheKey(livingStoryId));
  }

  private static String getLivingStoryThemeInfoCacheKey(Long livingStoryId) {
    return "themeinfo:" + String.valueOf(livingStoryId);
  }
  
  /** Contributor cache methods **/
  
  public static Map<Long, PlayerContentItem> getContributorsForLivingStory(Long livingStoryId) {
    return noExpirationCache.get(getContributorsForLivingStoryCacheKey(livingStoryId));
  }
  
  public static void setContributorsForLivingStory(
      Long livingStoryId, Map<Long, PlayerContentItem> contributors) {
    noExpirationCache.put(getContributorsForLivingStoryCacheKey(livingStoryId), contributors);
  }
  
  public static void clearContributorsForLivingStory(Long livingStoryId) {
    noExpirationCache.remove(getContributorsForLivingStoryCacheKey(livingStoryId));
  }
  
  private static String getContributorsForLivingStoryCacheKey(Long livingStoryId) {
    return "contributors:" + livingStoryId;
  }
  
  /** Display content item bundle cache methods **/
  
  private static final int DISPLAY_CONTENT_ITEM_BUNDLE_CACHE_SIZE = 5;
  
  public static DisplayContentItemBundle getDisplayContentItemBundle(Long livingStoryId,
      FilterSpec filter, Long focusedContentItemId, Date cutoff) {
    LRUCache<String, DisplayContentItemBundle> cache =
        noExpirationCache.get(getDisplayContentItemBundleCacheKey(livingStoryId));
    if (cache != null) {
      return cache.get(getDisplayContentItemBundleMapKey(filter, focusedContentItemId, cutoff));
    } else {
      return null;
    }
  }

  public static void setDisplayContentItemBundle(Long livingStoryId,
      FilterSpec filter, Long focusedContentItemId, Date cutoff, DisplayContentItemBundle bundle) {
    String cacheKey = getDisplayContentItemBundleCacheKey(livingStoryId);
    LRUCache<String, DisplayContentItemBundle> cache = noExpirationCache.get(cacheKey);
    if (cache == null) {
      cache = new LRUCache<String, DisplayContentItemBundle>(
          DISPLAY_CONTENT_ITEM_BUNDLE_CACHE_SIZE);
    }
    cache.put(getDisplayContentItemBundleMapKey(filter, focusedContentItemId, cutoff), bundle);
    noExpirationCache.put(cacheKey, cache);
  }

  public static void clearDisplayContentItemBundles(Long livingStoryId) {
    noExpirationCache.remove(getDisplayContentItemBundleCacheKey(livingStoryId));
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
    return noExpirationCache.get(getStartPageBundleCacheKey());
  }
  
  public static void setStartPageBundle(StartPageBundle bundle) {
    noExpirationCache.put(getStartPageBundleCacheKey(), bundle);
  }
  
  public static void clearStartPageBundle() {
    noExpirationCache.remove(getStartPageBundleCacheKey());
  }
  
  private static String getStartPageBundleCacheKey() {
    return "startpage:";
  }
}
