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

package com.google.livingstories.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.livingstories.client.ContentItemType;
import com.google.livingstories.client.BaseContentItem;
import com.google.livingstories.client.ContentRpcService;
import com.google.livingstories.client.ContentRpcServiceAsync;
import com.google.livingstories.client.util.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Widget that loads a list of content items based on an living story id, and has a built-in
 * filtering mechanism.
 * The underlying storage for the content items is a LinkedHashMap. To get incrementally-added items
 * to appear at the top of the list, rather than at the bottom, the order in which items are
 * stored in the LinkedHashMap is actually opposite to the display order.
 */
public class ContentItemListBox extends Composite {
  /**
   * Create a remote service proxy to talk to the server-side content persisting service.
   */
  private final ContentRpcServiceAsync contentService = GWT.create(ContentRpcService.class);

  private ItemList<BaseContentItem> itemList;
  protected EnumDropdown<ContentItemType> filter;
  private Map<Long, BaseContentItem> loadedContentItemsMap =
      new LinkedHashMap<Long, BaseContentItem>();

  public ContentItemListBox(final boolean multiSelect) {
    filter = EnumDropdown.newInstance(ContentItemType.class, "All");
    filter.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        refresh();
      }
    });
    
    HorizontalPanel filterPanel = new HorizontalPanel();
    filterPanel.add(new Label("Filter:"));
    filterPanel.add(filter);
    
    itemList = new ItemList<BaseContentItem>(multiSelect) {
      @Override
      public void loadItems() {
        if (!loadedContentItemsMap.isEmpty()) {
          // loads the items in reverse order from how they're stored.
          List<BaseContentItem> contentItems =
              new ArrayList<BaseContentItem>(loadedContentItemsMap.values());
          Collections.reverse(contentItems);

          for (BaseContentItem contentItem : contentItems) {
            if (testContentItem(contentItem)) {
              String content = contentItem.getDisplayString();
              if (content.length() > Constants.CONTENT_SNIPPET_LENGTH) {
                content = content.substring(0, Constants.CONTENT_SNIPPET_LENGTH).concat("...");
              }
              addItem(content, String.valueOf(contentItem.getId()));
            }
          }
        }
      }
    };

    VerticalPanel contentPanel = new VerticalPanel();
    contentPanel.add(filterPanel);
    contentPanel.add(itemList);
    initWidget(contentPanel);
  }
  
  /**
   * Tests whether a content item should be included in the displayed list, based on the filter
   * setting.
   */
  protected boolean testContentItem(BaseContentItem contentItem) {
    ContentItemType type = filter.getSelectedConstant();
    return type == null || contentItem.getContentItemType().equals(type) || isSelected(contentItem);
  }
  
  public void loadItemsForLivingStory(Long livingStoryId) {
    itemList.setSelectedIndex(-1);
    loadedContentItemsMap.clear();
    contentService.getContentItemsForLivingStory(livingStoryId, false,
        new AsyncCallback<List<BaseContentItem>>() {
          @Override
          public void onFailure(Throwable caught) {
            itemList.clear();
            itemList.addItem("Callback failed, please try again");
          }
          @Override
          public void onSuccess(List<BaseContentItem> result) {
            // Put result on loadedAContentItemsMap in reverse order. Can't use useful
            // Google Collections stuff for it, so:
            for (int i = result.size() - 1; i >= 0; i--) {
              BaseContentItem contentItem = result.get(i);
              loadedContentItemsMap.put(contentItem.getId(), contentItem);
            }
            refresh();
          }
        });
  }
  
  public void setVisibleItemCount(int count) {
    itemList.setVisibleItemCount(count);
  }

  public void addSelectionChangeHandler(ChangeHandler handler) {
    itemList.addChangeHandler(handler);
  }
  
  public void addFilterChangeHandler(ChangeHandler handler) {
    filter.addChangeHandler(handler);
  }
  
  public void clear() {
    itemList.clear();
  }
  
  public void refresh() {
    itemList.refresh();
  }
  
  public Long getSelectedContentItemId() {
    return itemList.hasSelection() ? Long.valueOf(itemList.getSelectedItemValue()) : null;
  }
  
  public BaseContentItem getSelectedContentItem() {
    return itemList.hasSelection() ? loadedContentItemsMap.get(getSelectedContentItemId()) : null;
  }

  public List<String> getSelectedItems() {
    return itemList.getSelectedItems();
  }
  
  public List<String> getSelectedValues() {
    return itemList.getSelectedItemValues();
  }
  
  public void setSelectedContentItemIds(Set<Long> selectedContentItemIds) {
    for (int i = 0; i < itemList.getItemCount(); i++) {
      itemList.setItemSelected(i,
          selectedContentItemIds.contains(Long.valueOf(itemList.getValue(i))));
    }
  }
  
  public List<BaseContentItem> getSelectedContentitems() {
    List<BaseContentItem> result = new ArrayList<BaseContentItem>();
    for (String contentItemId : itemList.getSelectedItemValues()) {
      result.add(loadedContentItemsMap.get(Long.valueOf(contentItemId)));
    }
    return result;
  }
  
  public Map<Long, BaseContentItem> getLoadedContentItemsMap() {
    return loadedContentItemsMap;
  }
  
  public void addOrUpdateContentItem(BaseContentItem contentItem) {
    boolean isAdd = !loadedContentItemsMap.containsKey(contentItem.getId());
    loadedContentItemsMap.put(contentItem.getId(), contentItem);
    // Change the filter if necessary so that the added/updated content item
    // is visible and selectable.
    if (filter.getSelectedConstant() != null
        && !contentItem.getContentItemType().equals(filter.getSelectedConstant())) {
      filter.selectConstant(null);
    }
    itemList.refresh();
    itemList.selectItemWithValue(String.valueOf(contentItem.getId()));
    if (isAdd) {
      itemList.fireEvent(new ChangeEvent(){});
    }
  }
  
  public void addContentItems(List<BaseContentItem> contentItems) {
    // Add to loadedContentItemsMap in reverse order to how the items were specified.
    for (int i = contentItems.size() - 1; i >= 0; i--) {
      BaseContentItem contentItem = contentItems.get(i);
      loadedContentItemsMap.put(contentItem.getId(), contentItem);
    }
    itemList.refresh();
  }
  
  public void removeContentItem(long contentItemId) {
    itemList.removeItemWithValue(String.valueOf(contentItemId));
    loadedContentItemsMap.remove(contentItemId);
  }
  
  private boolean isSelected(BaseContentItem contentItem) {
    if (itemList.isMultipleSelect()) {
      for (String value : itemList.getSelectedItemValues()) {
        if (Long.valueOf(value).equals(contentItem.getId())) {
          return true;
        }
      }
      return false;
    } else {
      return itemList.getSelectedItemValue() != null
          && Long.valueOf(itemList.getSelectedItemValue()).equals(contentItem.getId());
    }
  }
}
