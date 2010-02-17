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

package com.google.livingstories.client.lsp.views.contentitems;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.livingstories.client.AssetContentItem;
import com.google.livingstories.client.lsp.BylineWidget;
import com.google.livingstories.client.lsp.LspMessageHolder;
import com.google.livingstories.client.lsp.event.EventBus;
import com.google.livingstories.client.lsp.event.PopupImageLoadedEvent;
import com.google.livingstories.client.lsp.views.Resources;
import com.google.livingstories.client.ui.OverlayLabel;
import com.google.livingstories.client.util.BoundedImage;

/**
 * Popup view for an image.  Fires a PopupImageLoadedEvent through the event bus so
 * popup panels can recenter themselves based on the size of the image.
 */
public class ImagePopupView extends Composite {
  private static ImagePopupViewUiBinder uiBinder = GWT.create(ImagePopupViewUiBinder.class);
  interface ImagePopupViewUiBinder extends UiBinder<Widget, ImagePopupView> {
  }

  private static String BROKEN_LINK_TEXT = LspMessageHolder.consts.imageUnavailable();
  private static final int WINDOW_PADDING = 200;

  @UiField SimplePanel image;
  @UiField OverlayLabel caption;
  @UiField SimplePanel byline;

  public ImagePopupView(final AssetContentItem contentItem) {
    initWidget(uiBinder.createAndBindUi(this));
    
    new BoundedImage(
        contentItem.getContent(),
        Window.getClientWidth() - WINDOW_PADDING,
        Window.getClientHeight() - WINDOW_PADDING) {
      @Override
      public void onImageLoad(boolean failed) {
        PopupImageLoadedEvent event;
        if (failed) {
          Label brokenLink = new Label(BROKEN_LINK_TEXT);
          brokenLink.setStylePrimaryName(Resources.INSTANCE.css().error());
          image.setWidget(brokenLink);
          image.setSize("250px", "150px");
          event = new PopupImageLoadedEvent(250, 150);
        } else {
          image.setWidget(this);
          event = new PopupImageLoadedEvent(this.getDisplayWidth(), this.getDisplayHeight());
        }
        caption.setText(contentItem.getCaption());
        if (!contentItem.getContributorIds().isEmpty()) {
          byline.setWidget(new BylineWidget(contentItem, false));
        }
        EventBus.INSTANCE.fireEvent(event);
      }
    };
  }
}
