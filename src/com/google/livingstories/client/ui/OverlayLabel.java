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

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * Displays a label overlaid on top of some other content.
 */
public class OverlayLabel extends Composite {
  private FlowPanel contentPanel = new FlowPanel();
  private Label label = new Label();
  private SimplePanel background = new SimplePanel();
  
  public OverlayLabel() {
    contentPanel.setStyleName("overlay");
    contentPanel.add(label);
    contentPanel.add(background);
    
    background.setStyleName("overlayBackground");
    
    initWidget(contentPanel);
  }

  public void setText(String text) {
    label.setText(text);
    // remove and reattach the background widget to hack around bugginess in Internet
    // Explorer transparency.
    contentPanel.remove(background);
    DeferredCommand.addCommand(new Command() {
      @Override
      public void execute() {
        contentPanel.add(background);
        // There's a CSS-only way to get this effect for firefox, but it doesn't work
        // on IE.
        background.setHeight(contentPanel.getOffsetHeight() + "px");
      }
    });
  }
}
