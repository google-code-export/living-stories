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
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.livingstories.client.AssetContentItem;
import com.google.livingstories.client.lsp.views.Resources;
import com.google.livingstories.client.ui.ImagePreview;
import com.google.livingstories.client.ui.Slideshow;
import com.google.livingstories.client.util.Constants;
import com.google.livingstories.client.util.DecoratedBoundedImagePanel;
import com.google.livingstories.client.util.DecoratedBoundedImagePanel.IconPlacement;

import java.util.List;

/**
 * Renders a preview image for a slideshow with a 'Slideshow' header, and pops up the
 * appropriate widget when clicked.
 */
public class SlideshowPreview extends Composite {
  private static SlideshowPreviewUiBinder uiBinder = GWT.create(SlideshowPreviewUiBinder.class);
  interface SlideshowPreviewUiBinder extends UiBinder<Widget, SlideshowPreview> {
  }

  @UiField Label header;
  @UiField FocusPanel previewImage;
  
  private AssetContentItem contentItem;

  public SlideshowPreview(AssetContentItem contentItem) {
    this.contentItem = contentItem;
    
    initWidget(uiBinder.createAndBindUi(this));

    DecoratedBoundedImagePanel imagePanel = new DecoratedBoundedImagePanel(
        contentItem.getPreviewUrl(), Constants.MAX_IMAGE_PREVIEW_WIDTH, Integer.MAX_VALUE,
        ImagePreview.ZOOM_ICON, ImagePreview.ZOOM_WIDTH, ImagePreview.ZOOM_HEIGHT,
        IconPlacement.LOWER_RIGHT);
    imagePanel.addStyleName(Resources.INSTANCE.css().clickable());
    previewImage.add(imagePanel);
  }
  
  public SlideshowPreview hideHeader() {
    header.setVisible(false);
    return this;
  }
  
  @UiHandler("previewImage")
  public void handleClick(ClickEvent e) {
    List<AssetContentItem> allImages = contentItem.getRelatedAssets();
    new Slideshow(allImages).show(allImages.indexOf(contentItem));
  }
}
