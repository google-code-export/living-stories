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

package com.google.livingstories.client.contentmanager;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.user.client.ui.HTML;

/**
 * Class used for proofreading HTML that contains <object> and <embed> tags, turning them into
 * a canonical form that is cross-browser compatible.
 */
public class ObjectElementProofreader {
  /**
   * Class that contains information on which attributes of an <embed> tag should make
   * it into the corresponding <object> tag.
   * @author hiller@google.com (Matt Hiller)
   *
   */
  private enum TranslatedAttributeRecord {
    WIDTH("width", "width", true),
    HEIGHT("height", "height", true),
    SRC("src", "data", false),
    TYPE("type", "type", false);

    private String embedAttribute;
    private String objectAttribute;
    private boolean removePx;
    
    TranslatedAttributeRecord(String embedAttribute, String objectAttribute, boolean removePx) {
      this.embedAttribute = embedAttribute;
      this.objectAttribute = objectAttribute;
      this.removePx = removePx;
    }
    
    public static TranslatedAttributeRecord getFromEmbedAttribute(String embedAttribute) {
      for (TranslatedAttributeRecord record : TranslatedAttributeRecord.values()) {
        if (record.embedAttribute.equalsIgnoreCase(embedAttribute)) {
          return record;
        }
      }
      return null;
    }
    
    public void setAttributeForObject(Element objectElement, String value) {
      if (removePx && value.endsWith("px")) {
        value = value.replaceFirst("px$", "");
      }
      objectElement.setAttribute(objectAttribute, value);
    }
  }

  private static String[] EMBED_ATTRIBUTES = { 
    // Per http://kb2.adobe.com/cps/127/tn_12701.html:
    "width", "height", "src", "type",
    "id", "name", "swliveconnect", "play", "loop", "menu", "quality", "scale",
    "align", "salign", "wmode", "bgcolor", "base", "flashvars"
  };
  
  private Document document = Document.get();
    
  public String proofread(String incomingHtml) {
    HTML asHTML = new HTML(incomingHtml);
    Element element = asHTML.getElement();
    boolean changed = false;
    
    NodeList<Element> embeds = element.getElementsByTagName("embed");
    
    // since we will be removing embeds, iterate from len-1 to 0:
    for (int i = embeds.getLength() - 1; i >=0; i--) {
      changed = true;
      Element embed = embeds.getItem(i);
      Element embedParent = embed.getParentElement();
      if (embedParent.getTagName().equalsIgnoreCase("object")) {
        // The wrapped object idiom: shouldn't be necessary at all.
        embedParent.removeChild(embed);
      } else {
        Element objectElement = document.createObjectElement();

        for (String attribute : EMBED_ATTRIBUTES) {
          String value = embed.getAttribute(attribute);

          if (!value.isEmpty()) {
            TranslatedAttributeRecord record =
              TranslatedAttributeRecord.getFromEmbedAttribute(attribute);
            
            if (record == null) {
              addParam(objectElement, attribute, value);
            } else {
              record.setAttributeForObject(objectElement, value);
              if (record == TranslatedAttributeRecord.SRC) {
                addParam(objectElement, "movie", value);
              }
            }
          }
        }
        embedParent.replaceChild(objectElement /* new */, embed /* old */);
      }
    }
    
    // now iterate over objects, looking for places where the "data" value is not replicated as
    // a "movie" param.
    NodeList<Element> objectElements = element.getElementsByTagName("object");
    
    // again, iterate from len-1 to 0:
    for (int i = objectElements.getLength() - 1; i >= 0; i--) {
      Element objectElement = objectElements.getItem(i);
      Element objectParent = objectElement.getParentElement();
      
      if (objectParent.getTagName().equalsIgnoreCase("object")) {
        // nested object idiom. Unnecessary.
        changed = true;
        objectParent.removeChild(objectElement);
      } else {
        boolean foundMovieParam = false;
        
        for (Element child = objectElement.getFirstChildElement();
            !foundMovieParam && child != null; child = child.getNextSiblingElement()) {
          foundMovieParam = child.getNodeName().equalsIgnoreCase("param")
              && child.getAttribute("name").equalsIgnoreCase("movie");
        }
        
        if (!foundMovieParam) {
          // duplicate it from the data attribute of the object itself.
          changed = true;
          addParam(objectElement, "movie", objectElement.getAttribute("data"));
        }
      }
    }
    
    return (changed
        // so that we don't worry about embedded comment indicators in the original html:
        ? (element.getInnerHTML() + "\n<!--\n" + incomingHtml.replaceAll("-+", "-") + "\n-->\n")
        : null);
  }
    
  private void addParam(Element objectElement, String name, String value) {
    Element param = document.createParamElement();
    param.setAttribute("name", name);
    param.setAttribute("value", value);
    objectElement.appendChild(param);
  }
}
