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

import java.util.ArrayList;
import java.util.List;

/**
 * An animation effect that applies an interpolated change in integer
 * style attributes over a number of widgets and carries out the
 * transformation in a coordinated manner rather than one widget at a time.
 * 
 * Otherwise operates similarly to {@link com.google.livingstories.client.ui.StyleEffect}
 */
public class MultiWidgetStyleEffect extends Animation {
  protected class Bundle {
    public Widget widget;
    public String attribute;
    public int start;
    public int delta;
    
    Bundle(Widget widget, String attribute, int newValue) {
      this.widget = widget;
      this.attribute = attribute;
      this.start = DOM.getIntStyleAttribute(widget.getElement(), attribute);
      this.delta = newValue - start;
    }
  }
  
  protected List<Bundle> bundles = new ArrayList<Bundle>();
  
  public void addWidgetAndInfo(Widget widget, String attribute, int newValue) {
    bundles.add(new Bundle(widget, attribute, newValue));
  }
  
  @Override
  public void onUpdate(double progress) {
    progress = interpolate(progress);
    for (Bundle bundle : bundles) {
      DOM.setStyleAttribute(bundle.widget.getElement(),
          bundle.attribute, bundle.start + progress * bundle.delta + "px");
    }
  }
}
