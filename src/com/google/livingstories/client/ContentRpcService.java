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
 * RPC service for saving and retrieving atoms from the datastore.
 */
@RemoteServiceRelativePath("atomservice")
public interface ContentRpcService extends RemoteService {
  BaseAtom createOrChangeAtom(BaseAtom clientAtom);
  
  List<BaseAtom> getAtomsForLivingStory(Long livingStoryId, boolean onlyPublished);
  
  BaseAtom getAtom(Long id, boolean getLinkedAtoms);
  
  List<BaseAtom> getAtoms(Collection<Long> ids);
  
  List<PlayerAtom> getUnassignedPlayers();

  DisplayAtomBundle getRelatedAtoms(Long atomId, boolean byContribution, Date cutoff);
  
  List<BaseAtom> executeSearch(SearchTerms searchTerms);
  
  void deleteAtom(Long id);
  
  Integer getUpdateCountSinceTime(Long livingStoryId, Date time);
  
  List<BaseAtom> getUpdatesSinceTime(Long livingStoryId, Date time);

  DisplayAtomBundle getDisplayAtomBundle(Long livingStoryId, FilterSpec filterSpec,
      Long focusedAtomId, Date cutoff);  
  
  List<EventAtom> getImportantEventsForLivingStory(Long livingStoryId);
  
  List<PlayerAtom> getImportantPlayersForLivingStory(Long livingStoryId);
  
  Map<Long, PlayerAtom> getContributorsByIdForLivingStory(Long livingStoryId);
}
