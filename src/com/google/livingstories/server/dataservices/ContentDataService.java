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

import com.google.livingstories.client.ContentItemType;
import com.google.livingstories.client.BaseContentItem;
import com.google.livingstories.client.Importance;
import com.google.livingstories.client.PublishState;

import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Interface to create, modify and retrieve content for living stories from the datastore. The
 * 'content' here refers to any kind of content created for the story such as articles, images,
 * videos, quotes, maps, editorials, etc.
 */
public interface ContentDataService {
  
  /**
   * Persist a content object to the datastore. It can either be new, in which case the id will be
   * null, or an existing content, in which case the fields of the object with the given id should
   * be updated.
   * @param baseContent the content entity to save. It can have various optional properties
   * depending on the type of content.
   */
  BaseContentItem save(BaseContentItem baseContent);
  
  /**
   * Delete a content object from the datastore.
   * @param id database id of the content object to delete
   */
  void delete(Long id);
  
  /**
   * Delete all the content objects for a given living story.
   * @param livingStoryId database id of the living story for which all content should be deleted
   */
  void deleteContentForLivingStory(Long livingStoryId);
  
  /**
   * Remove a theme from every content object that it appears in.
   * @param themeId database id of the theme to remove from all content objects.
   */
  void removeTheme(Long themeId);
  
  /**
   * Fetch a content object from the datastore based on its id.
   * @param id database id of the content to fetch
   * @param populateLinkedEntities if true, all the other content entities linked to the fetched
   * entity should also be fetched and populated. If false, only the ids of the linked entities
   * need to be returned.  
   */
  BaseContentItem retrieveById(Long id, boolean populateLinkedEntities);
  
  /**
   * Fetch all content objects that belong to a living story from the datastore. 
   * @param livingStoryId database id of a living story
   * @param publishState if set, only content objects which are either 'published' or 'draft'
   * should be returned. If null, all content objects belonging to the living story should be
   * returned.
   */
  List<BaseContentItem> retrieveByLivingStory(Long livingStoryId, PublishState publishState);
  
  /**
   * Fetch a list of content objects given a list of their ids.
   * @param ids list of database ids of content entities
   */
  List<BaseContentItem> retrieveByIds(Collection<Long> ids);
  
  /**
   * Retrieve all the published content objects that link to the content object with the given id.
   * @param entityId id of the content object that each of the returned objects should link to
   */
  List<BaseContentItem> retrieveEntitiesThatLinkTo(Long entityId);
  
  /**
   * Retrieve all the published content objects that have been contributed by the object with the
   * given id.
   * @param contributorId database id of the content object that should be present in the
   * contributorIds field of all the return content objects
   */
  List<BaseContentItem> retrieveEntitiesContributedBy(Long contributorId);
  
  /**
   * Retrieve all content objects from the datastore that satisfy the given search parameters. If
   * any of the parameters is null, it should be ignored and the results should be based on the
   * others.
   * @param livingStoryId database id of the living story that the content object should belong to
   * @param contentItemType type of content object such as "Event", "Narrative", "Asset", etc.
   * @param afterDate the returned content objects should have their last modified time after this
   * time
   * @param beforeDate the returned content objects should have their last modified time before
   * this time 
   * @param importance the importance level of the returned content objects eg. High, Medium, etc.
   * @param publishState whether the returned objects should be in 'published' or 'draft' state
   */
  List<BaseContentItem> search(Long livingStoryId, ContentItemType contentItemType, Date afterDate,
      Date beforeDate, Importance importance, PublishState publishState);
  
  /**
   * Return the number of published content objects that have been changed since the given time. 
   * Restrict to content objects of a particular story and type if those parameters have been 
   * provided.
   * @param livingStoryId database id of the living story whose content objects to count. Should
   * be non-null.
   * @param entityType type of content objects to count. Should be non-null.
   * @param afterDate date after which the objects should have been modified. Should be non-null.
   * @throws IllegalArgumentException thrown if any of the arguments are null
   */
  Integer getNumberOfEntitiesUpdatedSinceTime(Long livingStoryId, ContentItemType entityType, 
      Date afterDate) throws IllegalArgumentException;
}
