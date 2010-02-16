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

import com.google.gwt.user.client.ui.Widget;
import com.google.livingstories.client.AssetType;
import com.google.livingstories.client.ContentItemType;
import com.google.livingstories.client.BaseContentItem;
import com.google.livingstories.client.FilterSpec;

/**
 * JSNI methods for global controls on the living story page.
 */
public class LivingStoryControls {
  public static native Widget getCurrentPage() /*-{
    return $wnd.getCurrentPage();
  }-*/;

  public static native void goToPage(Widget page) /*-{
    $wnd.goToPage(page);
  }-*/;

  public static native void goToOverview() /*-{
    $wnd.goToOverview();
  }-*/;
  
  public static native void showGlass(boolean show) /*-{
    $wnd.showGlass(show);
  }-*/;
  
  public static native void showLightbox(String title, BaseContentItem contentItem) /*-{
    $wnd.showLightbox(title, contentItem);
  }-*/;
  
  public static native void setEventListFilters(boolean importantOnly,
      ContentItemType contentItemType, AssetType assetType) /*-{
    $wnd.setEventListFilters(importantOnly, contentItemType, assetType);
  }-*/;
  
  public static native void setFilterZippyState(boolean open) /*-{
    $wnd.setFilterZippyState(open);
  }-*/;

  public static void goToContentItem(long contentItemId) {
    goToContentItemInternal((int)contentItemId);
  }
  
  private static native void goToContentItemInternal(int contentItemId) /*-{
    $wnd.goToContentItem(contentItemId);
  }-*/;
  
  public static native void repositionAnchoredPanel() /*-{
    $wnd.repositionAnchoredPanel();
  }-*/;
  
  public static native void getMoreContentItems() /*-{
    $wnd.getMoreContentItems();
  }-*/;
  
  public static native FilterSpec getCurrentFilterSpec() /*-{
    return $wnd.getCurrentFilterSpec();
  }-*/;
}
