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

/**
 * Service interface for returning login information about the user in the current request.
 */
public interface UserLoginService {
  
  /**
   * Return true if a user is logged into application.
   */
  boolean isUserLoggedIn();
  
  /**
   * Return the id of the currently logged in user.
   */
  String getUserId();
  
  /**
   * Return the name of the logged in user that can be display on the client. Return null if no user
   * is logged in.
   */
  String getUserDisplayName();
  
  /**
   * Return a URL for a page where a user can log in.
   * @param destinationUrl Url to redirect to after user has logged in.
   */
  String createLoginUrl(String destinationUrl);
  
  /**
   * Return a URL for a page that logs the user out.
   * @param destinationUrl Url to redirect to after the user has logged out.
   */
  String createLogoutUrl(String destinationUrl);
  
  /**
   * Return true if the currently logged in user is an admin.
   */
  boolean isAdmin();
}
