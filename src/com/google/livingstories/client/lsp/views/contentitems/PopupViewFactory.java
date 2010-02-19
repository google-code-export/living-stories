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

import com.google.gwt.user.client.ui.Widget;
import com.google.livingstories.client.AssetContentItem;
import com.google.livingstories.client.BaseContentItem;
import com.google.livingstories.client.PlayerContentItem;

/**
 * Factory for generating widgets for popups based on contentItem type.
 */
public class PopupViewFactory {
  public static Widget createView(BaseContentItem contentItem) {
    switch (contentItem.getContentItemType()) {
      case PLAYER:
        return new PlayerPopupView((PlayerContentItem) contentItem);
      case BACKGROUND:
        return new ContentItemPreviewWithHeader(contentItem);
      case ASSET:
        AssetContentItem asset = (AssetContentItem) contentItem;
        switch (asset.getAssetType()) {
          case VIDEO:
          case INTERACTIVE:
            return new BaseAssetPopupView(asset);
          case DOCUMENT:
            return new DocumentPopupView(asset);
          case IMAGE:
            return new ImagePopupView(asset);
          default:
            throw new IllegalArgumentException("Asset type " + asset.getAssetType()
                + " does not have a popup view defined.");
        }
      default:
        throw new IllegalArgumentException("Content item type " + contentItem.getContentItemType()
            + " does not have a popup view defined.");
    }
  }
}
