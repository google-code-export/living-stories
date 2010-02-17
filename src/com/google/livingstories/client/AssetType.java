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
 * Enum that specifies the different types of assets.
 */
public enum AssetType {
  LINK, IMAGE, VIDEO, AUDIO, INTERACTIVE, DOCUMENT;
  
  @Override
  public String toString() {
    return getText(1);
  }

  public String getNavLinkString() {
    if (GWT.isClient()) {
      return EnumTranslator.defaultOrOverride(getPluralPresentationString(),
          EnumTranslator.getNavLinkOverride(this));
    } else {
      return this.name() + " navlink (server-side translation not supported)";
    }
  }

  public String getTitleString() {
    return this == IMAGE ? "" : toString(); 
  }

  public String getText(int count) {
    return GWT.isClient() ? EnumTranslator.translate(this, count)
        : this.name() + " (server-side translation not supported)";
  }

  // TODO: Remove this method and have all callers just call getText directly.
  public String getPluralPresentationString() {
    // We consider "4" to be an adequately representative number for plural form. This will
    // produce odd results in some cases; hence the TODO above. 
    return getText(4);
  }
}
