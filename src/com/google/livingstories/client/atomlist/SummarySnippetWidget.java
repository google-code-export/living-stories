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

package com.google.livingstories.client.atomlist;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.livingstories.client.AtomType;
import com.google.livingstories.client.BaseAtom;
import com.google.livingstories.client.EventAtom;
import com.google.livingstories.client.NarrativeAtom;
import com.google.livingstories.client.lsp.ContentRenderer;
import com.google.livingstories.client.util.FourthestateUtil;
import com.google.livingstories.client.util.SnippetUtil;
import com.google.livingstories.client.util.dom.GwtNodeAdapter;

/**
 * Widget that shows either summary text or snippet text, but never both at once.
 * 
 * This is a guide for what gets shown in each case for the COLLAPSED view:
 *                     |              UNREAD                |                  READ
 * Event     - High    | Full summary                       | Full summary
 *           - Medium  | Full summary                       | Short summary snippet
 *           - Low     | Nothing                            | Nothing
 * Narrative - High    | Full summary/long content snippet  | Full summary/long content snippet
 *           - Medium  | Full summary/short content snippet | Short summary or content snippet
 *           - Low     | Nothing                            | Nothing
 * 
 * In the EXPANDED view, the full summary is always shown if available.
 */
public class SummarySnippetWidget extends Composite {
  private static final int LONG_SNIPPET_LENGTH = 450;
  private static final int SHORT_SNIPPET_LENGTH = 200;
  
  private SimplePanel contentPanel;
  private Widget expandedWidget;
  private Widget collapsedWidget;
  
  public SummarySnippetWidget(BaseAtom atom, boolean alreadyRead) {
    if (atom.getAtomType() == AtomType.EVENT) {
      String summary = ((EventAtom)atom).getEventSummary();
      expandedWidget = createSummaryWidget(summary);
      switch (atom.getImportance()) {
        case HIGH:
          collapsedWidget = createSummaryWidget(summary);
          break;
        case MEDIUM:
          if (alreadyRead) {
            collapsedWidget = createSnippetWidget(summary, true);
          } else {
            collapsedWidget = createSummaryWidget(summary);
          }
          break;
      }
    } else if (atom.getAtomType() == AtomType.NARRATIVE) {
      NarrativeAtom narrative = (NarrativeAtom)atom;
      String summary = narrative.getNarrativeSummary();
      String content = narrative.getContent();
      boolean isSummaryAvailable = !FourthestateUtil.isContentEmpty(summary);
      if (isSummaryAvailable) {
        expandedWidget = createSummaryWidget(summary);
      }
      switch (atom.getImportance()) {
        case HIGH:
          if (isSummaryAvailable) {
            collapsedWidget = createSummaryWidget(summary);
          } else {
            collapsedWidget = createSnippetWidget(content, false);
          }
          break;
        case MEDIUM:
          if (alreadyRead) {
            if (isSummaryAvailable) {
              collapsedWidget = createSnippetWidget(summary, true);
            } else {
              collapsedWidget = createSnippetWidget(content, true);
            }
          } else {
            if (isSummaryAvailable) {
              collapsedWidget = createSummaryWidget(summary);
            } else {
              collapsedWidget = createSnippetWidget(content, true);
            }
          }
          break;
      }
    }
    
    contentPanel = new SimplePanel();
    initWidget(contentPanel);
    
    collapse();
  }
  
  private Widget createSnippetWidget(String contentToSnippetize, boolean makeItShort) {
    String snippetHTML = SnippetUtil.createSnippet(GwtNodeAdapter.fromHtml(contentToSnippetize), 
        makeItShort ? SHORT_SNIPPET_LENGTH : LONG_SNIPPET_LENGTH);
    if (snippetHTML == null || snippetHTML.isEmpty()) {
      return null;
    } else {
      Widget widget = new SimplePanel();
      widget.getElement().setInnerHTML(snippetHTML);
      return widget;
    }
  }
  
  private Widget createSummaryWidget(String summary) {
    if (FourthestateUtil.isContentEmpty(summary)) {
      return null;
    } else {
      return new ContentRenderer(summary, false);
    }
  }

  public void collapse() {
    if (collapsedWidget != null) {
      contentPanel.setWidget(collapsedWidget);
      contentPanel.setVisible(true);
    } else {
      contentPanel.clear();
      contentPanel.setVisible(false);
    }
  }
  
  public void expand() {
    if (expandedWidget != null) {
      contentPanel.setWidget(expandedWidget);
      contentPanel.setVisible(true);
    } else {
      contentPanel.clear();
      contentPanel.setVisible(false);
    }
  }
}
