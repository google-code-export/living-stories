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
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.livingstories.client.AssetContentItem;
import com.google.livingstories.client.util.SquareImage;

import java.util.List;

/**
 * Widget that shows a strip of smaller widgets, and scrolls between them.
 * Useful for image thumbnails, etc.
 */
public class Filmstrip extends Composite {
  private static final String ENABLED_PREVIOUS_ARROW = "/images/arrow-left.gif";
  private static final String DISABLED_PREVIOUS_ARROW = "/images/arrow-left-disabled.gif";
  private static final String ENABLED_NEXT_ARROW = "/images/arrow-right.gif";
  private static final String DISABLED_NEXT_ARROW = "/images/arrow-right-disabled.gif";
  private static final String FILMSTRIP_STYLE = "filmstrip";
  private static final int VISIBLE_ITEM_COUNT = 5;
  private static final int ITEM_SIZE = 100;
  private static final int ITEM_SPACING = 2;
  private static final int CURRENT_ITEM_BORDER = 4;
  private static final int TOTAL_ITEM_WIDTH = ITEM_SIZE + ITEM_SPACING;
  
  
  private HorizontalPanel filmstrip;
  private Image previousArrow;
  private Image nextArrow;
  private HorizontalPanel contentPanel;
  private SimplePanel clippingPane;

  private int currentIndex = 0;

  private HandlerRegistration nativePreviewHandlerRegistration;
    
  public Filmstrip() {
    previousArrow = new Image(ENABLED_PREVIOUS_ARROW);
    previousArrow.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent e) {
        int newIndex = currentIndex - 1;
        if (newIndex >= 0) {
          navigate(newIndex);
        }
      }
    });
    nextArrow = new Image(ENABLED_NEXT_ARROW);
    nextArrow.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent e) {
        int newIndex = currentIndex + 1;
        if (newIndex < contentPanel.getWidgetCount()) {
          navigate(newIndex);
        }
      }
    });
    
    contentPanel = new HorizontalPanel();
    contentPanel.setSpacing(ITEM_SPACING);
    contentPanel.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
    DOM.setStyleAttribute(contentPanel.getElement(), "position", "relative");

    clippingPane = new SimplePanel();
    DOM.setStyleAttribute(clippingPane.getElement(), "overflow", "hidden");
    clippingPane.add(contentPanel);
    
    filmstrip = new HorizontalPanel();
    filmstrip.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
    filmstrip.setStylePrimaryName(FILMSTRIP_STYLE);
    filmstrip.add(previousArrow);
    filmstrip.add(clippingPane);
    filmstrip.add(nextArrow);

    initWidget(filmstrip);
  }

  public void loadImages(List<AssetContentItem> images) {
    contentPanel.clear();
    int i = 0;
    for (AssetContentItem image : images) {
      final SquareImage imageWidget = new SquareImage(image.getPreviewUrl(), ITEM_SIZE);
      final int imageIndex = i++;
      imageWidget.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          navigate(imageIndex);
        }
      });
      contentPanel.add(imageWidget);
    }
    clippingPane.setWidth(TOTAL_ITEM_WIDTH * getVisibleItemRange(0).getExtent()
        + CURRENT_ITEM_BORDER + "px");
  }

  public int getCurrentIndex() {
    return currentIndex;
  }

  public void startHandlingKeys() {
    stopHandlingKeys();
    nativePreviewHandlerRegistration = Event.addNativePreviewHandler(new NativePreviewHandler() {
      @Override
      public void onPreviewNativeEvent(NativePreviewEvent event) {
        Event nativeEvent = Event.as(event.getNativeEvent());
        if (nativeEvent.getTypeInt() == Event.ONKEYDOWN) {
          switch (nativeEvent.getKeyCode()) {
            case KeyCodes.KEY_HOME:
              navigate(0);
              break;
            case KeyCodes.KEY_LEFT:
              if (currentIndex > 0) {
                navigate(currentIndex - 1);
              }
              break;
            case KeyCodes.KEY_RIGHT:
              if (currentIndex < contentPanel.getWidgetCount() - 1) {
                navigate(currentIndex + 1);
              }
              break;
            case KeyCodes.KEY_END:
              navigate(contentPanel.getWidgetCount() - 1);
              break;
          }
          // we don't want any sort of keyboard scrolling while the slideshow is up, regardless
          // of the exact keycode. The combination of preventDefault() and stopPropagation() is
          // necessary in both FF and IE. As a historical note, one can do without the
          // stopPropagation() in FF if one also does a preventDefault on keypress,
          // but this is little more than a curiosity.
          nativeEvent.preventDefault();
          nativeEvent.stopPropagation();
        }
      }
    });
  }
  
  public void stopHandlingKeys() {
    // stop previewing page events
    if (nativePreviewHandlerRegistration != null) {
      nativePreviewHandlerRegistration.removeHandler();
      nativePreviewHandlerRegistration = null;
    }
  }
  
  public void onNavigate(int newIndex) {}
  
  public void navigate(int newIndex) {
    if (currentIndex >= 0) {
      Widget currentWidget = contentPanel.getWidget(currentIndex);
      currentWidget.removeStyleName("filmstrip-current");
    }
    
    Widget newWidget = contentPanel.getWidget(newIndex);
    newWidget.addStyleName("filmstrip-current");

    Range visibleItemRange = getVisibleItemRange(newIndex);
    
    clippingPane.setWidth(TOTAL_ITEM_WIDTH * (visibleItemRange.getExtent()) + 
        CURRENT_ITEM_BORDER + "px");

    StyleEffect slide = new StyleEffect(contentPanel, "left",
        -TOTAL_ITEM_WIDTH * visibleItemRange.start);
    slide.run(500);

    currentIndex = newIndex;
    
    if (currentIndex == 0) {
      previousArrow.setUrl(DISABLED_PREVIOUS_ARROW);
    } else {
      previousArrow.setUrl(ENABLED_PREVIOUS_ARROW);
    }
    
    if (currentIndex == contentPanel.getWidgetCount() - 1) {
      nextArrow.setUrl(DISABLED_NEXT_ARROW);
    } else {
      nextArrow.setUrl(ENABLED_NEXT_ARROW);
    }
    
    onNavigate(currentIndex);
  }
  
  private Range getVisibleItemRange(int itemIndex) {
    int start = itemIndex;
    int end = itemIndex + 1;
    while (start > 0 || end < contentPanel.getWidgetCount()) {
      if (start > 0) {
        start--;
      }
      if (end < contentPanel.getWidgetCount()) {
        end++;
      }
      if (end - start == VISIBLE_ITEM_COUNT) {
        break;
      }
    }
    return new Range(start, end);
  }
  
  private class Range {
    public final int start;
    public final int end;
    
    public Range(int start, int end) {
      this.start = start;
      this.end = end;
    }
    
    public int getExtent() {
      return end - start;
    }
  }
}
