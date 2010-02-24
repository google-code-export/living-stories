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

package com.google.livingstories.server.dataservices.impl;

import com.google.livingstories.client.FilterSpec;
import com.google.livingstories.server.dataservices.UserDataService;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Stub implementation of the user data service.
 * Useful if you don't want to support user login.
 */
public class StubUserDataServiceImpl implements UserDataService {
  @Override
  public void deleteVisitTimesForStory(Long livingStoryId) {
  }

  @Override
  public Map<Long, Date> getAllLastVisitTimes(String userId) {
    return new HashMap<Long, Date>();
  }

  @Override
  public FilterSpec getDefaultStoryView(String userId) {
    return null;
  }

  @Override
  public Date getLastVisitTimeForStory(String userId, Long livingStoryId) {
    return null;
  }

  @Override
  public int getVisitCountForStory(String userId, Long livingStoryId) {
    return 0;
  }

  @Override
  public boolean isUserSubscribedToEmails(String userId, Long livingStoryId) {
    return false;
  }

  @Override
  public void setDefaultStoryView(String userId, FilterSpec defaultView) {
  }

  @Override
  public void setEmailSubscription(String userId, Long livingStoryId, boolean subscribe) {
  }

  @Override
  public void updateVisitDataForStory(String userId, Long livingStoryId) {
  }
}
