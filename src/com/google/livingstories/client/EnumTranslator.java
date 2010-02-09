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
 * Handles client-side translation of enum values and enum-related fields
 */
public class EnumTranslator {
  private static ClientConstants consts = ClientMessageHolder.consts;
  private static ClientMessages msgs = ClientMessageHolder.msgs;
  
  public static String translate(AtomType atomType, int count) {
    switch (atomType) {
      case EVENT:
        return msgs.contentDisplayNameEvents(count);
      case PLAYER:
        return msgs.contentDisplayNamePlayers(count);
      case QUOTE:
        return msgs.contentDisplayNameQuotes(count);
      case BACKGROUND:
        return msgs.contentDisplayNameBackground(count);
      case DATA:
        return msgs.contentDisplayNameData(count);
      case ASSET:
        return msgs.contentDisplayNameAssets(count);
      case NARRATIVE:
        return msgs.contentDisplayNameNarratives(count);
      case REACTION:
        return msgs.contentDisplayNameReactions(count);
      default:
        assert false;
        return null;  
    }
  }

  public static String getNavLinkOverride(AtomType atomType) {
    switch (atomType) {
      case EVENT:
        return consts.contentNavLinkStringEvents();
      case PLAYER:
        return consts.contentNavLinkStringPlayers();
      case DATA:
        return consts.contentNavLinkStringData();
      case ASSET:	
        return consts.contentNavLinkStringAssets();
      case REACTION:
        return consts.contentNavLinkStringReactions();
      case QUOTE: case BACKGROUND: case NARRATIVE:   // these types aren't navlinked
      default:
        assert false;
        return null;  
    }
  }
  
  public static String getFilterStringOverride(AtomType atomType) {
    switch (atomType) {
      case EVENT:
        return consts.contentFilterStringEvents();
      case PLAYER:
        return consts.contentFilterStringPlayers();
      case QUOTE:
        return consts.contentFilterStringQuotes();
      case DATA:
        return consts.contentFilterStringData();
      case NARRATIVE:
        return consts.contentFilterStringNarratives();
      case BACKGROUND: case ASSET: case REACTION:   // these types aren't in the filter list
      default:
        assert false;
        return null;  
    }
  }
  
  public static String translate(AssetType assetType, int count) {
    switch (assetType) {
      case LINK:
        return msgs.assetDisplayNameLinks(count);
      case IMAGE:
        return msgs.assetDisplayNameImage(count);
      case VIDEO:
        return msgs.assetDisplayNameVideo(count);
      case AUDIO:
        return msgs.assetDisplayNameAudio(count);
      case INTERACTIVE:
        return msgs.assetDisplayNameInteractive(count);
      case DOCUMENT:
        return msgs.assetDisplayNameDocument(count);
      default:
        assert false;
        return null;
    }
  }
  
  public static String getNavLinkOverride(AssetType assetType) {
    switch (assetType) {
      case LINK:
        return consts.assetNavLinkStringLinks();
      case IMAGE:
        return consts.assetNavLinkStringImages();
      case VIDEO:
        return consts.assetNavLinkStringVideos();
      case AUDIO:
        return consts.assetNavLinkStringAudio();
      case INTERACTIVE:
        return consts.assetNavLinkStringInteractives();
      case DOCUMENT:
        return consts.assetNavLinkStringDocuments();
      default:
        assert false;
        return null;
    }
  }
  
  public static String translate(Importance importance) {
    switch (importance) {
      case HIGH:
        return consts.importanceHigh();
      case MEDIUM:
        return consts.importanceMedium();
      case LOW:
        return consts.importanceLow();
      default:
        assert false;
        return null;
    }
  }
  
  public static String translate(NarrativeType narrativeType, int count) {
    switch (narrativeType) {
      case FEATURE:
        return msgs.narrativeDisplayNameFeatures(count);
      case ANALYSIS:
        return msgs.narrativeDisplayNameAnalysis(count);
      case INVESTIGATION:
        return msgs.narrativeDisplayNameInvestigations(count);
      case PROFILE:
        return msgs.narrativeDisplayNameProfiles(count);
      case EDITORIAL:
        return msgs.narrativeDisplayNameEditorials(count);
      case OP_ED:
        return msgs.narrativeDisplayNameOpEd(count);
      case LETTER_TO_THE_EDITOR:
        return msgs.narrativeDisplayNameLetters(count);
      case REVIEW:
        return msgs.narrativeDisplayNameReviews(count);
      case COLUMN:
        return msgs.narrativeDisplayNameColumns(count);
      case OP_ED_COLUMN:
        return msgs.narrativeDisplayNameOpEdColumns(count);
      default:
        assert false;
        return null;
    }
  }
  
  public static String translate(PlayerType playerType) {
    switch (playerType) {
      case PERSON:
        return consts.playerDisplayNamePerson();
      case ORGANIZATION:
        return consts.playerDisplayNameOrganization();
      default:
        assert false;
        return null;
    }
  }
  
  public static String defaultOrOverride(String defaultString, String override) {
    return override.equals("") ? defaultString : override;
  }
}
