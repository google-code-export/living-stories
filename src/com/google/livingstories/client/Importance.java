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

package com.google.livingstories.client;

import com.google.gwt.core.client.GWT;

/**
 * Enum used for specifying importance of documents and updates.
 */
public enum Importance {
  HIGH(0), MEDIUM(1), LOW(2);
  
  private int value;
  
  private Importance(int value) {
    this.value = value;
  }
  
  public int getValue() {
    return value;
  }
  
  @Override
  public String toString() {
    return GWT.isClient() ? EnumTranslator.translate(this)
        : this.name() + " (localization not supported server-side)";
  }
  
  public static Importance getFromValue(int value) {
    for (Importance imp : values()) {
      if (imp.getValue() == value) {
        return imp;
      }
    }
    return null;
  }
}
