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
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.livingstories.client.BaseContentItem;
import com.google.livingstories.client.Importance;
import com.google.livingstories.client.lsp.ContentItemRenderer;
import com.google.livingstories.client.lsp.views.DateTimeRangeWidget;

import java.util.Set;

/**
 * For the filtered view such as "Only photos", "Only background", this implementation of 
 * {@link ContentItemListElement} can be used for the content items.
 */
public class SimpleContentItemListElement implements ContentItemListElement {
  private BaseContentItem contentItem;
  private ContentItemClickHandler handler;
  private DateTimeRangeWidget dateTimeWidget;
  
  public SimpleContentItemListElement(BaseContentItem contentItem,
      ContentItemClickHandler handler) {
    this.contentItem = contentItem;
    this.handler = handler;
  }
  
  @Override
  public Long getId() {
    return contentItem.getId();
  }
  
  @Override
  public Set<Long> getThemeIds() {
    return contentItem.getThemeIds();
  }

  @Override
  public Importance getImportance() {
    return contentItem.getImportance();
  }

  
  @Override
  public Widget render(boolean includeName) {
    HorizontalPanel row = new HorizontalPanel();
    row.addStyleName("bottomBorder");
    row.setWidth("100%");
    ContentItemRenderer renderer = new ContentItemRenderer(contentItem, includeName, true);
    if (handler == null) {
      row.add(renderer);
    } else {
      FocusPanel focusPanel = new FocusPanel();
      focusPanel.add(renderer);
      focusPanel.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent e) {
          if (handler != null) {
            handler.onClick(contentItem);
          }
        }
      });
      row.add(focusPanel);
    }
    
    dateTimeWidget = new DateTimeRangeWidget();
    dateTimeWidget.setDateTime(contentItem.getTimestamp(), null);
    DOM.setStyleAttribute(dateTimeWidget.getElement(), "float", "right");
    DOM.setStyleAttribute(dateTimeWidget.getElement(), "paddingTop", "5px");
    row.add(dateTimeWidget);
    row.setCellHorizontalAlignment(dateTimeWidget, HasHorizontalAlignment.ALIGN_RIGHT);
    row.setCellWidth(dateTimeWidget, "100px");
    return row;
  }
  
  @Override
  public String getDateString() {
    return dateTimeWidget.getDateString();
  }
  
  @Override
  public void setTimeVisible(boolean visible) {
    dateTimeWidget.setTimeVisible(visible);
  }
  
  @Override
  public boolean setExpansion(boolean expand) {
    // Do nothing
    return false;
  }
}
