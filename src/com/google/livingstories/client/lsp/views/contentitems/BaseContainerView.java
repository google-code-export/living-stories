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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.livingstories.client.AssetContentItem;
import com.google.livingstories.client.AssetType;
import com.google.livingstories.client.ContentItemType;
import com.google.livingstories.client.BaseContentItem;
import com.google.livingstories.client.Importance;
import com.google.livingstories.client.NarrativeContentItem;
import com.google.livingstories.client.lsp.LspMessageHolder;
import com.google.livingstories.client.lsp.event.EventBus;
import com.google.livingstories.client.lsp.event.NarrativeLinkClickedEvent;
import com.google.livingstories.client.lsp.views.Resources;
import com.google.livingstories.client.util.ContentItemComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A 'container' view is a view that renders other content item views within it.
 * Event stream views and narrative stream views are examples of container
 * views.  This class has common code used in containers.
 */
public abstract class BaseContainerView<T extends BaseContentItem> extends Composite {
  protected T contentItem;
  protected Map<ContentItemType, List<BaseContentItem>> linkedContentItemsByType;
  protected Map<AssetType, List<AssetContentItem>> linkedAssetsByType;
  
  private boolean hasImportantAssets = false;
  
  public BaseContainerView(T contentItem,
      Map<ContentItemType, List<BaseContentItem>> linkedContentItemsByType) {
    this.contentItem = contentItem;
    this.linkedContentItemsByType = linkedContentItemsByType;

    // Sort content items within their categories by their importance followed by their timestamp
    for (ContentItemType type : linkedContentItemsByType.keySet()) {
      Collections.sort(linkedContentItemsByType.get(type), new ContentItemComparator());
    }

    List<BaseContentItem> linkedAssets = linkedContentItemsByType.get(ContentItemType.ASSET);
    if (!linkedAssets.isEmpty()) {
      // Determine if this content item has important assets.
      // Since the asset list is sorted by importance, we can just check if the first
      // asset is important.
      hasImportantAssets = linkedAssets.get(0).getImportance() == Importance.HIGH;
    }
    
    // Create a map of linked asset content items to their type.  The lists of assets
    // in this map will also naturally be sorted by importance and timestamp,
    // since the source list is already sorted.
    linkedAssetsByType = new HashMap<AssetType, List<AssetContentItem>>();
    for (AssetType type : AssetType.values()) {
      linkedAssetsByType.put(type, new ArrayList<AssetContentItem>());
    }
    for (BaseContentItem linkedAsset : linkedAssets) {
      AssetContentItem asset = (AssetContentItem) linkedAsset;
      linkedAssetsByType.get(asset.getAssetType()).add(asset);
    }
    
    bind();
  }

  protected abstract void bind();

  protected T getContentItem() {
    return contentItem;
  }
  
  protected Widget createNarrativeLinks() {
    List<BaseContentItem> linkedNarratives = linkedContentItemsByType.get(ContentItemType.NARRATIVE);
    if (linkedNarratives == null || linkedNarratives.isEmpty()) {
      return null;
    } else {
      FlowPanel panel = new FlowPanel();
      panel.setWidth("100%");
      DOM.setStyleAttribute(panel.getElement(), "margin", "10px 0");
      Label relatedLabel = new Label(LspMessageHolder.consts.related());
      relatedLabel.setStylePrimaryName("contentItemHeader");
      panel.add(relatedLabel);
      
      for (final BaseContentItem linkedNarrative : linkedNarratives) {
        NarrativeContentItem narrative = (NarrativeContentItem)linkedNarrative;
        
        FlowPanel row = new FlowPanel();
        InlineLabel narrativeHeadline = new InlineLabel(narrative.getHeadline());
        narrativeHeadline.addStyleName(Resources.INSTANCE.css().clickable());
        InlineHTML narrativeType = new InlineHTML("&nbsp;-&nbsp;" 
            + narrative.getNarrativeType().toString());
        narrativeType.addStyleName("greyFont");
        row.add(narrativeHeadline);
        row.add(narrativeType);
        
        FocusPanel clickableRow = new FocusPanel();
        clickableRow.add(row);
        clickableRow.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent e) {
            EventBus.INSTANCE.fireEvent(
                new NarrativeLinkClickedEvent(contentItem.getId(), linkedNarrative.getId()));
          }
        });
        panel.add(clickableRow);
      }
      return panel;
    }
  }
  
  public boolean hasImportantAssets() {
    return hasImportantAssets;
  }
}
