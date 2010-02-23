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

package com.google.livingstories.client.util;

import com.google.livingstories.client.util.dom.NodeAdapter;

import java.util.List;
import java.util.Map.Entry;

/**
 * Class that implements utilities for snippetizing html strings.
 */
public class SnippetUtil {
  public static String createSnippet(NodeAdapter dom, int maximumLength) {
    return createSnippetInternal(dom.getChildNodes(), maximumLength).snippetHTML;
  }

  private static SnippetResult createSnippetInternal(List<NodeAdapter> nodes, int maximumLength) {
    int remainingLength = maximumLength;
    StringBuffer snippetHTML = new StringBuffer();
    for (NodeAdapter node : nodes) {
      if (remainingLength <= 0) {
        break;
      }
      if (node.getNodeType() == NodeAdapter.TEXT_NODE) {
        // Get the node text and collapse whitespace down to single spaces.
        String value = node.getNodeValue().replaceAll("\\s+", " ");
        if (value.length() > remainingLength) {
          // Find the first period after the number of characters that we need, so we can cut
          // off the snippet at the end of a sentence
          String remainingString = value.substring(remainingLength);
          int indexOfPeriod = remainingLength;
          // If the last character in the length we want is not already a period, find the next one
          if (value.charAt(remainingLength - 1) != '.' && remainingString.contains(".")) {
            indexOfPeriod += remainingString.indexOf(".");
          }
          value = value.substring(0, indexOfPeriod).concat("...");
        }
        snippetHTML.append(value);
        remainingLength -= value.length();
      } else if (node.getNodeType() == NodeAdapter.ELEMENT_NODE) {
        
        snippetHTML.append("<" + node.getTagName());
        for (Entry<String, String> attribute : node.getAttributes().entrySet()) {
          String attributeValue = attribute.getValue();
          if (attributeValue != null && !attributeValue.isEmpty()) {
            snippetHTML.append(" " + attribute.getKey() + "=\"" + attributeValue + "\"");
          }
        }
        snippetHTML.append(">");
        SnippetResult result = createSnippetInternal(node.getChildNodes(), remainingLength);
        snippetHTML.append(result.snippetHTML);
        remainingLength = result.remainingLength;
        snippetHTML.append("</" + node.getTagName() + ">");
      }
    }
    return new SnippetResult(snippetHTML.toString(), remainingLength);
  }
  
  private static class SnippetResult {
    public String snippetHTML;
    public int remainingLength;
    
    public SnippetResult(String snippetHTML, int remainingLength) {
      this.snippetHTML = snippetHTML;
      this.remainingLength = remainingLength;
    }
  }
}
