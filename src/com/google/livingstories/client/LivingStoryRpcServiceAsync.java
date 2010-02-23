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

import java.util.List;
import java.util.Map;

/**
 * Async version of {@link LivingStoryRpcService}.
 */
public interface LivingStoryRpcServiceAsync {

  void createLivingStory(String url, String title, AsyncCallback<LivingStory> callback);

  void getAllLivingStories(boolean onlyPublished, AsyncCallback<List<LivingStory>> callback);
  
  void getLivingStoriesForContentManager(AsyncCallback<List<LivingStory>> callback);
  
  void getLivingStoryById(long id, boolean allSummaryRevisions,
      AsyncCallback<LivingStory> callback);
  
  void getLivingStoryByUrl(String url, AsyncCallback<LivingStory> callback);
  
  void saveLivingStory(long id, String url, String title, PublishState publishState, String summary,
      AsyncCallback<LivingStory> callback);
  
  void deleteLivingStory(long id, AsyncCallback<Void> callback);
 
  void getThemesForLivingStory(long livingStoryId, AsyncCallback<List<Theme>> callback);

  void getThemeInfoForLivingStory(long livingStoryId,
      AsyncCallback<Map<Long, ContentItemTypesBundle>> callback);
  
  void getThemeById(long id, AsyncCallback<Theme> callback);
  
  void saveTheme(Theme theme, AsyncCallback<Theme> callback);
  
  void deleteTheme(long id, AsyncCallback<Void> callback);
  
  void getStartPageBundle(AsyncCallback<StartPageBundle> callback);
}
