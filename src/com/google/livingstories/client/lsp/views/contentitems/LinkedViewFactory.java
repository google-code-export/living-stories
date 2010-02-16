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
import com.google.livingstories.client.NarrativeContentItem;
import com.google.livingstories.client.PlayerContentItem;
import com.google.livingstories.client.QuoteContentItem;
import com.google.livingstories.client.lsp.views.Resources;

import java.util.Set;

/**
 * Factory for generating widgets for the event block view based on contentItem type.
 */
public class LinkedViewFactory {
  public static Widget createView(BaseContentItem contentItem, Set<Long> containerContributorIds) {
    Widget widget = null;
    switch (contentItem.getContentItemType()) {
      case BACKGROUND:
      case DATA:
      case REACTION:
        widget = new ContentItemPreviewWithHeader(contentItem);
        break;
      case NARRATIVE:
        widget = new NarrativeContentItemView((NarrativeContentItem) contentItem,
            containerContributorIds);
        break;
      case QUOTE:
        widget = new QuoteContentItemView((QuoteContentItem) contentItem, containerContributorIds);
        break;
      case PLAYER:
        widget = new BasePlayerPreview((PlayerContentItem) contentItem);
        break;
      case ASSET:
        AssetContentItem asset = (AssetContentItem) contentItem;
        switch (asset.getAssetType()) {
          case LINK:
            widget = new LinkAssetPreview(asset);
            break;
          case DOCUMENT:
            widget = new DocumentAssetPreview(asset);
            break;
          case AUDIO:
            widget = new AudioAssetView(asset);
            break;
          case INTERACTIVE:
            widget = new GraphicAssetPreview(asset);
            break;
          case VIDEO:
            widget = new VideoAssetPreview(asset);
            break;
          case IMAGE:
            // getRelatedAssets() will return a list that includes the current asset.
            // So we only make a slideshow if there's more than 1 asset in the list.
            if (asset.getRelatedAssets() != null && asset.getRelatedAssets().size() > 1) {
              widget = new SlideshowPreview(asset);
            } else {
              widget = new ImageAssetPreview(asset);
            }
            break;
          default:
            throw new IllegalArgumentException("Asset type " + asset.getAssetType()
                + " does not have a linked view defined.");
        }
        break;
      default:
        throw new IllegalArgumentException("Content item type " + contentItem.getContentItemType()
            + " does not have a linked view defined.");
    }
    widget.addStyleName(Resources.INSTANCE.css().linkedItemSpacing());
    return widget;
  }
}
