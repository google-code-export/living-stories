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

import com.google.livingstories.client.Theme;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * This class represents a grouping of atoms for a living story.
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class AngleEntity implements Serializable, JSONSerializable, HasSerializableLspId {

  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Long id;
  
  @Persistent
  private String name;
  
  @Persistent
  private Long lspId;
  
  public AngleEntity(String name, Long lspId) {
    this.name = name;
    this.lspId = lspId;
  }

  public String getName() {
    return name;
  }

  public Long getLspId() {
    return lspId;
  }
  
  public Long getId() {
    return id;
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
  public void setLspId(Long lspId) {
    this.lspId = lspId;
  }
 
  public Theme toClientObject() {
    return new Theme(getId(), getName(), getLspId());
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
      object.put("name", name);
      object.put("lspId", lspId);
    } catch (JSONException ex) {
      throw new RuntimeException(ex);
    }
    return object;
  }
  
  public static AngleEntity fromJSON(JSONObject json) {
    try {
      return new AngleEntity(json.getString("name"), json.getLong("lspId"));
    } catch (JSONException ex) {
      throw new RuntimeException(ex);
    }
  }
}
