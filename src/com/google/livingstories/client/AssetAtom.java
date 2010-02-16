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

package com.google.livingstories.client;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.livingstories.client.util.BoundedImage;

import com.reveregroup.gwt.imagepreloader.Dimensions;
import com.reveregroup.gwt.imagepreloader.ImageLoadEvent;
import com.reveregroup.gwt.imagepreloader.ImageLoadHandler;
import com.reveregroup.gwt.imagepreloader.ImagePreloader;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Client-side version of an Asset atom
 */
public class AssetAtom extends BaseAtom {
  private static final int MAX_TINY_WIDTH = 100;
  private static final int MAX_TINY_HEIGHT = 100;
  
  private AssetType assetType;
  private String caption;
  private String previewUrl;
  
  // Currently only used for image assets.  Allows other images to be set as 'related'
  // so that they can all be shown together in a slideshow when the current image is shown.
  private List<AssetAtom> relatedAssets;
  
  public AssetAtom() {}
  
  public AssetAtom(Long id, Date timestamp, Set<Long> contributorIds, String content,
      Importance importance, Long livingStoryId, AssetType assetType, String caption,
      String previewUrl) {
    super(id, timestamp, AtomType.ASSET, contributorIds, content, importance, livingStoryId);
    this.assetType = assetType;
    this.caption = caption;
    this.previewUrl = previewUrl;
  }

  public AssetType getAssetType() {
    return assetType;
  }

  public String getCaption() {
    return caption;
  }

  public String getPreviewUrl() {
    return previewUrl;
  }
  
  public void setRelatedAssets(List<AssetAtom> assets) {
    relatedAssets = assets;
  }
  
  public List<AssetAtom> getRelatedAssets() {
    return relatedAssets;
  }
  
  @Override
  public String getTypeString() {
    return assetType.toString();
  }

  @Override
  public String getTitleString() {
    return assetType.getTitleString();
  }
  
  @Override
  public String getNavLinkString() {
    return assetType.getNavLinkString();
  }

  @Override
  public Widget renderTiny() {
    if (assetType == AssetType.LINK) {
      return new Label(getContent());
    } else if (previewUrl != null && !previewUrl.isEmpty()) {
      return new BoundedImage(previewUrl, MAX_TINY_WIDTH, MAX_TINY_HEIGHT);
    } else {
      return new HTML(caption);
    }
  }
  
  @Override
  public String getBylineLeadin() {
    if (assetType == AssetType.IMAGE) {
      return "Image by";
    } else {
      // Note that getBylineLeadin shouldn't end up being called anyway for a number of the
      // asset types.
      return "By";
    }
  }
  
  @Override
  public String getDisplayString() {
    return "[" + getAssetType() + "] " + getCaption() + " : " + getContent();
  }
  
  @Override
  public void getDimensionsAsync(final DimensionHandler dimensionHandler) {
    switch (assetType) {
      case LINK:
      case VIDEO:
      case AUDIO:
      case INTERACTIVE:
        // These resource types should never be scrolled:
        super.getDimensionsAsync(dimensionHandler);
        break;
      case IMAGE:
        ImagePreloader.load(getContent(), new ImageLoadHandler() {
          public void imageLoaded(ImageLoadEvent event) {
            if (event.isLoadFailed()) {
              dimensionHandler.onFailure();
            } else {
              Dimensions d = event.getDimensions();
              dimensionHandler.onSuccess(new DimensionEvent(d.getWidth(), d.getHeight(), false));
            }
          }
        });
        break;
      case DOCUMENT:
        // A very rough heuristic: short content gets rendered in a fairly short box,
        // longer content into a longer box
        int height = getContent().length() < 600 ? 350 : 700;
        dimensionHandler.onSuccess(new DimensionEvent(500, height, true));
        break;
    }
  }
}
