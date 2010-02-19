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

import com.google.livingstories.server.dataservices.ContentDataService;
import com.google.livingstories.server.dataservices.LivingStoryDataService;
import com.google.livingstories.server.dataservices.ThemeDataService;
import com.google.livingstories.server.dataservices.UserDataService;
import com.google.livingstories.server.dataservices.UserLoginService;

/**
 * Instantiate the implementations of the various data interfaces.
 */
public class DataImplFactory {
  private static final LivingStoryDataService livingStoryService = new LivingStoryDataServiceImpl();
  private static final ThemeDataService themeService = new ThemeDataServiceImpl();
  private static final ContentDataService contentService = new ContentDataServiceImpl();
  private static final UserDataService userDataService = new StubUserDataServiceImpl();
  private static final UserLoginService userLoginService = new StubUserLoginServiceImpl();
  
  public static LivingStoryDataService getLivingStoryService() {
    return livingStoryService;
  }
  
  public static ThemeDataService getThemeService() {
    return themeService;
  }
  
  public static ContentDataService getContentService() {
    return contentService;
  }
  
  public static UserDataService getUserDataService() {
    return userDataService;
  }
  
  public static UserLoginService getUserLoginService() {
    return userLoginService;
  }
}
