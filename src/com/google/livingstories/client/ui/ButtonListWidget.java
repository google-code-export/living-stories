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

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Render a list of items in a button-ish looking list.
 * Each item in the list is clickable.
 */
public class ButtonListWidget extends Composite {
  private VerticalPanel container;
  
  public ButtonListWidget() {
    super();
    container = new VerticalPanel();
    initWidget(container);
  }

  public void addItem(Widget widget, ClickHandler clickHandler, boolean selected) {
    FocusPanel panel = new FocusPanel();
    panel.setStylePrimaryName("buttonListItem");
    if (selected) {
      panel.addStyleName("selectedButtonListItem");
    }
    panel.add(widget);
    panel.addClickHandler(clickHandler);
    container.add(panel);
  }
  
  public void selectItem(Widget selectedWidget) {
    int selectedIndex = container.getWidgetIndex(selectedWidget);
    selectItem(selectedIndex);
  }
  
  public void selectItem(int index) {
    for (int i = 0; i < container.getWidgetCount(); i++) {
      Widget widget = container.getWidget(i);
      if (i == index) {
        widget.addStyleName("selectedButtonListItem");
      } else {
        widget.removeStyleName("selectedButtonListItem");
      }
    }    
  }
  
  public void clear() {
    container.clear();
  }
}
