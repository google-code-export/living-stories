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

package com.google.livingstories.client.lsp;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.livingstories.client.AssetContentItem;
import com.google.livingstories.client.AssetType;
import com.google.livingstories.client.BaseContentItem;
import com.google.livingstories.client.ContentItemType;
import com.google.livingstories.client.DisplayContentItemBundle;
import com.google.livingstories.client.contentitemlist.ContentItemList;
import com.google.livingstories.client.util.GlobalUtil;
import com.google.livingstories.client.util.LivingStoryControls;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Widget for the main area of the LSP that contains the list of content. By default, a list of
 * events and narrative content items is shown. It can be filtered in various different ways, such
 * as seeing only content of a particular type, to see only important content, etc.; the callers
 * generally handle this filtering by making asynchronous calls and then replacing the
 * contents of this widget with new contents. A simple chronological reversal of the
 * elements to show, however, can be accomplished by calling doSimpleReversal().
 */
public class LspContentItemListWidget extends Composite {
  private VerticalPanel panel;
  private ContentItemList contentItemList;

  private Date nextDateInSequence;
  
  private Map<Long, BaseContentItem> idToContentItemMap;
  private Label moreLink;
  private Image loadingImage;
  private Label problemLabel;
  
  private static String VIEW_MORE = LspMessageHolder.consts.viewMore();
  private static String PROBLEM_TEXT = LspMessageHolder.consts.viewMoreProblem();
  
  public LspContentItemListWidget() {
    super();
    
    panel = new VerticalPanel();
    panel.addStyleName("contentItemList");
    
    contentItemList = ContentItemList.create();

    moreLink = new Label(VIEW_MORE);
    moreLink.setStylePrimaryName("primaryLink");
    moreLink.addStyleName("biggerFont");
    moreLink.setVisible(false);
    DOM.setStyleAttribute(moreLink.getElement(), "padding", "5px");
    addMoreLinkHandler(moreLink);

    loadingImage = new Image("/images/loading.gif");
    loadingImage.setVisible(false);
    
    problemLabel = new Label(PROBLEM_TEXT);
    problemLabel.addStyleName("error");
    problemLabel.setVisible(false);
    
    panel.add(contentItemList);
    panel.add(moreLink);
    panel.add(loadingImage);
    panel.add(problemLabel);

    clear();
    
    initWidget(panel);
  }
  
  protected void addMoreLinkHandler(HasClickHandlers moreLink) {
    moreLink.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        LivingStoryControls.getMoreContentItems();
      }
    });
  }
  
  public void clear() {
    contentItemList.clear();
    this.idToContentItemMap = new HashMap<Long, BaseContentItem>();
  }
    
  public void beginLoading() {
    moreLink.setVisible(false);
    loadingImage.setVisible(true);
    problemLabel.setVisible(false);
  }
  
  public void showError() {
    moreLink.setVisible(false);
    loadingImage.setVisible(false);
    problemLabel.setVisible(true);
  }
  
  public void finishLoading(DisplayContentItemBundle bundle) {
    // Create a map of all ids to the corresponding content items
    List<BaseContentItem> coreContentItems = bundle.getCoreContentItems();
    for (BaseContentItem contentItem : coreContentItems) {
      idToContentItemMap.put(contentItem.getId(), contentItem);
    }
    for (BaseContentItem contentItem : bundle.getLinkedContentItems()) {
      idToContentItemMap.put(contentItem.getId(), contentItem);
    }

    // Get all the images in the core content items list.  If it has a full view,
    // Then add it to the 'slideshowImages' list and set that list as the related
    // assets list on each of its members.  This allows us to pop up to slideshow
    // view for images in the content items stream.
    List<AssetContentItem> slideshowImages = new ArrayList<AssetContentItem>();
    for (BaseContentItem coreContentItem : coreContentItems) {
      if (coreContentItem.getContentItemType() == ContentItemType.ASSET) {
        AssetContentItem asset = (AssetContentItem) coreContentItem;
        if (asset.getAssetType() == AssetType.IMAGE
            && !GlobalUtil.isContentEmpty(asset.getContent())) {
          slideshowImages.add(asset);
        }
      }
    }
    for (AssetContentItem image : slideshowImages) {
      image.setRelatedAssets(slideshowImages);
    }
    
    contentItemList.adjustTimeOrdering(bundle.getAdjustedFilterSpec().oldestFirst);

    contentItemList.appendContentItems(coreContentItems, idToContentItemMap);
    
    nextDateInSequence = bundle.getNextDateInSequence();
    
    moreLink.setVisible(bundle.getNextDateInSequence() != null);
    loadingImage.setVisible(false);
    problemLabel.setVisible(false);
  }
  
  public void doSimpleReversal(boolean oldestFirst) {
    contentItemList.adjustTimeOrdering(oldestFirst);
  }
  
  /**
   * "Jumps to" the item indicated by contentItemId, scrolling it into view and opening it.
   * @return true if the event was found, false otherwise.
   */
  public boolean goToContentItem(long contentItemId) {
    Set<Long> contentItemIds = new HashSet<Long>();
    contentItemIds.add(contentItemId);
    return contentItemList.openElements(contentItemIds);
  }
  
  public Date getNextDateInSequence() {
    return nextDateInSequence == null ? null : new Date(nextDateInSequence.getTime());
  }
  
  public boolean hasMore() {
    return moreLink.isVisible();
  }
}
