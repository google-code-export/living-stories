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

import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Image;

/**
 * An extension of AbsolutePanel for rendering an image and a partially-opaque filter
 * image over the image.
 */
public class DecoratedBoundedImagePanel extends AbsolutePanel {
  private Image superposedIcon;
  private int superposedWidth;
  private int superposedHeight;
  private DecoratedBoundedImage decoratedBoundedImage;
  private IconPlacement placement;
  
  private static int CORNER_OFFSET = 5;
  
  public static enum IconPlacement {
    CENTER, LOWER_RIGHT
  }
  
  /**
   * Constructor; completely sets up the DecoratedBoundedImagePanel
   * @param previewUrl preview url for the main image
   * @param maxWidth max width at which to render the main image
   * @param maxHeight max height at which to render the main image
   * @param superposedIconUrl url for the superposed icon
   * @param superposedWidth actual width of the superposed icon
   * @param superposedHeight actual height of the superposed icon
   * @param placement indicator of where should the superposed icon should be placed
   */
  public DecoratedBoundedImagePanel(String previewUrl, int maxWidth, int maxHeight,
      String superposedIconUrl, int superposedWidth, int superposedHeight,
      IconPlacement placement) {
    superposedIcon = new Image(superposedIconUrl);
    superposedIcon.setStylePrimaryName("superposed");
    this.superposedWidth = superposedWidth;
    this.superposedHeight = superposedHeight;
    this.placement = placement;
    // NOTE: decoratedBoundedImage depends on all other class variables being properly
    // set first; make sure the following initialization is kept at the end of the constructor.
    decoratedBoundedImage = new DecoratedBoundedImage(previewUrl, maxWidth, maxHeight);
  }
  
  /**
   * Constructor that doesn't take separate maxWidth and maxHeight parameters, using
   * a single maxSize instead.
   */
  public DecoratedBoundedImagePanel(String previewUrl, int maxSize,
      String superposedIconUrl, int superposedWidth, int superposedHeight,
      IconPlacement placement) {
    this(previewUrl, maxSize, maxSize, superposedIconUrl, superposedWidth, superposedHeight,
        placement);
  }

  public BoundedImage getBoundedImage() {
    return decoratedBoundedImage;
  }
  
  private class DecoratedBoundedImage extends BoundedImage {
    DecoratedBoundedImage(String previewUrl, int maxWidth, int maxHeight) {
      super(previewUrl, maxWidth, maxHeight);
    }
    
    @Override
    public void onImageLoad(boolean failed) {
      if (!failed) {
        DecoratedBoundedImagePanel.this.add(this);
        if (!superposedIcon.getUrl().isEmpty()) {
          int insertionLeft;
          int insertionTop;
          // The results of getDisplayWidth() and getDisplayHeight() should all be
          // available here, though on the hosted browser they may return funny results.
          switch (placement) {
            case CENTER:
              insertionLeft = getDisplayWidth() / 2 - superposedWidth / 2;
              insertionTop = getDisplayHeight() / 2 - superposedHeight / 2;
              break;
            default:   // LOWER_RIGHT; this way prevents warnings about variable initialization
              insertionLeft = getDisplayWidth() - superposedWidth - CORNER_OFFSET;
              insertionTop = getDisplayHeight() - superposedHeight - CORNER_OFFSET;
              break;
          }
          DecoratedBoundedImagePanel.this.add(superposedIcon, insertionLeft, insertionTop);
        }
      }
    }
  }
}
