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

package com.google.livingstories.servlet;

import com.google.livingstories.server.dataservices.UserDataService;
import com.google.livingstories.server.dataservices.UserLoginService;
import com.google.livingstories.server.dataservices.impl.DataImplFactory;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SubscribeServlet extends HttpServlet {
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    UserLoginService userLoginService = DataImplFactory.getUserLoginService();
    UserDataService userDataService = DataImplFactory.getUserDataService();
    
    if (!userLoginService.isUserLoggedIn()) {
      // User needs to login first
      resp.sendRedirect(userLoginService.createLoginUrl(req.getRequestURL().toString()));
      return;
    }
    
    String livingStoryId = req.getParameter("livingStoryId");
    if (livingStoryId != null) {
      userDataService.setEmailSubscription(
          userLoginService.getUserId(), Long.valueOf(livingStoryId), true);
    }

    String lspUrl = req.getParameter("lspUrl");
    if (lspUrl != null) {
      resp.sendRedirect("/lsps/" + lspUrl);
    } else {
      resp.sendRedirect("/");
    }
  }
}
