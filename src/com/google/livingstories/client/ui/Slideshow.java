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
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;
import com.google.livingstories.client.AssetContentItem;
import com.google.livingstories.client.lsp.event.EventBus;
import com.google.livingstories.client.lsp.event.PopupImageLoadedEvent;
import com.google.livingstories.client.lsp.views.contentitems.ImagePopupView;
import com.google.livingstories.client.util.Constants;
import com.google.livingstories.client.util.LivingStoryControls;

import java.util.List;

/**
 * Simple slideshow widget.
 */
public class Slideshow {
  private static final int WINDOW_PADDING = 200;
  
  private AutoHidePopupPanel popup;
  private AutoHidePopupPanel filmstripPopup;
  private Filmstrip filmstrip;
  private VerticalPanel contentPanel;
  private SimplePanel imagePanel;
  
  private List<AssetContentItem> allImages;
  private HandlerRegistration popupImageLoadedHandler;

  public Slideshow(List<AssetContentItem> images) {
    allImages = images;
    
    Image closeButton = new Image(Constants.CLOSE_IMAGE_URL);
    closeButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent e) {
        hide();
      }
    });
    
    HorizontalPanel controlsPanel = new HorizontalPanel();
    controlsPanel.setWidth("100%");
    controlsPanel.add(closeButton);
    controlsPanel.setCellHorizontalAlignment(closeButton, HorizontalPanel.ALIGN_RIGHT);
    
    imagePanel = new SimplePanel();
    
    // Note that this needs to be a VerticalPanel, which is backed by a table,
    // rather than a flow panel, which is backed by a div.  Otherwise, the
    // width is unconstrained in chrome and the popup takes up the whole
    // width of the screen.
    contentPanel = new VerticalPanel();
    contentPanel.add(controlsPanel);
    contentPanel.add(imagePanel);
    
    popup = new AutoHidePopupPanel(true);
    popup.add(contentPanel);
    popup.addCloseHandler(new CloseHandler<PopupPanel>() {
      @Override
      public void onClose(CloseEvent<PopupPanel> event) {
        hide();
      }
    });
    
    filmstrip = new Filmstrip() {
      @Override
      public void onNavigate(int newIndex) {
        displayImage(newIndex);
      }
    };
    filmstrip.loadImages(allImages);

    filmstripPopup = new AutoHidePopupPanel(true);
    filmstripPopup.add(filmstrip);
    
    popup.addAutoHidePartner(filmstripPopup.getElement());
    filmstripPopup.addAutoHidePartner(popup.getElement());
  }
   
  public void show(int startIndex) {
    LivingStoryControls.showGlass(true);
    filmstrip.startHandlingKeys();
    filmstrip.navigate(startIndex);
    
    if (popupImageLoadedHandler != null) {
      popupImageLoadedHandler.removeHandler();
    }
    popupImageLoadedHandler = EventBus.INSTANCE.addHandler(PopupImageLoadedEvent.TYPE,
        new PopupImageLoadedEvent.Handler() {
          public void onImageLoaded(PopupImageLoadedEvent e) {
            // Reposition the popup/filmstrip to take image size changes, window size changes,
            // and scrolling into account.
            showPopup();
            showFilmstrip();
          }
        });
  }
  
  public void hide() {
    filmstrip.stopHandlingKeys();
    LivingStoryControls.showGlass(false);
    popup.hide();
    filmstripPopup.hide();
    if (popupImageLoadedHandler != null) {
      popupImageLoadedHandler.removeHandler();
      popupImageLoadedHandler = null;
    }
  }

  private void displayImage(int index) {
    final AssetContentItem currentImage = allImages.get(index);
    imagePanel.setWidget(new ImagePopupView(currentImage));
    // Reposition the popup/filmstrip to take image size changes, window size changes,
    // and scrolling into account.
    showPopup();
    showFilmstrip();
  }
  
  private void showFilmstrip() {
    if (allImages.size() > 1) {
      filmstripPopup.setPopupPositionAndShow(new PositionCallback() {
        @Override
        public void setPosition(int offsetWidth, int offsetHeight) {
          filmstripPopup.setPopupPosition(
              Window.getScrollLeft() + (Window.getClientWidth() - offsetWidth) / 2,
              Window.getScrollTop() + (Window.getClientHeight() - offsetHeight));
        }
      });
    }
  }
  
  private void showPopup() {
    popup.setPopupPositionAndShow(new PositionCallback() {
      @Override
      public void setPosition(int offsetWidth, int offsetHeight) {
        popup.setPopupPosition(
            Window.getScrollLeft() + (Window.getClientWidth() - offsetWidth) / 2,
            Window.getScrollTop() + (Window.getClientHeight() - offsetHeight) / 2
                - WINDOW_PADDING / 4);
      }      
    });
  }
}
