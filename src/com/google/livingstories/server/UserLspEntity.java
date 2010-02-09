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

import com.google.appengine.api.datastore.Key;

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
 * Class to model the data stored for a particular LSP for a particular user.
 * Currently, this data consists of only the time that the user last visited the LSP.
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class UserLspEntity implements Serializable, JSONSerializable {
  
  // This primary key is needed for the persistence to work. The lspId can't be used as
  // the primary key for this class because a Long primary key can only be auto-generated,
  // not manually set.
  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Key id;
  
  @Persistent
  private Long lspId;
  
  @Persistent
  private boolean subscribedToEmails = false;
  
  @Persistent
  private Date lastVisitedTime;
  
  @Persistent
  private Integer visitCount;
  
  public UserLspEntity(Long lspId, Date lastVisitedTime) {
    this.lspId = lspId;
    this.lastVisitedTime = lastVisitedTime;
    this.visitCount = 1;
  }
  
  public Key getId() {
    return id;
  }
  
  public Long getLspId() {
    return lspId;
  }
  
  public void setLspId(Long lspId) {
    this.lspId = lspId;
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
      object.put("lspId", lspId);
      object.put("lastVisitedTime", SimpleDateFormat.getInstance().format(lastVisitedTime));
      object.put("visitCount", visitCount);
      object.put("subscribedToEmails", subscribedToEmails);
    } catch (JSONException ex) {
      throw new RuntimeException(ex);
    }
    return object;
  }
  
  public static UserLspEntity fromJSON(JSONObject json) {
    try {
      UserLspEntity entity = new UserLspEntity(json.getLong("lspId"),
          SimpleDateFormat.getInstance().parse(json.getString("lastVisitedTime")));
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