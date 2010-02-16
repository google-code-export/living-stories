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
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.livingstories.client.AssetContentItem;
import com.google.livingstories.client.AssetType;
import com.google.livingstories.client.ContentItemType;
import com.google.livingstories.client.BaseContentItem;
import com.google.livingstories.client.Importance;
import com.google.livingstories.client.contentitemlist.SummarySnippetWidget;
import com.google.livingstories.client.util.GlobalUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Base code for a collapsed container view.
 */
public class ShortContainerView<T extends BaseContentItem> extends BaseContainerView<T> {
  private static ShortContainerViewUiBinder uiBinder =
      GWT.create(ShortContainerViewUiBinder.class);
  @SuppressWarnings("unchecked")
  interface ShortContainerViewUiBinder extends
      UiBinder<Widget, ShortContainerView> {
    // This interface should theoretically use a genericized version of ShortContainerView,
    // but there's a bug in GWT that prevents that from working.  Instead, we use the raw
    // type here.  This works in most situations, though there are certain things
    // you won't be able to do (e.g. @UiHandler won't be able to bind to a method that
    // takes a parameterized type.)
    // TODO: fix this when the next version of GWT comes out and the bug is fixed.
  }

  @UiField FlowPanel summary;
  @UiField FlowPanel narrativeLinks;
  @UiField FlowPanel importantImages;
  @UiField FlowPanel importantAssets;

  public ShortContainerView(T contentItem,
      Map<ContentItemType, List<BaseContentItem>> linkedContentItemsByType) {
    super(contentItem, linkedContentItemsByType);

    GlobalUtil.addIfNotNull(summary, createSummary());
    GlobalUtil.addIfNotNull(narrativeLinks, createNarrativeLinks());
    createImportantImages();
    createImportantAssets();
  }

  @Override
  public void bind() {
    initWidget(uiBinder.createAndBindUi(this));
  }
  
  private Widget createSummary() {
    return SummarySnippetWidget.create(contentItem);
  }
  
  private void createImportantImages() {
    List<AssetContentItem> linkedImages = linkedAssetsByType.get(AssetType.IMAGE);

    // Since the images are sorted by importance, if the first one isn't important,
    // then we can skip this method entirely.
    if (linkedImages.isEmpty() || linkedImages.get(0).getImportance() != Importance.HIGH) {
      return;
    }
    
    // Otherwise, separate the images into important thumbnail-only images, and slideshow images.
    List<AssetContentItem> slideshowImages = new ArrayList<AssetContentItem>();
    List<AssetContentItem> thumbnailOnlyImages = new ArrayList<AssetContentItem>();
    
    for (AssetContentItem image : linkedImages) {
      if (!GlobalUtil.isContentEmpty(image.getContent())) {
        slideshowImages.add(image);
      } else if (image.getImportance() == Importance.HIGH) {
        thumbnailOnlyImages.add(image);
      }
    }
    
    // Again, if the first image in the slideshow isn't important,
    // then the whole thing is unimportant.
    if (!slideshowImages.isEmpty() && slideshowImages.get(0).getImportance() == Importance.HIGH) {
      AssetContentItem previewImage = slideshowImages.get(0);
      previewImage.setRelatedAssets(slideshowImages);
      Widget previewPanel = LinkedViewFactory.createView(previewImage, contentItem.getContributorIds());
      importantImages.add(previewPanel);
    }

    // Add all the thumbnail-only images; we've already checked their importance in the loop above.
    for (AssetContentItem image : thumbnailOnlyImages) {
      Widget previewPanel = LinkedViewFactory.createView(image, contentItem.getContributorIds());
      importantImages.add(previewPanel);
    }
  }
  
  private void createImportantAssets() {
    for (Entry<AssetType, List<AssetContentItem>> linkedAssets : linkedAssetsByType.entrySet()) {
      // Render everything except images, which we've already done elsewhere.
      if (linkedAssets.getKey() != AssetType.IMAGE) {
        for (AssetContentItem assetContentItem : linkedAssets.getValue()) {
          if (assetContentItem.getImportance() == Importance.HIGH) {
            importantAssets.add(LinkedViewFactory.createView(assetContentItem, contentItem.getContributorIds()));
          }
        }
      }
    }
  }
}
