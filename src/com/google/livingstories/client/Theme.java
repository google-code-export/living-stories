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
 * This is the client version of a 
 * {@link com.google.livingstories.server.dataservices.entities.ThemeEntity}.
 * The {@link ContentRpcService} converts Themes to
 * and from this class which is used by the client.
 */
public class Theme implements Serializable {
  private Long id;
  private String name;
  private Long livingStoryId;
  
  public Theme() {
  }
  
  public Theme(Long id, String name, Long livingStoryId) {
    this.id = id;
    this.name = name;
    this.livingStoryId = livingStoryId;
  }
  
  public Long getId() {
    return id;
  }
  
  public String getName() {
    return name;
  }
  
  public Long getLivingStoryId() {
    return livingStoryId;
  }
}
