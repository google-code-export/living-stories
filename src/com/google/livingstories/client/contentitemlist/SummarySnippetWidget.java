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

package com.google.livingstories.client.contentitemlist;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.livingstories.client.ContentItemType;
import com.google.livingstories.client.BaseContentItem;
import com.google.livingstories.client.EventContentItem;
import com.google.livingstories.client.Importance;
import com.google.livingstories.client.NarrativeContentItem;
import com.google.livingstories.client.lsp.ContentRenderer;
import com.google.livingstories.client.util.GlobalUtil;
import com.google.livingstories.client.util.SnippetUtil;
import com.google.livingstories.client.util.dom.GwtNodeAdapter;

/**
 * Widget that renders a narrative/event summary, depending on the read state and importance.
 * 
 * This is a guide for what gets shown in each case:
 *                     |              UNREAD                |                  READ
 * Event     - High    | Full summary                       | Full summary
 *           - Medium  | Full summary                       | Short summary snippet
 *           - Low     | Nothing                            | Nothing
 * Narrative - High    | Full summary/long content snippet  | Full summary/long content snippet
 *           - Medium  | Full summary/short content snippet | Short summary or content snippet
 *           - Low     | Nothing                            | Nothing
 * 
 * Note that the constructor of this class is private; you should use the static 'create' method
 * to instantiate it.  This allows us to differentiate between different content item types, and
 * may also return null if we don't want to render anything (e.g. if the content item is
 * unimportant).
 */
public class SummarySnippetWidget extends Composite {
  public static SummarySnippetWidget create(BaseContentItem contentItem) {
    if (contentItem.getImportance() != Importance.LOW) {
      if (contentItem.getContentItemType() == ContentItemType.EVENT) {
        EventContentItem event = (EventContentItem) contentItem;
        if (!GlobalUtil.isContentEmpty(event.getEventSummary())) {
          return new SummarySnippetWidget(event);
        }
      } else if (contentItem.getContentItemType() == ContentItemType.NARRATIVE) {
        NarrativeContentItem narrative = (NarrativeContentItem) contentItem;
        if (!GlobalUtil.isContentEmpty(narrative.getNarrativeSummary())
            || !GlobalUtil.isContentEmpty(narrative.getContent())) {
          return new SummarySnippetWidget(narrative);
        }
      }
    }
    return null;
  }
  
  private static final int LONG_SNIPPET_LENGTH = 450;
  private static final int SHORT_SNIPPET_LENGTH = 200;
  
  private SummarySnippetWidget(EventContentItem eventItem) {
    String summary = eventItem.getEventSummary();
    if (eventItem.getImportance() == Importance.HIGH || !eventItem.getRenderAsSeen()) {
      // If it's important or unread, render the summary.
      initWidget(createSummaryWidget(summary));
    } else {
      // Otherwise, render a short summary snippet.
      initWidget(createSnippetWidget(summary, true));
    }
  }

  private SummarySnippetWidget(NarrativeContentItem narrativeItem) {
    String summary = narrativeItem.getNarrativeSummary();
    String content = narrativeItem.getContent();
    boolean isSummaryAvailable = !GlobalUtil.isContentEmpty(summary);

    if (narrativeItem.getImportance() == Importance.HIGH || !narrativeItem.getRenderAsSeen()) {
      if (isSummaryAvailable) {
        initWidget(createSummaryWidget(summary));
      } else {
        initWidget(createSnippetWidget(content,
            narrativeItem.getImportance() != Importance.HIGH || narrativeItem.getRenderAsSeen()));
      }
    } else {
      initWidget(createSnippetWidget(isSummaryAvailable ? summary : content, true));
    }        
  }
  
  private Widget createSnippetWidget(String contentToSnippetize, boolean makeShort) {
    String snippetHTML = SnippetUtil.createSnippet(GwtNodeAdapter.fromHtml(contentToSnippetize), 
        makeShort ? SHORT_SNIPPET_LENGTH : LONG_SNIPPET_LENGTH);
    if (snippetHTML == null || snippetHTML.isEmpty()) {
      return null;
    } else {
      Widget widget = new SimplePanel();
      widget.getElement().setInnerHTML(snippetHTML);
      return widget;
    }
  }
  
  private Widget createSummaryWidget(String summary) {
    if (GlobalUtil.isContentEmpty(summary)) {
      return null;
    } else {
      return new ContentRenderer(summary, false);
    }
  }
}
