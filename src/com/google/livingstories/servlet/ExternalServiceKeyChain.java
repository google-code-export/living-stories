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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.mail.internet.InternetAddress;
import javax.servlet.ServletContext;

/**
 * A class that reads externalServiceKeys.properties and returns appropriate information
 * on the service keys contained therein to response servlets
 */
public class ExternalServiceKeyChain {
  private static final String FILENAME = "/WEB-INF/externalServiceKeys.properties";
  private static final String FRIEND_CONNECT_PROP_KEY = "friendConnectSiteId";
  private static final String MAPS_PROP_KEY = "mapsKey";
  private static final String ANALYTICS_ACCOUNT_PROP_KEY= "analyticsAccountId";
  private static final String FROM_ADDRESS_NAME = "fromAddressName";
  private static final String FROM_ADDRESS_EMAIL = "fromAddressEmail";
  
  private String[] friendConnectArray;
  private String mapsKey;
  private String analyticsAccountId;
  private InternetAddress fromAddress;
  
  
  public ExternalServiceKeyChain(ServletContext context) {
    Properties properties = new Properties();
    try {
      InputStream stream = context.getResourceAsStream(FILENAME);
      properties.load(stream);

      String fcProp = safeGetProperty(properties, FRIEND_CONNECT_PROP_KEY);
      friendConnectArray = fcProp.isEmpty() ? new String[0]: fcProp.split("\\s*,\\s*"); 

      assert friendConnectArray.length % 2 == 0;
      mapsKey = safeGetProperty(properties, MAPS_PROP_KEY);
      analyticsAccountId = safeGetProperty(properties, ANALYTICS_ACCOUNT_PROP_KEY);
      fromAddress = new InternetAddress(safeGetProperty(properties, FROM_ADDRESS_EMAIL),
          safeGetProperty(properties, FROM_ADDRESS_NAME));
      
      stream.close();
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }

  /**
   * Wrapper around Properties.getProperty() that translates null to "".
   */
  public String safeGetProperty(Properties properties, String name) {
    String ret = properties.getProperty(name);
    return ret == null ? "" : ret;
  }
  
  public String getFriendConnectSiteId(String hostname) {
    for (int i = 0; i < friendConnectArray.length; i += 2) {
      if (hostname.endsWith(friendConnectArray[i])) {
        return friendConnectArray[i + 1];
      }
    }
    return "";
  }
  
  public String getMapsKey() {
    return mapsKey;
  }
  
  public String getAnalyticsAccountId() {
    return analyticsAccountId;
  }
  
  public InternetAddress getFromAddress() {
    return fromAddress;
  }
}
