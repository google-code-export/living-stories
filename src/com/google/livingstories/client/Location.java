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
 * Client class to represent a location.
 */
public class Location implements Serializable {
  private Double latitude;
  private Double longitude;
  private String description;
  
  // Zero-arg constructor to keep GWT happy
  public Location() {
  }
  
  /**
   * Description can be an address such as "111 8th avenue, New York" or just some non-precise
   * description of a location such as "in the Yangon Lake area".
   */
  public Location(Double latitude, Double longitude, String description) {
    if (latitude != null || longitude != null) {
      assert(latitude != null && longitude != null);
    }
    this.latitude = latitude;
    this.longitude = longitude;
    this.description = description;
  }

  public Double getLatitude() {
    return latitude;
  }

  public Double getLongitude() {
    return longitude;
  }
  
  public String getDescription() {
    return description;
  }
}
