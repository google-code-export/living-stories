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

package com.google.livingstories.server.dataservices;

import com.google.livingstories.client.LivingStory;
import com.google.livingstories.client.PublishState;

import java.util.List;

/**
 * Interface to create, modify and retrieve Living Story objects from the datastore.
 */
public interface LivingStoryDataService {
  
  /**
   * Persist a living story object to the datastore. It can either be a new object, in which case
   * the id will be null, or it can be an existing story object, in which case its fields should be
   * updated with the new values.
   * @param id database id of the story to be updated. Null if this story hasn't been persisted yet.
   * @param urlName short name for the story used in the URL which acts as a unique identifier.
   * @param title full title of the story
   * @param publishState whether this story should be shown to the user or if it's still draft
   * @param summary The HTML summary of the story. All revisions of the summary should be saved.
   * The given summary will be added as the latest revision for the story.
   * @throws IllegalArgumentException thrown if the id is null (i.e. a new story is being created)
   * but another story already exists in the database with the provided urlName
   */
  LivingStory save(Long id, String urlName, String title, PublishState publishState,
      String summary) throws IllegalArgumentException;
  
  /**
   * Delete a living story from the database.
   * @param id database id of the story to be deleted.
   */
  void delete(Long id);
  
  /**
   * Fetch a living story object from the datastore given its id. Returns null if a living story
   * with the given id is not found.
   * @param id database id of the living story
   * @param latestRevisionsOnly if true, only the last few revisions (upto a maximum of 5) of the
   * summary will be returned with the living story. If false, all the revisions of the summary will
   * be returned.
   */
  LivingStory retrieveById(Long id, boolean latestRevisionsOnly);
  
  /**
   * Fetch a living story object from the datastore given its URL name. This is a short name for 
   * the story used in the URL which acts as a unique identifier. Returns null if a living story
   * with the given url is not found.
   * @param urlName the story name used in the URL
   * @param latestRevisionsOnly if true, only the last few revisions (upto a maximum of 5) of the
   * summary will be returned with the living story. If false, all the revisions of the summary will
   * be returned.
   */
  LivingStory retrieveByUrlName(String urlName, boolean latestRevisionsOnly);
  
  /**
   * Fetch all the living stories from the datastore in a given publish state.
   * @param publishState whether the returned living stories should be 'published' or 'draft'. If
   * null, all living stories should be returned regardless of their publish state.
   * @param latestRevisionsOnly if true, only the last few revisions (upto a maximum of 5) of the
   * summary will be returned with each living story. If false, all the revisions of the summary
   * will be returned with every story.
   */
  List<LivingStory> retrieveAll(PublishState publishState, boolean latestRevisionsOnly);
}
