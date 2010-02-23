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

package com.google.livingstories.client.util;

import com.google.gwt.i18n.client.DateTimeFormat;

import java.util.Date;

/**
 * Miscellaneous date-related utilities. i18n-friendly; calls to static methods in
 * DateTimeFormat.getXFormat() will return a DateTimeFormat instance appropriate to the
 * current locale. Calls into DateTimeFormat.getFormat("[format string]") are not, in
 * general, i18n-friendly. (In principle you could do such things for the default locale,
 * checking via the LocaleInfo class if you're in the default locale, so long as you have
 * a fixed format to fall back on.)
 */
public class DateUtil {
  public static long MILLISECONDS_PER_DAY = 1000 * 60 * 60 * 24;

  // If we define DATE_FORMAT as a direct class constant, then we can't use this class
  // at all from server-side code. Instead, use an additional static class as a holder.
  private static class DateFormatHolder {
    public static final DateTimeFormat MEDIUM_DATE_FORMAT = DateTimeFormat.getMediumDateFormat();
    public static final DateTimeFormat MEDIUM_DATE_TIME_FORMAT =
        DateTimeFormat.getMediumDateTimeFormat();
    public static final DateTimeFormat SHORT_TIME_FORMAT = DateTimeFormat.getShortTimeFormat();
    public static final DateTimeFormat SHORT_DATE_FORMAT = DateTimeFormat.getShortDateFormat();
  }

  public static Date getDateMidnight(Date date) {
    return new Date(getDateMidnightMillis(date == null ? new Date() : date));
  }
  
  public static Date getDateMidnight() {
    return getDateMidnight(new Date());
  }
  
  /**
   * returns the number of whole days between d1 and d2. Both dates are normalized to
   * midnight of that day before the comparison is made.
   * @param d1 the earlier date
   * @param d2 the later date
   * @return the number of days apart the 
   */
  public static long numberOfDaysApart(Date d1, Date d2) {
    return (getDateMidnightMillis(d2) - getDateMidnightMillis(d1)) / MILLISECONDS_PER_DAY;
  }

  private static long getDateMidnightMillis(Date date) {
    long millis = date.getTime();
    return millis - millis % MILLISECONDS_PER_DAY;
  }
  
  @SuppressWarnings("deprecation")
  public static Date makeDate(int year, int month, int date) {
    return new Date(year - 1900, month - 1, date);
  }
  
  /**
   *  Call from client-side code only!
   */
  public static String formatDate(Date date) {
    return DateFormatHolder.MEDIUM_DATE_FORMAT.format(date);
  }
  
  /**
   *  Call from client-side code only!
   */
  public static String formatDateTime(Date date) {
    return DateFormatHolder.MEDIUM_DATE_TIME_FORMAT.format(date);
  }
  
  /**
   *  Call from client-side code only!
   */
  public static String formatTime(Date date) {
    return DateFormatHolder.SHORT_TIME_FORMAT.format(date);
  }

  /**
   *  Call from client-side code only.
   *  Implementation note: this is built around the "out"-valued form of the parse
   *  function. It's better to use these 'cause they won't disturb the output parameter
   *  and won't throw an exception if given entirely invalid input.
   */
  public static int parseTime(String text, Date out) {
    return DateFormatHolder.SHORT_TIME_FORMAT.parse(text, 0, out);
  }
  
  /**
   *  Call from client-side code only!
   *  Implementation note: this is built around the "out"-valued form of the parse
   *  function. It's better to use these 'cause they won't disturb the output parameter
   *  and won't throw an exception if given entirely invalid input.
   */
  public static int parseShortDate(String text, Date out) {
    return DateFormatHolder.SHORT_DATE_FORMAT.parse(text, 0, out);
  }

  public static Date laterDate(Date d1, Date d2) {
    if (d1 == null) {
      if (d2 == null) {
        return null;
      } else {
        return d1;
      }
    } else {
      if (d2 == null) {
        return d1;
      } else {
        return d1.after(d2) ? d1 : d2; 
      }
    }
  }
}
