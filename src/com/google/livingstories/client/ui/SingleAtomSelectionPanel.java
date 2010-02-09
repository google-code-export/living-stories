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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.livingstories.client.BaseAtom;
import com.google.livingstories.client.ui.AtomSelector.SingleSelectionHandler;

/**
 * Widget to display a single selection.
 * Gives the user a 'select' button that pops up an AtomSelector.  User clicks on
 * the desired atom, and the selection is set and processed.
 */
public class SingleAtomSelectionPanel extends Composite {
  private Grid selectionPanel;
  private AtomSelector atomSelector;
  private Button clearButton;
  
  private BaseAtom selection;
  
  public SingleAtomSelectionPanel() {
    atomSelector = new AtomSelector();
    
    selectionPanel = new Grid(1, 2);
    selectionPanel.setWidget(0, 1, createButtonPanel());
    initWidget(selectionPanel);

    setSelection(null);
  }
  
  public BaseAtom getSelection() {
    return selection;
  }
  
  public void setSelection(BaseAtom selection) {
    this.selection = selection;
    if (selection == null) {
      selectionPanel.setWidget(0, 0, new Label("No selection"));
      clearButton.setEnabled(false);
    } else {
      selectionPanel.setWidget(0, 0, selection.renderTiny());
      clearButton.setEnabled(true);
    }
  }
  
  private Widget createButtonPanel() {
    Button selectionButton = new Button("Select atom");
    selectionButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent e) {
        atomSelector.show(new SingleSelectionHandler() {
          @Override
          public void onSelect(BaseAtom atom) {
            setSelection(atom);
          }
        });
      }
    });
    
    clearButton = new Button("Clear selection");
    clearButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent e) {
        setSelection(null);
      }
    });
    
    VerticalPanel buttonPanel = new VerticalPanel();
    buttonPanel.add(selectionButton);
    buttonPanel.add(clearButton);
    return buttonPanel;
  }
}
