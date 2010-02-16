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
import com.google.gwt.user.client.ui.HTML;

/**
 * Widget that displays a small preview of some content.
 * Currently this just means displaying all the content up to
 * the first 'break' tag, but we'll probably want to add a more advanced
 * algorithm later on to truncate after/up to a certain number of characters.
 * (This isn't currently done because we would need to handle mismatched
 * html tags)
 */
public class TruncatedContent extends Composite {
  private static final String BREAK_TAG = "<break></break>";

  private HTML content;
  
  public TruncatedContent(String htmlContent) {
    content = new HTML(htmlContent.split(BREAK_TAG)[0]);
    initWidget(content);
  }
}
