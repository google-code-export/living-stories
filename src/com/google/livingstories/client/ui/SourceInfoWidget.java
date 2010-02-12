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

package com.google.livingstories.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.livingstories.client.BaseAtom;
import com.google.livingstories.client.ContentRpcService;
import com.google.livingstories.client.ContentRpcServiceAsync;
import com.google.livingstories.client.lsp.LspMessageHolder;
import com.google.livingstories.client.lsp.SourcePopupWidget;

/**
 * Render the source information for an atom as a link clicking on which brings up a popup
 * that has the source description followed by the full source atom, if any. Assumes that
 * this widget is called only when the atom has some source information attached to it.
 */
public class SourceInfoWidget extends Composite {
  private final ContentRpcServiceAsync atomService = GWT.create(ContentRpcService.class);
  
  private Link sourceLink;
  private SourcePopupWidget popup;
  
  private static String SOURCE_LINK_TEXT = LspMessageHolder.consts.sourceLinkText();
  
  /**
   * Any of the input params can be null. If the sourceAtom is not passed and the atom id is passed,
   * an async call will be made to fetch the atom contents.
   */
  public SourceInfoWidget(Long sourceAtomId, BaseAtom sourceAtom, String sourceDescription) {
    super();
    
    // Create the link that will bring up the popup
    sourceLink = new Link(SOURCE_LINK_TEXT, true);
    sourceLink.addStyleName("sourceInfoLink");
    sourceLink.addClickHandler(new SourceLinkClickHandler(
        sourceAtomId, sourceAtom, sourceDescription));
    
    popup = new SourcePopupWidget();
    
    initWidget(sourceLink);
  }
  
  public SourceInfoWidget(BaseAtom atom) {
    this(atom.getSourceAtomId(), atom.getSourceAtom(), atom.getSourceDescription());
  }
  
  private class SourceLinkClickHandler implements ClickHandler {
    private String sourceDescription;
    private Long sourceAtomId;
    private BaseAtom sourceAtom;
    
    public SourceLinkClickHandler(Long sourceAtomId, BaseAtom sourceAtom, 
        String sourceDescription) {
      this.sourceDescription = sourceDescription;
      this.sourceAtomId = sourceAtomId;
      this.sourceAtom = sourceAtom;
    }
    
    @Override
    public void onClick(ClickEvent e) {
      if (sourceAtom == null && sourceAtomId != null) {
        atomService.getAtom(sourceAtomId, false, new AsyncCallback<BaseAtom>() {
          public void onFailure(Throwable t) {
            // Do nothing
          }
          
          public void onSuccess(BaseAtom atom) {
            sourceAtom = atom;
            createPopUpAndShow();
          }
        });
      } else {
        createPopUpAndShow();
      }
    }
    
    private void createPopUpAndShow() {
      popup.show(sourceDescription, sourceAtom, sourceLink.getElement());
    }
  }
}
