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
import com.google.livingstories.client.AtomType;
import com.google.livingstories.client.BaseAtom;
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
  
  public static native void showLightbox(String title, BaseAtom atom) /*-{
    $wnd.showLightbox(title, atom);
  }-*/;
  
  public static native void setEventListFilters(boolean importantOnly,
      AtomType atomType, AssetType assetType) /*-{
    $wnd.setEventListFilters(importantOnly, atomType, assetType);
  }-*/;
  
  public static native void setFilterZippyState(boolean open) /*-{
    $wnd.setFilterZippyState(open);
  }-*/;

  public static void goToAtom(long atomId) {
    goToAtomInternal((int)atomId);
  }
  
  private static native void goToAtomInternal(int atomId) /*-{
    $wnd.goToAtom(atomId);
  }-*/;
  
  public static native void repositionAnchoredPanel() /*-{
    $wnd.repositionAnchoredPanel();
  }-*/;
  
  public static native void getMoreAtoms() /*-{
    $wnd.getMoreAtoms();
  }-*/;
  
  public static native FilterSpec getCurrentFilterSpec() /*-{
    return $wnd.getCurrentFilterSpec();
  }-*/;
}
