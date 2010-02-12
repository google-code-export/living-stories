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

/**
 * Entity to represent suggestions submitted by users for improving the content of a story.
 */
public class UserSuggestion implements Serializable {
  
  public enum Status {
    NEW,
    FIXED,
    IGNORED,
    DUPLICATE;
  }

  private Long id;
  private Long livingStoryId;
  private Long atomId;
  private String userEmail;
  private String highlightedText;
  private SuggestionType suggestionType;
  private String comments;
  private Status status;
  
  public UserSuggestion() {
  }
  
  public UserSuggestion(Long id, Long livingStoryId, Long atomId, String userEmail,
      String highlightedText, SuggestionType suggestionType, String comments, Status status) {
    this.id = id;
    this.livingStoryId = livingStoryId;
    this.atomId = atomId;
    this.userEmail = userEmail;
    this.highlightedText = highlightedText;
    this.suggestionType = suggestionType;
    this.comments = comments;
    this.status = status;
  }

  public Long getId() {
    return id;
  }

  public Long getLivingStoryId() {
    return livingStoryId;
  }

  public Long getAtomId() {
    return atomId;
  }

  public String getUserEmail() {
    return userEmail;
  }

  public String getHighlightedText() {
    return highlightedText;
  }

  public SuggestionType getSuggestionType() {
    return suggestionType;
  }

  public String getComments() {
    return comments;
  }

  public Status getStatus() {
    return status;
  }
}
