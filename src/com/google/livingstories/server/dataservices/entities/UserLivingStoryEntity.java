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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Class to model the data stored for a particular Living Story for a particular user.
 * Currently, this data consists of only the time that the user last visited the Living Story.
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class UserLivingStoryEntity implements Serializable, JSONSerializable {
  
  // This primary key is needed for the persistence to work. The livingStoryId can't be used as
  // the primary key for this class because a Long primary key can only be auto-generated,
  // not manually set.
  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Long id;
  
  @Persistent
  private String parentEmailAddress;
  
  @Persistent
  private Long livingStoryId;
  
  @Persistent
  private boolean subscribedToEmails = false;
  
  @Persistent
  private Date lastVisitedTime;
  
  @Persistent
  private Integer visitCount;
  
  public UserLivingStoryEntity(String parentEmailAddress, Long livingStoryId,
      Date lastVisitedTime) {
    this.parentEmailAddress = parentEmailAddress;
    this.livingStoryId = livingStoryId;
    this.lastVisitedTime = lastVisitedTime;
    this.visitCount = 1;
  }
  
  public Long getId() {
    return id;
  }
  
  public String getParentEmailAddress() {
    return parentEmailAddress;
  }
  
  public Long getLivingStoryId() {
    return livingStoryId;
  }
  
  public void setLivingStoryId(Long livingStoryId) {
    this.livingStoryId = livingStoryId;
  }

  public boolean isSubscribedToEmails() {
    return subscribedToEmails;
  }
  
  public void setSubscribedToEmails(boolean value) {
    subscribedToEmails = value;
  }
  
  public Date getLastVisitedTime() {
    return lastVisitedTime;
  }
  
  public void setLastVisitedTime(Date lastVisitedTime) {
    this.lastVisitedTime = lastVisitedTime;
  }
  
  public int getVisitCount() {
    return visitCount == null ? 1 : visitCount;
  }
  
  public void setVisitCount(int visitCount) {
    this.visitCount = visitCount;
  }
  
  public void incrementVisitCount() {
    if (visitCount == null) {
      visitCount = 2;
    } else {
      visitCount++;
    }
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
      object.put("parentEmailAddress", parentEmailAddress);
      object.put("livingStoryId", livingStoryId);
      object.put("lastVisitedTime", SimpleDateFormat.getInstance().format(lastVisitedTime));
      object.put("visitCount", visitCount);
      object.put("subscribedToEmails", subscribedToEmails);
    } catch (JSONException ex) {
      throw new RuntimeException(ex);
    }
    return object;
  }
  
  public static UserLivingStoryEntity fromJSON(JSONObject json) {
    try {
      UserLivingStoryEntity entity = new UserLivingStoryEntity(
          json.getString("parentEmailAddress"), json.getLong("livingStoryId"),
          SimpleDateFormat.getInstance().parse(json.getString("lastVisitedTime")));
      // Note: if the JSON that you're importing uses a different naming convention for
      // the living story id, convert it before processing here.
      if (json.has("visitCount")) {
        entity.setVisitCount(json.getInt("visitCount"));
      }
      if (json.has("subscribedToEmails")) {
        entity.setSubscribedToEmails(json.getBoolean("subscribedToEmails"));
      }
      return entity;
    } catch (JSONException ex) {
      throw new RuntimeException(ex);
    } catch (ParseException ex) {
      throw new RuntimeException(ex);
    }
  }
}