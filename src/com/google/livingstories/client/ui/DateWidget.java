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

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.livingstories.client.util.DateUtil;

import java.util.Date;

/**
 * Widget that takes a series of dates, and displays the first one that is non-null.
 */
public class DateWidget extends Composite {
  private Label dateWidget;
  
  public DateWidget(Date... dates) {
    for (Date date : dates) {
      if (date != null) {
        dateWidget = new Label(DateUtil.formatDate(date));
        break;
      }
    }
    
    dateWidget.setStylePrimaryName("greyFont");
    initWidget(dateWidget);
  }
}
