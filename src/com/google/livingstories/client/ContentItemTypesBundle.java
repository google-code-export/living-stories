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
import java.util.HashSet;
import java.util.Set;

/**
 * Results for the query that determines which filters to show based on
 * what content items are available.
 */
public class ContentItemTypesBundle implements Serializable {
  // to keep serialization machinery happy:
  @SuppressWarnings("unused")
  private ContentItemTypesBundle() {}
  
  public ContentItemTypesBundle(String themeName) {
    this.themeName = themeName;
  }
  
  public Set<ContentItemType> availableContentItemTypes = new HashSet<ContentItemType>();
  public Set<AssetType> availableAssetTypes = new HashSet<AssetType>();
  public boolean opinionAvailable;
  
  // Actually not related to the main purpose of this class, but included as a convenience.
  public String themeName;
}
