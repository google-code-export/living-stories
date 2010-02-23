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

package com.google.livingstories.client.lsp.views.contentitems;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.livingstories.client.NarrativeContentItem;
import com.google.livingstories.client.lsp.BylineWidget;
import com.google.livingstories.client.lsp.ContentRenderer;
import com.google.livingstories.client.util.GlobalUtil;

import java.util.Set;

/**
 * Renders a narrative with the narrative type as the header.
 */
public class NarrativeContentItemView extends Composite {

  private static NarrativeContentItemViewUiBinder uiBinder =
      GWT.create(NarrativeContentItemViewUiBinder.class);

  interface NarrativeContentItemViewUiBinder extends UiBinder<Widget, NarrativeContentItemView> {
  }

  @UiField Label narrativeType;
  @UiField Label headline;
  @UiField SimplePanel byline;
  @UiField FlowPanel summary;
  @UiField(provided=true) BaseContentItemPreview content;
  
  public NarrativeContentItemView(NarrativeContentItem contentItem,
      Set<Long> containingContributorIds) {
    content = new BaseContentItemPreview(contentItem);
    initWidget(uiBinder.createAndBindUi(this));
    narrativeType.setText(contentItem.getNarrativeType().toString());
    headline.setText(contentItem.getHeadline());
    
    Widget bylineWidget = BylineWidget.makeContextSensitive(contentItem, containingContributorIds);
    if (bylineWidget != null) {
      byline.add(bylineWidget);
    }

    String summaryText = contentItem.getNarrativeSummary();
    if (!GlobalUtil.isContentEmpty(summaryText)) {
      summary.add(new ContentRenderer(summaryText, false));
      summary.add(new Label("--"));
    }
  }
}
