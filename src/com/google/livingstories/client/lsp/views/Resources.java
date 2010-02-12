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

package com.google.livingstories.client.lsp.views;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

/**
 * A resources file that allow programmatic access to resources, including css styles.
 */
public interface Resources extends ClientBundle {
  public static final Resources INSTANCE = GWT.create(Resources.class);
  
  @Source({"Constants.css", "LivingStoryPage.css"})
  public Styles css();
  
  public interface Styles extends CssResource {
    // Constants
    String primaryLinkColor();
    String secondaryLinkColor();
    String primaryTextColor();
    String fadedTextColor();
    String secondaryTextColor();

    // Classes
    String clickable();
    String read();
    String error();
    String atomHeader();
    String linkedItemSpacing();
    String substituteHeaderSpacing();
    String linkedAtomsPanel();
    String hidden();
  }
}
