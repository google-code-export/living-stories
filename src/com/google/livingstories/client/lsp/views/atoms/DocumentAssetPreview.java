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
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.livingstories.client.AssetAtom;
import com.google.livingstories.client.lsp.views.Resources;
import com.google.livingstories.client.util.LivingStoryControls;

/**
 * Renders a document link with a document icon.
 */
public class DocumentAssetPreview extends Composite {
  private static DocumentAssetPreviewUiBinder uiBinder =
      GWT.create(DocumentAssetPreviewUiBinder.class);
  interface DocumentAssetPreviewUiBinder extends UiBinder<Widget, DocumentAssetPreview> {
  }

  private static final String DOCUMENT_ICON = "/images/document_icon.gif";

  @UiField SimplePanel iconPanel;
  @UiField HTML content;
  
  private AssetAtom atom;

  public DocumentAssetPreview(AssetAtom atom) {
    this.atom = atom;
    
    initWidget(uiBinder.createAndBindUi(this));
    
    iconPanel.add(new Image(DOCUMENT_ICON));
    content.setHTML(atom.getCaption());
    content.addStyleName(Resources.INSTANCE.css().clickable());
    if (atom.getRenderAsSeen()) {
      addStyleName(Resources.INSTANCE.css().read());
    }
  }
  
  @UiHandler("content")
  public void handleClick(ClickEvent e) {
    // TODO: Use an event bus here instead of popping up the lightbox directly.
    LivingStoryControls.showLightbox(atom.getTitleString(), atom);
  }
}
