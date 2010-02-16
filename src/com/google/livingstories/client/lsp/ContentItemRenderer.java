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

package com.google.livingstories.client.lsp;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.livingstories.client.BaseContentItem;
import com.google.livingstories.client.ui.SourceInfoWidget;

import java.util.Set;

/**
 * Class that renders a content item or a given content widget in a manner similar to a top-level
 * event block, for the story views broken down by event type.
 */
public class ContentItemRenderer extends Composite {
  private BaseContentItem contentItem;
  private boolean includeHeader = true;
  
  private Widget contentWidget;
  private FlowPanel contentItemPanel;
  
  public ContentItemRenderer(BaseContentItem contentItem, boolean includeHeader,
      boolean renderPreview, Set<Long> containingContributorIds) {
    super();
    this.contentItem = contentItem;
    this.includeHeader = includeHeader;
    this.contentWidget = renderPreview ? 
        contentItem.renderPreview() : contentItem.renderContent(containingContributorIds);
    
    contentItemPanel = new FlowPanel();
    contentItemPanel.addStyleName("baseContentItemPanel");
    populate();
    initWidget(contentItemPanel);
  }

  public ContentItemRenderer(BaseContentItem contentItem, boolean includeHeader,
      boolean renderPreview) {
    this(contentItem, includeHeader, renderPreview, null);
  }
  
  public boolean wholeRendererIsLink() {
    return contentWidget.getStyleName().contains("wholeRenderIsLink");
  }
  
  private void populate() {
    String titleString = contentItem.getTitleString();
    if (includeHeader && !titleString.isEmpty()) {
      Label titleLabel = new Label(titleString);
      titleLabel.addStyleName("contentItemHeader");
      contentItemPanel.add(titleLabel);
    } else {
      // we want to give a little extra space where the title would have been 
      contentItemPanel.addStyleName("substituteHeaderSpace");
    }
    contentItemPanel.add(contentWidget);
    
    // Add source information after the content if it exists
    if (contentItem.hasSourceInformation()) {
      contentItemPanel.add(new SourceInfoWidget(contentItem));
    }
  }
}
