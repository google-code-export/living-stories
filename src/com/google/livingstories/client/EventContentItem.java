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

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.google.livingstories.client.lsp.ContentRenderer;

import java.util.Date;
import java.util.Set;

/**
 * Client-side version of an event content entity
 */
public class EventContentItem extends BaseContentItem {
  private Date startDate;
  private Date endDate;
  private String update;
  private String summary;
  
  public EventContentItem() {}
  
  public EventContentItem(Long id, Date timestamp, Set<Long> contributorIds,
      Importance importance, Long livingStoryId, Date startDate, Date endDate,
      String update, String summary, String detailedContent) {
    super(id, timestamp, ContentItemType.EVENT, contributorIds, detailedContent, importance, 
        livingStoryId);
    this.startDate = startDate;
    this.endDate = endDate;
    this.update = update;
    this.summary = summary;
  }
  
  @Override
  public String getTypeString() {
    return ClientMessageHolder.consts.contentTypeStringEvent();
  }
  
  public Date getEventStartDate() {
    return startDate;
  }

  public Date getEventEndDate() {
    return endDate;
  }
  
  public String getEventUpdate() {
    return update;
  }
  
  public String getEventSummary() {
    return summary;
  }
  
  public Widget renderUpdate() {
    return new ContentRenderer(update, false);
  }
  
  public Widget renderSummary() {
    return new ContentRenderer(summary, false);
  }
  
  @Override
  public Widget renderTiny() {
    return new HTML(update);
  }

  /**
   * Returns the date to use when sorting events by date, or filtering based on date
   */
  @Override
  public Date getDateSortKey() {
    return getEventEndDate() == null ? 
        (getEventStartDate() == null ? getTimestamp() : getEventStartDate()) 
        : getEventEndDate();
  }
  
  @Override
  public String getDisplayString() {
    return "[" + getContentItemType().toString() + "] " + getEventUpdate();
  }
  
  @Override
  public boolean displayTopLevel() {
    return true;
  }
}
