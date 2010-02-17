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

import com.google.gwt.dom.client.BodyElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * Semitransparent overlay that dims that page for dialog boxes and lightboxes.
 * 
 * There's an existing GlassPanel implementation in gwt-incubator, but this is
 * a tinier version that is sufficient for our needs.
 */
public class GlassPanel extends Composite {
  private SimplePanel glass;
  private Timer timer;
  
  public GlassPanel() {
    glass = new SimplePanel();
    glass.setStylePrimaryName("fixedGlass");
    Element element = glass.getElement();
    DOM.setStyleAttribute(element, "backgroundColor", "#000000");
    DOM.setStyleAttribute(element, "opacity", "0.50");
    DOM.setStyleAttribute(element, "MozOpacity", "0.50");
    DOM.setStyleAttribute(element, "filter",  "alpha(opacity=50)");
    glass.setVisible(false);
    initWidget(glass);
    Window.addResizeHandler(new ResizeHandler() {
      @Override
      public void onResize(ResizeEvent event) {
        if (isVisible()) {
          sizeToDocument();
          
          if (timer != null) {
            timer.cancel();
          }
          // We need to call sizeToDocument() a second time, asynchronously, to get inappropriate
          // scrollbars to disappear when making the window smaller. We do this with a small
          // timeout, though, so as not to repeatedly call extra sizeToDocument over
          // the course of a drag.
          timer = new Timer() {
            @Override
            public void run() {
              // the size actually has to change to get the layout to reflow...
              glass.setSize("1px", "1px");
              sizeToDocument();
              timer = null;
            }
          };
          timer.schedule(100);
        }
      }
    });
  }
  
  @Override
  public void setVisible(boolean visible) {
    if (visible) {
      sizeToDocument();
    }
    super.setVisible(visible);
  }
  
  private void sizeToDocument() {
    BodyElement body = Document.get().getBody();
    glass.setSize(body.getScrollWidth() + "px", body.getScrollHeight() + "px");
  }
}
