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

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import com.google.livingstories.client.contentmanager.SearchTerms;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * RPC service for saving and retrieving content items from the datastore.
 */
@RemoteServiceRelativePath("contentservice")
public interface ContentRpcService extends RemoteService {
  BaseContentItem createOrChangeContentItem(BaseContentItem contentItem);
  
  List<BaseContentItem> getContentItemsForLivingStory(Long livingStoryId, boolean onlyPublished);
  
  BaseContentItem getContentItem(Long id, boolean getLinkedContentItems);
  
  List<BaseContentItem> getContentItems(Collection<Long> ids);
  
  List<PlayerContentItem> getUnassignedPlayers();

  DisplayContentItemBundle getRelatedContentItems(Long contentItemId, boolean byContribution,
      Date cutoff);
  
  List<BaseContentItem> executeSearch(SearchTerms searchTerms);
  
  void deleteContentItem(Long id);
  
  Integer getUpdateCountSinceTime(Long livingStoryId, Date time);
  
  List<BaseContentItem> getUpdatesSinceTime(Long livingStoryId, Date time);

  DisplayContentItemBundle getDisplayContentItemBundle(Long livingStoryId, FilterSpec filterSpec,
      Long focusedContentItemId, Date cutoff);  
  
  List<EventContentItem> getImportantEventsForLivingStory(Long livingStoryId);
  
  List<PlayerContentItem> getImportantPlayersForLivingStory(Long livingStoryId);
  
  Map<Long, PlayerContentItem> getContributorsByIdForLivingStory(Long livingStoryId);
}
