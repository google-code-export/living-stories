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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Widget;
import com.google.livingstories.client.lsp.LspMessageHolder;
import com.google.livingstories.client.util.AnalyticsUtil;
import com.google.livingstories.client.util.LivingStoryData;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that provides functionality to display a piece of text that is separated by 
 * <break></break> tags. The first "chunk" of the text is displayed followed by a 'Read more'
 * link. When clicked, the link disappears and the next chunk is displayed and so on. It also
 * handles the case when we may might want to ignore chunk boundaries, eg. to highlight new
 * content.
 */
public class ReadMoreLink extends Link {
  private List<Widget> chunks = new ArrayList<Widget>();
  private boolean trackReadMoreWithAnalytics = false;
  private static String READ_MORE = LspMessageHolder.consts.readMore();
  
  public ReadMoreLink(String label, boolean trackReadMoreWithAnalytics, Widget... chunks) {
    super(label);
    this.trackReadMoreWithAnalytics = trackReadMoreWithAnalytics;
    this.setVisible(false);

    for (Widget chunk : chunks) {
      if (chunk != null) {
        this.chunks.add(chunk);
      }
    }
  }
  
  public ReadMoreLink(boolean trackReadMoreWithAnalytics, Widget... chunks) {
    this(READ_MORE, trackReadMoreWithAnalytics, chunks);
  }
  
  /**
   * When a 'read more' link is clicked, it should display the associated chunk and the
   * next 'read more' link if there is one. It should also hide itself.
   */
  @Override
  protected void onClick(ClickEvent e) {
    for (Widget chunk : chunks) {
      chunk.setVisible(true);
    }
    this.setVisible(false);
    if (trackReadMoreWithAnalytics) {
      AnalyticsUtil.trackSummaryExpansion(LivingStoryData.getLivingStoryUrl());
    }
  }
}
