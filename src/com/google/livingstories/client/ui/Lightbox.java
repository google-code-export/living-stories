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
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.livingstories.client.lsp.event.EventBus;
import com.google.livingstories.client.lsp.event.PopupImageLoadedEvent;
import com.google.livingstories.client.util.Constants;
import com.google.livingstories.client.util.LivingStoryControls;

/**
 * A widget that displays content in a centered popup window, with the background dimmed.
 */
public class Lightbox extends AutoHidePopupPanel {
  private Image closeButton;
  private HandlerRegistration popupImageLoadedHandler;
  
  public Lightbox() {
    super(true);
    addCloseHandler(new CloseHandler<PopupPanel>() {
      @Override
      public void onClose(CloseEvent<PopupPanel> event) {
        popupImageLoadedHandler.removeHandler();
        popupImageLoadedHandler = null;
        LivingStoryControls.showGlass(false);
      }
    });
    
    closeButton = new Image(Constants.CLOSE_IMAGE_URL);
    closeButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent e) {
        hide();
      }
    });
  }
  
  /**
   * Show an item in the lightbox. If we have specific knowledge of the desired rendering
   * dimensions we sometimes put the contents in a scroll panel.
   * @param title the title to use
   * @param item the widget to render in the lightbox
   */
  public void showItem(String title, Widget item) {
    VerticalPanel canvas = new VerticalPanel();
    canvas.addStyleName("padding10");
    if (title != null) {
      canvas.add(createHeaderPanel(title));
    }

    canvas.add(item);

    setWidget(canvas);
    center();
    LivingStoryControls.showGlass(true);

    if (popupImageLoadedHandler != null) {
      popupImageLoadedHandler.removeHandler();
    }
    popupImageLoadedHandler = EventBus.INSTANCE.addHandler(PopupImageLoadedEvent.TYPE,
        new PopupImageLoadedEvent.Handler() {
          public void onImageLoaded(PopupImageLoadedEvent e) {
            center();
          }
        });
  }

  /**
   * Reimplementation of center() that does not allow the popup to extend above or to the left
   * of the current scroll position.
   */
  @Override
  public void center() {
    setPopupPositionAndShow(new PositionCallback() {
      @Override
      public void setPosition(int offsetWidth, int offsetHeight) {
        int left = Math.max(0, (Window.getClientWidth() - getOffsetWidth()) / 2);
        int top = Math.max(0, (Window.getClientHeight() - getOffsetHeight()) / 2);
        setPopupPosition(Window.getScrollLeft() + left, Window.getScrollTop() + top);
      }
    });
  }
  
  @Override
  public void hide() {
    super.hide();
  }
  
  private Widget createHeaderPanel(String title) {
    HorizontalPanel headerPanel = new HorizontalPanel();
    headerPanel.setWidth("100%");
    headerPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_LEFT);
    Label titleLabel = new Label(title);
    titleLabel.addStyleName("lightboxTitle");
    headerPanel.add(titleLabel);
    headerPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_RIGHT);
    headerPanel.add(closeButton);
    return headerPanel;
  }
}
