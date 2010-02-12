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
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasWordWrap;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;
import com.google.livingstories.client.util.LivingStoryData;

/**
 * A pane containing links to manage the user's state:
 * Log in/log out, feedback, filing bugs, etc.
 *
 * Owner class for StartPageManagementLinks.ui.xml
 */
public class ManagementLinks extends Composite {

  private static ManagementLinksUiBinder uiBinder = GWT.create(ManagementLinksUiBinder.class);

  interface ManagementLinksUiBinder extends UiBinder<HTMLPanel, ManagementLinks> {
  }
  
  @UiField HTMLPanel root;
  @UiField InlineLabel username;
  @UiField InlineLabel usernameSeparator;
  @UiField Anchor logIn;
  @UiField Anchor logOut;
  
  public ManagementLinks() {
    bind();
    
    String usernameString = LivingStoryData.getUsername();
    if (LivingStoryData.getUsername() == null) {
      logIn.setHref(LivingStoryData.getLoginUrl());
      logIn.setVisible(true);
    } else {
      username.setText(usernameString);
      usernameSeparator.setVisible(true);
      logOut.setHref(LivingStoryData.getLogoutUrl());
      logOut.setVisible(true);
    }
    
    makePanelChildrenNonWrapping();
  }
  
  protected void bind() {
    initWidget(uiBinder.createAndBindUi(this));
  }
  
  public void makePanelChildrenNonWrapping() {
    for (Widget child : root) {
      if (child instanceof HasWordWrap) {
        ((HasWordWrap) child).setWordWrap(false);
      }
    }
  }
}
