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

package com.google.livingstories.client.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.HTML;

/**
 * A widget to create a link that has a ClickHandler. Use GWT's Anchor class for simple links.
 */
public class Link extends HTML {
  public Link() {
    this("", false);
  }
  
  public Link(String content) {
    this(content, false);
  }
  
  public Link(String content, boolean isSecondary) {
    super(content);
    setStylePrimaryName(isSecondary ? "secondaryLink" : "primaryLink");
    addClickHandler(new LinkClickHandler());    
  }

  // Override this method to change the behavior when the link is clicked.
  protected void onClick(ClickEvent e) {}
  
  private class LinkClickHandler implements ClickHandler {
    @Override
    public void onClick(ClickEvent e) {
      Link.this.onClick(e);
    }
  }
}
