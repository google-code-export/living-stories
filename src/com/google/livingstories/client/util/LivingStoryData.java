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

import com.google.gwt.core.client.JavaScriptObject;

import java.util.Date;

/**
 * Utility class to easily get living story data values from the page via JSNI
 * The getters all check that LIVING_STORY exists so we don't throw exceptions,
 * since these methods may be called by things like EventBlockWidget,
 * which are also used outside the context of an LSP
 * (in the contentmanager previews, for example).
 */
public class LivingStoryData {
  private static Date cookieBasedLastVisitDate;
  
  private static native LivingStoryObject getLivingStoryData() /*-{
    if (!$wnd.LIVING_STORY) {
      $wnd.LIVING_STORY = {};
    }
    return $wnd.LIVING_STORY;
  }-*/;
  
  public static Long getLspId() {
    long lspId = getLivingStoryData().getIntValue("ID");
    return lspId < 0 ? null : lspId;
  }
  
  public static void setLspId(Long lspId) {
    getLivingStoryData().setValue("ID", lspId == null ? -1 : lspId.intValue());
  }
  
  public static String getLspTitle() {
    return getLivingStoryData().getStringValue("TITLE");
  }
  
  public static String getLspUrl() {
    return getLivingStoryData().getStringValue("STORY_URL");
  }
  
  public static String getLspPublisher() {
    return getLivingStoryData().getStringValue("PUBLISHER");
  }
  
  public static String getSummary() {
    return getLivingStoryData().getStringValue("SUMMARY");
  }
  
  public static Date getLastVisitDate() {
    Date ret = getLivingStoryData().getDateValue("LAST_VISIT_DATE");
    
    if (ret == null) {
      ret = cookieBasedLastVisitDate;
    }
    
    return ret;
  }
  
  public static void setCookieBasedLastVisitDate(Date date) {
    cookieBasedLastVisitDate = date;
  }
   
  public static boolean isSubscribedToEmails() {
    return getLivingStoryData().getBooleanValue("SUBSCRIPTION_STATUS");
  }
  
  public static void setSubscribedToEmails(boolean value) {
    getLivingStoryData().setValue("SUBSCRIPTION_STATUS", value);
  }
  
  public static String getSubscribeUrl() {
    return getLivingStoryData().getStringValue("SUBSCRIBE_URL");
  }
  
  public static String getDefaultPage() {
    return getLivingStoryData().getStringValue("DEFAULT_PAGE");
  }
  
  public static void setDefaultPage(String defaultPage) {
    getLivingStoryData().setValue("DEFAULT_PAGE", defaultPage);
  }

  public static boolean isLoggedIn() {
    return getUsername() != null;
  }
  
  public static String getUsername() {
    return getLivingStoryData().getStringValue("USER_NAME");
  }
  
  public static String getLoginUrl() {
    return getLivingStoryData().getStringValue("LOGIN_URL");
  }
  
  public static String getLogoutUrl() {
    return getLivingStoryData().getStringValue("LOGOUT_URL");
  }

  public static String getFriendConnectSiteId() {
    return getLivingStoryData().getStringValue("FRIEND_CONNECT_SITE_ID");
  }
  
  public static JavaScriptObject getStoryVisitTimes() {
    return getLivingStoryData().getObjectValue("LAST_VISIT_TIMES");
  }
  
  private static final class LivingStoryObject extends JavaScriptObject {
    @SuppressWarnings("unused")
    protected LivingStoryObject() {}
    
    public native String getStringValue(String key) /*-{
      return this[key];
    }-*/;
    
    public native boolean getBooleanValue(String key) /*-{
      return this[key];
    }-*/;

    public native int getIntValue(String key) /*-{
      return this[key] ? this[key] : -1;
    }-*/;

    public native Date getDateValue(String key) /*-{
      if (this[key]) {
        return @java.util.Date::new(Ljava/lang/String;)(this[key]);
      } else {
        return null;
      }
      return this[key];
    }-*/;
    
    public native JavaScriptObject getObjectValue(String key) /*-{
      return this[key];
    }-*/;
    
    public native void setValue(String key, String value) /*-{
      this[key] = value;
    }-*/;

    public native void setValue(String key, boolean value) /*-{
      this[key] = value;
    }-*/;

    public native void setValue(String key, int value) /*-{
      this[key] = value;
    }-*/;

  }
}
