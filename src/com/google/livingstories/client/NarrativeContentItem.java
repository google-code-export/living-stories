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

import java.util.Date;
import java.util.Set;

/**
 * Client-side version of a narrative content entity
 */
public class NarrativeContentItem extends BaseContentItem {
  private String headline;
  private NarrativeType narrativeType;
  private boolean isStandalone;
  private Date narrativeDate;
  private String narrativeSummary;
  
  public NarrativeContentItem() {}
  
  public NarrativeContentItem(Long id, Date timestamp, Set<Long> contributorIds, String content,
      Importance importance, Long livingStoryId, String headline, NarrativeType narrativeType,
      boolean isStandalone, Date narrativeDate, String narrativeSummary) {
    super(id, timestamp, ContentItemType.NARRATIVE, contributorIds, content, importance,
        livingStoryId);
    this.headline = headline;
    this.narrativeType = narrativeType;
    this.isStandalone = isStandalone;
    this.narrativeDate = narrativeDate;
    this.narrativeSummary = narrativeSummary;
  }

  public String getHeadline() {
    return headline;
  }

  public NarrativeType getNarrativeType() {
    return narrativeType;
  }
  
  public boolean isStandalone() {
    return isStandalone;
  }

  public Date getNarrativeDate() {
    return narrativeDate;
  }

  public String getNarrativeSummary() {
    return narrativeSummary;
  }

  @Override
  public String getTypeString() {
    return narrativeType.toString();
  }
  
  @Override
  public Widget renderTiny() {
    return new HTML(headline);
  }
  
  @Override
  public Date getDateSortKey() {
    return narrativeDate == null ? getTimestamp() : narrativeDate;
  }
  
  @Override
  public String getDisplayString() {
    return "[" + getTypeString() + "] " + getHeadline();
  }
  
  @Override
  public boolean displayTopLevel() {
    return isStandalone;
  }
  
  public boolean isOpinion() {
    return narrativeType.isOpinion();
  }
}
