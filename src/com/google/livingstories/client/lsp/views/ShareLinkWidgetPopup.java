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
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class ShareLinkWidgetPopup extends PopupPanel {

  private static ShareLinkWidgetPopupUiBinder uiBinder =
      GWT.create(ShareLinkWidgetPopupUiBinder.class);

  interface ShareLinkWidgetPopupUiBinder extends UiBinder<Widget, ShareLinkWidgetPopup> {
  }

  @UiField
  TextBox shareBox;

  public ShareLinkWidgetPopup() {
    super(true /* autohide */);
    setWidget(uiBinder.createAndBindUi(this));
    addStyleName("sharePopup");
  }
  
  public void showRelativeTo(Widget widget, String shareBoxText) {
    shareBox.setText(shareBoxText);
    showRelativeTo(widget);
  }

  @UiHandler("shareBox")
  void onClick(ClickEvent e) {
    shareBox.selectAll();
  }
}
