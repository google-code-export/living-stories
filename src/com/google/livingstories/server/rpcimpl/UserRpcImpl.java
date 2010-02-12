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

package com.google.livingstories.server.rpcimpl;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.livingstories.client.FilterSpec;
import com.google.livingstories.client.Publisher;
import com.google.livingstories.client.UserRpcService;
import com.google.livingstories.server.dataservices.UserDataService;
import com.google.livingstories.server.dataservices.UserLoginService;
import com.google.livingstories.server.dataservices.impl.DataImplFactory;

import java.util.Date;

/**
 * User information persistence service implementation.
 */
public class UserRpcImpl extends RemoteServiceServlet implements UserRpcService {
  
  private UserLoginService userLoginService = DataImplFactory.getUserLoginService();
  private UserDataService userDataService = DataImplFactory.getUserDataService();
  
  @Override
  public boolean isUserLoggedIn() {
    return userLoginService.isUserLoggedIn();
  }
  
  @Override
  public String getUserNickname() {
    return userLoginService.getUserDisplayName();
  }
  
  private String getLoggedInUserId() {
    return userLoginService.getUserId();
  }
  
  @Override
  public Date getLastVisitedTime(Long livingStoryId) {
    return userDataService.getLastVisitTimeForStory(getLoggedInUserId(), livingStoryId);
  }
  
  @Override
  public void updateLastVisitedTime(Long livingStoryId) {
    userDataService.updateVisitDataForStory(getLoggedInUserId(), livingStoryId);
  }

  @Override
  public boolean isSubscribedToEmails(Long livingStoryId) {
    return userDataService.isUserSubscribedToEmails(getLoggedInUserId(), livingStoryId);
  }
  
  /**
   * Sets the user's subscription status for a living story page.
   * Assumes that the user is logged in, and that a UserLivingStoryEntity exists
   * for this user/living-story pair.
   */
  @Override
  public void setSubscribedToEmails(Long livingStoryId, boolean subscribe) {
    userDataService.setEmailSubscription(getLoggedInUserId(), livingStoryId, subscribe);
  }
  
  @Override
  public FilterSpec getDefaultStoryView() {
    return userDataService.getDefaultStoryView(getLoggedInUserId());
  }
  
  /**
   * Sets the user's default page view for a living story page.
   * Assumes that the user is logged in, and that a UserLivingStoryEntity exists
   * for this user/living-story pair.
   */
  @Override
  public void setDefaultStoryView(FilterSpec defaultStoryView) {
    userDataService.setDefaultStoryView(getLoggedInUserId(), defaultStoryView);
  }
  
  /**
   * The following types of users are whitelisted for accessing the lsps: app admins, anyone with
   * an @google.com email address and explicitly listed external google accounts.
   */
  @Override
  public synchronized boolean isWhitelisted() {
    if (isUserLoggedIn()) {
      String userEmail = getLoggedInUserId();
      if (userLoginService.isAdmin() || userEmail.endsWith("@google.com")) {
        return true;
      } else {
        return new WhitelistingRpcImpl().isUserWhitelisted(userEmail);
      }
    } else {
      return false;
    }
  }
  
  public Publisher getPublisherForAdminUser() {
    if (userLoginService.isAdmin()) {
      return userDataService.getPublisherForAdminUser(getLoggedInUserId());
    } else {
      return null;
    }
  }
}
