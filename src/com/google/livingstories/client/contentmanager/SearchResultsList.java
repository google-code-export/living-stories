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

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.livingstories.client.BaseAtom;
import com.google.livingstories.client.atomlist.AtomClickHandler;

import java.util.List;

public class SearchResultsList extends Composite {
  private FlowPanel atomList;

  private AtomClickHandler handler;
  
  private static ContentManagerConstants consts = GWT.create(ContentManagerConstants.class);
  
  public SearchResultsList(AtomClickHandler handler) {
    this.handler = handler;
    
    atomList = new FlowPanel();
    
    initWidget(atomList);
  }
  
  public void load(List<BaseAtom> atoms) {
    atomList.clear();
    if (atoms.isEmpty()) {
      atomList.add(new Label(consts.noSearchResults()));
    } else {
      for (final BaseAtom atom : atoms) {
        Label displayString = new Label(atom.getAtomType().toString());
        displayString.setStylePrimaryName("atomHeader");
  
        FlowPanel atomPanel = new FlowPanel();
        atomPanel.add(displayString);
        atomPanel.add(atom.renderTiny());
        
        FocusPanel clickPanel = new FocusPanel(atomPanel);
        clickPanel.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            handler.onClick(atom);
          }
        });
        atomList.add(clickPanel);
        atomList.add(new HTML("<hr/>"));
      }
    }
  }
  
  public void clear() {
    atomList.clear();
  }
}
