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
import com.google.livingstories.client.BaseContentItem;
import com.google.livingstories.client.ContentRpcService;
import com.google.livingstories.client.ContentRpcServiceAsync;
import com.google.livingstories.client.lsp.LspMessageHolder;
import com.google.livingstories.client.lsp.SourcePopupWidget;

/**
 * Render the source information for a content item as a link. Clicking on it brings up a popup
 * that has the source description followed by the full source content item, if any. Assumption:
 * this widget is instantiated only when the content item has source information attached to it.
 */
public class SourceInfoWidget extends Composite {
  private final ContentRpcServiceAsync contentService = GWT.create(ContentRpcService.class);
  
  private Link sourceLink;
  private SourcePopupWidget popup;
  
  private static String SOURCE_LINK_TEXT = LspMessageHolder.consts.sourceLinkText();
  
  /**
   * Any of the input params can be null. If the sourceContentItem is null but sourceContentItemId
   * is non-null, an async call will be made to fetch the appropriate content item.
   */
  public SourceInfoWidget(Long sourceContentItemId, BaseContentItem sourceContentItem,
      String sourceDescription) {
    super();
    
    // Create the link that will bring up the popup
    sourceLink = new Link(SOURCE_LINK_TEXT, true);
    sourceLink.addStyleName("sourceInfoLink");
    sourceLink.addClickHandler(new SourceLinkClickHandler(
        sourceContentItemId, sourceContentItem, sourceDescription));
    
    popup = new SourcePopupWidget();
    
    initWidget(sourceLink);
  }
  
  public SourceInfoWidget(BaseContentItem contentItem) {
    this(contentItem.getSourceContentItemId(), contentItem.getSourceContentItem(),
        contentItem.getSourceDescription());
  }
  
  private class SourceLinkClickHandler implements ClickHandler {
    private String sourceDescription;
    private Long sourceContentItemId;
    private BaseContentItem sourceContentItem;
    
    public SourceLinkClickHandler(Long sourceContentItemId, BaseContentItem sourceContentItem, 
        String sourceDescription) {
      this.sourceDescription = sourceDescription;
      this.sourceContentItemId = sourceContentItemId;
      this.sourceContentItem = sourceContentItem;
    }
    
    @Override
    public void onClick(ClickEvent e) {
      if (sourceContentItem == null && sourceContentItemId != null) {
        contentService.getContentItem(sourceContentItemId, false, new AsyncCallback<BaseContentItem>() {
          public void onFailure(Throwable t) {
            // Do nothing
          }
          
          public void onSuccess(BaseContentItem contentItem) {
            sourceContentItem = contentItem;
            createPopUpAndShow();
          }
        });
      } else {
        createPopUpAndShow();
      }
    }
    
    private void createPopUpAndShow() {
      popup.show(sourceDescription, sourceContentItem, sourceLink.getElement());
    }
  }
}
