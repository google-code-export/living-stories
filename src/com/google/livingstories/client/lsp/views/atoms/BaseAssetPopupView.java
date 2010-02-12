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
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.TableCaptionElement;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.livingstories.client.AssetAtom;
import com.google.livingstories.client.lsp.BylineWidget;
import com.google.livingstories.client.lsp.ContentRenderer;
import com.google.livingstories.client.lsp.event.EventBus;
import com.google.livingstories.client.lsp.event.PopupImageLoadedEvent;
import com.google.livingstories.client.util.GlobalUtil;

/**
 * Renders an asset with an HTML panel.
 */
public class BaseAssetPopupView extends Composite {

  private static BaseAssetPopupViewUiBinder uiBinder = GWT.create(BaseAssetPopupViewUiBinder.class);

  interface BaseAssetPopupViewUiBinder extends UiBinder<Widget, BaseAssetPopupView> {
  }

  @UiField TableCaptionElement caption;
  @UiField SimplePanel content;
  @UiField SimplePanel byline;

  public BaseAssetPopupView(AssetAtom atom) {
    initWidget(uiBinder.createAndBindUi(this));
    
    // Set the caption if there is one
    String captionText = atom.getCaption();
    if (!GlobalUtil.isContentEmpty(captionText)) {
      caption.appendChild(new ContentRenderer(captionText, false).getElement());
    }

    content.add(getContent(atom));
    
    // Add a byline if available
    if (!atom.getContributorIds().isEmpty()) {
      byline.add(new BylineWidget(atom, false));
    }
  }
  
  protected Widget getContent(AssetAtom atom) {
    // Add the content
    HTML contentHTML = new HTML(atom.getContent());
    // So that lightbox centering in firefox works, enclose each sized <object>
    // with a div styled to exactly that size.
    NodeList<Element> objectElements = contentHTML.getElement().getElementsByTagName("object");
    Document document = Document.get();
    for (int i = 0, len = objectElements.getLength(); i < len; i++) {
      Element objectElement = objectElements.getItem(i);
      String width = objectElement.getAttribute("width");
      String height = objectElement.getAttribute("height");
      if (width.matches("[0-9]+%?") && height.matches("[0-9]+%?")) {
        DivElement div = document.createDivElement();
        div.getStyle().setProperty("width", width + (width.endsWith("%") ? "" : "px"));
        div.getStyle().setProperty("height", height + (height.endsWith("%") ? "" : "px"));
        objectElement.getParentElement().replaceChild(div, objectElement);
        div.appendChild(objectElement);
      }
    }
    // In case there are images within the content, we should fire a PopupImageLoadedEvent
    // so that any popup window displaying this view has a chance to reposition itself.
    NodeList<Element> imageElements = contentHTML.getElement().getElementsByTagName("img");
    for (int i = 0; i < imageElements.getLength(); i++) {
      ImageElement image = imageElements.getItem(i).cast();
      addImageLoadHandler(image);
    }
    return contentHTML;
  }
  
  @SuppressWarnings("unused")
  private void firePopupImageLoadedEvent(ImageElement image) {
    EventBus.INSTANCE.fireEvent(
        new PopupImageLoadedEvent(image.getOffsetWidth(), image.getOffsetHeight()));
  }
  
  private native void addImageLoadHandler(ImageElement image) /*-{
    var instance = this;
    image.onload = function() {
      instance.
          @com.google.livingstories.client.lsp.views.atoms.BaseAssetPopupView::firePopupImageLoadedEvent(Lcom/google/gwt/dom/client/ImageElement;)
          .call(instance, image);
    };
  }-*/;
}
