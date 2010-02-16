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
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.livingstories.client.AssetContentItem;
import com.google.livingstories.client.AssetType;
import com.google.livingstories.client.util.BoundedImage;
import com.google.livingstories.client.util.DecoratedBoundedImagePanel;
import com.google.livingstories.client.util.GlobalUtil;

/**
 * Renders the preview for image content items.
 */
public class ImagePreview extends Composite {
  public static String ZOOM_ICON = "/images/zoom_icon.png";
  public static int ZOOM_WIDTH = 16;
  public static int ZOOM_HEIGHT = 16;

  private static final int MAX_PREVIEW_SIZE = 200;
  
  private FlowPanel contentPanel = new FlowPanel();
  private FocusPanel previewPanel = new FocusPanel();
  
  public ImagePreview(AssetContentItem contentItem, boolean alreadySeen, boolean hasFullView) {
    assert contentItem.getAssetType() == AssetType.IMAGE
        && !GlobalUtil.isContentEmpty(contentItem.getPreviewUrl());
    
    BoundedImage image;
    
    if (hasFullView) {
      DecoratedBoundedImagePanel imagePanel = new DecoratedBoundedImagePanel(
          contentItem.getPreviewUrl(), MAX_PREVIEW_SIZE,
          ZOOM_ICON, ZOOM_WIDTH, ZOOM_HEIGHT, DecoratedBoundedImagePanel.IconPlacement.LOWER_RIGHT);
      image = imagePanel.getBoundedImage();
      contentPanel.add(imagePanel);
    } else {
      image = new BoundedImage(contentItem.getPreviewUrl(), MAX_PREVIEW_SIZE);
      contentPanel.add(image);
    }
    image.addStyleName(alreadySeen ? "assetPreviewImageSeen" : "assetPreviewImage");
    image.addStyleName("thumbnail");

    previewPanel.add(contentPanel);
    
    initWidget(previewPanel);
  }
  
  public void addCaption(Widget caption) {
    contentPanel.add(caption);
  }
  
  public void addClickHandler(ClickHandler handler) {
    DOM.setStyleAttribute(previewPanel.getElement(), "cursor", "pointer");
    previewPanel.addClickHandler(handler);
  }
}
