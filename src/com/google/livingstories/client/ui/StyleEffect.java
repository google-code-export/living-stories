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

import com.google.gwt.animation.client.Animation;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Widget;

/**
 * Animation effect that takes an integer style attribute and
 * transitions between the current state of the widget and the
 * specified end state.
 */
public class StyleEffect extends Animation {
  protected Widget widget;
  private String attribute;
  private int start;
  private int delta;
  
  public StyleEffect(Widget widget, String attribute, int newValue) {
    this.widget = widget;
    this.attribute = attribute;
    this.start = DOM.getIntStyleAttribute(widget.getElement(), attribute);
    this.delta = newValue - start;
  }
  
  @Override
  public void onUpdate(double progress) {
    progress = interpolate(progress);
    DOM.setStyleAttribute(widget.getElement(),
        attribute, start + progress * delta + "px");
  }
}
