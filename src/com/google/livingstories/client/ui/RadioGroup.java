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
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.VerticalPanel;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Widget that displays a group of radio buttons in a vertical or
 * horizontal layout.  Abstracts away the buttons themselves and lets
 * users deal with selection values directly.
 * 
 * @param <T> The type of value to select between.  Can be anything, though
 *     for multiple selections, an enum is recommended.
 */
public class RadioGroup<T> extends Composite {
  public enum Layout { HORIZONTAL, VERTICAL }
  
  private String groupName;
  private Panel container;
  private Map<T, RadioButton> buttons;
  private T checkedButtonValue;
  private RadioClickHandler<T> handler;
  
  public RadioGroup(String name, Layout layout) {
    groupName = name;
    buttons = new LinkedHashMap<T, RadioButton>();
    
    switch (layout) {
      case HORIZONTAL:
        container = new HorizontalPanel();
        break;
      case VERTICAL:
        container = new VerticalPanel();
        break;
      default:
        throw new IllegalArgumentException("Not a valid layout");
    }
    
    initWidget(container);
  }

  public void addButton(final T value, String caption) {
    RadioButton button = new RadioButton(groupName, caption);
    button.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent e) {
        setValue(value);
        if (handler != null) {
          handler.onClick(value);
        }
      }
    });
    buttons.put(value, button);
    container.add(button);
  }

  public void setValue(T value) {
    RadioButton button = buttons.get(value);
    if (button != null) {
      if (checkedButtonValue != null) {
        buttons.get(checkedButtonValue).setValue(false);
      }
      button.setValue(true);
      checkedButtonValue = value;
    } else {
      throw new IllegalArgumentException("Button " + value + " does not exist");
    }
  }
  
  public T getValue() {
    return checkedButtonValue;
  }
  
  public void setClickHandler(RadioClickHandler<T> handler) {
    this.handler = handler;
  }
  
  public interface RadioClickHandler<T> {
    void onClick(T value);
  }
}
