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

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.livingstories.client.EventAtom;
import com.google.livingstories.client.util.DateUtil;
import com.google.livingstories.client.util.GlobalUtil;

import java.util.Date;

/**
 * A class for displaying a java.util.Date range. Most classes that implement the
 * DateTimeDisplayer interface will in fact use this class via composition. 
 */
public class DateTimeRangeWidget extends Composite {
  private Label dateLabel;
  private Label timeLabel;

  private static final LspMessages msgs = GWT.create(LspMessages.class);
  
  public DateTimeRangeWidget(Date startDateTime, Date endDateTime) {
    super();
    boolean multiDay = datesFallOnDifferentDays(startDateTime, endDateTime);

    // create the date label
    String dateString = DateUtil.formatDate(startDateTime);
    if (multiDay) {
      // endDateTime will be non-null
      dateString = msgs.dateRange(dateString, DateUtil.formatDate(endDateTime));
    }
    dateLabel = new Label(dateString);
    dateLabel.setStylePrimaryName("dateLabel");
    
    // create the possibly-null time label
    if (multiDay) {
      timeLabel = null;
    } else {
      String timeString = DateUtil.formatTime(startDateTime);
      
      if (endDateTime != null && !startDateTime.equals(endDateTime)) {
        timeString = msgs.timeRange(timeString, DateUtil.formatTime(endDateTime));
      }

      timeLabel = new Label(timeString);
      timeLabel.setStylePrimaryName("timeLabel");
      if (!datesFallOnDifferentDays(startDateTime, new Date())) {
        // for events that happened today, we always show the time, but never
        // show the date
        dateLabel.setVisible(false);
        timeLabel.setStylePrimaryName("dateLabel");
      }
    }
    
    FlowPanel panel = new FlowPanel();
    panel.add(dateLabel);
    GlobalUtil.addIfNotNull(panel, timeLabel);
    
    initWidget(panel);
  }
  
  public static DateTimeRangeWidget makeForEventAtom(EventAtom event) {
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
    
    return new DateTimeRangeWidget(startDateTime, endDateTime);
  }
    
  public String getDateString() {
    return dateLabel.getText();
  }

  public void setTimeVisible(boolean visible) {
    if (dateLabel.isVisible() && timeLabel != null) {
      timeLabel.setVisible(visible);
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
