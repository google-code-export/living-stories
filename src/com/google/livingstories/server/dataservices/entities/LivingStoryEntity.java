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

package com.google.livingstories.server.dataservices.entities;

import com.google.common.collect.Lists;
import com.google.livingstories.client.LivingStory;
import com.google.livingstories.client.PublishState;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Embedded;
import javax.jdo.annotations.Extension;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Order;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Persistable entity for living stories.
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class LivingStoryEntity
    implements Serializable, JSONSerializable, HasSerializableLivingStoryId {
  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Long id;
  
  @Persistent
  private String url;
  
  @Persistent
  private String title;
  
  @Persistent
  private PublishState publishState;
  
  @Persistent
  @Order(extensions = @Extension(vendorName="datanucleus", key="list-ordering",
      value="timestamp asc"))
  private List<Summary> summaryRevisions = new ArrayList<Summary>();

  @PersistenceCapable(identityType = IdentityType.APPLICATION)
  public static class Summary implements Serializable {
    @SuppressWarnings("unused")
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    @Extension(vendorName="datanucleus", key="gae.encoded-pk", value="true")
    private String id;

    @Persistent
    @Embedded(members={
      @Persistent(name="value", columns=@Column(name="content"))
    })
    private LongStringHolder content;
    
    @Persistent
    private Date timestamp;

    Summary(String content, Date timestamp) {
      this.content = new LongStringHolder(content);
      this.timestamp = timestamp;
    }

    public String getContent() {
      return content.getValue();
    }

    public void setContent(String content) {
      this.content = new LongStringHolder(content);
    }
    
    private Date getTimestamp() {
      return timestamp;
    }

    public JSONObject toJSON() {
      JSONObject object = new JSONObject();
      try {
        object.put("id", id);
        object.put("content", content.getValue());
        object.put("timestamp", SimpleDateFormat.getInstance().format(timestamp));
      } catch (JSONException ex) {
        throw new RuntimeException(ex);
      }
      return object;
    }

    public static Summary fromJSON(JSONObject json) {
      try {
        Summary entity = new Summary(json.getString("content"),
            SimpleDateFormat.getInstance().parse(json.getString(("timestamp"))));
        return entity;
      } catch (JSONException ex) {
        throw new RuntimeException(ex);
      } catch (ParseException ex) {
        throw new RuntimeException(ex);
      }
    }
  }


  public LivingStoryEntity(String url, String title, PublishState publishState) {
    this.url = url;
    this.title = title;
    this.publishState = publishState;
  }
  
  public LivingStoryEntity(String url, String title, PublishState publishState, String summary) {
    this.url = url;
    this.title = title;
    this.publishState = publishState;
    addSummaryRevision(summary);
  }

  public long getId() {
    return id;
  }
  
  public Long getLivingStoryId() {
    return getId();
  }
  
  public String getUrl() {
    return url;
  }
  
  public void setUrl(String url) {
    this.url = url;
  }
  
  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }
  
  public PublishState getPublishState() {
    return publishState == null ? PublishState.PUBLISHED : publishState;
  }

  public void setPublishState(PublishState publishState) {
    this.publishState = publishState;
  }

  public void addSummaryRevision(String summary) {
    summaryRevisions.add(new Summary(summary, new Date()));
  }
  
  public String getSummary() {
    if (summaryRevisions.isEmpty()) {
      return "";
    } else {
      return summaryRevisions.get(summaryRevisions.size() - 1).getContent();
    }
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
      Date revisionTimestamp = revision.getTimestamp();
      if (revisionTimestamp.before(time)) {
        lastRevisionContent = revision.getContent();
      } else {
        break;
      }
    }
    return lastRevisionContent;
  }
  
  public List<Summary> getAllSummaryRevisions() {
    return Lists.newArrayList(summaryRevisions);
  }
  
  public void setSummaryRevisions(List<Summary> summaryRevisions) {
    this.summaryRevisions = Lists.newArrayList(summaryRevisions);
  }
  
  public LivingStory toClientObject(boolean latestRevisionsOnly) {
    List<LivingStory.Summary> clientRevisions = new ArrayList<LivingStory.Summary>();
    // If latestRevisionsOnly is true, only return an object with the last 5 summary revisions.
    int i = latestRevisionsOnly ? Math.max(0, summaryRevisions.size() - 6) : 0;
    for (; i < summaryRevisions.size(); i++) {
      Summary summary = summaryRevisions.get(i);
      clientRevisions.add(new LivingStory.Summary(summary.getContent(), summary.getTimestamp()));
    }
    return new LivingStory(id, url, title, getPublishState(), clientRevisions);
  }
  
  @Override
  public String toString() {
    try {
      return toJSON().toString(2);
    } catch (JSONException ex) {
      throw new RuntimeException(ex);
    }
  }
  
  @Override
  public JSONObject toJSON() {
    JSONObject object = new JSONObject();
    try {
      object.put("id", id);
      object.put("url", url);
      object.put("title", title);
      object.put("publishState", getPublishState().name());
      JSONArray revisionsJSON = new JSONArray();
      for (Summary revision : summaryRevisions) {
        revisionsJSON.put(revision.toJSON());
      }
      object.put("summaryRevisions", revisionsJSON);
    } catch (JSONException ex) {
      throw new RuntimeException(ex);
    }
    return object;
  }
  
  public static LivingStoryEntity fromJSON(JSONObject json) {
    try {
      LivingStoryEntity entity = new LivingStoryEntity(
          json.getString("url"), json.getString("title"), 
          json.has("publishState") ? 
              PublishState.valueOf(json.getString("publishState")) : PublishState.PUBLISHED);
      JSONArray revisions = json.getJSONArray("summaryRevisions");
      for (int i = 0; i < revisions.length(); i++) {
        entity.summaryRevisions.add(Summary.fromJSON(revisions.getJSONObject(i)));
      }
      return entity;
    } catch (JSONException ex) {
      throw new RuntimeException(ex);
    }
  }
}
