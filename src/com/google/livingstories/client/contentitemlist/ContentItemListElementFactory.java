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

import com.google.livingstories.client.ContentItemType;
import com.google.livingstories.client.BaseContentItem;
import com.google.livingstories.client.lsp.views.contentitems.StreamViewFactory;

import java.util.Map;

/**
 * Static method to create an {@link ContentItemListElement} object for a content item,
 * depending on its type.
 */
public class ContentItemListElementFactory {
  
  /**
   * Create an {@link ContentItemListElement} object for a content item, depending on its type.
   * @param contentItem source content item for this
   * @param idToContentItemMap Map from content item ids to the objects themselves.
   * @param handler Handler that handles onClick events on the content items. Can be null
   * if no onclick behavior is needed.
   */
  public static ContentItemListElement createContentItemListElement(BaseContentItem contentItem, 
      Map<Long, BaseContentItem> idToContentItemMap, ContentItemClickHandler handler,
      boolean noHistoryOnToggle) {
    if (contentItem.getContentItemType() == ContentItemType.EVENT
        || contentItem.getContentItemType() == ContentItemType.NARRATIVE) {
      return StreamViewFactory.createView(contentItem, idToContentItemMap);
    } else {
      return new SimpleContentItemListElement(contentItem, handler);
    }
  }
}
