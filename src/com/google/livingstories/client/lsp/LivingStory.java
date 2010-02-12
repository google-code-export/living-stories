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

package com.google.livingstories.client.lsp;

import com.google.gwt.ajaxloader.client.AjaxLoader;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.livingstories.client.lsp.views.LivingStoryPage;
import com.google.livingstories.client.lsp.views.Resources;
import com.google.livingstories.client.util.Constants;
import com.google.livingstories.client.util.HistoryManager;
import com.google.livingstories.client.util.LivingStoryData;

import java.util.Date;

/**
 * Chrome around a living story.
 */
public class LivingStory implements EntryPoint {
  private static final long SIXTY_DAYS_IN_MILLISECONDS = 5184000000L;
  
  @Override
  public void onModuleLoad() {
    // Inject the contents of the CSS file
    Resources.INSTANCE.css().ensureInjected();

    AjaxLoader.init();

    String cookieName = Constants.getCookieName(LivingStoryData.getLivingStoryUrl());
    String cookieValue = Cookies.getCookie(cookieName);
    
    if (cookieValue != null) {
      try {
        LivingStoryData.setCookieBasedLastVisitDate(new Date(Long.valueOf(cookieValue)));
      } catch (NumberFormatException e) {
      }
    }

    // note the visit.
    Date now = new Date();
    Date cookieExpiry = new Date(now.getTime() + SIXTY_DAYS_IN_MILLISECONDS);
    Cookies.setCookie(cookieName, String.valueOf(now.getTime()), cookieExpiry);
    // TODO: consider setting the domain appropriately, and the path
    // here to "/lsps/"

    RootPanel.get("storyBody").add(new LivingStoryPage());

    HistoryManager.initialize();
    
    // Also set appropriate i18n text for a couple of constants:
    Document doc = Document.get();
    doc.getElementById("rssLink").setAttribute(
        "title", LspMessageHolder.msgs.rssFeedTitle(LivingStoryData.getLivingStoryTitle()));
    doc.getElementById("readOtherStories").setInnerText(
        LspMessageHolder.msgs.otherStoriesText(LivingStoryData.getLivingStoryPublisher()));
  }
}
