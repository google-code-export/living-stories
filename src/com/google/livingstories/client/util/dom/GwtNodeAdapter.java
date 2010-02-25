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

package com.google.livingstories.client.util.dom;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.user.client.DOM;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Client-side implementation of html string parsing.
 * Uses GWT libraries that take advantage of native browser html parsing.
 */
public class GwtNodeAdapter implements NodeAdapter {
  private Node node;
  
  public GwtNodeAdapter(Node node) {
    this.node = node;
  }

  @Override
  public Map<String, String> getAttributes() {
    Element element = Element.as(node);
    Map<String, String> attributes = new HashMap<String, String>();
    for (String key : whitelistedAttributes) {
      String value = element.getAttribute(key);
      if (value != null) {
        attributes.put(key, value);
      }
    }
    return attributes;
  }

  @Override
  public List<NodeAdapter> getChildNodes() {
    NodeList<Node> childNodes = node.getChildNodes();
    List<NodeAdapter> nodes = new ArrayList<NodeAdapter>(childNodes.getLength());
    for (int i = 0; i < childNodes.getLength(); i++) {
      nodes.add(new GwtNodeAdapter(childNodes.getItem(i)));
    }
    return nodes;
  }

  @Override
  public int getNodeType() {
    return node.getNodeType();
  }

  @Override
  public String getNodeValue() {
    return node.getNodeValue();
  }

  @Override
  public String getTagName() {
    Element element = Element.as(node);
    return element.getTagName();
  }
  
  public static NodeAdapter fromHtml(String html) {
    Element dom = DOM.createDiv();
    dom.setInnerHTML(html);
    return new GwtNodeAdapter(dom);
  }
}
