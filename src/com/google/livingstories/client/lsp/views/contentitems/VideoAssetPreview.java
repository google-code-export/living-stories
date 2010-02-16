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
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.livingstories.client.AssetContentItem;
import com.google.livingstories.client.util.DecoratedBoundedImagePanel;
import com.google.livingstories.client.util.DecoratedBoundedImagePanel.IconPlacement;

/**
 * Extends ImageAssetPreview for video assets.  Uses a different icon to decorate the
 * preview image.
 */
public class VideoAssetPreview extends ImageAssetPreview {
  private static VideoAssetPreviewUiBinder uiBinder =
      GWT.create(VideoAssetPreviewUiBinder.class);
  interface VideoAssetPreviewUiBinder extends UiBinder<Widget, VideoAssetPreview> {
  }

  private static final String PLAY_ICON = "/images/play_icon.png";
  private static final int PLAY_WIDTH = 22;
  private static final int PLAY_HEIGHT = 22;
  private static final int MAX_PREVIEW_WIDTH = 200;

  @SuppressWarnings("hiding")
  @UiField Label header;
  
  public VideoAssetPreview(AssetContentItem contentItem) {
    super(contentItem);
  }

  @Override
  protected void bind() {
    initWidget(uiBinder.createAndBindUi(this));    
  }
  
  @Override
  public VideoAssetPreview hideHeader() {
    header.setVisible(false);
    return this;
  }
  
  @Override
  protected DecoratedBoundedImagePanel createPreviewImage(AssetContentItem contentItem) {
    return new DecoratedBoundedImagePanel(
        contentItem.getPreviewUrl(), MAX_PREVIEW_WIDTH, Integer.MAX_VALUE, PLAY_ICON,
        PLAY_WIDTH, PLAY_HEIGHT, IconPlacement.CENTER);
  }
}
