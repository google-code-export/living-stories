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

package com.google.livingstories.server.rpcimpl;

import com.google.appengine.repackaged.com.google.common.base.Pair;
import com.google.appengine.repackaged.com.google.common.collect.Lists;
import com.google.appengine.repackaged.com.google.common.collect.Sets;
import com.google.livingstories.client.AssetType;
import com.google.livingstories.client.ContentItemType;
import com.google.livingstories.client.BackgroundContentItem;
import com.google.livingstories.client.PlayerContentItem;
import com.google.livingstories.client.PlayerType;
import com.google.livingstories.server.dataservices.entities.BaseContentEntity;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for scanning the text of a content item's main content and replacing the first
 * occurrence of a player's name or alias with an <a href="javascript:showContentItemPopup()"> tag
 * so that appropriate popup links will show on the LSP. It's more efficient to do this at the time
 * of saving the content in the content manager than on-the-fly while rendering on the LSP.
 * New as of 12/16/2009: whenever a new showContentItemPopup() link is added, the corresponding
 * player will also be added to the linked content entities of contentEntity.
 * New as of 1/15/2010: background content items with a name i.e. concepts are also auto-linked in
 * the content. They are not returned in the suggestions.
 */
public class AutoLinkEntitiesInContent {
  
  /**
   * Looks for matches of the names and aliases of each of the given players in the content entity.
   * If a match is found and a corresponding link is not found around it, creates 1 per
   * player. Also adds these players to the base content entity as new linked content entities.
   * Also looks for matches of the names of concepts and creates a link if one doesn't exist. 
   */
  public static Set<Long> createLinks(BaseContentEntity contentEntity, 
      List<PlayerContentItem> playerContentItems, List<BackgroundContentItem> concepts) {
    Set<Long> suggestedAdditionIds = Sets.newHashSet();
    ContentItemType contentItemType = contentEntity.getContentItemType();
    Long contentEntityId = contentEntity.getId();
    if (contentItemType != ContentItemType.ASSET && contentItemType != ContentItemType.PLAYER) {
      MatchResult matchResult = match(contentEntityId, contentEntity.getContent(),
          playerContentItems, concepts);
      // If matches were found, set the current content string with the new one that contains
      // <contentItem> tags
      if (matchResult.matchesFound) {
        contentEntity.setContent(matchResult.newContent);
      }
      suggestedAdditionIds.addAll(matchResult.suggestedPlayerIds);
    }
    
    // Do the same for event summary
    if (contentItemType == ContentItemType.EVENT) {
      MatchResult matchResult = match(contentEntityId, contentEntity.getEventSummary(),
          playerContentItems, concepts);
      if (matchResult.matchesFound) {
        contentEntity.setEventSummary(matchResult.newContent);
      }
      suggestedAdditionIds.addAll(matchResult.suggestedPlayerIds);
    }
    
    // For narrative summary: (some need for refactoring here!)
    if (contentItemType == ContentItemType.NARRATIVE) {
      MatchResult matchResult = match(contentEntityId, contentEntity.getNarrativeSummary(),
          playerContentItems, concepts);
      if (matchResult.matchesFound) {
        contentEntity.setNarrativeSummary(matchResult.newContent);
      }
      suggestedAdditionIds.addAll(matchResult.suggestedPlayerIds);
    }
    
    // For asset caption, if applicable: (again, refactoring would be very good...)
    if (contentItemType == ContentItemType.ASSET
        && contentEntity.getAssetType() != AssetType.LINK) {
      // Send an empty concept list here because we are only looking for player names to suggest
      MatchResult matchResult = match(contentEntityId, contentEntity.getCaption(),
          playerContentItems, Lists.<BackgroundContentItem>newArrayList());
      // We _don't_ reset the caption, which is just plain text, not HTML.
      suggestedAdditionIds.addAll(matchResult.suggestedPlayerIds);
    }
    return suggestedAdditionIds;
  }
  
  
  private static MatchResult match(Long contentEntityId, String content,
      List<PlayerContentItem> playerContentItems, List<BackgroundContentItem> concepts) {
    MatchResult matchResult = new MatchResult();
    // We need to remove the newline characters \n from the string so we can look for matches
    content = content.replaceAll("\\s+", " ");
    
    // First find the concept matches and construct the result partially
    for (BackgroundContentItem concept : concepts) {
      Long conceptId = concept.getId();
      String javascript = "showContentItemPopup(" + conceptId + ", this)";
      if (!conceptId.equals(contentEntityId) && !content.contains(javascript)) {
        String conceptName = concept.getConceptName();
        Pair<Boolean, String> result = matchAndReplace(content, conceptName, javascript);
        if (result.first) {
          matchResult.matchesFound = true;
          content = result.second;
        }
      }
    }

    // Then look for the player matches
    Set<Long> suggestedPlayerIds = Sets.newHashSet();
    for (PlayerContentItem playerContentItem : playerContentItems) {
      Long playerId = playerContentItem.getId();
      String javascript = "showContentItemPopup(" + playerId + ", this)";      
      if (content.contains(javascript)) {
        // If a showContentItemPopup() link for a player is already there, we should consistently
        // and repeatedly suggest that the player content item be linked as well. Note that it's no
        // problem if the suggestion duplicates a content item that has already really been linked
        // up; the frontend treats this as a sane, expected case.
        suggestedPlayerIds.add(playerId);
      } else {
        String playerName = playerContentItem.getName();
        // If a link to that player doesn't already exist, first look for the player's full
        // name in the content
        Pair<Boolean, String> result = matchAndReplace(content, playerName, javascript);
        // If that doesn't exist, look for the aliases
        Iterator<String> aliasIterator = playerContentItem.getAliases().iterator();
        while (!result.first && aliasIterator.hasNext()) {
          result = matchAndReplace(content, aliasIterator.next(), javascript);
        }
        if (!result.first && playerContentItem.getPlayerType() == PlayerType.PERSON) {
          // If the full name or aliases don't exist, just look for the last part of the name
          // for people (but not for organizations because the last words in their names are often
          // common words such as "Group" or "Association")
          String[] playerNameParts = playerName.split("\\s");
          if (playerNameParts.length > 1) {
            result = matchAndReplace(content, playerNameParts[playerNameParts.length - 1], 
                javascript);
          }
        }
        // Note: the order in which the matches are looked for above can lead to a corner case
        // in which the alias is mentioned first in the text and the full name is mentioned later.
        // The full name will be linked later on in the text, instead of the alias being linked.
        // This is acceptable because in writing, they usually put the full name in the
        // first occurrence followed by shortened versions.
        
        if (result.first) {
          matchResult.matchesFound = true;
          suggestedPlayerIds.add(playerId);
          content = result.second;
        }
      }
    }
    matchResult.newContent = content;
    matchResult.suggestedPlayerIds = suggestedPlayerIds;
    return matchResult;
  }

  private static Pair<Boolean, String> matchAndReplace(String content, String playerName, 
      String javascript) {
    // a long-standing bug in the Content manager means that players that originally had
    // the empty string entered for their aliases actually are saved in the datastore as having
    // one alias, "". We avoid paying attention to this as follows (which should catch some
    // other cases too).
    if (playerName.trim().isEmpty()) {
      return Pair.of(false, content);
    }
    
    Matcher matcher = Pattern.compile("(\\b|\\W)(" + Pattern.quote(playerName) + ")(\\b|\\W)")
        .matcher(content);
    boolean foundMatch = false;
    StringBuffer sb = new StringBuffer();
    if (matcher.find()) {
      foundMatch = true;
      String match = matcher.group();
      String contentItemLink = "<a href=\"javascript:;\" onclick=\"" +
          javascript + "\">" + matcher.group(2) + "</a>";
      match = match.replace(playerName, contentItemLink);
      matcher.appendReplacement(sb, Matcher.quoteReplacement(match));
    }
    matcher.appendTail(sb);
    return Pair.of(foundMatch, sb.toString());
  }
  
  private static class MatchResult {
    public boolean matchesFound = false;
    public String newContent;
    public Set<Long> suggestedPlayerIds;
  }
}
