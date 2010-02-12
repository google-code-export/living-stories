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

import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasWordWrap;
import com.google.livingstories.client.lsp.LspMessageHolder;

/**
 * Link that opens a feedback form in a new window.
 */
public class FeedbackWidget extends Composite implements HasWordWrap {
  private static final String LINK_TEXT = LspMessageHolder.consts.feedback();
  private static final String SURVEY_URL =
      "http://www.google.com/support/News/bin/request.py?contact_type=living_story";
  private Anchor feedbackLink;
  
  public FeedbackWidget() {
    super();
    feedbackLink = new Anchor(LINK_TEXT, SURVEY_URL, "_blank");
    initWidget(feedbackLink);
  }

  @Override
  public boolean getWordWrap() {
    return feedbackLink.getWordWrap();
  }

  @Override
  public void setWordWrap(boolean wrap) {
    feedbackLink.setWordWrap(wrap);
  }
}
