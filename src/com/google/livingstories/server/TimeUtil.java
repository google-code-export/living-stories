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

package com.google.livingstories.server;

import org.joda.time.Period;
import org.joda.time.PeriodType;

import java.util.Date;

/**
 * Utility methods to manipulate time.
 */
public class TimeUtil {
  
  /**
   * Return a String representation of the time that has passed from the given time to
   * right now.
   * This method returns an approximate user-friendly duration. Eg. If 2 months, 12 days and 4 hours
   * have passed, the method will return "2 months ago".
   */
  public static String getElapsedTimeString(Date updateCreationTime) {
    Period period = new Period(updateCreationTime.getTime(), new Date().getTime(),
        PeriodType.yearMonthDayTime());

    int years = period.getYears();
    int months = period.getMonths();
    int days = period.getDays();
    int hours = period.getHours();
    int minutes = period.getMinutes();
    int seconds = period.getSeconds();
    
    String timeLabel = "";

    if (years > 0) {
      timeLabel = years == 1 ? " year " : " years ";
      return "" + years + timeLabel + "ago";
    } else if (months > 0) {
      timeLabel = months == 1 ? " month " : " months ";
      return "" + months + timeLabel + "ago";
    } else if (days > 0) {
      timeLabel = days == 1 ? " day " : " days ";
      return "" + days + timeLabel + "ago";
    } else if (hours > 0) {
      timeLabel = hours == 1 ? " hour " : " hours ";
      return "" + hours + timeLabel + "ago";
    } else if (minutes > 0) {
      timeLabel = minutes == 1 ? " minute " : " minutes ";
      return "" + minutes + timeLabel + "ago";
    } else if (seconds > 0) {
      timeLabel = seconds == 1 ? " second " : " seconds ";
      return "" + seconds + timeLabel + "ago";
    } else {
      return "1 second ago";
    }
  }

}
