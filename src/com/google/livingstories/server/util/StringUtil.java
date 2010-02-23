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

package com.google.livingstories.server.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utilities for use on the server side to parse and modify strings.
 * In particular, functions that use the java.util.regex.* libraries
 * aren't GWT compatible, and belong here.
 */
public class StringUtil {
  
  // Prevent instantiation
  private StringUtil() {}
  
  private static final Pattern JS_LINK_PATTERN = Pattern.compile(
      "<a[^>]+javascript:[^>]+>(.+?)</a>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

  /**
   * Strips javascript links out of the content.  Useful when sending html in an email
   * or RSS feed so that we don't include links that won't work.
   */
  public static String stripJsLinks(String content) {
    if (content == null) {
      return null;
    }
    Matcher matcher = JS_LINK_PATTERN.matcher(content);
    StringBuffer sb = new StringBuffer();
    while (matcher.find()) {
      matcher.appendReplacement(sb, "$1");
    }
    matcher.appendTail(sb);
    return sb.toString();
  }
  
  private static final Pattern TIMELINE_PATTERN = Pattern.compile(
      "<code[^>]*>lsp:timeline</code>", Pattern.CASE_INSENSITIVE);

  /**
   * Strips timeline tags out of the content.  Useful when displaying content without
   * the ContentRenderer so we don't get random content showing up.
   */  
  public static String stripTimelineTags(String content) {
    if (content == null) {
      return null;
    }
    Matcher matcher = TIMELINE_PATTERN.matcher(content);
    StringBuffer sb = new StringBuffer();
    while (matcher.find()) {
      matcher.appendReplacement(sb, "");
    }
    matcher.appendTail(sb);
    return sb.toString();    
  }
  
  public static String stripForExternalSites(String content) {
    return stripJsLinks(stripTimelineTags(content));
  }
}
