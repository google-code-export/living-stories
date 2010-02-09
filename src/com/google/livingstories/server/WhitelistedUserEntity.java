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

import com.google.livingstories.client.WhitelistedUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Entity to represent the email addresses of non-Google users who are allowed access to the
 * application during the preview launch phase.
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class WhitelistedUserEntity implements Serializable, JSONSerializable {
  
  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Long id;
  
  @Persistent
  private String email;
  
  public WhitelistedUserEntity(String email) {
    this.email = email;
  }

  public String getEmail() {
    return email;
  }
  
  public WhitelistedUser toClientObject() {
    return new WhitelistedUser(id, email);
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
      object.put("email", email);
    } catch (JSONException ex) {
      throw new RuntimeException(ex);
    }
    return object;
  }
  
  public static WhitelistedUserEntity fromJSON(JSONObject json) {
    try {
      return new WhitelistedUserEntity(json.getString("email"));
    } catch (JSONException ex) {
      throw new RuntimeException(ex);
    }
  }
}
