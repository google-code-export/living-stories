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

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.livingstories.server.dataservices.UserLoginService;

/**
 * Implementation of the user login interface using the AppEngine User API that uses
 * Google Accounts. More information at: http://code.google.com/appengine/docs/java/users/
 */
public class UserLoginServiceImpl implements UserLoginService {
  private UserService userService;
  
  public UserLoginServiceImpl() {
    userService = UserServiceFactory.getUserService();
  }
  
  @Override
  public boolean isUserLoggedIn() {
    return userService.isUserLoggedIn();
  }
  
  @Override
  public String getUserId() {
    return isUserLoggedIn() ? userService.getCurrentUser().getEmail() : null;
  }

  @Override
  public String getUserDisplayName() {
    return isUserLoggedIn() ? userService.getCurrentUser().getNickname() : null;
  }
  
  @Override
  public String createLoginUrl(String destinationUrl) {
    return userService.createLoginURL(destinationUrl);
  }

  @Override
  public String createLogoutUrl(String destinationUrl) {
    return userService.createLogoutURL(destinationUrl);
  }

  @Override
  public boolean isAdmin() {
    return isUserLoggedIn() ? userService.isUserAdmin() : false;
  }
}
