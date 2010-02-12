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
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.livingstories.client.BaseAtom;
import com.google.livingstories.client.atomlist.AtomClickHandler;
import com.google.livingstories.client.atomlist.AtomList;
import com.google.livingstories.client.util.Constants;

import java.util.Set;

/**
 * Widget that allows the user to select an arbitrary atom through a popup
 * search interface.  This class can handle single or multiple selections, depending
 * on what kind of selection handler the show() method is invoked with.
 */
public class AtomSelector extends PopupPanel {
  private DockPanel contentPanel;
  private ScrollPanel scrollPanel;
  private VerticalPanel masterPanel;
  private SearchWidget search;
  private AtomList cart;
  private HorizontalPanel buttonPanel;
  private Button submitButton;
  private Button cancelButton;

  private boolean multiSelect;
  private SingleSelectionHandler singleSelectionHandler;
  private MultipleSelectionHandler multipleSelectionHandler;
  
  private static final int MAX_SCROLL_WIDTH = 400;
  private static final int MAX_SCROLL_HEIGHT = 600;
  // how much smaller must the scroll be than the window?
  private static final int WINDOW_WIDTH_OFFSET = 50;
  private static final int WINDOW_HEIGHT_OFFSET = 80;
  
  public AtomSelector() {
    super();
    contentPanel = new DockPanel();
    contentPanel.add(createCart(), DockPanel.EAST);
    contentPanel.add(createSearchPage(), DockPanel.CENTER);
    contentPanel.add(createControls(), DockPanel.SOUTH);
    contentPanel.setWidth("100%");
    
    scrollPanel = new ScrollPanel(contentPanel);
    scrollPanel.setAlwaysShowScrollBars(false);

    masterPanel = new VerticalPanel();
    masterPanel.add(createHeader());
    masterPanel.add(scrollPanel);

    add(masterPanel);
  }
  
  public void show(SingleSelectionHandler handler) {
    multiSelect = false;
    singleSelectionHandler = handler;
    multipleSelectionHandler = null;
    cart.setVisible(false);
    buttonPanel.setVisible(false);
    setScrollSize();
    center();
  }

  public void show(MultipleSelectionHandler handler) {
    multiSelect = true;
    multipleSelectionHandler = handler;
    singleSelectionHandler = null;
    cart.setVisible(true);
    buttonPanel.setVisible(true);
    setScrollSize();
    center();
  }
  
  private void setScrollSize() {
    // we should take reasonable steps to keep the dialog smaller than the window, so that
    // the close button doesn't get positioned so that it's unclickable. (If we want to fully
    // go to extremes here, we could register a native event handler to close the popup when
    // the user hits escape.)
    int width = Math.min(MAX_SCROLL_WIDTH, Window.getClientWidth() - WINDOW_WIDTH_OFFSET);
    int height = Math.min(MAX_SCROLL_HEIGHT, Window.getClientHeight() - WINDOW_HEIGHT_OFFSET);
    
    scrollPanel.setSize(width + "px", height + "px");
    masterPanel.setWidth(width + "px");
  }

  private Widget createHeader() {
    HorizontalPanel panel = new HorizontalPanel();
    panel.setWidth("100%");
    
    panel.add(new Label("Select Atom(s)"));
    
    panel.setHorizontalAlignment(HorizontalPanel.ALIGN_RIGHT);
    Image closeButton = new Image(Constants.CLOSE_IMAGE_URL);
    closeButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent e) {
        hide();
      }
    });
    panel.add(closeButton);
    
    return panel;
  }
  
  private Widget createSearchPage() {
    search = new SearchWidget(new AtomClickHandler() {
      public void onClick(BaseAtom atom) {
        if (multiSelect) {
          cart.addAtom(atom);
          submitButton.setEnabled(cart.getAtomCount() != 0);
        } else {
          hide();
          singleSelectionHandler.onSelect(atom);
        }
      }
    });
    return search;
  }
  
  private Widget createCart() {
    cart = AtomList.createClickable(new AtomClickHandler() {
      public void onClick(BaseAtom atom) {
        cart.removeAtom(atom.getId());
        submitButton.setEnabled(cart.getAtomCount() != 0);
      }
    });
    return cart;
  }
  
  private Widget createControls() {
    submitButton = new Button("Save selection");
    submitButton.setEnabled(false);
    submitButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent e) {
        hide();
        multipleSelectionHandler.onSelect(cart.getAtomSet());
      }
    });
    
    cancelButton = new Button("Cancel");
    cancelButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent e) {
        hide();
      }
    });
    
    buttonPanel = new HorizontalPanel();
    buttonPanel.add(submitButton);
    buttonPanel.add(cancelButton);
    return buttonPanel;
  }
  
  @Override
  public void hide() {
    hide(false);
  }
  
  @Override
  public void hide(boolean autoClosed) {
    super.hide(autoClosed);
    search.clear();
    cart.clear();
    submitButton.setEnabled(false);
  }
  
  public interface SingleSelectionHandler {
    void onSelect(BaseAtom atom);
  }
  
  public interface MultipleSelectionHandler {
    void onSelect(Set<BaseAtom> atom);
  }
}
