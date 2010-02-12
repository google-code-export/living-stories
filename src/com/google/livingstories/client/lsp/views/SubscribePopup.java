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
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.livingstories.client.util.LivingStoryData;

public class SubscribePopup extends PopupPanel {

  private static SubscribePopupUiBinder uiBinder = GWT.create(SubscribePopupUiBinder.class);

  interface SubscribePopupUiBinder extends UiBinder<Widget, SubscribePopup> {
  }

  @UiField
  HTML subscribeInstructions;
  @UiField
  HTML logInAndSubscribeInstructions;
  @UiField
  HTML unsubscribeInstructions;
  
  @UiField
  Button subscribe;
  @UiField
  Button logInAndSubscribe;
  @UiField
  Button unsubscribe;
  @UiField
  Button cancel;
  
  @UiField
  SpanElement title0, title1, title2;
  @UiField
  SpanElement username;
  
  private LivingStoryManagementLinks managementLinks;
  private boolean subscribing;
  
  public SubscribePopup(LivingStoryManagementLinks managementLinks, boolean subscribing) {
    super(true /* autohide */, true /* modal */);
    this.managementLinks = managementLinks;
    this.subscribing = subscribing;
    
    Widget content = uiBinder.createAndBindUi(this);
    this.setWidget(content);

    // show the appropriate explanation text and button, depending on the user's state
    if (subscribing) {
      if (LivingStoryData.isLoggedIn()) {
        subscribeInstructions.setVisible(true);
        subscribe.setVisible(true);
      } else {
        logInAndSubscribeInstructions.setVisible(true);
        logInAndSubscribe.setVisible(true);
      }
    } else {   // unsubscribing
      unsubscribeInstructions.setVisible(true);
      unsubscribe.setVisible(true);
    }
    
    String storyTitle = LivingStoryData.getLivingStoryTitle();
    title0.setInnerText(storyTitle);
    title1.setInnerText(storyTitle);
    title2.setInnerText(storyTitle);
    
    username.setInnerText(
        LivingStoryData.getUsername() == null ? "" : LivingStoryData.getUsername());
  }

  @UiHandler({"subscribe", "unsubscribe"})
  void handleSimpleClick(ClickEvent e) {
    hide();
    managementLinks.setSubscribed(subscribing);
  }

  @UiHandler("logInAndSubscribe")
  void handleRedirectClick(ClickEvent e) {
    hide();
    Window.Location.assign(LivingStoryData.getSubscribeUrl());
  }

  @UiHandler("cancel")
  void handleCancelClick(ClickEvent e) {
    hide();
  }
}
