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
import com.google.livingstories.client.util.GlobalUtil;

import java.util.Date;
import java.util.Set;

/**
 * Represents a player in a story. The 'content' field of this content item is used for a
 * description of the player's role in this particular story, not his/her general bio/background.
 * The item refers to the general player content item which contains the general information about
 * the person or organization.
 */
public class StoryPlayerContentItem extends PlayerContentItem {
  private PlayerContentItem parentPlayerContentItem;
  
  public StoryPlayerContentItem() {}
  
  public StoryPlayerContentItem(Long id, Date timestamp, Set<Long> contributorIds, 
      String storySpecificDescription, Importance importance, Long livingStoryId, 
      PlayerContentItem parentPlayerContentItem) {
    super(id, timestamp, contributorIds, storySpecificDescription, importance, 
        parentPlayerContentItem.getName(), parentPlayerContentItem.getAliases(),
        parentPlayerContentItem.getPlayerType(), parentPlayerContentItem.getPhotoContentItem());
    this.parentPlayerContentItem = parentPlayerContentItem;
    setLivingStoryId(livingStoryId);
  }
  
  public PlayerContentItem getParentPlayerContentItem() {
    return parentPlayerContentItem;
  }
  
  @Override
  public String getPreviewContentToRender() {
    String storySpecificDescription = getContent();
    if (GlobalUtil.isContentEmpty(storySpecificDescription)) {
      return parentPlayerContentItem.getPreviewContentToRender();
    } else {
      return storySpecificDescription.split(Constants.BREAK_TAG)[0];
    }
  }
  
  @Override
  public String getFullContentToRender() {
    String storySpecificDescription = getContent();
    if (GlobalUtil.isContentEmpty(storySpecificDescription)) {
      return parentPlayerContentItem.getFullContentToRender();
    } else {
      return storySpecificDescription + "<br/><br/>"
          + parentPlayerContentItem.getFullContentToRender();
    }
  }
}
