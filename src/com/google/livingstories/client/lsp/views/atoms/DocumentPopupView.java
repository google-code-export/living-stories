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

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.livingstories.client.AssetAtom;

/**
 * Displays a document in a popup view.
 * 
 * Since documents can be either PDFs displayed in iframes, or direct html/text entered
 * into the content field, this class uses a simple heuristic to determine whether or not
 * to put the content into a scroll panel.
 */
public class DocumentPopupView extends BaseAssetPopupView {
  private static final int CONTENT_LENGTH_THRESHOLD = 250;
  private static final int CONTENT_WIDTH = 500;
  private static final int CONTENT_MARGIN = 100;
  
  public DocumentPopupView(AssetAtom atom) {
    super(atom);
  }

  @Override
  protected Widget getContent(AssetAtom atom) {
    Widget content = super.getContent(atom);
    // A rough heuristic: if the content length is less than the threshold,
    // it's probably an iframe with an embedded PDF, which we don't want to
    // scroll.
    // Otherwise, it's probably text content, which we put into a scrollpanel.
    if (atom.getContent().length() < CONTENT_LENGTH_THRESHOLD) {
      return content;
    } else {
      int contentHeight = Window.getClientHeight() - 2 * CONTENT_MARGIN;
      ScrollPanel scrollPanel = new ScrollPanel(content);
      scrollPanel.setSize(CONTENT_WIDTH + "px", contentHeight + "px");
      return scrollPanel;      
    }
  }
}
