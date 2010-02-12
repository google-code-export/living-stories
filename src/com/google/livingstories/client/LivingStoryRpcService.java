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

import java.util.List;
import java.util.Map;

/**
 * RPC service for saving and retrieving living stories from the datastore.
 */
@RemoteServiceRelativePath("livingstoryservice")
public interface LivingStoryRpcService extends RemoteService {
  
  LivingStory createLivingStory(String url, String title);

  List<LivingStory> getAllLivingStories(boolean onlyPublished);
  
  List<LivingStory> getLivingStoriesForContentManager();
  
  LivingStory getLivingStoryById(long id, boolean allSummaryRevisions);

  LivingStory getLivingStoryByUrl(String url);
  
  LivingStory saveLivingStory(long id, String url, String title, Publisher publisher, 
      PublishState publishState, String summary);
  
  void deleteLivingStory(long id);
  
  // Theme management functions. Themes are sufficiently tied to living stories that
  // their implementation belongs on the same service.
  List<Theme> getThemesForLivingStory(long livingStoryId);
  
  Map<Long, AtomTypesBundle> getThemeInfoForLivingStory(long livingStoryId);
  
  Theme getThemeById(long id);
  
  Theme saveTheme(Theme theme);
  
  void deleteTheme(long id);
  
  StartPageBundle getStartPageBundle();
}
