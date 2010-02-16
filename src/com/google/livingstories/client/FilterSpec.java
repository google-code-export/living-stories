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
 * Class for storing the state of the various filter selections to fetch content items from the
 * server. 
 */
public class FilterSpec implements Serializable {
  public boolean importantOnly = false;
  public boolean oldestFirst = false;
  public boolean opinion = false;  // filter for 'Editorial' and 'Op-Ed' narratives.
                                   // Meaningful only if contentItemType is NARRATIVE
  public ContentItemType contentItemType;     // null means filter to "display"-mode content items
  public AssetType assetType;           // filter on asset type. Used if contentItemType is ASSET
  public Long themeId = null;                  // 0 means no filter
  
  // Filters that will appear in isolation from the above filter, generally speaking:
  public Long contributorId = null;
  public Long playerId = null;
  
  public FilterSpec() {}
  
  public FilterSpec(String filterParams) {
    String[] params = filterParams.split(",");
    importantOnly = Boolean.valueOf(params[0]);
    oldestFirst = Boolean.valueOf(params[1]);
    opinion = Boolean.valueOf(params[2]);
    contentItemType = params[3].equals("n") ? null : ContentItemType.valueOf(params[3]);
    assetType = params[4].equals("n") ? null : AssetType.valueOf(params[4]);
    themeId = params[5].equals("n") ? null : Long.valueOf(params[5]);
  }
  
  public FilterSpec(FilterSpec other) {
    importantOnly = other.importantOnly;
    oldestFirst = other.oldestFirst;
    opinion = other.opinion;
    contentItemType = other.contentItemType;
    assetType = other.assetType;
    themeId = other.themeId;
    contributorId = other.contributorId;
    playerId = other.playerId;
  }
  
  /**
   * Used to get a string representation of the filter, for the history manager. Doesn't,
   * at present, put anything in the string for the contributorId and linkedContentItemId
   * parameters, which are put into the history via another method.
   * @return the string to put into the URL
   */
  public String getFilterParams() {
    return importantOnly + "," + oldestFirst + "," + opinion + "," +
        (contentItemType == null ? "n" : contentItemType.name()) + "," +
        (assetType == null ? "n" : assetType.name()) + "," +
        (themeId == null ? "n" : themeId);
  }
  
  /*
   * a string representation of the filter that includes information on contributorId
   * and playerId. Used by the server-side Caches implementation.
   */
  public String getMapKeyString() {
    return getFilterParams() + "," + contributorId + "," + playerId;
  }
  
  public boolean doesContentItemMatch(BaseContentItem contentItem) {
    ContentItemType specificType = contentItem.getContentItemType();

    if (specificType == ContentItemType.BACKGROUND
        || specificType == ContentItemType.REACTION) {
      return false;
    } else {
      // check each relevant condition in turn.
      return (contentItemType == null
          ? contentItem.displayTopLevel() : contentItemType == specificType)
          && ((specificType != ContentItemType.ASSET || assetType == null
             || matchesAssetType((AssetContentItem) contentItem)))
          && (contentItemType != ContentItemType.NARRATIVE
              || matchesOpinion((NarrativeContentItem)contentItem))
          && (contentItemType != ContentItemType.PLAYER
              || matchesPlayerType((PlayerContentItem)contentItem))
          && (themeId == null || contentItem.getThemeIds().contains(themeId))
          && (importantOnly == false || contentItem.getImportance() == Importance.HIGH);
    }
  }
  
  private boolean matchesOpinion(NarrativeContentItem narrativeContentItem) {
    return opinion == narrativeContentItem.isOpinion();
  }
  
  private boolean matchesAssetType(AssetContentItem assetContentItem) {
    AssetType specificAssetType = assetContentItem.getAssetType();
    // We want to group links/resources and documents together in the same filter. So assets
    // of both types should match if the asset type in the current FilterSpec is "LINK"
    if (assetType == AssetType.LINK) {
      return specificAssetType == AssetType.LINK || specificAssetType == AssetType.DOCUMENT;
    } else {
      return assetType == specificAssetType;
    }
  }
  
  private boolean matchesPlayerType(PlayerContentItem playerContentItem) {
    PlayerType playerType = playerContentItem.getPlayerType();
    return playerType == PlayerType.PERSON || playerType == PlayerType.ORGANIZATION;
  }

  /** Generated by eclipse */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((themeId == null) ? 0 : themeId.hashCode());
    result = prime * result + ((assetType == null) ? 0 : assetType.hashCode());
    result = prime * result + ((contentItemType == null) ? 0 : contentItemType.hashCode());
    result = prime * result + ((contributorId == null) ? 0 : contributorId.hashCode());
    result = prime * result + (importantOnly ? 1231 : 1237);
    result = prime * result + ((playerId == null) ? 0 : playerId.hashCode());
    result = prime * result + (oldestFirst ? 1231 : 1237);
    result = prime * result + (opinion ? 1231 : 1237);
    return result;
  }

  /** Generated by eclipse (with minor shortcuts added -- can directly compare enum values) */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    FilterSpec other = (FilterSpec) obj;
    if (themeId == null) {
      if (other.themeId != null) return false;
    } else if (!themeId.equals(other.themeId)) return false;
    if (assetType != other.assetType) return false;
    if (contentItemType != other.contentItemType) return false;
    if (contributorId == null) {
      if (other.contributorId != null) return false;
    } else if (!contributorId.equals(other.contributorId)) return false;
    if (importantOnly != other.importantOnly) return false;
    if (playerId == null) {
      if (other.playerId != null) return false;
    } else if (!playerId.equals(other.playerId)) return false;
    if (oldestFirst != other.oldestFirst) return false;
    if (opinion != other.opinion) return false;
    return true;
  }
  
  /**
   * Variant of equals, for determining if one filter is the reverse of another:
   */
  public boolean isReverseOf(FilterSpec other) {
    if (other == null || this == other) {
      return false;
    }
    FilterSpec otherCopy = new FilterSpec(other);
    otherCopy.oldestFirst = !otherCopy.oldestFirst;
    return equals(otherCopy);
  }
}
