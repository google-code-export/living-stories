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

package com.google.livingstories.client.util;

import com.google.livingstories.client.BaseAtom;

import java.util.Comparator;

/**
 * Comparator that sorts atoms first by importance (high to low),
 * and then by timestamp (newer to older).
 */
public class AtomComparator implements Comparator<BaseAtom> {
  @Override
  public int compare(BaseAtom lhs, BaseAtom rhs) {
    // Sort by importance
    int result = lhs.getImportance().compareTo(rhs.getImportance());
    if (result != 0) {
      return result;
    }
    // Sort reverse chronologically
    // TODO: should we be using .getDateSortKey() here instead?
    result = -lhs.getTimestamp().compareTo(rhs.getTimestamp());
    if (result != 0) {
      return result;
    }
    return lhs.getId().compareTo(rhs.getId());
  }
}
