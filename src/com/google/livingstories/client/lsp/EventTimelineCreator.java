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
import com.google.livingstories.client.ClientCaches;
import com.google.livingstories.client.EventAtom;
import com.google.livingstories.client.ui.TimelineData;
import com.google.livingstories.client.ui.TimelineWidget;
import com.google.livingstories.client.util.AnalyticsUtil;
import com.google.livingstories.client.util.DateUtil;
import com.google.livingstories.client.util.LivingStoryControls;
import com.google.livingstories.client.util.LivingStoryData;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides methods for attaching a timeline to a specific DOM element and for loading
 * it with eventAtom data when available.
 */
public class EventTimelineCreator {
  
  // Prevent instantiation
  private EventTimelineCreator() {}

  /**
   * Create a new timeline widget.
   * TODO: implement different policies on which events get mentioned in the timeline.
   *   Setup for those policies should be here; implementation in @link{loadTimeline}.
   */
  public static TimelineWidget<Long> createTimeline(Integer width, Integer height) {
    final TimelineWidget<Long> timeline = new TimelineWidget<Long>(
        width, height, new TimelineWidget.OnClickBehavior<Long>() {
          @Override
          public void onClick(ClickEvent event, Long data) {
            AnalyticsUtil.trackHorizontalTimelineClick(LivingStoryData.getLspUrl(), data);
            LivingStoryControls.goToAtom(data);
          }
        });
    timeline.setVisible(false);

    // Load the timeline data asynchronously.
    ClientCaches.getImportantEvents(new AsyncCallback<List<EventAtom>>() {
      @Override
      public void onFailure(Throwable caught) {
        // Ignore the error.
      }
      @Override
      public void onSuccess(List<EventAtom> result) {
        loadTimeline(timeline, result);
      }
    });

    return timeline;
  }

  /**
   * Actually loads timeline data into the timeline widget.
   */
  public static void loadTimeline(TimelineWidget<Long> timeline, List<EventAtom> importantEvents) {
    // TODO: implement policies other than "important events only".
    Map<Date, TimelineData<Long>> pointEvents = new LinkedHashMap<Date, TimelineData<Long>>();
    Map<TimelineWidget.Interval, TimelineData<Long>> rangeEvents =
      new LinkedHashMap<TimelineWidget.Interval, TimelineData<Long>>();
    Date earliest = null, latest = null;
    
    for (EventAtom event : importantEvents) {
      // TODO: very similar to code in DateTimeRangeWidget.makeForEventAtom. Refactor.
      Date startDate = event.getEventStartDate();
      Date endDate = event.getEventEndDate();

      if (startDate == null) {
        if (endDate == null) {
          // If there is no special start and end date on the event, use the creation time
          // as a backup.
          startDate = event.getTimestamp();
        } else {
          // if only one of eventStartDate & eventEndDate was specified, treat the values as
          // though it was, in fact, eventStartDate that was specified.
          startDate = endDate;
          endDate = null;
        }
      }

      Date effectiveEndDate;
      TimelineData<Long> data = makeData(event.getEventUpdate(), event.getId());

      if (endDate == null
          || DateUtil.numberOfDaysApart(startDate, endDate) == 0) {
        pointEvents.put(startDate, data);
        effectiveEndDate = startDate;
      } else {
        rangeEvents.put(new TimelineWidget.Interval(startDate, endDate), data);
        effectiveEndDate = endDate;
      }

      if (earliest == null || earliest.after(startDate)) {
        earliest = startDate;
      }
      if (latest == null || latest.before(effectiveEndDate)) {
        latest = effectiveEndDate;
      }
    }
    
    // earliest and latest are nominally the endpoints for our timeline. Pad them out by
    // another ~10%, or 2 days, whichever is greater.
    long timePadding = Math.max((latest.getTime() - earliest.getTime()) / 10,
        2 * DateUtil.MILLISECONDS_PER_DAY);
    
    earliest = new Date(earliest.getTime() - timePadding);
    latest = new Date(latest.getTime() + timePadding);
    
    if (!(rangeEvents.isEmpty() && pointEvents.isEmpty())) {
      timeline.load(new TimelineWidget.Interval(earliest, latest), pointEvents, rangeEvents);
      timeline.setVisible(true);
    }
  }
  
  private static TimelineData<Long> makeData(String label, Long data) {
    return new TimelineData<Long>(label, data);
  }
}
