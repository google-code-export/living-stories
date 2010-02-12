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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.livingstories.client.BaseAtom;
import com.google.livingstories.client.ContentRpcServiceAsync;
import com.google.livingstories.client.PlayerAtom;

import java.util.HashMap;
import java.util.Map;

/**
 * A text-box that suggests existing unassigned player names and lets you select one of them
 * or add a new one.
 */
public class PlayerSuggestAndAddPanel extends Composite {
  private Map<String, PlayerAtom> nameToPlayerMap;
  
  private MultiWordSuggestOracle oracle;
  private SuggestBox suggestBox;
  private Button addOrCreateButton;
  private TextBoxBase textBox;
  private Label problemLabel;
  private HorizontalPanel panel;
  
  public PlayerSuggestAndAddPanel(ContentRpcServiceAsync atomService, boolean isContributor, 
      AsyncCallback<BaseAtom> callbackWork) {
    this.nameToPlayerMap = new HashMap<String, PlayerAtom>();
    
    this.oracle = new MultiWordSuggestOracle();
    this.suggestBox = new SuggestBox(oracle);
    this.addOrCreateButton = new Button("Create or Add");
    this.textBox = suggestBox.getTextBox();
    
    this.problemLabel = new Label();
    problemLabel = new Label("Selection did not succeed. Please try again.");
    problemLabel.setStylePrimaryName("serverResponseLabelError");
    problemLabel.setVisible(false);
    
    addClickHandlerForButton(atomService, isContributor, callbackWork);
    addEnterKeyHandlerForSuggestBox(callbackWork);
    
    this.panel = new HorizontalPanel();
    this.panel.add(suggestBox);
    this.panel.add(addOrCreateButton);
    this.panel.add(problemLabel);
    
    initWidget(panel);
  }
  
  /**
   * If the user clicks on the button, either they selected an existing player which will be
   * returned to the caller. Or they entered a new name, in which case, they will be presented
   * with a popup to enter the information for the new player.
   */
  private void addClickHandlerForButton(ContentRpcServiceAsync atomService, boolean isContributor, 
      final AsyncCallback<BaseAtom> callbackWork) {
    final OnTheFlyPlayerBox onTheFlyPlayerBox = new OnTheFlyPlayerBox(atomService, isContributor, 
        new AsyncCallback<BaseAtom>() {
        @Override
        public void onFailure(Throwable caught) {
          problemLabel.setVisible(true);
        }

        @Override
        public void onSuccess(BaseAtom result) {
          problemLabel.setVisible(false);
          addPlayer((PlayerAtom) result);
          callbackWork.onSuccess(result);
          clear();
        }
      });

    addOrCreateButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        String playerName = textBox.getText();
        PlayerAtom selectedPlayer = nameToPlayerMap.get(playerName);
        if (selectedPlayer == null) {
          onTheFlyPlayerBox.open(playerName, addOrCreateButton);
        } else {
          callbackWork.onSuccess(selectedPlayer);
          clear();
        }
      }
    });
  }
  
  /**
   * Pressing 'enter' after selecting an item from the suggest list or after entering a new name
   * in the text box acts the same as clicking on the button.
   */
  private void addEnterKeyHandlerForSuggestBox(final AsyncCallback<BaseAtom> callbackWork) {
    textBox.addKeyUpHandler(new KeyUpHandler() {
      @Override
      public void onKeyUp(KeyUpEvent event) {
        if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
          addOrCreateButton.click();
        }
      }
    });
  }

  public void addPlayer(PlayerAtom player) {
    nameToPlayerMap.put(player.getName(), player);
    oracle.add(player.getName());
  }
  
  public void removePlayer(String playerName) {
    if (nameToPlayerMap.containsKey(playerName)) {
      nameToPlayerMap.remove(playerName);
      // MultiWordSuggestOracle doesn't have the ability to remove suggestions one at a time. Boo!
      oracle.clear();
      oracle.addAll(nameToPlayerMap.keySet());
    }
  }
  
  public void clear() {
    textBox.setText("");
  }
}
