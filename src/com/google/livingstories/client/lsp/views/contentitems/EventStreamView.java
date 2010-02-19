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

import com.google.gwt.user.client.ui.Widget;
import com.google.livingstories.client.ContentItemType;
import com.google.livingstories.client.BaseContentItem;
import com.google.livingstories.client.EventContentItem;
import com.google.livingstories.client.lsp.ContentRenderer;
import com.google.livingstories.client.util.GlobalUtil;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * The stream view for an event entity.  Displays the content of the event
 * as well as linked content.
 */
public class EventStreamView extends ContainerStreamView<EventContentItem> {
  public EventStreamView(EventContentItem event,
      Map<ContentItemType, List<BaseContentItem>> linkedContentItemsByType) {
    super(event, linkedContentItemsByType);
  }

  @Override
  protected String getHeadline() {
    return contentItem.getEventUpdate();
  }
  
  @Override
  protected Date getStartDate() {
    return contentItem.getEventStartDate();
  }
  
  @Override
  protected Date getEndDate() {
    return contentItem.getEventEndDate();
  }
  
  @Override
  protected LongContainerView<EventContentItem> getLongContainerView() {
    return new LongContainerView<EventContentItem>(contentItem, linkedContentItemsByType) {
      @Override
      protected Widget createSummary() {
        if (GlobalUtil.isContentEmpty(contentItem.getEventSummary())) {
          return null; 
        } else { 
          return new ContentRenderer(contentItem.getEventSummary(), false);
        }
      }
      
      @Override
      protected Widget createDetails() {
        if (GlobalUtil.isContentEmpty(contentItem.getContent())) {
          return null;
        } else {
          return new ContentItemPreviewWithHeader(contentItem);
        }
      }      
    };
  }
}
