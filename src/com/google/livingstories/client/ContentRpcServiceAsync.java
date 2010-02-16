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

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.livingstories.client.contentmanager.SearchTerms;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Async version of {@link ContentRpcService}.
 */
public interface ContentRpcServiceAsync {
  void createOrChangeContentItem(BaseContentItem contentItem,
      AsyncCallback<BaseContentItem> callback);
  
  void getContentItemsForLivingStory(Long livingStoryId, boolean onlyPublished, 
      AsyncCallback<List<BaseContentItem>> callback);
  
  void getContentItem(Long id, boolean getLinkedContentItems,
      AsyncCallback<BaseContentItem> callback);
  
  void getContentItems(Collection<Long> ids, AsyncCallback<List<BaseContentItem>> callback);
  
  void getUnassignedPlayers(AsyncCallback<List<PlayerContentItem>> callback);

  void getRelatedContentItems(Long contentItemId, boolean byContribution, Date cutoff,
      AsyncCallback<DisplayContentItemBundle> callback);
  
  void executeSearch(SearchTerms searchTerms, AsyncCallback<List<BaseContentItem>> callback);
  
  void deleteContentItem(Long id, AsyncCallback<Void> callback);
  
  void getUpdateCountSinceTime(Long livingStoryId, Date time, AsyncCallback<Integer> callback);
  
  void getUpdatesSinceTime(Long livingStoryId, Date time, AsyncCallback<List<BaseContentItem>> callback);

  void getDisplayContentItemBundle(Long livingStoryId, FilterSpec filterSpec,
      Long focusedContentItemId, Date cutoff, AsyncCallback<DisplayContentItemBundle> callback);
  
  void getImportantEventsForLivingStory(Long livingStoryId,
      AsyncCallback<List<EventContentItem>> callback);

  void getImportantPlayersForLivingStory(Long livingStoryId,
      AsyncCallback<List<PlayerContentItem>> callback);
  
  void getContributorsByIdForLivingStory(Long livingStoryId,
      AsyncCallback<Map<Long, PlayerContentItem>> callback);

}
