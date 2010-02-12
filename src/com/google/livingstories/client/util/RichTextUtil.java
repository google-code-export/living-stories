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

import com.google.gwt.user.client.Element;
import com.google.livingstories.client.ui.JavascriptLink;

/**
 * Utilities to perform extra tasks in the rich text editor.
 */
public class RichTextUtil {
  public native String getSelection(Element richTextArea) /*-{
    return richTextArea.contentWindow.getSelection().toString();
  }-*/;
  
  public native void insertHTML(Element richTextArea, String html) /*-{
    richTextArea.contentWindow.document.execCommand("insertHTML", false, html);
  }-*/;
  
  public void createJavascriptLink(Element richTextArea, String javascript) {
    createJavascriptLink(richTextArea, javascript, null);
  }
  
  public void createJavascriptLink(Element richTextArea, String javascript, String tooltip) {
    String selection = getSelection(richTextArea);
    JavascriptLink link = new JavascriptLink(selection);
    link.setOnClick(javascript);
    if (tooltip != null) {
      link.setTitle(tooltip);
    }
    insertHTML(richTextArea, link.getOuterHTML());
  }
}
