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

import com.google.gxp.base.GxpContext;
import com.google.livingstories.gxps.StartPageHtml;
import com.google.livingstories.server.dataservices.UserDataService;
import com.google.livingstories.server.dataservices.UserLoginService;
import com.google.livingstories.server.dataservices.impl.DataImplFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet for the living stories start page.
 */
public class StartPageServlet extends HttpServlet {
  protected UserLoginService userLoginService;
  protected UserDataService userDataService;
  
  public StartPageServlet() {
    this.userLoginService = DataImplFactory.getUserLoginService();
    this.userDataService = DataImplFactory.getUserDataService();
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String loggedInUser = userLoginService.getUserId();
    
    JSONObject lastVisitTimes = new JSONObject();
    if (loggedInUser != null) {
      // Note: the date format that will be transferred via JSON should match with the date format
      // being used in {@link StartPage} which parses it. In particular, the timezone must be
      // included in the date being transferred.
      DateFormat dateFormatter = new SimpleDateFormat("MMMMM d, yyyy HH:mm:ss aaa ZZZZ");
      Map<Long, Date> allStoryMap = userDataService.getAllLastVisitTimes(loggedInUser);
      for (Map.Entry<Long, Date> entry : allStoryMap.entrySet()) {
        if (entry.getValue() != null) {
          try {
            lastVisitTimes.put(entry.getKey().toString(), dateFormatter.format(entry.getValue()));
          } catch (JSONException e) {
            // Do nothing
          }
        }
      }
    }
    
    String currentUrl = req.getRequestURI();
    StartPageHtml.write(
        resp.getWriter(),
        new GxpContext(req.getLocale()),
        req.getRequestURI(),
        userLoginService.getUserDisplayName(),
        userLoginService.createLoginUrl(currentUrl),
        userLoginService.createLogoutUrl(currentUrl),
        lastVisitTimes,
        loggedInUser == null ? null : userDataService.getDefaultStoryView(loggedInUser),
        new ExternalServiceKeyChain(getServletContext()).getAnalyticsAccountId());
  }
}
