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

import com.google.livingstories.client.util.Constants;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Client-side version of a player content entity, which represents a person or an organization, not
 * specific to a story.
 */
public class PlayerContentItem extends BaseContentItem {
  protected String name;
  protected List<String> aliases;
  protected PlayerType playerType;
  protected AssetContentItem photoContentItem;

  public PlayerContentItem() {}
  
  public PlayerContentItem(Long id, Date timestamp, Set<Long> contributorIds, String content,
      Importance importance, String name, List<String> aliases, PlayerType playerType, 
      AssetContentItem photoContentItem) {
    super(id, timestamp, ContentItemType.PLAYER, contributorIds, content, importance, null);
    this.name = name;
    this.aliases = (aliases == null ? new ArrayList<String>() : new ArrayList<String>(aliases));
    this.playerType = playerType;
    this.photoContentItem = photoContentItem;
  }

  public String getName() {
    return name;
  }
  
  public List<String> getAliases() {
    return aliases;
  }

  public PlayerType getPlayerType() {
    return playerType;
  }

  public boolean hasPhoto() {
    return photoContentItem != null;
  }
  
  public Long getPhotoContentItemId() {
    return photoContentItem == null ? null : photoContentItem.getId();
  }
  
  public AssetContentItem getPhotoContentItem() {
    return photoContentItem;
  }
  
  @Override
  public String getTitleString() {
    return "";
  }
  
  @Override
  public String getTypeString() {
    return playerType.toString();
  }
  
  public String getPreviewContentToRender() {
    return getContent().split(Constants.BREAK_TAG)[0];
  }
  
  public String getFullContentToRender() {
    return getContent();
  }
  
  @Override
  public String getDisplayString() {
    return "[" + getTypeString() + "] " + getName();
  }
}
