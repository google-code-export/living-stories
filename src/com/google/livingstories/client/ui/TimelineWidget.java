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

package com.google.livingstories.client.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.livingstories.client.util.DateUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * A widget used to render an interactive timeline, with boxes, pop-up captions, and
 * optional event handlers for clicking on a particular box or its caption.
 */
public class TimelineWidget<T> extends Composite {
  private Date displayStarts;
  private Date displayEnds;
  private Map<Date, TimelineData<T>> pointEvents;
  private Map<Interval, TimelineData<T>> rangeEvents;  
  private Set<Interval> filteredRangeKeys;  
  private AbsolutePanel absolutePanel;
  private long daysSpanned;
  private float pixelsPerDay;
  private int eventInsertionHeight;
  private int widthInPixels;
  private int heightInPixels;
  private int maxXPos;
  private OnClickBehavior<T> onClickBehavior;

  private Image leftArrow;
  private Image rightArrow;
  private int globalXOffset = 0;
  private int offscreenXLeft = 10000;  // proper values will be < HALF_EVENT_WIDTH
  private int offscreenXRight = -10000;   // proper values will be > maxXPos
  
  public static int DEFAULT_WIDTH = 450;
  public static int DEFAULT_HEIGHT = 150;
  
  // Nominal width of a date label; actually narrower than this; the text will be
  // centered within the box.
  private static final int DATE_LABEL_WIDTH = 400;
  private static final int DATE_LABEL_OFFSET = 26;
  private static final int HASHMARK_HEIGHT = 12;
  private static final int ARROW_HEAD_WIDTH = 10;
  private static final int ARROW_HEAD_HEIGHT = 11;
  private static final int ARROW_BODY_HEIGHT = 5;
  private static final int EVENT_WIDTH = 90;
  private static final int HALF_EVENT_WIDTH = EVENT_WIDTH / 2;
  // Actual event descriptions are placed slightly off-center compared to their
  // hash marks because the event box text is left-justified, not center-justified. Lining up
  // the hashmark to the center of the box doesn't work well.
  private static final int EVENT_OFFSET = 15;
  private static final int PADDED_EVENT_WIDTH = EVENT_WIDTH + 10;

  /**
   * Constructs a new TimelineWidget
   * @param widthInPixels the width of the timeline
   * @param heightInPixels the height of the timeline
   * @param onClickBehavior an object encapsulating what should happen when the user clicks on the
   *   timeline. null is allowable, although in such cases the generic type of the TimelineWidget
   *   will not be deducible.
   */
  public TimelineWidget(Integer widthInPixels, Integer heightInPixels,
      OnClickBehavior<T> onClickBehavior) {
    this.widthInPixels = (widthInPixels == null ? DEFAULT_WIDTH : widthInPixels);
    this.heightInPixels = (heightInPixels == null ? DEFAULT_HEIGHT : heightInPixels);
    this.onClickBehavior = onClickBehavior;
    maxXPos = this.widthInPixels - HALF_EVENT_WIDTH - EVENT_OFFSET;
    
    // Make eventInsertionHeight one-third of the way down into the widget, rounding down.
    eventInsertionHeight = Math.round(((float) this.heightInPixels) / 3); 

    absolutePanel = new AbsolutePanel();
    absolutePanel.setSize(this.widthInPixels + "px", this.heightInPixels + "px");
    absolutePanel.setStylePrimaryName("timelinePanel");
    
    leftArrow = new Image("/images/inverse-arrowhead-left.gif");
    leftArrow.setStylePrimaryName("disabledArrowHead");

    rightArrow = new Image("/images/inverse-arrowhead-right.gif");
    rightArrow.setStylePrimaryName("disabledArrowHead");

    addClickHandlers();

    initWidget(absolutePanel);
  }
  
  public void load(
      Interval displayRange,
      Map<Date, TimelineData<T>> pointEvents,
      Map<Interval, TimelineData<T>> rangeEvents) {
    this.pointEvents = pointEvents;
    this.rangeEvents = rangeEvents;
    this.filteredRangeKeys = filterRangeEvents(rangeEvents.keySet(), pointEvents.keySet()); 
    
    displayStarts = displayRange.getStartDateTime();
    displayEnds = displayRange.getEndDateTime();
    daysSpanned = DateUtil.numberOfDaysApart(displayStarts, displayEnds);
    pixelsPerDay = ((float) maxXPos - HALF_EVENT_WIDTH) / Math.max(daysSpanned, 1);

    globalXOffset = 0;
    
    loadImpl();
  }
  
  private void addClickHandlers() {
    leftArrow.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        if (offscreenXLeft < HALF_EVENT_WIDTH) {
          // adjust globalXOffset so that one more leftward timeline marker is just visible.
          // The goal is to increase globalXOffset
          globalXOffset += HALF_EVENT_WIDTH - offscreenXLeft;
          reload();
        }
      }
    });
    
    rightArrow.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        if (offscreenXRight > maxXPos) {
          // adjust globalXOffset so that one more rightward timeline marker is just visible.
          // The goal is to decrease globalXOffset
          globalXOffset -= offscreenXRight - maxXPos;
          reload();
        }
      }
    });
  }
  
  /**
   * Call this to clear the absolutepanel and reload all widgets.
   */
  private void reload() {
    absolutePanel.clear();
    loadImpl();
  }
    
  /**
   * Determines which of the intervals in ranges can be shown on the timeline, in that
   * they don't overlap any point events or an earlier range. Note that if there's a set of
   * overlapping event ranges, some will usually be shown; the algorithm will drop some of the
   * conflicting ones, though, usually favoring earlier events over later. (The exact details
   * depend on the relative endpoints of the range and whether there were any interfering point
   * events as well.) Returns the filtered ranges, as a set.
   * @param ranges the ranges to check
   * @param points the point events to check
   * @return ranges, filtered
   */
  private Set<Interval> filterRangeEvents(Set<Interval> ranges, Set<Date> points) {
    // Trailing nulls below serve as sentinel values
    List<Interval> rangeList = new ArrayList<Interval>(ranges);
    Collections.sort(rangeList);
    rangeList.add(null);
    List<Date> pointList = new ArrayList<Date>(points);
    Collections.sort(pointList);
    pointList.add(null);

    // Iterate through the points and ranges in tandem. 
    Iterator<Interval> rangeIt = rangeList.iterator();
    Iterator<Date> pointIt = pointList.iterator();
    Set<Interval> ret = new HashSet<Interval>();
    
    Interval range = rangeIt.next();
    Date point = pointIt.next();
    Interval previousAddedRange = null;
    
    while (range != null) {   // a good loop condition, since rangeList has a sentinel null
      if (point != null && point.before(range.getStartDateTime())) {
        // advance point, but do nothing else
        point = pointIt.next();
      } else { 
        // advance the range, putting it into ret if appropriate.
        if (point == null || point.after(range.getEndDateTime())
            && (previousAddedRange == null
                || previousAddedRange.getEndDateTime().before(range.getStartDateTime()))) {
          // Point can't possibly overlap range, nor does range overlap with the previously
          // added range. Add it to the return set.
          ret.add(range);
          previousAddedRange = range;
        }
        range = rangeIt.next();
      }
    }

    return ret;
  }
  
  private void loadImpl() {
    // put the background arrow on the widget
    int arrowTop = eventInsertionHeight - HASHMARK_HEIGHT / 2;

    absolutePanel.add(leftArrow, 0, arrowTop);
    absolutePanel.add(rightArrow, widthInPixels - ARROW_HEAD_WIDTH, arrowTop);
    
    SimplePanel arrowBody = new SimplePanel();
    arrowBody.setStylePrimaryName("arrowBody");
    arrowBody.setSize((widthInPixels - 2 * ARROW_HEAD_WIDTH) + "px", ARROW_BODY_HEIGHT + "px");
    absolutePanel.add(arrowBody, ARROW_HEAD_WIDTH,
        arrowTop + (ARROW_HEAD_HEIGHT - ARROW_BODY_HEIGHT) / 2);
            
    // Now build up an alternate map of Intervals to event strings. Using a TreeMap gives us
    // increasing dates by key, which is convenient.
    Map<Interval, TimelineData<T>> events = new TreeMap<Interval, TimelineData<T>>();
    for (Interval rangeKey : filteredRangeKeys) {
      events.put(rangeKey, rangeEvents.get(rangeKey));
    }
    for (Date pointKey : pointEvents.keySet()) {
      events.put(new Interval(pointKey, pointKey), pointEvents.get(pointKey));
    }
    
    // some sufficiently positive value here to start. Don't be tempted to use
    // Integer.MAX_VALUE here; the subtraction below may end up overflowing.
    int previousXPosMid = 500000;
    
    offscreenXLeft = 10000;
    offscreenXRight = -10000;

    // we process the events from most recent to least, rather than the other way around, so that,
    // when globalXOffset is 0, we're biased towards showing the most-recent events rather than
    // the least-recent.
    List<Interval> intervalsReversed = new ArrayList<Interval>(events.keySet());
    Collections.reverse(intervalsReversed);
    
    for (Interval interval : intervalsReversed) {
      Date startDate = interval.getStartDateTime(); 
      Date endDate = interval.getEndDateTime();
      
      int xPosStart = globalXOffset + mapDateToPixelPosition(startDate);
      int xPosEnd = startDate.equals(endDate) ? xPosStart
          : (globalXOffset + mapDateToPixelPosition(endDate)); 
      int xPosMid = (xPosStart + xPosEnd) / 2; 
      
      int widthShortfall = xPosMid + PADDED_EVENT_WIDTH - previousXPosMid;
      
      if (widthShortfall > 0) {
        xPosStart -= widthShortfall;
        xPosMid -= widthShortfall;
        xPosEnd -= widthShortfall;
      }
      if (xPosMid > maxXPos) {
        offscreenXRight = xPosMid; 
        continue;
      }
      if (xPosMid < HALF_EVENT_WIDTH) {
        offscreenXLeft = xPosMid;
        break;
      }

      // We enclose the date label in an extra-wide widget to ensure that it's centered
      // over the hash mark.
      String dateString = DateUtil.formatDate(startDate);
      if (!startDate.equals(endDate)) {
        dateString += " - " + DateUtil.formatDate(endDate);
      }
      Label dateLabel = new Label(dateString);
      dateLabel.setStylePrimaryName("timelineDate");
      dateLabel.setWidth(DATE_LABEL_WIDTH + "px");
      dateLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

      SimplePanel hashMark = new SimplePanel();
      hashMark.setStylePrimaryName("hashMark");
      hashMark.setWidth((xPosEnd - xPosStart + 1) + "px");
      
      TimelineData<T> timelineData = events.get(interval);
      Label eventLabel = new Label(timelineData.getLabel(), true);
      eventLabel.setStylePrimaryName("timelineEvent");
      eventLabel.setWidth(EVENT_WIDTH + "px");
      int eventBoxY = eventInsertionHeight + HASHMARK_HEIGHT / 2;
      eventLabel.getElement().getStyle().setPropertyPx(
          "maxHeight", heightInPixels - eventBoxY);
      final T data = timelineData.getData();
      if (onClickBehavior != null && data != null) {
        eventLabel.addStyleName("clickableTimelineEvent");
        eventLabel.addStyleName("secondaryLink");
        eventLabel.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            onClickBehavior.onClick(event, data);
          }
        });
      }
      
      absolutePanel.add(dateLabel, xPosMid- DATE_LABEL_WIDTH / 2,
          eventInsertionHeight - DATE_LABEL_OFFSET);
      absolutePanel.add(hashMark, xPosStart, eventInsertionHeight - HASHMARK_HEIGHT / 2);
      absolutePanel.add(eventLabel, xPosMid - HALF_EVENT_WIDTH + EVENT_OFFSET, eventBoxY);

      previousXPosMid = xPosMid;
    }
    
    // offScreenXLeft will be < HALF_EVENT_WIDTH iff it was actually set. Similarly for
    // offScreenXRight.
    leftArrow.setStylePrimaryName(offscreenXLeft < HALF_EVENT_WIDTH
        ? "enabledArrowHead" : "disabledArrowHead");
    rightArrow.setStylePrimaryName(offscreenXRight > maxXPos
        ? "enabledArrowHead" : "disabledArrowHead");
  }
  
  private int mapDateToPixelPosition(Date date) {
    long daysAfterStart = DateUtil.numberOfDaysApart(displayStarts, date);
    return Math.round(daysAfterStart * pixelsPerDay + HALF_EVENT_WIDTH);
  }
  
  public static class Interval implements Comparable<Interval> {
    private Date startDateTime;
    private Date endDateTime;
    
    public Interval(Date startDateTime, Date endDateTime) {
      this.startDateTime = startDateTime;
      this.endDateTime = endDateTime;
    }

    /**
     * A utility constructor; leverages DateUtil.makeDate {@link DateUtil}
     */
    public Interval(int y1, int m1, int d1, int y2, int m2, int d2) {
      this(DateUtil.makeDate(y1, m1, d1), DateUtil.makeDate(y2, m2, d2));
    }
    
    public Date getStartDateTime() {
      return startDateTime;
    }
    
    public Date getEndDateTime() {
      return endDateTime;
    }

    @Override
    public int compareTo(Interval rhs) {
      int t = startDateTime.compareTo(rhs.startDateTime);
      return (t == 0) ? endDateTime.compareTo(rhs.endDateTime) : t;
    }

  }
  
  /**
   * Classes that implement this interface encapsulate a behavior triggered when the user clicks
   * on a timeline label. (Timeline labels have generic data associated with them of type T.) 
   */
  public interface OnClickBehavior<T> {
    void onClick(ClickEvent event, T arg);
  }
}
