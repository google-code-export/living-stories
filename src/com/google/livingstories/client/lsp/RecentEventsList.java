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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.livingstories.client.ClientCaches;
import com.google.livingstories.client.EventContentItem;
import com.google.livingstories.client.ui.DateWidget;
import com.google.livingstories.client.ui.Link;
import com.google.livingstories.client.util.AnalyticsUtil;
import com.google.livingstories.client.util.LivingStoryControls;
import com.google.livingstories.client.util.LivingStoryData;

import java.util.List;

/**
 * Widget that displays a list of the 5 latest important events.
 * Clicking on an event should scroll the user to that event in the
 * main content item list and open it.
 */
public class RecentEventsList extends Composite {
  private static final int MAX_EVENT_COUNT = 5;
  
  private FlowPanel contentPanel;
  
  private static String TITLE = LspMessageHolder.consts.recentEventsListTitle();
  private static String PROBLEM_TEXT = LspMessageHolder.consts.recentEventsListProblem();
  
  public RecentEventsList() {
    contentPanel = new FlowPanel();
    contentPanel.add(getHeader());
    contentPanel.setWidth("250px");
    
    initWidget(contentPanel);
  }

  public void load() {
    ClientCaches.getImportantEvents(new ImportantContentItemsCallback());
  }
  
  private Widget getHeader() {
    Label header = new Label(TITLE);
    header.setStylePrimaryName("sectionHeader");
    return header;
  }
  
  private class ImportantContentItemsCallback implements AsyncCallback<List<EventContentItem>> {
    @Override
    public void onFailure(Throwable caught) {
      contentPanel.add(new Label(PROBLEM_TEXT));
    }
    
    @Override
    public void onSuccess(List<EventContentItem> result) {
      if (result.isEmpty()) {
        contentPanel.setVisible(false);
      } else {
        for (int i = 0; i < result.size(); i++) {
          if (i == MAX_EVENT_COUNT) {
            break;
          }
          contentPanel.add(createEventWidget(result.get(i)));        
        }
      }
    }
    
    private Widget createEventWidget(final EventContentItem event) {
      FlowPanel eventPanel = new FlowPanel();
      Link update = new Link(event.getEventUpdate()) {
        @Override
        protected void onClick(ClickEvent e) {
          AnalyticsUtil.trackVerticalTimelineClick(
              LivingStoryData.getLivingStoryUrl(), event.getId());
          LivingStoryControls.goToContentItem(event.getId());
        }
      };
      eventPanel.add(update);
      eventPanel.add(new DateWidget(
          event.getEventEndDate(), event.getEventStartDate(), event.getTimestamp()));
      eventPanel.setStylePrimaryName("rightColumnElement");
      return eventPanel;
    }
  }
}
