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

package com.google.livingstories.client.contentmanager;

/**
 * Interface to represent the messages contained in resource bundle:
 * 	ContentManagerMessages.properties
 */
public interface ContentManagerMessages extends com.google.gwt.i18n.client.Messages {
  
  /**
   * Translated "{0} - {1} ".
   * 
   * @return translated "{0} - {1} "
   */
  @DefaultMessage("{0} - {1} ")
  @Key("dateRange")
  String dateRange(String arg0,  String arg1);
}
