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

import com.google.livingstories.client.ContentItemType;
import com.google.livingstories.client.BaseContentItem;
import com.google.livingstories.client.EventContentItem;
import com.google.livingstories.client.NarrativeContentItem;
import com.google.livingstories.client.contentitemlist.ContentItemListElement;
import com.google.livingstories.client.util.LivingStoryData;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Factory that creates stream views based on the content type.
 */
public class StreamViewFactory {
  public static ContentItemListElement createView(BaseContentItem contentItem,
      Map<Long, BaseContentItem> idToContentItemMap) {
    Map<ContentItemType, List<BaseContentItem>> linkedContentItemsByType;
    switch (contentItem.getContentItemType()) {
      case EVENT:
        linkedContentItemsByType = processContentItem(contentItem, idToContentItemMap);
        return new EventStreamView((EventContentItem) contentItem, linkedContentItemsByType);
      case NARRATIVE:
        linkedContentItemsByType = processContentItem(contentItem, idToContentItemMap);
        return new NarrativeStreamView((NarrativeContentItem) contentItem,
            linkedContentItemsByType);
      default:
        return new BaseStreamView(contentItem);
    }
  }
  
  /**
   * Processes a content item by setting its read state and returning a map of content items that
   * are linked to it.
   */
  private static Map<ContentItemType, List<BaseContentItem>> processContentItem(
      BaseContentItem contentItem, Map<Long, BaseContentItem> idToContentItemMap) {
    Date lastVisitDate = LivingStoryData.getLastVisitDate();
    if (lastVisitDate != null) {
      contentItem.setRenderAsSeen(contentItem.getTimestamp().before(lastVisitDate));
    }
    
    Map<ContentItemType, List<BaseContentItem>> linkedContentItemsByType =
        new HashMap<ContentItemType, List<BaseContentItem>>();
    for (ContentItemType type : ContentItemType.values()) {
      linkedContentItemsByType.put(type, new ArrayList<BaseContentItem>());
    }
    for (Long contentItemId : contentItem.getLinkedContentItemIds()) {
      BaseContentItem linkedContentItem = idToContentItemMap.get(contentItemId);
      if (linkedContentItem != null) {
        linkedContentItemsByType.get(linkedContentItem.getContentItemType()).add(linkedContentItem);

        if (contentItem.getRenderAsSeen() && lastVisitDate != null
            && linkedContentItem.getTimestamp().after(lastVisitDate)) {
          contentItem.setRenderAsSeen(false);
        }
      }
    }
    for (BaseContentItem narrative : linkedContentItemsByType.get(ContentItemType.NARRATIVE)) {
      List<BaseContentItem> linkedContentItems = new ArrayList<BaseContentItem>();
      for (Long contentItemId : narrative.getLinkedContentItemIds()) {
        linkedContentItems.add(idToContentItemMap.get(contentItemId));
      }
      narrative.setLinkedContentItems(linkedContentItems);
    }
    return linkedContentItemsByType;
  }
}
