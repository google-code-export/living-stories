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

package com.google.livingstories.client.contentmanager;

import com.google.livingstories.client.AssetType;
import com.google.livingstories.client.ContentItemType;
import com.google.livingstories.client.Importance;
import com.google.livingstories.client.NarrativeType;
import com.google.livingstories.client.PlayerType;
import com.google.livingstories.client.PublishState;

import java.io.Serializable;
import java.util.Date;

public class SearchTerms implements Serializable {
  public Long livingStoryId;
  public ContentItemType contentItemType;
  public PlayerType playerType;
  public AssetType assetType;
  public NarrativeType narrativeType;
  public Date afterDate;
  public Date beforeDate;
  public Importance importance;
  public PublishState publishState;
}
