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
import com.google.livingstories.client.FilterSpec;
import com.google.livingstories.client.LivingStory;
import com.google.livingstories.client.PublishState;
import com.google.livingstories.client.util.Constants;
import com.google.livingstories.gxps.LivingStoryHtml;
import com.google.livingstories.server.dataservices.LivingStoryDataService;
import com.google.livingstories.server.dataservices.UserDataService;
import com.google.livingstories.server.dataservices.UserLoginService;
import com.google.livingstories.server.dataservices.impl.DataImplFactory;

import java.io.IOException;
import java.util.Date;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet to serve the new version of the lsp page.
 */
public class LspDispatcher extends HttpServlet {
  // These IDs are assigned by friend connect.  Each ID applies to one root url,
  // so we need to switch them depending on whether we're running locally,
  // deployed internally, or deployed externally.
  // The root url for each ID can be managed through the friend connect admin pages.
  private static final String LOCAL_FRIEND_CONNECT_SITE_ID = "06472697331222075712";
  private static final String INTERNAL_FRIEND_CONNECT_SITE_ID = "08713752064473386876";
  private static final String PUBLIC_DOGFOOD_FRIEND_CONNECT_SITE_ID = "05439374789167226920";
  private static final String EXTERNAL_FRIEND_CONNECT_SITE_ID = "16438544277001602951";
  
  protected LivingStoryDataService livingStoryDataService;
  protected UserLoginService userLoginService;
  protected UserDataService userDataService;

  public LspDispatcher() {
    this.livingStoryDataService = DataImplFactory.getLivingStoryService();
    this.userLoginService = DataImplFactory.getUserLoginService();
    this.userDataService = DataImplFactory.getUserDataService();
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    // Get the path info, minus the leading slash.
    String lspUrl = req.getPathInfo().substring(1);
    
    LivingStory livingStory = livingStoryDataService.retrieveByUrlName(lspUrl, true);
    if (livingStory == null) {
      resp.sendRedirect("/");
      return;
    }
    Long livingStoryId = livingStory.getId();
    Date lastVisitTime = null;
    boolean subscribedToEmails = false;
    FilterSpec defaultView = null;

    String loggedInUser = userLoginService.getUserId();
    if (loggedInUser != null) {
      lastVisitTime = userDataService.getLastVisitTimeForStory(loggedInUser, livingStoryId);
      subscribedToEmails = userDataService.isUserSubscribedToEmails(loggedInUser, livingStoryId);
      defaultView = userDataService.getDefaultStoryView(loggedInUser);
    }

    if (lastVisitTime == null) {
      // No last visit time found (either user was not logged in,
      // or user logged in for the first time)
      // Try to get the cookie with the last visit time
      String cookieName = Constants.getCookieName(lspUrl);
      Cookie[] cookies = req.getCookies();
      if (cookies != null) {
        for (Cookie cookie : cookies) {
          if (cookieName.equals(cookie.getName())) {
            try {
              lastVisitTime = new Date(Long.valueOf(cookie.getValue()));
            } catch (NumberFormatException e) {
            }
          }
        }
      }
    }
    
    // If the last visit time is not available in the default list of summary revisions,
    // we need to query the datastore for the full list of revisions.  This is slow,
    // but is not expected to happen often.
    if (!livingStory.dateWithinAvailableRevisions(lastVisitTime)) {
      // TODO: decide if this should be an async call from the client.
      livingStory = livingStoryDataService.retrieveById(livingStoryId, false);
    }
    
    String currentUrl = req.getRequestURI();
    // Display the page according to when the user last visited the story
    LivingStoryHtml.write(
        resp.getWriter(),
        new GxpContext(req.getLocale()),
        currentUrl,
        livingStory,
        userLoginService.getUserDisplayName(),
        userLoginService.createLoginUrl(currentUrl),
        userLoginService.createLogoutUrl(currentUrl),
        getSubscriptionUrl(livingStoryId, lspUrl),
        lastVisitTime,
        subscribedToEmails,
        defaultView,
        getFriendConnectSiteId(req.getServerName()),
        livingStoryDataService.retrieveByPublisher(livingStory.getPublisher(),
            PublishState.PUBLISHED, true));
    
    // After the page has been created, update the stored value for the time the user
    // last visited the page to the current value
    if (loggedInUser != null) {
      userDataService.updateVisitDataForStory(loggedInUser, livingStoryId);
    }
  }
  
  private String getSubscriptionUrl(Long livingStoryId, String livingStoryUrl) {
    return userLoginService.createLoginUrl("/subscribe?lspId=" + livingStoryId + "&amp;lspUrl="
        + livingStoryUrl);
  }

  protected String getFriendConnectSiteId(String hostname) {
    if (hostname.endsWith("prom.corp.google.com")) {
      return INTERNAL_FRIEND_CONNECT_SITE_ID;
    } else if (hostname.endsWith("appspot.com")) {
      return PUBLIC_DOGFOOD_FRIEND_CONNECT_SITE_ID;
    } else if (hostname.endsWith("googlelabs.com")) {
      return EXTERNAL_FRIEND_CONNECT_SITE_ID;
    } else {
      return LOCAL_FRIEND_CONNECT_SITE_ID;
    }
  }
}
