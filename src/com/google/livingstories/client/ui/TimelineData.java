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

package com.google.livingstories.client.ui;

/**
 * The value class for the different maps the user passes in when loading a TimelineWidget.
 * A TimelineData instance may contain a null key, in which case no onclick behavior will
 * be in effect for the corresponding label. 
 */
public class TimelineData<T> {
  private String label;
  private T data;

  public TimelineData(String label, T data) {
    this.label = label;
    this.data = data;
  }

  public String getLabel() {
    return label;
  }

  public T getData() {
    return data;
  }
}
