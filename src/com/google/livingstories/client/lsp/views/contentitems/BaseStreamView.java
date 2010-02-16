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
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.livingstories.client.AssetContentItem;
import com.google.livingstories.client.BaseContentItem;
import com.google.livingstories.client.Importance;
import com.google.livingstories.client.PlayerContentItem;
import com.google.livingstories.client.QuoteContentItem;
import com.google.livingstories.client.contentitemlist.ContentItemListElement;
import com.google.livingstories.client.lsp.views.DateTimeRangeWidget;

import java.util.Set;

/**
 * Class that renders all element types (other than event and narrative) in the stream view.
 * Mostly reuses existing classes that render previews of each content type. 
 */
public class BaseStreamView extends ContentItemListElement {

  private static BaseStreamViewUiBinder uiBinder = GWT.create(BaseStreamViewUiBinder.class);

  interface BaseStreamViewUiBinder extends UiBinder<Widget, BaseStreamView> {
  }

  @UiField DateTimeRangeWidget timestamp;
  @UiField SimplePanel content;

  private BaseContentItem contentItem;
  
  public BaseStreamView(BaseContentItem contentItem) {
    this.contentItem = contentItem;
    
    initWidget(uiBinder.createAndBindUi(this));

    timestamp.setDateTime(contentItem.getTimestamp(), null);

    switch (contentItem.getContentItemType()) {
      case BACKGROUND:
      case DATA:
      case REACTION:
        content.setWidget(new BaseContentItemPreview(contentItem));
        break;
      case QUOTE:
        content.setWidget(new QuoteContentItemView((QuoteContentItem) contentItem).hideHeader());
        break;
      case PLAYER:
        content.setWidget(new BasePlayerPreview((PlayerContentItem) contentItem).hideHeader());
        break;
      case ASSET:
        AssetContentItem asset = (AssetContentItem) contentItem;
        switch (asset.getAssetType()) {
          case LINK:
            content.setWidget(new LinkAssetPreview(asset).hideHeader());
            break;
          case DOCUMENT:
            content.setWidget(new DocumentAssetPreview(asset).hideHeader());
            break;
          case AUDIO:
            content.setWidget(new AudioAssetView(asset).hideHeader());
            break;
          case INTERACTIVE:
            content.setWidget(new GraphicAssetPreview(asset).hideHeader());
            break;
          case VIDEO:
            content.setWidget(new VideoAssetPreview(asset).hideHeader());
            break;
          case IMAGE:
            content.setWidget(new ImageAssetPreview(asset).hideHeader());
            break;
          default:
            throw new IllegalArgumentException("Asset type " + asset.getAssetType()
                + " does not have a stream view defined.");
        }
        break;
      default:
        throw new IllegalArgumentException("Content item type " + contentItem.getContentItemType()
            + " does not have a stream view defined.");        
    }
  }

  @Override
  public Long getId() {
    return contentItem.getId();
  }
  
  @Override
  public BaseContentItem getContentItem() {
    return contentItem;
  }
  
  @Override
  public Set<Long> getThemeIds() {
    return contentItem.getThemeIds();
  }

  @Override
  public Importance getImportance() {
    return contentItem.getImportance();
  }

  @Override
  public String getDateString() {
    return timestamp.getDateString();
  }
  
  @Override
  public void setTimeVisible(boolean visible) {
    timestamp.setTimeVisible(visible);
  }
  
  @Override
  public boolean setExpansion(boolean expand) {
    // Do nothing
    return false;
  }
}
