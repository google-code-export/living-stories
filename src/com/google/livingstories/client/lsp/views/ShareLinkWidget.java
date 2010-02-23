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

package com.google.livingstories.client.lsp.views;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;
import com.google.livingstories.client.util.HistoryManager;
import com.google.livingstories.client.util.LivingStoryControls;
import com.google.livingstories.client.util.HistoryManager.HistoryPages;

public class ShareLinkWidget extends Composite {

  private static ShareLinkWidgetUiBinder uiBinder = GWT.create(ShareLinkWidgetUiBinder.class);

  interface ShareLinkWidgetUiBinder extends UiBinder<Widget, ShareLinkWidget> {
  }

  @UiField
  InlineLabel label;
  
  private Long contentItemId;
  private ShareLinkWidgetPopup shareLinkWidgetPopup;

  public ShareLinkWidget(final Long contentItemId) {
    this.contentItemId = contentItemId;
    initWidget(uiBinder.createAndBindUi(this));
    shareLinkWidgetPopup = new ShareLinkWidgetPopup();
  }

  @UiHandler("label")
  void onClick(ClickEvent e) {
    // Consume this event.
    e.stopPropagation();
    shareLinkWidgetPopup.showRelativeTo(label, HistoryManager.getLink(
        HistoryPages.OVERVIEW,
        LivingStoryControls.getCurrentFilterSpec().getFilterParams(),
        String.valueOf(contentItemId)));
  }
}
