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

package com.google.livingstories.client.start;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.livingstories.client.ContentItemType;
import com.google.livingstories.client.BaseContentItem;
import com.google.livingstories.client.ClientMessageHolder;
import com.google.livingstories.client.EventContentItem;
import com.google.livingstories.client.LivingStory;
import com.google.livingstories.client.LivingStoryRpcService;
import com.google.livingstories.client.LivingStoryRpcServiceAsync;
import com.google.livingstories.client.NarrativeContentItem;
import com.google.livingstories.client.StartPageBundle;
import com.google.livingstories.client.lsp.views.ManagementLinks;
import com.google.livingstories.client.util.DateUtil;
import com.google.livingstories.client.util.GlobalUtil;
import com.google.livingstories.client.util.HistoryManager;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Living Story start page
 *
 * @author ericzhang@google.com (Eric Zhang), nehasingh@google.com (Neha Singh)
 */
public class StartPage implements EntryPoint {
  private final LivingStoryRpcServiceAsync livingStoryService = 
      GWT.create(LivingStoryRpcService.class);
  private static final int ONE_MINUTE_MILLIS = 1000 * 60;
  private static final int ONE_HOUR_MILLIS = ONE_MINUTE_MILLIS * 60;
  private static final int ONE_DAY_MILLIS = ONE_HOUR_MILLIS * 24;
  private static final int ONE_WEEK_MILLIS = ONE_DAY_MILLIS * 7;
  private static final int MONTHS_PER_YEAR = 12;
  private static final int YEAR_CUTOFF = MONTHS_PER_YEAR;
  
  private VerticalPanel startPageWidget;
  
  @Override
  public void onModuleLoad() {
    RootPanel.get("managementLinks").add(new ManagementLinks());
    startPageWidget = new VerticalPanel();
    startPageWidget.setWidth("100%");
    RootPanel.get("storyList").add(startPageWidget);
    
    livingStoryService.getStartPageBundle(new AsyncCallback<StartPageBundle>() {
      @Override
      public void onFailure(Throwable t) {
        startPageWidget.add(new Label(ClientMessageHolder.consts.startPageLoadFailed()));
      }
      
      @Override
      public void onSuccess(StartPageBundle bundle) {
        populate(bundle.getStories(), bundle.getStoryIdToUpdateMap());
      }
    });
  }
  
  private void populate(List<LivingStory> stories,
      Map<Long, List<BaseContentItem>> storyIdToUpdateMap) {
    for (LivingStory story : stories) {
      GlobalUtil.addIfNotNull(startPageWidget,
          createStoryWidget(story, storyIdToUpdateMap.get(story.getId())));
    }
  }
  
  private Widget createStoryWidget(LivingStory story, List<BaseContentItem> updates) {
    if (updates.isEmpty()) {
      return null;
    }
    VerticalPanel panel = new VerticalPanel();
    panel.setWidth("100%");
    DOM.setStyleAttribute(panel.getElement(), "marginBottom", "15px");
    
    Anchor storyName = new Anchor(story.getTitle(), getStoryUrl(story));
    storyName.addStyleName("startPageStoryName");
    
    InlineHTML dash = new InlineHTML("- ");
    dash.addStyleName("startPagePublisherName");
    
    Anchor publisherName = new Anchor(story.getPublisher().toString(),
        true, getStoryUrl(story));
    publisherName.setStylePrimaryName("startPagePublisherName");

    FlowPanel headerTitle = new FlowPanel();
    headerTitle.add(storyName);
    headerTitle.add(new InlineLabel(" "));
    FlowPanel publisherFlowPanel = new FlowPanel();
    publisherFlowPanel.setStylePrimaryName("nowrap");
    DOM.setStyleAttribute(publisherFlowPanel.getElement(), "display", "inline");
    publisherFlowPanel.add(dash);
    publisherFlowPanel.add(publisherName);
    headerTitle.add(publisherFlowPanel);
    
    Label updateCount = getUpdateRecencyLabel(
        DateUtil.laterDate(updates.get(0).getTimestamp(), story.getLastChangeTimestamp()));
    updateCount.setWordWrap(false);
    
    HorizontalPanel header = new HorizontalPanel();
    header.setWidth("100%");
    header.addStyleName("sectionHeader");
    DOM.setStyleAttribute(header.getElement(), "marginBottom", "10px");
    header.setHorizontalAlignment(HorizontalPanel.ALIGN_LEFT);
    
    header.add(headerTitle);
    header.add(updateCount);
    header.setCellHorizontalAlignment(updateCount, HorizontalPanel.ALIGN_RIGHT);
    panel.add(header);
    
    for (BaseContentItem update : updates) {
      Anchor headline = null;
      if (update.getContentItemType() == ContentItemType.NARRATIVE) {
        NarrativeContentItem narrative = (NarrativeContentItem)update;
        headline = new Anchor(narrative.getHeadline() + "<span class=\"greyFont\">&nbsp;-&nbsp;" 
            + narrative.getNarrativeType().toString(),
            true, getStoryUrl(story) + getUpdateUrl(narrative));
      } else {
        EventContentItem event = (EventContentItem)update;
        headline = new Anchor(event.getEventUpdate(), true, 
            getStoryUrl(story) + getUpdateUrl(event));
      }
      DOM.setStyleAttribute(headline.getElement(), "paddingLeft", "5px");
      panel.add(headline);
      
      Label dateLabel = new Label(DateUtil.formatDate(update.getDateSortKey()));
      dateLabel.addStyleName("greyFont");
      DOM.setStyleAttribute(dateLabel.getElement(), "padding", "0 0 13px 5px");
      
      panel.add(dateLabel);
    }
    
    Anchor viewAll =
        new Anchor(ClientMessageHolder.consts.startPageViewAll(), true, getStoryUrl(story));
    viewAll.setStylePrimaryName("startPageViewAll");
    panel.add(viewAll);
    
    return panel;
  }
  
  private String getStoryUrl(LivingStory story) {
    return "lsps/" + story.getUrl();
  }
  
  private String getUpdateUrl(BaseContentItem update) {
    return HistoryManager.getTokenStringForFocusedContentItem(update.getId());
  }
  
  private Label getUpdateRecencyLabel(Date lastUpdateTime) {
    Date now = new Date();
    
    long millisPassed = now.getTime() - lastUpdateTime.getTime();
    int monthsApart = fullMonthsApart(lastUpdateTime, now);
    
    String displayString;
    
    if (millisPassed < ONE_HOUR_MILLIS) {
      // "1 minute ago" is the most recent an update gets here.
      displayString = ClientMessageHolder.msgs.nMinutesAgo(
          Math.max(intDiv(millisPassed, ONE_MINUTE_MILLIS), 1));
    } else if (millisPassed < ONE_DAY_MILLIS) {
      displayString = ClientMessageHolder.msgs.nHoursAgo(intDiv(millisPassed, ONE_HOUR_MILLIS));
    } else if (millisPassed < ONE_WEEK_MILLIS) {
      displayString = ClientMessageHolder.msgs.nDaysAgo(intDiv(millisPassed, ONE_DAY_MILLIS));
    } else if (monthsApart < 1) {
      displayString = ClientMessageHolder.msgs.nWeeksAgo(intDiv(millisPassed, ONE_WEEK_MILLIS));
    } else if (monthsApart < YEAR_CUTOFF) {
      displayString = ClientMessageHolder.msgs.nMonthsAgo(monthsApart);
    } else {
      displayString = ClientMessageHolder.msgs.nYearsAgo(monthsApart / MONTHS_PER_YEAR);
    }
    
    Label label = new Label(displayString);
    label.addStyleName("normalFontWeight");
    return label;
  }

  @SuppressWarnings("deprecation")
  /** returns the number of full months between d1 -- which should be the earlier date -- and d2 */
  private int fullMonthsApart(Date d1, Date d2) {
    int yearsApart = d2.getYear() - d1.getYear();
    int fullMonths = 12 * yearsApart;
    Date d1Modified = new Date(d1.getTime());
    d1Modified.setYear(d2.getYear());
    
    int monthsApart = d2.getMonth() - d1Modified.getMonth();
    
    fullMonths += monthsApart;   // this could, effectively, be a subtraction
    d1Modified.setMonth(d2.getMonth());
    
    // if the day, hour, minute, etc.; components of d1Modified actually indicate it's after d2,
    // we've overcounted the number of full months by 1.
    if (d1Modified.compareTo(d2) > 0) {
      fullMonths--;
    }

    return fullMonths;
  }
  
  private int intDiv(long n, long d) {
    long ret = n / d;
    return ret > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) ret;
  }
}
