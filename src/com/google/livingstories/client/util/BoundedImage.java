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

package com.google.livingstories.client.util;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Image;

import com.reveregroup.gwt.imagepreloader.Dimensions;
import com.reveregroup.gwt.imagepreloader.ImageLoadEvent;
import com.reveregroup.gwt.imagepreloader.ImageLoadHandler;
import com.reveregroup.gwt.imagepreloader.ImagePreloader;

/**
 * Minimal reimplementation of FitImage to fix some bugs.
 */
public class BoundedImage extends Image {
  private int maxWidth;
  private int maxHeight;
  
  private int width;
  private int height;
  
  private int displayWidth = -1;
  private int displayHeight = -1;
  
  public BoundedImage(String url, int maxWidth, int maxHeight) {
    super(url);
    this.maxWidth = maxWidth;
    this.maxHeight = maxHeight;

    ImagePreloader.load(url, new ImageLoadHandler() {
      @Override
      public void imageLoaded(ImageLoadEvent event) {
        if (!event.isLoadFailed()) {
          Dimensions dimensions = event.getDimensions();
          width = dimensions.getWidth();
          height = dimensions.getHeight();
          resize();
        }
        onImageLoad(event.isLoadFailed());
      }
    });
  }

  public BoundedImage(String url, int maxSize) {
    this(url, maxSize, maxSize);
  }

  public void onImageLoad(boolean failed) {}

  public int getDisplayWidth() {
    if (displayWidth == -1) {
      if (GWT.isScript()) {
        throw new RuntimeException("No display width has yet been computed.");
      }
      // In hosted mode, return some kind of default.
      return getWidth();
    }
    return displayWidth;
  }

  public int getDisplayHeight() {
    if (displayHeight == -1) {
      if (GWT.isScript()) {
        throw new RuntimeException("No display height has yet been computed.");
      }
      // In hosted mode, return some kind of default.
      return getHeight();
    }
    return displayHeight;
  }
  
  private void resize() {
    if (width < maxWidth && height < maxHeight) {
      displayWidth = width;
      displayHeight = height;
      return;
    }
    double aspectRatio = ((double) width) / height;
    double boundedAspectRatio = ((double) maxWidth) / maxHeight;
    if (aspectRatio > boundedAspectRatio) {
      displayWidth = maxWidth;
      displayHeight = (int) (maxWidth / aspectRatio);
    } else {
      displayWidth = (int) (maxHeight * aspectRatio);
      displayHeight = maxHeight;
    }
    setWidth(displayWidth + "px");
    setHeight(displayHeight + "px");
  }
}
