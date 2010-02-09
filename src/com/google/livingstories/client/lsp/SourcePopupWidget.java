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

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.livingstories.client.BaseAtom;
import com.google.livingstories.client.ui.AutoHidePopupPanel;
import com.google.livingstories.client.util.FourthestateUtil;

/**
 * Create a popup that shows source information.
 */
public class SourcePopupWidget extends Composite {
  private static final String POPUP_WIDTH = "400px";
  private AutoHidePopupPanel popup;
  
  public SourcePopupWidget() {
    popup = new AutoHidePopupPanel();
    popup.setWidth(POPUP_WIDTH);
  }
  
  public void show(String description, BaseAtom atom, Element showRelativeTo) {
    // Create the contents of the popup
    VerticalPanel sourcePanel = new VerticalPanel();
    if (!FourthestateUtil.isContentEmpty(description)) {
      sourcePanel.add(new HTML(description));
    }
    if (atom != null) {
      sourcePanel.add(new AtomRenderer(atom, true, false));
    }
    
    popup.setWidget(sourcePanel);
    popup.setPopupPosition(showRelativeTo.getAbsoluteLeft(),
        showRelativeTo.getAbsoluteTop() + showRelativeTo.getOffsetHeight());
    popup.show();
  }
}
