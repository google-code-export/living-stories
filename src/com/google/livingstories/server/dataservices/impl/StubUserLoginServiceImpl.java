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

import com.google.livingstories.server.dataservices.UserLoginService;

/**
 * Stub implementation of the user login service.
 * Useful if you don't want to support user login.
 */
public class StubUserLoginServiceImpl implements UserLoginService {
  @Override
  public String createLoginUrl(String destinationUrl) {
    return null;
  }

  @Override
  public String createLogoutUrl(String destinationUrl) {
    return null;
  }

  @Override
  public String getUserDisplayName() {
    return null;
  }

  @Override
  public String getUserId() {
    return null;
  }

  @Override
  public boolean isAdmin() {
    return false;
  }

  @Override
  public boolean isUserLoggedIn() {
    return false;
  }
}
