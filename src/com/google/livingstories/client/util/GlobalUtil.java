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

import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * A utility class containing data and widget manipulations common to several classes
*/
public class GlobalUtil {
  
  /**
   * Copies a set, returning an empty set in the case of a null incoming
   * argument.
   * @param set the set, allowably null.
   */
  public static <T> Set<T> copySet(Set<T> set) {
    return set == null ? new HashSet<T>() : new HashSet<T>(set);
  }

  /**
   * Joins a collection of strings into one with the specified delimiter.
   */
  public static String join(String delimiter, Collection<String> strings) {
    if (strings.isEmpty()) {
      return "";
    }
    Iterator<String> i = strings.iterator();
    StringBuilder sb = new StringBuilder(i.next());
    while (i.hasNext()) {
      sb.append(delimiter);
      sb.append(i.next());
    }
    return sb.toString();
  }
  
  public static void addIfNotNull(Panel panel, Widget newWidget) {
    if (newWidget != null) {
      panel.add(newWidget);
    }
  }
  
  public static boolean isContentEmpty(String content) {
    // Sometimes the rich-text editor puts line breaks into the content if you click into it
    // and don't type anything. We don't want to display the atom in this case.
    return content == null || content.isEmpty() || content.equals("\n") || content.equals("<br>");
  }
  
  // Implementation copied from com.google.common.base.Objects
  public static boolean equal(Object a, Object b) {
    return a == b || (a != null && a.equals(b));
  }
}
