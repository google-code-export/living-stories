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

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Window.ScrollEvent;
import com.google.gwt.user.client.Window.ScrollHandler;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;


/**
 * Panel that makes its contents scroll with the page.
 * However, the scrollable area is bounded by the box defined by this panel.
 * When the content hits a boundary, it will stop scrolling.
 */
public class AnchoredPanel extends SimplePanel {
  private boolean scrolling;
  private HandlerRegistration scrollHandler;
  private HandlerRegistration resizeHandler;
  
  
  @Override
  public void setWidget(Widget widget) {
    super.setWidget(widget);
    Style style = widget.getElement().getStyle();
    style.setProperty("position", "relative");
    style.setPropertyPx("left", 0);
    style.setPropertyPx("top", 0);

    removeHandlers();
    
    scrollHandler = Window.addWindowScrollHandler(new ScrollHandler() {
      @Override
      public void onWindowScroll(ScrollEvent event) {
        reposition();
      }
    });
    
    resizeHandler = Window.addResizeHandler(new ResizeHandler() {
      @Override
      public void onResize(ResizeEvent event) {
        reposition();
      }
    });
  }
  
  public void remove() {
    super.clear();
    removeHandlers();
  }
  
  private void removeHandlers() {
    if (scrollHandler != null) {
      scrollHandler.removeHandler();
      scrollHandler = null;
    }
    if (resizeHandler != null) {
      resizeHandler.removeHandler();
      resizeHandler = null;
    }    
  }
  
  public void reposition() {
    Widget content = getWidget();
    Element boundingBox = getElement().getParentElement();
    int windowTop = Window.getScrollTop();
    int windowLeft = Window.getScrollLeft();
    int topBound = boundingBox.getAbsoluteTop();
    int bottomBound = topBound + boundingBox.getOffsetHeight() - content.getOffsetHeight();
    if (!scrolling) {
      if (windowTop > topBound && windowTop < bottomBound) {
        scrolling = true;
        Style style = content.getElement().getStyle();
        style.setProperty("position", "fixed");
        style.setPropertyPx("top", 0);
        style.setPropertyPx("left", boundingBox.getAbsoluteLeft() - windowLeft);
      }
    } else {
      if (windowTop < topBound) {
        scrolling = false;
        Style style = content.getElement().getStyle();
        style.setProperty("position", "relative");
        style.setPropertyPx("top", 0);
        style.setPropertyPx("left", 0);            
      } else if (windowTop > bottomBound) {
        scrolling = false;
        Style style = content.getElement().getStyle();
        style.setProperty("position", "relative");
        // Can't use bottom:0px here because the spacer has no height.
        // Even if we set the spacer's height to be 100%, it won't necessarily
        // work if this panel is in a table cell.
        style.setPropertyPx("top", bottomBound - topBound);
        style.setPropertyPx("left", 0);            
      } else {
        content.getElement().getStyle().setPropertyPx(
            "left", boundingBox.getAbsoluteLeft() - windowLeft);
      }
    }
  }
}
