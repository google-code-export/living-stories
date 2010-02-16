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
 * Client-side version of a player atom, which represents a person or an organization, not 
 * specific to a story.
 */
public class PlayerAtom extends BaseAtom {
  protected String name;
  protected List<String> aliases;
  protected PlayerType playerType;
  protected AssetAtom photoAtom;

  public PlayerAtom() {}
  
  public PlayerAtom(Long id, Date timestamp, Set<Long> contributorIds, String content,
      Importance importance, String name, List<String> aliases, PlayerType playerType, 
      AssetAtom photoAtom) {
    super(id, timestamp, AtomType.PLAYER, contributorIds, content, importance, null);
    this.name = name;
    this.aliases = (aliases == null ? new ArrayList<String>() : new ArrayList<String>(aliases));
    this.playerType = playerType;
    this.photoAtom = photoAtom;
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
    return photoAtom != null;
  }
  
  public Long getPhotoAtomId() {
    return photoAtom == null ? null : photoAtom.getId();
  }
  
  public AssetAtom getPhotoAtom() {
    return photoAtom;
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
