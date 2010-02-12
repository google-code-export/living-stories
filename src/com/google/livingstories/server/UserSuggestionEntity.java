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

package com.google.livingstories.server;

import com.google.appengine.api.datastore.Text;
import com.google.livingstories.client.SuggestionType;
import com.google.livingstories.client.UserSuggestion;
import com.google.livingstories.client.UserSuggestion.Status;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Entity to represent suggestions submitted by users for improving the content of a story.
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class UserSuggestionEntity implements Serializable, JSONSerializable {

  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Long id;
  
  @Persistent
  private Long livingStoryId;
  
  @Persistent
  private Long atomId;
  
  @Persistent
  private String userEmail;
  
  @Persistent
  private Text highlightedText;
  
  @Persistent
  private SuggestionType suggestionType;
  
  @Persistent
  private Text comments;
  
  @Persistent
  private Status status;
  
  public UserSuggestionEntity(Long livingStoryId, Long atomId, SuggestionType suggestionType) {
    this.livingStoryId = livingStoryId;
    this.atomId = atomId;
    this.suggestionType = suggestionType;
    this.status = UserSuggestion.Status.NEW;
  }
  
  public Long getId() {
    return id;
  }

  public Long getLivingStoryId() {
    return livingStoryId;
  }
  
  public void setLivingStoryId(Long livingStoryId) {
    this.livingStoryId = livingStoryId;
  }

  public Long getAtomId() {
    return atomId;
  }
  
  public void setAtomId(Long atomId) {
    this.atomId = atomId;
  }

  public String getUserEmail() {
    return userEmail;
  }

  public void setUserEmail(String userEmail) {
    this.userEmail = userEmail;
  }

  public String getHighlightedText() {
    return highlightedText == null ? null : highlightedText.getValue();
  }

  public void setHighlightedText(String highlightedText) {
    if (highlightedText != null) {
      this.highlightedText = new Text(highlightedText);
    }
  }

  public SuggestionType getSuggestionType() {
    return suggestionType;
  }

  public void setSuggestionType(SuggestionType suggestionType) {
    this.suggestionType = suggestionType;
  }

  public String getComments() {
    return comments == null ? null : comments.getValue();
  }

  public void setComments(String comments) {
    if (comments != null) {
      this.comments = new Text(comments);
    }
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public UserSuggestion toClientObject() {
    return new UserSuggestion(id, livingStoryId, atomId, userEmail, getHighlightedText(),
        suggestionType, getComments(), status);
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
      object.put("livingStoryId", livingStoryId);
      object.put("atomId", atomId);
      object.put("userEmail", userEmail);
      object.put("highlightedText", getHighlightedText());
      object.put("suggestionType", suggestionType.name());
      object.put("comments", getComments());
      object.put("status", status.name());
    } catch (JSONException ex) {
      throw new RuntimeException(ex);
    }
    return object;
  }
  
  public static UserSuggestionEntity fromJSON(JSONObject json) {
    try {
      UserSuggestionEntity entity = new UserSuggestionEntity(json.getLong("livingStoryId"), 
          json.getLong("atomId"), SuggestionType.valueOf(json.getString("suggestionType")));
      // Note: if the JSON that you're importing uses a different naming convention for
      // the living story id, convert it before processing here.

      entity.setStatus(Status.valueOf(json.getString("status")));
      if (json.has("userEmail")) {
        entity.setUserEmail(json.getString("userEmail"));
      }
      if (json.has("highlightedText")) {
        entity.setHighlightedText(json.getString("highlightedText"));
      }
      if (json.has("comments")) {
        entity.setComments(json.getString("comments"));
      }
      return entity;
    } catch (JSONException ex) {
      throw new RuntimeException(ex);
    }
  }
}
