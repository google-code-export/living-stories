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

import com.google.common.collect.Lists;
import com.google.livingstories.client.BaseContentItem;
import com.google.livingstories.client.ContentItemType;
import com.google.livingstories.client.ContentRpcService;
import com.google.livingstories.client.EventContentItem;
import com.google.livingstories.client.LivingStory;
import com.google.livingstories.client.LivingStoryRpcService;
import com.google.livingstories.client.NarrativeContentItem;
import com.google.livingstories.client.util.GlobalUtil;
import com.google.livingstories.client.util.SnippetUtil;
import com.google.livingstories.client.util.dom.JavaNodeAdapter;
import com.google.livingstories.server.rpcimpl.ContentRpcImpl;
import com.google.livingstories.server.rpcimpl.LivingStoryRpcImpl;
import com.google.livingstories.server.util.StringUtil;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedOutput;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet to serve rss feeds for living stories
 */
public class FeedServlet extends HttpServlet {
  private static final String DEFAULT_FEED_TYPE = "rss_2.0";
  private static final long MILLIS_PER_DAY = 1000 * 60 * 60 * 24;
  private static final int MAXIMUM_SNIPPET_LENGTH = 500;
  
  private LivingStoryRpcService livingStoryService;
  private ContentRpcService contentService;

  public FeedServlet() {
    livingStoryService = new LivingStoryRpcImpl();
    contentService = new ContentRpcImpl();
  }

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String feedType = req.getParameter("type");
    if (feedType == null) {
      feedType = DEFAULT_FEED_TYPE;
    }
    
    SyndFeed feed = getFeed(req);
    if (feed == null) {
      resp.sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    }
    feed.setFeedType(feedType);
    resp.setContentType("application/xml; charset=utf-8");
    SyndFeedOutput output = new SyndFeedOutput();
    try {
      output.output(feed, resp.getWriter());
    } catch (FeedException ex) {
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Could not generate feed");
    }
  }

  private SyndFeed getFeed(HttpServletRequest req) {
    // Get the path info, minus the leading slash.
    String lspUrl = req.getPathInfo().substring(1);
    Date twoWeeksAgo = new Date(new Date().getTime() - 14 * MILLIS_PER_DAY);
    
    LivingStory livingStory = livingStoryService.getLivingStoryByUrl(lspUrl);
    if (livingStory == null) {
      return null;
    }
    
    List<BaseContentItem> updates = contentService.getUpdatesSinceTime(
        livingStory.getId(), twoWeeksAgo);
    Collections.sort(updates, BaseContentItem.REVERSE_COMPARATOR);
    
    SyndFeed feed = new SyndFeedImpl();
    feed.setTitle(livingStory.getTitle());
    feed.setLink(createLspUrl(req));
    SyndContent feedDescription = new SyndContentImpl();
    feedDescription.setType("text/html");
    feedDescription.setValue(StringUtil.stripForExternalSites(livingStory.getSummary()));
    feed.setDescriptionEx(feedDescription);
    
    List<SyndEntry> items = Lists.newArrayList();
    for (BaseContentItem update : updates) {
      SyndContent title = new SyndContentImpl();
      SyndContent content = new SyndContentImpl();
      if (update.getContentItemType() == ContentItemType.EVENT) {
        EventContentItem event = (EventContentItem) update;
        title.setType("text/html");
        title.setValue(event.getEventUpdate());
        content.setType("text/html");
        content.setValue(StringUtil.stripForExternalSites(event.getEventSummary()));
      } else if (update.getContentItemType() == ContentItemType.NARRATIVE) {
        NarrativeContentItem narrative = (NarrativeContentItem) update;
        title.setType("text/html");
        title.setValue(narrative.getHeadline() + "&nbsp;-&nbsp;" 
            + narrative.getNarrativeType().toString());
        content.setType("text/html");
        String narrativeSummary = StringUtil.stripForExternalSites(
            narrative.getNarrativeSummary());
        if (GlobalUtil.isContentEmpty(narrativeSummary)) {
          content.setValue(SnippetUtil.createSnippet(
              JavaNodeAdapter.fromHtml(narrative.getContent()), MAXIMUM_SNIPPET_LENGTH));
        } else {
          content.setValue(narrativeSummary);
        }
      }
      SyndEntry entry = new SyndEntryImpl();
      entry.setLink(createContentItemUrl(req, update.getId()));
      entry.setPublishedDate(update.getDateSortKey());
      entry.setTitleEx(title);
      entry.setDescription(content);
      items.add(entry);
    }
    
    feed.setEntries(items);
    return feed;
  }
  
  private String createLspUrl(HttpServletRequest req) {
    return req.getRequestURL().toString().replace("feeds", "lsps");
  }
  
  private String createContentItemUrl(HttpServletRequest req, long contentItemId) {
    return createLspUrl(req) + "#OVERVIEW:false,false,false,n,n,n:" + contentItemId + ";";
  }
}
