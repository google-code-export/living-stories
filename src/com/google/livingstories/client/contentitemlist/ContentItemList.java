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

package com.google.livingstories.client.contentitemlist;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.livingstories.client.BaseContentItem;
import com.google.livingstories.client.lsp.LspMessageHolder;
import com.google.livingstories.client.ui.WindowScroll;
import com.google.livingstories.client.util.LivingStoryControls;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

/**
 * Widget that displays a list of {@link ContentItemListElement} objects in a vertical list. There
 * is also a method for reversing whether the items are shown in chronological or
 * reverse-chronological order. The content items know how to render their content and their
 * day and time.
 */
public class ContentItemList extends Composite {
  private Map<BaseContentItem, ContentItemListRow> currentContentItemToRowMap;
  private Set<Long> contentItemIdsInPanel;
  private Map<Long, BaseContentItem> idToContentItemMap;
  
  private Label statusLabel;
  private VerticalPanel contentPanel;
  
  private static final String NO_ITEMS_TEXT = LspMessageHolder.consts.contentItemListNoItemsText();
  
  private boolean includeName;
  private ContentItemClickHandler handler;
  private boolean noHistoryOnToggle;
  
  private boolean chronological = false;
  
  private ContentItemList(boolean includeName, ContentItemClickHandler handler,
      boolean noHistoryOnToggle) {
    super();
    this.includeName = includeName;
    this.handler = handler;
    this.noHistoryOnToggle = noHistoryOnToggle;
    contentItemIdsInPanel = new HashSet<Long>();
    currentContentItemToRowMap = new TreeMap<BaseContentItem, ContentItemListRow>(
        new Comparator<BaseContentItem>() {
          public int compare(BaseContentItem a1, BaseContentItem a2) {
            int ret = a1.getDateSortKey().compareTo(a2.getDateSortKey());
            return ret == 0 ? ((int) Math.signum(a1.getId() - a2.getId())) : ret;
          }
        });
    statusLabel = new Label("");
    statusLabel.setStylePrimaryName("greyFont");
    
    contentPanel = new VerticalPanel();
    contentPanel.setWidth("100%");
    
    VerticalPanel container = new VerticalPanel();
    container.setWidth("100%");
    container.add(statusLabel);
    container.add(contentPanel);
    
    initWidget(container);
  }
  
  public static ContentItemList create(boolean includeName) {
    return new ContentItemList(includeName, null, false);
  }
  
  public static ContentItemList createWithHandler(
      boolean includeName, ContentItemClickHandler handler) {
    return new ContentItemList(includeName, handler, false);
  }
  
  public static ContentItemList createNoHistoryOnToggle(boolean includeName) {
    return new ContentItemList(includeName, null, true);
  }
  
  /**
   * Sets the content item click handler object, separately from the construction-time handler.
   * Should only be called on an list that is presently empty
   */
  public void setContentItemClickHandler(ContentItemClickHandler handler) {
    assert currentContentItemToRowMap.isEmpty();
    this.handler = handler;
  }

  /**
   * @param contentItems content items that this list was created to display
   * @param idToContentItemMap Map from content items ids to the content items. It should include
   * entries for all of the content items in the list and all linked content items. If the map is
   * null, empty or doesn't contain the linked items, the complex content items
   * will be displayed in a degenerate form.
   */
  public void load(
      List<BaseContentItem> contentItems, Map<Long, BaseContentItem> idToContentItemMap) {
    clear();    
    appendContentItems(contentItems, idToContentItemMap);
  }
    
  /**
   * @param contentItems contentItems that should be added to this
   * @param idToContentItemMap a complete idTocontentItemMap for all content items relevant to the
   * current reporting.
   */
  public void appendContentItems(
      List<BaseContentItem> contentItems, Map<Long, BaseContentItem> idToContentItemMap) {
    this.idToContentItemMap = idToContentItemMap;
    
    for (BaseContentItem contentItem : contentItems) {
      addContentItem(contentItem);
    }

    if (currentContentItemToRowMap.isEmpty()) {
      statusLabel.setText(NO_ITEMS_TEXT);
    } else {
      statusLabel.setText("");
    }
    
    updateContentPanel();
  }

  /**
   * Updates the content panel contents based on the current contents of currentContentItemToRowMap.
   */
  private void updateContentPanel() {
    ContentItemListElement previousElement = null;
        
    int insertionPosition = 0;
    for (BaseContentItem contentItem : getContentItemList()) {
      ContentItemListRow row = currentContentItemToRowMap.get(contentItem);
      if (!contentItemIdsInPanel.contains(contentItem.getId())) {
        // the appropriate widget is not in the contentPanel. Add it!
        contentPanel.insert(row.elementWidget, insertionPosition);
        contentItemIdsInPanel.add(contentItem.getId());
      }
      
      setTimeVisibility(row.contentItemListElement, previousElement);
      previousElement = row.contentItemListElement;
      insertionPosition++;
    }
  }
  
  /**
   * Adds a new content item to currentContentItemToRowMap, if it's not already there.
   * Doesn't alter contentItemIdsInPanel.
   */
  public void addContentItem(BaseContentItem contentItem) {
    if (!currentContentItemToRowMap.containsKey(contentItem)) {
      ContentItemListElement element = ContentItemListElementFactory.createContentItemListElement(
          contentItem, idToContentItemMap, handler, noHistoryOnToggle);
      currentContentItemToRowMap.put(contentItem, new ContentItemListRow(
          element, element.render(includeName)));
    }
  }
  
  /**
   * Removes a content item from the list if it is currently present.
   */
  public void removeContentItem(Long contentItemId) {
    for (Entry<BaseContentItem, ContentItemListRow> entry : currentContentItemToRowMap.entrySet()) {
      if (entry.getKey().getId() == contentItemId) {
        contentPanel.remove(entry.getValue().elementWidget);
        currentContentItemToRowMap.remove(entry.getKey());
        contentItemIdsInPanel.remove(entry.getKey().getId());
        break;
      }
    }
  }
  
  public Set<BaseContentItem> getContentItemSet() {
    return currentContentItemToRowMap.keySet();
  }
  
  public int getContentItemCount() {
    return currentContentItemToRowMap.size();
  }
  
  public List<BaseContentItem> getContentItemList() {
    // This would be simpler if GWT emulated TreeMap.descendingKeySet().
    List<BaseContentItem> ret = new ArrayList<BaseContentItem>(currentContentItemToRowMap.keySet());
    if (!chronological) {
      Collections.reverse(ret);
    }
    return ret;
  }
  
  public void clear() {
    contentPanel.clear();
    currentContentItemToRowMap.clear();
    contentItemIdsInPanel.clear();
  }
  
  public void adjustTimeOrdering(boolean chronological) {
    if (this.chronological == chronological) {
      return;
    }
    this.chronological = chronological;
    contentPanel.clear();
    for (ContentItemListRow row : currentContentItemToRowMap.values()) {
      if (chronological) {
        contentPanel.add(row.elementWidget);
      } else {
        contentPanel.insert(row.elementWidget, 0);
      }
    }
  }
  
  public boolean getChronological() {
    return chronological;
  }
  
  /**
   * Opens content items from the given set that are present in the current view. If only a single
   * id is passed in, "jumps to it" i.e. scrolls it into view and opens its contents. All other
   * previously expanded content items are collapsed.
   * @return true if all requested content items were opened; false if one or more could not be
   * found
   */
  public boolean openElements(Set<Long> contentItemIds) {
    /* Implementation notes:
     * - To avoid unintended scrolling to elements that this routine closes, we suppress extra
     *   expansion actions.
     * - To avoid scrolling to the position of a single target element that then changes
     *   due to contractions elsewhere in the list, we save the relevant ContentItemListRow in the
     *   loop, but actually act on this knowledge only after the loop is done. */
    int countFound = 0;
    ContentItemListRow singleRowToScrollTo = null;

    for (Entry<BaseContentItem, ContentItemListRow> entry : currentContentItemToRowMap.entrySet()) {
      ContentItemListRow row = entry.getValue();
      ContentItemListElement listElement = row.contentItemListElement;

      if (contentItemIds.contains(entry.getKey().getId())) {
        listElement.setExpansion(true);
        if (contentItemIds.size() == 1) {
          singleRowToScrollTo = row;
        }
        countFound++;
      } else {
        listElement.setExpansion(false);
      }
    }
    
    if (singleRowToScrollTo != null) {
      WindowScroll.scrollTo(singleRowToScrollTo.elementWidget.getAbsoluteTop(),
          new Command() {
            @Override
            public void execute() {
              LivingStoryControls.repositionAnchoredPanel();
            }        
          });
    }
    
    return countFound == contentItemIds.size();
  }
  
  private void setTimeVisibility(ContentItemListElement currentElement, ContentItemListElement previousElement) {
    if (previousElement != null 
        && previousElement.getDateString().equals(currentElement.getDateString())) {
      previousElement.setTimeVisible(true);
      currentElement.setTimeVisible(true);
    } else {
      currentElement.setTimeVisible(false);
    }
  }
  
  private class ContentItemListRow {
    public ContentItemListElement contentItemListElement;
    public Widget elementWidget;
    
    public ContentItemListRow(ContentItemListElement contentItemListElement, Widget elementWidget) {
      this.contentItemListElement = contentItemListElement;
      this.elementWidget = elementWidget;
    }
  }
}
