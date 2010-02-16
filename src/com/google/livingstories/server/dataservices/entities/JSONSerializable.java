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

package com.google.livingstories.server.dataservices.entities;

import org.json.JSONObject;

/**
 * Interface that defines a way to get a JSON serialization of an object.
 * This interface is used by the DataExportServlet.
 * 
 * Note that implementers of JSONSerializable for use in the DataExportServlet
 * should also implement a static factory method called 'fromJSON' which takes
 * a JSONObject and instantiates an instance of the given class.
 * This method is called via reflection from the DataImportServlet.
 */
public interface JSONSerializable {
  JSONObject toJSON();
}
