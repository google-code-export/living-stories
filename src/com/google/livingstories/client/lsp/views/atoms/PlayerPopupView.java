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
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.livingstories.client.PlayerAtom;

/**
 * Uses the BasePlayerPreview but adds a 'Read more' link to it.  Reuses the goToPlayerPage
 * method for the read more link click handler.
 */
public class PlayerPopupView extends BasePlayerPreview {

  private static PlayerPopupViewUiBinder uiBinder = GWT.create(PlayerPopupViewUiBinder.class);

  interface PlayerPopupViewUiBinder extends UiBinder<Widget, PlayerPopupView> {
  }

  @UiField Label readMore;

  public PlayerPopupView(PlayerAtom atom) {
    super(atom);
  }

  @Override
  protected void bind() {
    initWidget(uiBinder.createAndBindUi(this));
  }
  
  @Override
  @UiHandler("readMore") void goToPlayerPage(ClickEvent e) {
    super.goToPlayerPage(e);
  }
}
