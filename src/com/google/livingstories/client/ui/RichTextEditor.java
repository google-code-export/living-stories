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

import com.google.gwt.event.logical.shared.BeforeSelectionEvent;
import com.google.gwt.event.logical.shared.BeforeSelectionHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RichTextArea;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.livingstories.client.ui.richtexttoolbar.RichTextToolbar;

/**
 * A TabPanel that has tabs for editing the same content either in rich-text mode or in HTML mode.
 * The rich text mode consists of a {@link RichTextToolbar} and a {@link RichTextArea}. The
 * HTML mode just consists of a {@link TextArea}.
 */
public class RichTextEditor extends Composite {
  private static final int EDITOR_WIDTH = 650;
  
  private TabPanel tabPanel;
  private TextArea textArea;
  private RichTextArea richTextArea;
  private VerticalPanel richTextVerticalPanel;
  
  public RichTextEditor() {
    super();
    this.tabPanel = new TabPanel();
    tabPanel.setWidth((EDITOR_WIDTH + 15) + "px");
    createEditor();
    
    initWidget(tabPanel);
  }
  
  private void createEditor() {
    richTextVerticalPanel = new VerticalPanel();
    populateRichTextVerticalPanel();
    
    textArea = new TextArea();
    textArea.setWidth(EDITOR_WIDTH + "px");
    textArea.setVisibleLines(20);
    
    tabPanel.add(richTextVerticalPanel, "Rich Text");
    tabPanel.add(textArea, "HTML");
    tabPanel.selectTab(0);
    // When the tabs are switched, populate the edits in the content from the current tab to the
    // other one.
    tabPanel.getTabBar().addBeforeSelectionHandler(new BeforeSelectionHandler<Integer>() {
      public void onBeforeSelection(BeforeSelectionEvent<Integer> event) {
        if (event.getItem() == 0) {
          // This is a workaround for some bugs in RichTextArea. It is necessary to create a new
          // RichTextArea when selecting this tab because once the RichTextArea loses focus to the
          // other TextArea, it does not get focus back. The cursor is lost and all the toggle 
          // buttons are depressed. See post on GWT-users titled "Weird behavior in RichTextArea"
          // for details.
          populateRichTextVerticalPanel();
          richTextArea.setHTML(textArea.getText());
        } else {
          textArea.setText(richTextArea.getHTML());
        }
      }
    });
  }
  
  /**
   * Create a RichTextArea and a toolbar connected to it and add them to the VerticalPanel.
   */
  private void populateRichTextVerticalPanel() {
    richTextArea = new RichTextArea();
    richTextArea.setSize(EDITOR_WIDTH + "px", "300px");
    
    RichTextToolbar richTextToolbar = new RichTextToolbar(richTextArea);
    richTextToolbar.setWidth(EDITOR_WIDTH + "px");
    
    richTextVerticalPanel.clear();
    richTextVerticalPanel.add(richTextToolbar);
    richTextVerticalPanel.add(richTextArea);
  }
  
  public void setContent(String content) {
    richTextArea.setHTML(content);
    textArea.setText(content);
  }
  
  /**
   * Return the HTML from the currently selected tab.
   */
  public String getContent() {
    if (tabPanel.getTabBar().getSelectedTab() == 1) {
      return textArea.getText();
    } else {
      return richTextArea.getHTML();
    }
  }
  
  @Override
  public void setHeight(String height) {
    textArea.setHeight(height);
    richTextArea.setHeight(height);
  }
}
