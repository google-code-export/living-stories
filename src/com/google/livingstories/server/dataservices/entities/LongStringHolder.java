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
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

/**
 *
 * This is a wrapper class to use for persisting long strings (possibly >500 characters)
 * in the datastore. The annotations as set-up by default work for supporting
 * a Google App Engine datastore, but if you're using a different backing datastore instead
 * , e.g., MySQL, you can should alter this by swapping the Persistent/NotPersistent annotations
 * on textValue and stringValue.
 */
@PersistenceCapable
@EmbeddedOnly
public class LongStringHolder {
  @NotPersistent
  private Text textValue;
  
  @Persistent
  private String stringValue;
  
  public LongStringHolder(String value) {
    textValue = new Text(value);
    stringValue = value;
  }
    
  public void setTextValue(Text textValue) {
    this.textValue = textValue;
  }
  
  public void setStringValue(String stringValue) {
    this.stringValue = stringValue;
  }
  
  public String getValue() {
    // If LongStringHolder was instantiated by retrieval from the datastore, one of textValue or
    // stringValue should be null. In the default implementation, the null field will be
    // stringValue, but this can be overridden as described above. Instances of the class that
    // were explicitly instantiated will have valid settings for both textValue and stringValue,
    // but this is okay.
    return (textValue != null) ? textValue.getValue() : stringValue;
  }
}
