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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Entity to store the state of the launch - whether we are opening it up to everyone, or whether
 * it's still restricted to a whitelist. This data type will have only 1 single entity.
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class WhitelistRestrictionEntity implements Serializable, JSONSerializable {
  
  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Long id;
  
  @Persistent
  private Boolean restrictedToWhitelist;
  
  public WhitelistRestrictionEntity(Boolean restrictedToWhitelist) {
    this.restrictedToWhitelist = restrictedToWhitelist;
  }

  public Long getId() {
    return id;
  }

  public Boolean getRestrictedToWhitelist() {
    return restrictedToWhitelist == null ? true : restrictedToWhitelist;
  }

  public void setRestrictedToWhitelist(Boolean restrictedToWhitelist) {
    this.restrictedToWhitelist = restrictedToWhitelist;
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
      object.put("restrictedToWhitelist", restrictedToWhitelist);
    } catch (JSONException ex) {
      throw new RuntimeException(ex);
    }
    return object;
  }
  
  public static WhitelistRestrictionEntity fromJSON(JSONObject json) {
    try {
      return new WhitelistRestrictionEntity(json.getBoolean("restrictedToWhitelist"));
    } catch (JSONException ex) {
      throw new RuntimeException(ex);
    }
  }

}
