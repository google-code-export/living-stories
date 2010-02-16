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

import com.google.livingstories.client.LivingStory;

import java.util.Iterator;

/**
 * An iterator that keeps count of how many stories have been iterated over, and also excludes
 * a provided story from the iteration.
 */
public class LivingStoryIterator implements Iterator<LivingStory> {
  private final Iterator<LivingStory> wrapped;
  private long count = 0;
  private LivingStory excludedStory;
  private long countLimit;
  
  public LivingStoryIterator(Iterator<LivingStory> iterator, LivingStory excludedStory) {
    this(iterator, excludedStory, Long.MAX_VALUE);
  }
  
  public LivingStoryIterator(Iterator<LivingStory> iterator, LivingStory excludedStory,
      long countLimit) {
    this.wrapped = iterator;
    this.excludedStory = excludedStory;
    this.countLimit = countLimit;
  }
  
  public boolean hasNext() {
    return count < countLimit && wrapped.hasNext();
  }

  public LivingStory next() {
    LivingStory result = wrapped.next();
    if (result.getId() == excludedStory.getId()) {
      if (hasNext()) {
        return next();
      } else {
        return null;
      }
    } else {
      ++count;
      return result;
    }
  }
  
  public void remove()  {
    wrapped.remove();
  }
  
  /**
   * @return the number of elements returned by next().
   */
  public long getCount() {
    return count;
  }
}
