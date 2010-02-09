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

import com.google.livingstories.client.FilterSpec;
import com.google.livingstories.client.lsp.views.OverviewPage;

/**
 * Class that caches commonly used pages.  Currently only the Overview page is cached here,
 * but we may eventually want to cache things like popular player pages, etc.
 */
public class PageCache {
  private static OverviewPage overviewPage;
  
  /**
   * Gets the current overviewPage instance, changes its filters according to
   * the specified filter parameters, and highlights the specified atom.
   * If the filter parameters are null, it just returns the page as-is.
   */
  public static OverviewPage getOverviewPage(FilterSpec filterParams, Long focusedAtomId) {
    if (overviewPage == null) {
      overviewPage = new OverviewPage();
    }
    if (filterParams != null) {
      overviewPage.update(filterParams, focusedAtomId);
    }
    return overviewPage;    
  }
}
