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

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.TableElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.livingstories.client.lsp.BylineWidget;
import com.google.livingstories.client.lsp.ContentRenderer;
import com.google.livingstories.client.ui.ImagePreview;
import com.google.livingstories.client.util.BoundedImage;
import com.google.livingstories.client.util.DecoratedBoundedImagePanel;
import com.google.livingstories.client.util.GlobalUtil;
import com.google.livingstories.client.util.LivingStoryControls;
import com.google.livingstories.client.util.DecoratedBoundedImagePanel.IconPlacement;

import com.reveregroup.gwt.imagepreloader.Dimensions;
import com.reveregroup.gwt.imagepreloader.ImageLoadEvent;
import com.reveregroup.gwt.imagepreloader.ImageLoadHandler;
import com.reveregroup.gwt.imagepreloader.ImagePreloader;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Client-side version of an Asset content entity
 */
public class AssetContentItem extends BaseContentItem {
  private static final String AUDIO_ICON = "/images/audio_icon.gif";
  private static final String DOCUMENT_ICON = "/images/document_icon.gif";
  private static final String LINK_ICON = "/images/link_icon.gif";
  private static final String PLAY_ICON = "/images/play_icon.png";
  private static final int PLAY_WIDTH = 22;
  private static final int PLAY_HEIGHT = 22;
  private static final int MAX_PREVIEW_WIDTH = 200;
  private static final int MAX_TINY_WIDTH = 100;
  private static final int MAX_TINY_HEIGHT = 100;
  
  private AssetType assetType;
  private String caption;
  private String previewUrl;
  
  // Currently only used for image assets.  Allows other images to be set as 'related'
  // so that they can all be shown together in a slideshow when the current image is shown.
  private List<AssetContentItem> relatedAssets;
  
  public AssetContentItem() {}
  
  public AssetContentItem(Long id, Date timestamp, Set<Long> contributorIds, String content,
      Importance importance, Long livingStoryId, AssetType assetType, String caption,
      String previewUrl) {
    super(id, timestamp, ContentItemType.ASSET, contributorIds, content, importance, livingStoryId);
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
  
  public void setRelatedAssets(List<AssetContentItem> assets) {
    relatedAssets = assets;
  }
  
  public List<AssetContentItem> getRelatedAssets() {
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
  public Widget renderPreview() {
    DockPanel content = new DockPanel();
    if (assetType == AssetType.LINK) {
      Image linkIcon = new Image(LINK_ICON);
      linkIcon.addStyleName("thumbnail");
      content.add(linkIcon, DockPanel.WEST);
      content.add(new InlineHTML(getContent()), DockPanel.EAST);
      return content;
    } else {
      boolean hasFullView = !GlobalUtil.isContentEmpty(getContent());
      Widget image = null;
      if (assetType == AssetType.DOCUMENT) {
        image = new Image(DOCUMENT_ICON);
        content.add(image, DockPanel.WEST);
      } else if (assetType == AssetType.AUDIO) {
        image = new Image(AUDIO_ICON);
        content.add(image, DockPanel.WEST);
      } else if (previewUrl != null && !previewUrl.isEmpty()) {
        if (hasFullView) {
          // setup for creating a DecoratedBoundedImagePanel:
          String superposedIconUrl = "";
          int superposedWidth = 0;
          int superposedHeight = 0;
          DecoratedBoundedImagePanel.IconPlacement placement = IconPlacement.CENTER;
          if (assetType == AssetType.IMAGE || assetType == AssetType.INTERACTIVE) {
            // TODO: Check with same if he'd like a different, third icon
            // for interactives.
            superposedIconUrl = ImagePreview.ZOOM_ICON;
            superposedWidth = ImagePreview.ZOOM_WIDTH;
            superposedHeight = ImagePreview.ZOOM_HEIGHT;
            placement = IconPlacement.LOWER_RIGHT;
            // Another note: this code path isn't exercised in the events view, but
            // it is exercised in the images view.
          } else if (assetType == AssetType.VIDEO) {
            superposedIconUrl = PLAY_ICON;
            superposedWidth = PLAY_WIDTH;
            superposedHeight = PLAY_HEIGHT;
          }
          DecoratedBoundedImagePanel imagePanel = new DecoratedBoundedImagePanel(
              previewUrl, MAX_PREVIEW_WIDTH, Integer.MAX_VALUE,
              superposedIconUrl, superposedWidth, superposedHeight, placement);
          image = imagePanel.getBoundedImage();
          content.add(imagePanel, DockPanel.NORTH);
        } else {
          image = new BoundedImage(previewUrl, MAX_PREVIEW_WIDTH);
          content.add(image, DockPanel.NORTH);
        }
        image.addStyleName(renderAsSeen ? "assetPreviewImageSeen" : "assetPreviewImage");
      }
      if (image != null) {
        image.addStyleName("thumbnail");
      }
      if (caption != null && !caption.isEmpty()) {
        HTML captionLabel = new HTML(caption);
        if (hasFullView) {
          captionLabel.addStyleName(renderAsSeen ? "secondaryLink" : "primaryLink");
        }
        content.add(captionLabel, DockPanel.CENTER);
      }

      FocusPanel panel = new FocusPanel();
      panel.add(content);
      
      if (assetType != AssetType.IMAGE) {
        // The click handler for images is handled through another mechanism
        DOM.setStyleAttribute(panel.getElement(), "cursor", "pointer");
        panel.addClickHandler(new ClickHandler() {
          public void onClick(ClickEvent e) {
            getDimensionsAsync(new DimensionHandler() {
              @Override
              public void onFailure() {
                // Do nothing
              }

              @Override
              public void onSuccess(DimensionEvent event) {
                LivingStoryControls.showLightbox(getTitleString(), AssetContentItem.this);
              }
            });
          }
        });
      }
      return panel;
    }
  }
  
  /* (non-Javadoc)
   * @see com.google.livingstories.client.BaseContentItem#renderContent()
   */
  @Override
  public Widget renderContent(Set<Long> containingContributorIds) {
    VerticalPanel panel = new VerticalPanel();
    panel.setWidth("400px");
    
    if (caption != null && !caption.isEmpty()) {
      Widget captionWidget = new ContentRenderer(caption, false);
      captionWidget.setStyleName("lightboxCaption");
      TableElement table = panel.getElement().cast();
      table.createCaption().appendChild(captionWidget.getElement());
    }
    String content = getContent();
    if (content != null && !content.isEmpty()) {
      if (assetType == AssetType.IMAGE) {
        Image image = new Image();
        panel.add(image);
        image.setUrl(content);
      } else {
        HTML contentHTML = new HTML(content);
        // So that lightbox centering in firefox works, enclose each sized <object>
        // with a div styled to exactly that size.
        NodeList<Element> objectElements = contentHTML.getElement().getElementsByTagName("object");
        Document document = Document.get();
        for (int i = 0, len = objectElements.getLength(); i < len; i++) {
          Element objectElement = objectElements.getItem(i);
          String width = objectElement.getAttribute("width");
          String height = objectElement.getAttribute("height");
          if (width.matches("[0-9]+%?") && height.matches("[0-9]+%?")) {
            DivElement div = document.createDivElement();
            div.getStyle().setProperty("width", width + (width.endsWith("%") ? "" : "px"));
            div.getStyle().setProperty("height", height + (height.endsWith("%") ? "" : "px"));
            objectElement.getParentElement().replaceChild(div, objectElement);
            div.appendChild(objectElement);
          }
        }
        panel.add(contentHTML);
      }
    } else if (previewUrl != null && !previewUrl.isEmpty()) {
      BoundedImage boundedImage =
        new BoundedImage(previewUrl, MAX_PREVIEW_WIDTH, Integer.MAX_VALUE);
      panel.add(boundedImage);
    }
    // If the asset has known contributors in the system, add a byline at the end.
    switch (assetType) {
      case IMAGE:
      case VIDEO:
      case AUDIO:
      case INTERACTIVE:
        GlobalUtil.addIfNotNull(
            panel, BylineWidget.makeContextSensitive(this, containingContributorIds));
        break;
      default:
        break;
        
    }
    
    return panel;
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
