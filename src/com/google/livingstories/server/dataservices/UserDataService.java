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

import com.google.livingstories.client.FilterSpec;

import java.util.Date;
import java.util.Map;

/**
 * Interface for getting information about users and saving their preferences.
 */
public interface UserDataService {
  
  /**
   * Return the last time the given user visited the given living story. If no data exists for 
   * this user and this story, return null.
   * @param userId identifier for a user
   * @param livingStoryId database id of a living story
   */
  Date getLastVisitTimeForStory(String userId, Long livingStoryId);
  
  /**
   * Return the last visit times for all the living stories that the given user has visited.
   * @param userId identifier for the user
   * @return map from the living story database id to the last visited time for that story. The
   * return map will not necessarily contain all the living stories that exist, but only the 
   * ones that the user has visited.
   */
  Map<Long, Date> getAllLastVisitTimes(String userId);
  
  /**
   * Return true if the given user has subscribed to email alerts for the given
   * living story.
   * @param userId identifier for a user
   * @param livingStoryId database id of a living story
   */
  boolean isUserSubscribedToEmails(String userId, Long livingStoryId);
  
  /**
   * Return the default story view preference for the given user. Users can choose
   * a set of filters that should be applied to every story by default when they visit it. Returns
   * null if the user doesn't have a preference set.
   * @param userId identifier for a user
   */
  FilterSpec getDefaultStoryView(String userId);
  
  /**
   * Return the number of times the given user has visited the given story.
   * @param userId identifier for a user
   * @param livingStoryId database id of a living story
   */
  int getVisitCountForStory(String userId, Long livingStoryId);

  /**
   * Update the last visited time for the given story for the given user. The time
   * should be updated to the current time. And also update the visit count for the story.
   * @param userId identifier for a user
   * @param livingStoryId database id of the living story
   */
  void updateVisitDataForStory(String userId, Long livingStoryId);
  
  /**
   * Set the email alerts subscription for the given user for the given living story
   * to the provided value.
   * @param userId identifier for a user
   * @param livingStoryId database id of the a living story
   * @param subscribe whether the subscription should be turned on or off
   */
  void setEmailSubscription(String userId, Long livingStoryId, boolean subscribe, String localeId);
  
  /**
   * Set the default story view preference for the given user to the provided value.
   * @param userId identifier for a user
   * @param defaultView an encoding of the filter values
   */
  void setDefaultStoryView(String userId, FilterSpec defaultView);
  
  /**
   * Delete all the last visited times saved for users for a particular living story.
   * @param livingStoryId database id of the living story for which to delete the last visited 
   * times for all users
   */
  void deleteVisitTimesForStory(Long livingStoryId);
}
