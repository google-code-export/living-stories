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
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.livingstories.client.WhitelistedUser;
import com.google.livingstories.client.WhitelistingService;
import com.google.livingstories.client.WhitelistingServiceAsync;
import com.google.livingstories.client.ui.ItemList;
import com.google.livingstories.client.ui.RadioGroup;
import com.google.livingstories.client.ui.RadioGroup.Layout;
import com.google.livingstories.client.ui.RadioGroup.RadioClickHandler;

/**
 * Page to manage whitelisted users.
 */
public class WhitelistManager extends ManagerPane {
  
  private static enum EditMode {
    ADD, DELETE;
  }
  
  /**
   * Create a remote service proxy to talk to the server-side whitelisting persisting service.
   */
  private final WhitelistingServiceAsync whitelistingService 
      = GWT.create(WhitelistingService.class);
  
  private RadioGroup<EditMode> modeSelector;
  private TextBox emailTextBox;
  private HorizontalPanel editorPanel;
  private ItemList<WhitelistedUser> whitelistedUsersListBox;
  private Button saveButton;
  private Button deleteButton;
  private Label statusLabel;
  
  private Label currentWhitelistRestrictionLabel;
  private Button changeWhitelistRestrictionButton;
  private Boolean restrictedToWhitelist = null;
  
  public WhitelistManager() {
    final VerticalPanel contentPanel = new VerticalPanel();
    
    contentPanel.add(createWhitelistRestrictionControl());
    
    // Add the radio buttons to add or delete whitelisted users
    contentPanel.add(createModeSelector());
    
    // Add the text box and the user listbox in a horizontal panel at the center
    HorizontalPanel horizontalPanel = new HorizontalPanel();
    horizontalPanel.add(createEditorPanel());
    horizontalPanel.add(createUserListBox());
    contentPanel.add(horizontalPanel);
    
    // Add buttons to save and delete at the bottom
    contentPanel.add(createSaveDeletePanel());
    createSaveButtonHandler();
    createDeleteButtonHandler();
    
    initWidget(contentPanel);
  }
  
  private Widget createWhitelistRestrictionControl() {
    HorizontalPanel panel = new HorizontalPanel();
    currentWhitelistRestrictionLabel = new Label();
    panel.add(currentWhitelistRestrictionLabel);
    
    changeWhitelistRestrictionButton = new Button();
    changeWhitelistRestrictionButton.setEnabled(false);
    changeWhitelistRestrictionButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent e) {
        if (restrictedToWhitelist != null) {
          whitelistingService.setRestrictedToWhitelist(!restrictedToWhitelist, 
              new WhitelistRestrictionCallback());
        }
      }
    });
    panel.add(changeWhitelistRestrictionButton);
    
    whitelistingService.isRestrictedToWhitelist(new WhitelistRestrictionCallback());
    return panel;
  }
  
  private class WhitelistRestrictionCallback implements AsyncCallback<Boolean> {
    @Override
    public void onFailure(Throwable t) {
      currentWhitelistRestrictionLabel.setText("Unable to get current whitelist status");
      changeWhitelistRestrictionButton.setEnabled(false);
      restrictedToWhitelist = null;
    }
    
    @Override
    public void onSuccess(Boolean result) {
      restrictedToWhitelist = result;
      currentWhitelistRestrictionLabel.setText("Current Status : " 
          + (restrictedToWhitelist ? "WHITELIST ONLY" : "OPEN TO EVERYONE"));
      changeWhitelistRestrictionButton.setEnabled(true);
      changeWhitelistRestrictionButton.setText(
          restrictedToWhitelist ? "LAUNCH TO EVERYONE!" : "Revert to whitelist mode");
    }
  }
  
  /**
   * Create radio buttons at the top of the tab to choose between creating a new theme
   * or editing an existing one.
   */
  private Widget createModeSelector() {
    modeSelector = new RadioGroup<EditMode>("whitelistedUserModeSelector", Layout.HORIZONTAL);
    modeSelector.addButton(EditMode.ADD, "Add a new user to the whitelist");
    modeSelector.addButton(EditMode.DELETE, "Delete a user from the whitelist");
    modeSelector.setValue(EditMode.ADD);
    modeSelector.setClickHandler(new RadioClickHandler<EditMode>() {
      @Override
      public void onClick(EditMode mode) {
        clearEditArea();
        editorPanel.setVisible(mode == EditMode.ADD);
        whitelistedUsersListBox.setVisible(mode == EditMode.DELETE);
        saveButton.setVisible(mode == EditMode.ADD);
        deleteButton.setVisible(mode == EditMode.DELETE);
      }
    });
    return modeSelector;
  }
  
  /**
   * Create a panel for showing the text box to name or rename an theme.
   */
  private Widget createEditorPanel() {
    emailTextBox = new TextBox();
    editorPanel = new HorizontalPanel();
    editorPanel.add(new Label("Enter email address of user to whitelist:"));
    editorPanel.add(emailTextBox);
    
    return editorPanel;
  }
    
  /**
   * Create a list box for displaying all the users that are whitelisted so that the admin
   * can select one to edit or remove.
   */
  private Widget createUserListBox() {
    whitelistedUsersListBox = new ItemList<WhitelistedUser>() {
      @Override
      public void loadItems() {
        whitelistingService.getAllWhitelistedUsers(getCallback(new WhitelistedUserAdaptor()));
      }
    };
    whitelistedUsersListBox.setVisibleItemCount(15);
    whitelistedUsersListBox.setVisible(false);
    return whitelistedUsersListBox;
  }
  
  private class WhitelistedUserAdaptor extends ItemList.ListItemAdapter<WhitelistedUser> {
    @Override
    public String getItemText(WhitelistedUser user) {
      return user.getEmail();
    }

    @Override
    public String getItemValue(WhitelistedUser user) {
      return Long.toString(user.getId());
    }
  }
  
  /**
   * Create buttons to add and delete whitelisted users. And a label for error messages if the
   * updates don't work.
   */
  private Widget createSaveDeletePanel() {
    saveButton = new Button("Save");
    deleteButton = new Button("Delete");
    deleteButton.setVisible(false);
    
    statusLabel = new Label();
    
    HorizontalPanel buttonPanel = new HorizontalPanel();
    buttonPanel.add(saveButton);
    buttonPanel.add(deleteButton);
    buttonPanel.add(statusLabel);
    return buttonPanel;
  }
  
  /**
   * Create a handler for the 'Save' Button. Saves a new content entity if the 'create' radio button
   * has been selected. Saves changes to an existing content entity if the 'edit' radio button has
   * been selected.
   */
  private void createSaveButtonHandler() {
    ClickHandler saveHandler = new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        whitelistingService.addWhitelistedUser(emailTextBox.getText(), 
            new AsyncCallback<WhitelistedUser>() {
          
          @Override
          public void onFailure(Throwable caught) {
            statusLabel.setText("Save not successful. Try again.");
            statusLabel.setStyleName("serverResponseLabelError");
          }
          
          @Override
          public void onSuccess(WhitelistedUser whitelistedUser) {
            statusLabel.setText("Added!");
            statusLabel.setStyleName("serverResponseLabelSuccess");
            emailTextBox.setText("");
            whitelistedUsersListBox.addItem(whitelistedUser.getEmail(), 
                String.valueOf(whitelistedUser.getId()));
          }
          
        });
      }
    };
    saveButton.addClickHandler(saveHandler);
  }
  
  /**
   * Create a handler for the 'Delete' Button.
   */
  private void createDeleteButtonHandler() {
    ClickHandler deleteHandler = new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        if (whitelistedUsersListBox.hasSelection()) {
          final String idToBeDeleted = whitelistedUsersListBox.getSelectedItemValue();
          whitelistingService.deleteWhitelistedUser(Long.valueOf(idToBeDeleted), 
              new AsyncCallback<Void>() {
            
            @Override
            public void onFailure(Throwable caught) {
              statusLabel.setText("Delete not successful. Try again.");
              statusLabel.setStyleName("serverResponseLabelError");
            }

            @Override
            public void onSuccess(Void result) {
              statusLabel.setText("Deleted!");
              statusLabel.setStyleName("serverResponseLabelSuccess");
              whitelistedUsersListBox.removeItemWithValue(idToBeDeleted);
            }
          });
        }
      }
    };
    deleteButton.addClickHandler(deleteHandler);
  }
  
  private void clearEditArea() {
    emailTextBox.setText("");
    statusLabel.setText("");
  }
}
