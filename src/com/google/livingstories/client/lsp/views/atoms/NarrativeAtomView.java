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

package com.google.livingstories.client.lsp.views.atoms;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.livingstories.client.NarrativeAtom;
import com.google.livingstories.client.lsp.BylineWidget;
import com.google.livingstories.client.lsp.ContentRenderer;
import com.google.livingstories.client.util.GlobalUtil;

import java.util.Set;

/**
 * Renders a narrative with the narrative type as the header.
 */
public class NarrativeAtomView extends Composite {

  private static NarrativeAtomViewUiBinder uiBinder = GWT.create(NarrativeAtomViewUiBinder.class);

  interface NarrativeAtomViewUiBinder extends UiBinder<Widget, NarrativeAtomView> {
  }

  @UiField Label narrativeType;
  @UiField Label headline;
  @UiField SimplePanel byline;
  @UiField FlowPanel summary;
  @UiField(provided=true) BaseAtomPreview content;
  
  public NarrativeAtomView(NarrativeAtom atom, Set<Long> containingContributorIds) {
    content = new BaseAtomPreview(atom);
    initWidget(uiBinder.createAndBindUi(this));
    narrativeType.setText(atom.getNarrativeType().toString());
    headline.setText(atom.getHeadline());
    
    Widget bylineWidget = BylineWidget.makeContextSensitive(atom, containingContributorIds);
    if (bylineWidget != null) {
      byline.add(bylineWidget);
    }

    String summaryText = atom.getNarrativeSummary();
    if (!GlobalUtil.isContentEmpty(summaryText)) {
      summary.add(new ContentRenderer(summaryText, false));
      summary.add(new Label("--"));
    }
  }
}
