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

package com.google.livingstories.client.lsp.views.atoms;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.livingstories.client.AssetAtom;
import com.google.livingstories.client.AssetType;
import com.google.livingstories.client.AtomType;
import com.google.livingstories.client.BaseAtom;
import com.google.livingstories.client.Importance;
import com.google.livingstories.client.atomlist.SummarySnippetWidget;
import com.google.livingstories.client.util.GlobalUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Base code for a collapsed container view.
 */
public class ShortContainerView<T extends BaseAtom> extends BaseContainerView<T> {
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

  public ShortContainerView(T atom, Map<AtomType, List<BaseAtom>> linkedAtomsByType) {
    super(atom, linkedAtomsByType);

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
    return SummarySnippetWidget.create(atom);
  }
  
  private void createImportantImages() {
    List<AssetAtom> linkedImages = linkedAssetsByType.get(AssetType.IMAGE);

    // Since the images are sorted by importance, if the first one isn't important,
    // then we can skip this method entirely.
    if (linkedImages.isEmpty() || linkedImages.get(0).getImportance() != Importance.HIGH) {
      return;
    }
    
    // Otherwise, separate the images into important thumbnail-only images, and slideshow images.
    List<AssetAtom> slideshowImages = new ArrayList<AssetAtom>();
    List<AssetAtom> thumbnailOnlyImages = new ArrayList<AssetAtom>();
    
    for (AssetAtom image : linkedImages) {
      if (!GlobalUtil.isContentEmpty(image.getContent())) {
        slideshowImages.add(image);
      } else if (image.getImportance() == Importance.HIGH) {
        thumbnailOnlyImages.add(image);
      }
    }
    
    // Again, if the first image in the slideshow isn't important,
    // then the whole thing is unimportant.
    if (!slideshowImages.isEmpty() && slideshowImages.get(0).getImportance() == Importance.HIGH) {
      AssetAtom previewImage = slideshowImages.get(0);
      previewImage.setRelatedAssets(slideshowImages);
      Widget previewPanel = LinkedViewFactory.createView(previewImage, atom.getContributorIds());
      importantImages.add(previewPanel);
    }

    // Add all the thumbnail-only images; we've already checked their importance in the loop above.
    for (AssetAtom image : thumbnailOnlyImages) {
      Widget previewPanel = LinkedViewFactory.createView(image, atom.getContributorIds());
      importantImages.add(previewPanel);
    }
  }
  
  private void createImportantAssets() {
    for (Entry<AssetType, List<AssetAtom>> linkedAssets : linkedAssetsByType.entrySet()) {
      // Render everything except images, which we've already done elsewhere.
      if (linkedAssets.getKey() != AssetType.IMAGE) {
        for (AssetAtom assetAtom : linkedAssets.getValue()) {
          if (assetAtom.getImportance() == Importance.HIGH) {
            importantAssets.add(LinkedViewFactory.createView(assetAtom, atom.getContributorIds()));
          }
        }
      }
    }
  }
}
