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

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.livingstories.client.BaseAtom;
import com.google.livingstories.client.NarrativeAtom;
import com.google.livingstories.client.lsp.ContentRenderer;
import com.google.livingstories.client.util.FourthestateUtil;

import java.util.Date;
import java.util.Map;

/**
 * A narrative atom as a complex element in the atom list.
 */
public class NarrativeAtomListElement extends ComplexAtomListElement {
  private NarrativeAtom narrative;
  
  public NarrativeAtomListElement(BaseAtom atom, Map<Long, BaseAtom> allAtomsMap,
      boolean noHistoryOnToggle) {
    super(atom, allAtomsMap, noHistoryOnToggle);
    narrative = (NarrativeAtom)atom;
  }
  
  @Override
  public Date getStartDate() {
    return narrative.getDateSortKey();
  }

  @Override
  public Date getEndDate() {
    return null;
  }
  
  @Override
  public String getHeadlineHtml() {
    return narrative.getHeadline() + "<span style=\"color: #777;\">&nbsp;-&nbsp;" 
        + narrative.getNarrativeType().toString() + "</span>";
  }

  @Override
  public SummarySnippetWidget getSummarySnippetWidget() {
    return new SummarySnippetWidget(narrative, alreadySeen);
  }
  
  @Override
  public Widget getDetailsWidget() {
    Widget contentWidget = new ContentRenderer(narrative.getContent(), false);
    if (FourthestateUtil.isContentEmpty(narrative.getNarrativeSummary())) {
      return contentWidget;
    } else {
      FlowPanel panel = new FlowPanel();
      panel.add(new Label("--"));
      panel.add(contentWidget);
      return panel;
    }
  }
}
