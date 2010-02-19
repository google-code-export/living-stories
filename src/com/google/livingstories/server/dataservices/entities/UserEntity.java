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

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Service for saving information related to the signed-in user and their status on various
 * living story pages such as the last time they visited a page, etc. We will use the Google
 * Account as the identifier for a user. The appengine documentation cautions that while storing
 * the User object returned by the Google Accounts API is supported, they may not behave as stable
 * identifiers. If a user changes their Google Account email address, the identifier changes.
 * However, no other unique user ID beyond the email address is exposed at this point.
 * 
 * Currently, this class only stores the last time a user visited a Living Story Page. In the
 * future, other data might be stored such as which stories they clicked on, etc.
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class UserEntity implements Serializable, JSONSerializable {
  
  // The name property on this key is actually the user's email address.
  @PrimaryKey
  @Persistent
  private Key googleAccountId;
  
  @Persistent
  private String defaultLspView;
  
  // This method is needed to tell JDO not to auto-generate the Key for this class.
  public void setGoogleAccountId(Key googleAccountId) {
    this.googleAccountId = googleAccountId;
  }
  
  /**
   * Return the current user's login email.
   */
  public String getEmail() {
    return googleAccountId.getName();
  }
  
  public String getDefaultLspView() {
    return defaultLspView;
  }
  
  public void setDefaultLspView(String defaultLspView) {
    this.defaultLspView = defaultLspView;
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
      object.put("googleAccountId", googleAccountId.getName());
      object.put("defaultLspView", defaultLspView);
    } catch (JSONException ex) {
      throw new RuntimeException(ex);
    }
    return object;
  }
  
  public static UserEntity fromJSON(JSONObject json) {
    try {
      UserEntity entity = new UserEntity();
      entity.setGoogleAccountId(KeyFactory.createKey(UserEntity.class.getSimpleName(),
          json.getString("googleAccountId")));
      if (json.has("defaultLspView")) {
        entity.setDefaultLspView(json.getString("defaultLspView"));
      }
      return entity;
    } catch (JSONException ex) {
      throw new RuntimeException(ex);
    }
  }
}
