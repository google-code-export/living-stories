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

/**
 * Helper method to track specific events on the page in Analytics.
 */
public class AnalyticsUtil {
  public static void trackOpenEventAction(String url, Long contentItemId) {
    trackEvent(url, "openEvent", String.valueOf(contentItemId));
  }
  
  public static void trackThemeClick(String url, String theme) {
    trackEvent(url, "angleClick", theme);
  }
  
  public static void trackFilterClick(String url, String filterName) {
    trackEvent(url, "filterClick", filterName);
  }
  
  public static void trackSummaryExpansion(String url) {
    trackEvent(url, "summaryExpansion", "");
  }
  
  public static void trackHorizontalTimelineClick(String url, Long contentItemId) {
    trackEvent(url, "horizontalTimeline", String.valueOf(contentItemId));
  }
  
  public static void trackVerticalTimelineClick(String url, Long contentItemId) {
    trackEvent(url, "verticalTimeline", String.valueOf(contentItemId));
  }

  private static native void trackEvent(String category, String action, String value) /*-{
    if ($wnd._gaq) {
      $wnd._gaq.push(['_trackEvent', category, action, value]);
    }
  }-*/;
}
