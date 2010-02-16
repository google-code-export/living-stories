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

package com.google.livingstories.client.contentitemlist;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.livingstories.client.BaseContentItem;
import com.google.livingstories.client.Importance;

import java.util.Set;

/**
 * Wraps an AtomListElement in a FocusPanel and fires AtomClickedEvents whenever
 * they are clicked.
 */
public class ClickableContentItemListElement extends ContentItemListElement {
  private ContentItemListElement element;
  private FocusPanel focusPanel;
  
  public ClickableContentItemListElement(ContentItemListElement child,
      final ContentItemClickHandler handler) {
    this.element = child;
    focusPanel = new FocusPanel(element);
    focusPanel.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        handler.onClick(element.getContentItem());
      }
    });
  }

  @Override
  public String getDateString() {
    return element.getDateString();
  }

  @Override
  public Long getId() {
    return element.getId();
  }

  @Override
  public BaseContentItem getContentItem() {
    return element.getContentItem();
  }
  
  @Override
  public Importance getImportance() {
    return element.getImportance();
  }

  @Override
  public Set<Long> getThemeIds() {
    return element.getThemeIds();
  }

  @Override
  public boolean setExpansion(boolean expand) {
    return element.setExpansion(expand);
  }

  @Override
  public void setTimeVisible(boolean visible) {
    element.setTimeVisible(visible);
  }
}
