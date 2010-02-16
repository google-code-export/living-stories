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

package com.google.livingstories.client.lsp.views;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.livingstories.client.EventContentItem;
import com.google.livingstories.client.lsp.LspMessages;
import com.google.livingstories.client.util.DateUtil;

import java.util.Date;

/**
 * A class for displaying a java.util.Date range. Most classes that implement the
 * DateTimeDisplayer interface will in fact use this class via composition. 
 */
public class DateTimeRangeWidget extends Composite {
  private static DateTimeRangeWidgetUiBinder uiBinder =
      GWT.create(DateTimeRangeWidgetUiBinder.class);
  interface DateTimeRangeWidgetUiBinder extends UiBinder<Widget, DateTimeRangeWidget> {
  }

  private static final LspMessages msgs = GWT.create(LspMessages.class);

  @UiField Label date;
  @UiField Label time;

  private String dateString;
  
  public DateTimeRangeWidget() {
    initWidget(uiBinder.createAndBindUi(this));
    
    // TODO: this is temporary; remove this once we have a
    // narrative stream view.
    date.setStyleName("dateLabel");
    time.setStyleName("timeLabel");
  }
  
  public DateTimeRangeWidget(Date startDateTime, Date endDateTime) {
    this();
    setDateTime(startDateTime, endDateTime);
  }
  
  public void setDateTime(Date startDateTime, Date endDateTime) {
    boolean multiDay = datesFallOnDifferentDays(startDateTime, endDateTime);

    // create the date label
    dateString = DateUtil.formatDate(startDateTime);
    if (multiDay) {
      // endDateTime will be non-null
      dateString = msgs.dateRange(dateString, DateUtil.formatDate(endDateTime));
    }
    date.setText(dateString);
    
    // create the time label
    if (!multiDay) {
      String timeString = DateUtil.formatTime(startDateTime);
      
      if (endDateTime != null && !startDateTime.equals(endDateTime)) {
        timeString = msgs.timeRange(timeString, DateUtil.formatTime(endDateTime));
      }

      if (!datesFallOnDifferentDays(startDateTime, new Date())) {
        // for events that happened today, we just show the time instead of the date
        date.setText(timeString);
      } else {
        time.setText(timeString);
        time.setVisible(true);
      }
    }
  }
  
  public static DateTimeRangeWidget makeForEventContentItem(EventContentItem event) {
    Date startDateTime = event.getEventStartDate();
    Date endDateTime = event.getEventEndDate();
    
    if (startDateTime == null) {
      if (endDateTime == null) {
        // If there is no special start and end date on the event, just show the creation time
        // as the event time.
        startDateTime = event.getTimestamp();
      } else {
        // if only one of eventStartDate & eventEndDate was specified, treat the values as
        // though it was, in fact, eventStartDate that was specified.
        startDateTime = endDateTime;
        endDateTime = null;
      }
    }
    
    DateTimeRangeWidget widget = new DateTimeRangeWidget();
    widget.setDateTime(startDateTime, endDateTime);
    return widget;
  }
    
  public String getDateString() {
    return dateString;
  }

  public void setTimeVisible(boolean visible) {
    if (date.isVisible() && time != null) {
      time.setVisible(visible);
    }
  }

  /**
   * returns true if d1 and d2 fall on the same calendar day, even if not the same time.
   */
  @SuppressWarnings("deprecation")
  private boolean datesFallOnDifferentDays(Date d1, Date d2) {
    return d1 != null && d2 != null && (d1.getYear() != d2.getYear()
        || d1.getMonth() != d2.getMonth() || d1.getDate() != d2.getDate());
  }
}
