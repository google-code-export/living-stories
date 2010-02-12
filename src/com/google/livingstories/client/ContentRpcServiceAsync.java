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
  void createOrChangeAtom(BaseAtom clientAtom, AsyncCallback<BaseAtom> callback);
  
  void getAtomsForLivingStory(Long livingStoryId, boolean onlyPublished, 
      AsyncCallback<List<BaseAtom>> callback);
  
  void getAtom(Long id, boolean getLinkedAtoms, AsyncCallback<BaseAtom> callback);
  
  void getAtoms(Collection<Long> ids, AsyncCallback<List<BaseAtom>> callback);
  
  void getUnassignedPlayers(AsyncCallback<List<PlayerAtom>> callback);

  void getRelatedAtoms(Long atomId, boolean byContribution, Date cutoff,
      AsyncCallback<DisplayAtomBundle> callback);
  
  void executeSearch(SearchTerms searchTerms, AsyncCallback<List<BaseAtom>> callback);
  
  void deleteAtom(Long id, AsyncCallback<Void> callback);
  
  void getUpdateCountSinceTime(Long livingStoryId, Date time, AsyncCallback<Integer> callback);
  
  void getUpdatesSinceTime(Long livingStoryId, Date time, AsyncCallback<List<BaseAtom>> callback);

  void getDisplayAtomBundle(Long livingStoryId, FilterSpec filterSpec, Long focusedAtomId,
      Date cutoff, AsyncCallback<DisplayAtomBundle> callback);
  
  void getImportantEventsForLivingStory(Long livingStoryId,
      AsyncCallback<List<EventAtom>> callback);

  void getImportantPlayersForLivingStory(Long livingStoryId,
      AsyncCallback<List<PlayerAtom>> callback);
  
  void getContributorsByIdForLivingStory(Long livingStoryId,
      AsyncCallback<Map<Long, PlayerAtom>> callback);
}
