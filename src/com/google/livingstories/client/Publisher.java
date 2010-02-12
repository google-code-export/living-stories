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

/**
 * An enum to store the different publishers from which we will be taking the content for the
 * preview launch.
 */
public enum Publisher {
  NYT("The New York Times", "/images/nyt_logo.jpg"),
  WAPO("The Washington Post", "/images/wapo_logo.jpg");
  
  private String displayName;
  private String logoPath;
  
  private Publisher(String displayName, String logoPath) {
    this.displayName = displayName;
    this.logoPath = logoPath;
  }

  @Override
  public String toString() {
    return displayName;
  }

  public String getLogoPath() {
    return logoPath;
  }
  
  public static Publisher fromString(String s) {
    for (Publisher publisher : Publisher.values()) {
      if (publisher.displayName.equals(s)) {
        return publisher;
      }
    }
    
    return null;
  }
}
