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
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasWordWrap;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;
import com.google.livingstories.client.UserRpcService;
import com.google.livingstories.client.UserRpcServiceAsync;
import com.google.livingstories.client.util.LivingStoryData;

/**
 * A pane containing links to manage the user's state, and for showing
 * the user controls relating to the current story or seeing all stories.
 */
public class LivingStoryManagementLinks extends Composite {
  interface MyUiBinder extends UiBinder<HTMLPanel, LivingStoryManagementLinks> {}
  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
  private final UserRpcServiceAsync userInfoService = GWT.create(UserRpcService.class);

  @UiField HTMLPanel root;
  @UiField InlineLabel subscribeLink;
  @UiField InlineLabel unsubscribeLink;
  @UiField InlineLabel subscribeLinkSeparator;
  @UiField Anchor rssFeed;
  
  public LivingStoryManagementLinks() {
    initWidget(uiBinder.createAndBindUi(this));
    makePanelChildrenNonWrapping();

    setSubscribeLinkVisibility();
    rssFeed.setHref("/feeds/" + LivingStoryData.getLivingStoryUrl());
  }

  @UiHandler({"subscribeLink", "unsubscribeLink"})
  void handleClick(ClickEvent e) {
    new SubscribePopup(this, !LivingStoryData.isSubscribedToEmails())
        .showRelativeTo((UIObject) e.getSource());
  }
  
  public void setSubscribed(final boolean subscribed) {
    userInfoService.setSubscribedToEmails(LivingStoryData.getLivingStoryId(), subscribed,
        LocaleInfo.getCurrentLocale().getLocaleName(),
        new AsyncCallback<Void>() {
      public void onFailure(Throwable t) {
        // Ignore the error for now, user can just click again.
      }
      public void onSuccess(Void ignore) {
        LivingStoryData.setSubscribedToEmails(subscribed);
        setSubscribeLinkVisibility();
      }
    });
  }

  private void setSubscribeLinkVisibility() {
    if (LivingStoryData.getLoginUrl() == null) {
      // Login url may be null if we're using the stub login service that doesn't allow
      // user logins.  If so, hide the subscribe stuff.
      unsubscribeLink.setVisible(false);
      subscribeLink.setVisible(false);
      subscribeLinkSeparator.setVisible(false);
    } else {
      boolean canUnsubscribe =
          LivingStoryData.isLoggedIn() && LivingStoryData.isSubscribedToEmails();
      unsubscribeLink.setVisible(canUnsubscribe);
      subscribeLink.setVisible(!canUnsubscribe);
    }
  }
  
  public void makePanelChildrenNonWrapping() {
    for (Widget child : root) {
      if (child instanceof HasWordWrap) {
        ((HasWordWrap) child).setWordWrap(false);
      }
    }
  }
}
