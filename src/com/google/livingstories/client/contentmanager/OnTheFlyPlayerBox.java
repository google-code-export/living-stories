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
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.livingstories.client.AssetContentItem;
import com.google.livingstories.client.AssetType;
import com.google.livingstories.client.BaseContentItem;
import com.google.livingstories.client.ContentRpcServiceAsync;
import com.google.livingstories.client.Importance;
import com.google.livingstories.client.Location;
import com.google.livingstories.client.PlayerContentItem;
import com.google.livingstories.client.PlayerType;
import com.google.livingstories.client.PublishState;
import com.google.livingstories.client.ui.EnumDropdown;
import com.google.livingstories.client.util.GlobalUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

/**
 * A popup panel used for creating general players and contributors on-the-fly.
 * TODO: convert to using UiBinder
 */
public class OnTheFlyPlayerBox extends PopupPanel {
  private static final String OK_TEXT = "Save";
  private static final String WORKING_TEXT = "Working...";
  private static final String PLAYER_TEXT = "player";
  private static final String CONTRIBUTOR_TEXT = "contributor";
  
  private ContentRpcServiceAsync contentService;
  private AsyncCallback<BaseContentItem> callbackWork;
  private boolean isContributor = false;
  
  private TextBox nameBox;
  private TextBox aliasesBox;
  private EnumDropdown<PlayerType> typeSelector;
  private TextBox previewPhotoUrlBox;
  private TextBox photoUrlBox;
  private TextArea bioArea;
  private Button okButton;
  private Button cancelButton;
  private Label problemLabel;
  
  /**
   * Constructor
   * @param contentService a reference to an existing ContentRpcServiceAsync handle
   * @param isContributor set to true if the content item being saved is a contributor and false if
   * it is a general player
   * @param callbackWork work to be executed in the parent UI on the successful (or failed)
   * creation of a new contributor content item
   */
  public OnTheFlyPlayerBox(ContentRpcServiceAsync contentService, boolean isContributor,
      AsyncCallback<BaseContentItem> callbackWork) {
    super(true /* allows autohide */, true /*modal*/);
  
    this.contentService = contentService;
    this.isContributor = isContributor;
    this.callbackWork = callbackWork;

    Widget widget = createUiLayout();
    addEventHandlers();
    
    this.setWidget(widget);
  }
  
  /**
   * Call this to actually show the OnTheFlyContributorBox
   * @param startingName the name to fill in to the name box. Allowably the empty string.
   * @param relativeTo what UI object (typically a button) to open the popup next to.
   */
  public void open(String startingName, UIObject relativeTo) {
    nameBox.setText(startingName);
    aliasesBox.setText("");
    typeSelector.selectConstant(PlayerType.PERSON);
    previewPhotoUrlBox.setText("");
    photoUrlBox.setText("");
    bioArea.setText("");
    problemLabel.setVisible(false);
    
    setOkEnablement();
    showRelativeTo(relativeTo);
  }
  
  private Widget createUiLayout() {
    nameBox = new TextBox();
    aliasesBox = new TextBox();
    typeSelector = EnumDropdown.newInstance(PlayerType.class);
    previewPhotoUrlBox = new TextBox();
    photoUrlBox = new TextBox();
    bioArea = new TextArea();
    bioArea.setCharacterWidth(50);
    bioArea.setVisibleLines(10);
    
    Grid playerPanel = new Grid(6, 2);
    playerPanel.setWidget(0, 0, new Label("Name:"));
    playerPanel.setWidget(0, 1, nameBox);
    playerPanel.setWidget(1, 0, new Label("Aliases:"));
    playerPanel.setWidget(1, 1, aliasesBox);
    playerPanel.setWidget(2, 0, new Label("Type:"));
    playerPanel.setWidget(2, 1, typeSelector);
    playerPanel.setWidget(3, 0, new Label("Photo preview URL:"));
    playerPanel.setWidget(3, 1, previewPhotoUrlBox);
    playerPanel.setWidget(4, 0, new Label("Photo URL:"));
    playerPanel.setWidget(4, 1, photoUrlBox);
    playerPanel.setWidget(5, 0, new Label("Bio:"));
    playerPanel.setWidget(5, 1, bioArea);
    
    okButton = new Button(OK_TEXT);
    setOkEnablement();
    cancelButton = new Button("Cancel");
    FlowPanel buttonPanel = new FlowPanel();
    problemLabel = new Label("Save was unsuccessful. Please try again.");
    problemLabel.setStylePrimaryName("serverResponseLabelError");
    problemLabel.setVisible(false);
    buttonPanel.add(okButton);
    buttonPanel.add(cancelButton);
    buttonPanel.add(problemLabel);
    
    VerticalPanel panel = new VerticalPanel();
    panel.setWidth("450px");
    String playerText = isContributor ? CONTRIBUTOR_TEXT : PLAYER_TEXT;
    panel.add(new Label("Enter details for the " + playerText + ". They can be edited later by " +
        "selecting 'Unassigned' from the story selection dropdown on the top left and then by " +
        "selecting the name of the " + playerText + ".")); 
    panel.add(playerPanel);
    panel.add(buttonPanel);
    return panel;
  }
  
  private void setOkEnablement() {
    okButton.setEnabled(!nameBox.getText().isEmpty());
  }
  
  private void addEventHandlers() {
    nameBox.addKeyUpHandler(new KeyUpHandler() {
      @Override
      public void onKeyUp(KeyUpEvent event) {
        setOkEnablement();
      }
    });
    
    cancelButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        hide();
      }
    });
   
    okButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        disableButtons();
        
        String name = nameBox.getText();
        String previewPhotoUrl = previewPhotoUrlBox.getText();
        String photoUrl = photoUrlBox.getText();
        if (GlobalUtil.isContentEmpty(photoUrl)) {
          photoUrl = previewPhotoUrl;
        }
        
        // If a photo URL is provided, we first have to save the photo content item via an RPC call,
        // and then save the player content item via another RPC. If there is no photo URL provided,
        // the player can be saved via 1 RPC call directly.
        if (GlobalUtil.isContentEmpty(photoUrl)) {
          savePlayer(null);
        } else {
          AssetContentItem photo = new AssetContentItem(null, new Date(), new HashSet<Long>(), photoUrl, 
              Importance.MEDIUM, null, AssetType.IMAGE, name, previewPhotoUrl);
          photo.setLocation(new Location(null, null, ""));
          photo.setPublishState(PublishState.PUBLISHED);
          contentService.createOrChangeContentItem(photo, new AsyncCallback<BaseContentItem>() {
            @Override
            public void onFailure(Throwable caught) {
              problemLabel.setVisible(true);
              callbackWork.onFailure(caught);
              resetButtons();
            }

            @Override
            public void onSuccess(BaseContentItem result) {
              savePlayer((AssetContentItem)result);
            }
          });
        }
        
        
      }
      
      private void savePlayer(AssetContentItem photo) {
        List<String> aliasList = new ArrayList<String>();
        for (String alias : aliasesBox.getText().split(",")) {
          String trimmed = alias.trim();
          if (!trimmed.isEmpty()) {
            aliasList.add(trimmed);
          }
        }
        
        // Collections.emptySet() and Collections.emptyList() don't serialize properly.
        PlayerContentItem player = new PlayerContentItem(null, new Date(), new HashSet<Long>(),
            bioArea.getText(), Importance.MEDIUM, nameBox.getText(), aliasList, 
            typeSelector.getSelectedConstant(), photo);
        // Kinda broken, but this isn't done automatically:
        player.setLocation(new Location(null, null, ""));
        player.setPublishState(PublishState.PUBLISHED);
        problemLabel.setVisible(false);
        contentService.createOrChangeContentItem(player, new AsyncCallback<BaseContentItem>() {
          @Override
          public void onFailure(Throwable caught) {
            problemLabel.setVisible(true);
            callbackWork.onFailure(caught);
            resetButtons();
          }

          @Override
          public void onSuccess(BaseContentItem result) {
            callbackWork.onSuccess(result);
            hide();
            resetButtons();
          }
        });
      }

      private void disableButtons() {
        okButton.setText(WORKING_TEXT);        
        okButton.setEnabled(false);
        cancelButton.setEnabled(false);
      }
      
      private void resetButtons() {
        okButton.setText(OK_TEXT);
        okButton.setEnabled(true);
        cancelButton.setEnabled(true);
      }
    });
  }
}
