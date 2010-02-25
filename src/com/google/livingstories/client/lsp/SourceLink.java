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

package com.google.livingstories.client.lsp;

import com.google.livingstories.client.ui.JavascriptLink;

/**
 * Create an inline source content item link.
 */
public class SourceLink extends JavascriptLink {
  private static String LINK_TEXT = LspMessageHolder.consts.sourceLinkText();
  
  public SourceLink(String description, Long contentItemId) {
    super(LINK_TEXT);
    setStylePrimaryName("secondaryLink");
    addStyleName("sourceInfoLink");
    setOnClick("showSourcePopup('" + description + "', " + contentItemId + ", this)");
  }
}
