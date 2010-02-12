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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.livingstories.client.lsp.event.EventBus;
import com.google.livingstories.client.lsp.event.ShowMoreEvent;

/**
 * Extends behaviors of the LspAtomListWidget in ways that are appropriate for the Player Page
 */
public class PlayerPageAtomListWidget extends LspAtomListWidget {
  @Override
  protected void addMoreLinkHandler(HasClickHandlers moreLink) {
    moreLink.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        EventBus.INSTANCE.fireEvent(new ShowMoreEvent());
      }
    });
  }
}
