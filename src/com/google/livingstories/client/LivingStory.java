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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Client side version of a LivingStoryEntity.
 */
public class LivingStory implements Serializable {
  private long id;
  private String url;
  private String title;
  private PublishState publishState;
  private List<Summary> summaryRevisions;
  
  public static class Summary implements Serializable {
    private String content;
    private Date timestamp;

    // zero-arg constructor to keep GWT happy
    public Summary() {
    }

    public Summary(String content, Date timestamp) {
      this.content = content;
      this.timestamp = timestamp;
    }
  }
  
  // GWT needs a zero-argument constructor to be happy.
  public LivingStory() {
  }
  
  public LivingStory(long id, String url, String title, String summary, PublishState publishState) {
    init(id, url, title, publishState);
    this.summaryRevisions.add(new Summary(summary, new Date()));
  }
  
  public LivingStory(long id, String url, String title, PublishState publishState, 
      List<Summary> summaryRevisions) {
    init(id, url, title, publishState);
    this.summaryRevisions = new ArrayList<Summary>(summaryRevisions);
  }
  
  private void init(long id, String url, String title, PublishState publishState) {
    this.id = id;
    this.url = url;
    this.title = title;
    this.publishState = publishState;
  }

  // Accessors
  public long getId() {
    return id;
  }
  
  public String getUrl() {
    return url;
  }
  
  public String getTitle() {
    return title;
  }

  public PublishState getPublishState() {
    return publishState;
  }

  public String getSummary() {
    return getSummary(getRevisionCount());
  }
  
  public String getSummary(int revision) {
    if (summaryRevisions == null || summaryRevisions.isEmpty()) {
      return null;
    } else {
      return getRevision(revision).content;
    }
  }
  
  public Date getLastChangeTimestamp() {
    return getRevision(getRevisionCount()).timestamp;
  }
  
  public List<Summary> getSummaryRevisions() {
    return new ArrayList<Summary>(summaryRevisions);
  }
  
  public int getRevisionCount() {
    return summaryRevisions.size();
  }
  
  /**
   * Gets the revision data for the 1-indexed revision number of the summary
   * @param revision
   * @return the appropriate Summary object.
   */
  private Summary getRevision(int revision) {
    if (revision <= 0 || revision > getRevisionCount()) {
      throw new IllegalArgumentException("The summary does not have revision " + revision);
    }
    return summaryRevisions.get(revision - 1);
  }
  
  /**
   * Return the contents of the last summary revision that was saved before a given time.
   * If the provided time is null, returns the contents of the latest summary revision.
   */
  public String getLastSummaryRevisionBeforeTime(Date time) {
    if (time == null) {
      return getSummary();
    }
    String lastRevisionContent = "";
    for (Summary revision : summaryRevisions) {
      Date revisionTimestamp = revision.timestamp;
      if (revisionTimestamp.before(time)) {
        lastRevisionContent = revision.content;
      } else {
        break;
      }
    }
    return lastRevisionContent;
  }
  
  public boolean dateWithinAvailableRevisions(Date time) {
    return time == null || time.after(summaryRevisions.get(0).timestamp);
  }
}
