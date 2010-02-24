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

import com.google.appengine.api.datastore.Text;

import javax.jdo.annotations.EmbeddedOnly;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

/* non-Javadoc:
 * This implementation leverages appengine-specific facilities. To use
 * other storage methods, replace this file with the contents of LongStringHolder.java.plainstring
 */

@PersistenceCapable
@EmbeddedOnly
public class LongStringHolder implements LongStringHolderInterface {
  @Persistent
  Text value;
  
  public LongStringHolder(String value) {
    this.value = new Text(value);
  }
  
  @Override
  public String getValue() {
    return value == null ? null : value.getValue();
  }
}
