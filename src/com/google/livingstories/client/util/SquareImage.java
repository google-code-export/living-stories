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

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Image;

import com.reveregroup.gwt.imagepreloader.Dimensions;
import com.reveregroup.gwt.imagepreloader.ImageLoadEvent;
import com.reveregroup.gwt.imagepreloader.ImageLoadHandler;
import com.reveregroup.gwt.imagepreloader.ImagePreloader;

/**
 * Image that is forced to be a certain square size, cropping the longer edge and
 * resizing the shorter edge to fit.
 */
public class SquareImage extends Composite {
  private int size;
  private int originalWidth;
  private int originalHeight;
  
  private FocusPanel container;
  private Image image;
  
  public SquareImage(String url, int size) {
    this.size = size;

    container = new FocusPanel();
    container.setStylePrimaryName("squareImage");
    container.setPixelSize(size, size);
    
    image = new Image(url);

    ImagePreloader.load(url, new ImageLoadHandler() {
      @Override
      public void imageLoaded(ImageLoadEvent event) {
        if (!event.isLoadFailed()) {
          Dimensions dimensions = event.getDimensions();
          originalWidth = dimensions.getWidth();
          originalHeight = dimensions.getHeight();
          addImage();
        }
        onImageLoad(event.isLoadFailed());
      }
    });
    
    initWidget(container);
  }

  public void onImageLoad(boolean failed) {}

  public void addClickHandler(ClickHandler handler) {
    container.addClickHandler(handler);
  }
  
  private void addImage() {
    double aspectRatio = (double) originalWidth / originalHeight;
    if (aspectRatio < 1) {
      // Vertical image
      int newHeight = (int) (size / aspectRatio);
      image.setPixelSize(size, newHeight);
      int clipStart = (newHeight - size) / 2;
      DOM.setIntStyleAttribute(image.getElement(), "top", -clipStart);
    } else {
      int newWidth = (int) (size * aspectRatio);
      image.setPixelSize(newWidth, size);
      int clipStart = (newWidth - size) / 2;
      DOM.setIntStyleAttribute(image.getElement(), "left", -clipStart);
    }
    container.setWidget(image);
  }
}
