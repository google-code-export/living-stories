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

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Server-side implementation of html string parsing.
 * Uses JTidy to parse html into a DOM.
 */
public class JavaNodeAdapter implements NodeAdapter {
  private Node node;
  
  public JavaNodeAdapter(Node node) {
    this.node = node;
  }

  @Override
  public Map<String, String> getAttributes() {
    Element element = (Element) node;
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
    NodeList childNodes = node.getChildNodes();
    List<NodeAdapter> nodes = new ArrayList<NodeAdapter>(childNodes.getLength());
    for (int i = 0; i < childNodes.getLength(); i++) {
      nodes.add(new JavaNodeAdapter(childNodes.item(i)));
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
    Element element = (Element) node;
    return element.getTagName();
  }
  
  public static NodeAdapter fromHtml(String html) {
    Tidy tidy = new Tidy();
    return new JavaNodeAdapter(tidy.parseDOM(new StringReader(html), null));
  }
}
