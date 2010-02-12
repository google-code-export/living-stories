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

package com.google.livingstories.server.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Simple implementation of a threadsafe LRU Cache of a fixed size.
 */
public class LRUCache<K,V> implements Serializable {
  private Map<K, V> cache = new HashMap<K, V>();
  private LinkedList<K> cacheKeys = new LinkedList<K>();
  private int maxSize;
  
  public LRUCache(int maxSize) {
    this.maxSize = maxSize;
  }
  
  public synchronized V get(K key) {
    V value = cache.get(key);
    if (value != null) {
      // Access successful, refresh the key value
      cacheKeys.remove(key);
      cacheKeys.push(key);
    }
    return value;
  }
  
  public synchronized void put(K key, V value) {
    if (get(key) != null) {
      return;
    } else {
      if (cacheKeys.size() == maxSize) {
        // Cache is at the maximum size.  Remove the least recently used
        // cache value.
        K lruKey = cacheKeys.removeLast();
        cache.remove(lruKey);        
      }
      cacheKeys.push(key);
      cache.put(key, value);
    }
  }
}
