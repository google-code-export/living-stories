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
import com.google.gwt.dom.client.Node;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.livingstories.client.QuoteAtom;
import com.google.livingstories.client.lsp.ContentRenderer;

/**
 * Renders a quote with a blockquote icon.
 */
public class QuoteAtomView extends Composite {
  private static QuoteAtomViewUiBinder uiBinder = GWT.create(QuoteAtomViewUiBinder.class);
  interface QuoteAtomViewUiBinder extends UiBinder<Widget, QuoteAtomView> {
  }

  @UiField Label header;
  @UiField SimplePanel content;

  public QuoteAtomView(QuoteAtom atom) {
    initWidget(uiBinder.createAndBindUi(this));    

    // if the rendered HTML content leads with a double-quote character, we replace the underlying
    // text with 2 non-breaking spaces.
    HTML detachedHTML = new HTML(atom.getContent());

    // walk through the content looking for the first text node:
    Node firstTextNode = getFirstTextNode(detachedHTML.getElement().getFirstChild());
    
    if (firstTextNode != null) {
      // replace check the first text node to see if there's quotiness to replace.
      String firstText = firstTextNode.getNodeValue();
      // replace leading whitespace plus a quote character with two &nbsp;s.
      // \u201C is the opening-double-quote codepoint; \u00A0 is a non-breaking space.
      String replacedText = firstText.replaceFirst("^\\s*[\"\u201C]", "\u00A0\u00A0");
      if (!replacedText.equals(firstText)) {
        firstTextNode.setNodeValue(replacedText);
      }
    }
    content.add(new ContentRenderer(detachedHTML.getElement().getInnerHTML(), false));
  }

  public QuoteAtomView hideHeader() {
    header.setVisible(false);
    return this;
  }

  // gets the first non-all-whitespace text node in the DOM tree rooted at node,
  // found depth-first.
  public Node getFirstTextNode(Node node) {
    if (node == null) {
      return null;
    }
    
    if (node.getNodeType() == Node.TEXT_NODE && !node.getNodeValue().trim().isEmpty()) {
      return node;
    }
    Node childResult = getFirstTextNode(node.getFirstChild());
    return (childResult == null) ? getFirstTextNode(node.getNextSibling()) : childResult;
  }
}
