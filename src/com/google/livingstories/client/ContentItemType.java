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

package com.google.livingstories.client;

import com.google.gwt.core.client.GWT;

/**
 * This class represents the type of a piece of content such as "Event", "Background", "Photo",
 * etc.
 */
public enum ContentItemType {
  EVENT(true, true), PLAYER(true, true), QUOTE(false, true), BACKGROUND(false, false),
  DATA(true, true), ASSET(true, false), NARRATIVE(false, true), REACTION(true, false);
  
  private boolean validForNav;
  private boolean validForFilter;
  
  ContentItemType(boolean validForNav, boolean validForFilter) {
    this.validForNav = validForNav;
    this.validForFilter = validForFilter;
  }
  
  @Override
  public String toString() {
    return GWT.isClient() ? EnumTranslator.translate(this, 1)
        : this.name() + " (server-side translation not supported)";
  }
  
  private String getPluralPresentationString() {
    // We consider "4" to be an adequately representative number for plural form. This will
    // produce odd results in some cases.
    return EnumTranslator.translate(this, 4);
  }
  
  public String getNavLinkString() {
    if (validForNav) {
      if (GWT.isClient()) {
        return EnumTranslator.defaultOrOverride(getPluralPresentationString(),
            EnumTranslator.getNavLinkOverride(this));
      } else {
        return this.name() + " navlink (server-side translation not supported)";
      }
    } else {
      return null;
    }
  }
  
  public String getFilterString() {
    if (validForFilter) {
      if (GWT.isClient()) {
        return EnumTranslator.defaultOrOverride(getPluralPresentationString(),
            EnumTranslator.getFilterStringOverride(this));
      } else {
        return this.name() + " filter string (server-side translation not supported)";
      }
    } else {
      return null;
    }
  }
}
