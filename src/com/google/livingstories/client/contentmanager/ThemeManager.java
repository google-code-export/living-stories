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
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.livingstories.client.LivingStoryRpcService;
import com.google.livingstories.client.LivingStoryRpcServiceAsync;
import com.google.livingstories.client.Theme;
import com.google.livingstories.client.ui.CoordinatedLspSelector;
import com.google.livingstories.client.ui.ItemList;
import com.google.livingstories.client.ui.RadioGroup;
import com.google.livingstories.client.ui.RadioGroup.Layout;
import com.google.livingstories.client.ui.RadioGroup.RadioClickHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Page to enter themes.
 */
public class ThemeManager extends ManagerPane {
  private static enum EditMode {
    CREATE, EDIT;
  }
  
  /**
   * Create a remote service proxy to talk to the server-side lsp persisting service.
   */
  private final LivingStoryRpcServiceAsync livingStoryService
      = GWT.create(LivingStoryRpcService.class);
  
  private RadioGroup<EditMode> modeSelector;
  private TextBox nameBox;
  private CoordinatedLspSelector lspSelector;
  private ChangeHandler lspSelectionHandler;
  private ItemList<Theme> themeListBox;
  private Button saveButton;
  private Button deleteButton;
  private Label statusLabel;
  
  private Map<Long, Theme> idToContentMap = new HashMap<Long, Theme>();
  
  public ThemeManager() {
    final VerticalPanel contentPanel = new VerticalPanel();
    
    contentPanel.add(createLspSelectorPanel());
    
    // Add the radio buttons to create or edit themes
    contentPanel.add(createModeSelector());
    
    // Add the editor and the content listbox in a horizontal panel at the center
    HorizontalPanel horizontalPanel = new HorizontalPanel();
    horizontalPanel.add(createEditorPanel());
    horizontalPanel.add(createThemeListBox());
    contentPanel.add(horizontalPanel);
    
    // Add buttons to save and delete at the bottom
    contentPanel.add(createSaveDeletePanel());
    
    // Event handlers
    createLspSelectionHandler();
    createThemeSelectionHandler();
    createSaveButtonHandler();
    createDeleteButtonHandler();
    
    initWidget(contentPanel);
  }
  
  private Widget createLspSelectorPanel() {
    lspSelector = new CoordinatedLspSelector(livingStoryService);
    return lspSelector.makeContainingPanel();
  }
  
  /**
   * Create radio buttons at the top of the tab to choose between creating a new Theme
   * or editing an existing one.
   */
  private Widget createModeSelector() {
    modeSelector = new RadioGroup<EditMode>("modeSelector", Layout.HORIZONTAL);
    modeSelector.addButton(EditMode.CREATE, "Create a new theme");
    modeSelector.addButton(EditMode.EDIT, "Rename an existing theme");
    modeSelector.setValue(EditMode.CREATE);
    modeSelector.setClickHandler(new RadioClickHandler<EditMode>() {
      @Override
      public void onClick(EditMode mode) {
        clearEditArea();
        if (mode == EditMode.EDIT) {
          populateThemeList();
          themeListBox.setVisible(true);
        } else {
          themeListBox.setVisible(false);
        }
      }
    });
    return modeSelector;
  }
  
  /**
   * Create a panel for showing the text box to name or rename an theme.
   */
  private Widget createEditorPanel() {
    nameBox = new TextBox();
    HorizontalPanel nameBoxPanel = new HorizontalPanel();
    nameBoxPanel.add(new Label("Enter theme name:"));
    nameBoxPanel.add(nameBox);
    
    return nameBoxPanel;
  }
    
  /**
   * Create a list box for displaying all the themes for the selected LSP so that the user
   * can select one to edit.
   */
  private Widget createThemeListBox() {
    themeListBox = new ItemList<Theme>() {
      @Override
      public void loadItems() {
        // Don't load the list of content pieces for an LSP by default. They are only loaded when
        // the user selects the 'edit' radio button.
      }
    };
    themeListBox.setVisibleItemCount(15);
    themeListBox.setVisible(false);
    return themeListBox;
  }
  
  /**
   * Create buttons to save and delete themes. And a label for error messages if the
   * updates don't work.
   */
  private Widget createSaveDeletePanel() {
    saveButton = new Button("Save");
    deleteButton = new Button("Delete");
    deleteButton.setEnabled(false);
    
    statusLabel = new Label();
    
    HorizontalPanel buttonPanel = new HorizontalPanel();
    buttonPanel.add(saveButton);
    buttonPanel.add(deleteButton);
    buttonPanel.add(statusLabel);
    return buttonPanel;
  }
  
  /**
   * Create a handler to handle selection in the LSP list box.
   */
  private void createLspSelectionHandler() {
    lspSelectionHandler = new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        modeSelector.setValue(EditMode.CREATE);
        clearEditArea();
        themeListBox.setVisible(false);
      }
    };
    lspSelector.addChangeHandler(lspSelectionHandler);
  }
  
  /**
   * Create a handler to handle selection of a theme from the theme list box.
   */
  private void createThemeSelectionHandler() {
    ChangeHandler themeSelectionHandler = new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        clearEditArea();
        Long selectedContentId = Long.valueOf(themeListBox.getSelectedItemValue());
        Theme selectedContent = idToContentMap.get(selectedContentId);
        nameBox.setText(selectedContent.getName());
        
        deleteButton.setEnabled(true);
      }
    };
    themeListBox.addChangeHandler(themeSelectionHandler);
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
        boolean createNewContent = (modeSelector.getValue() == EditMode.CREATE);
        Long lspId = Long.valueOf(lspSelector.getSelectedItemValue());
        String name = nameBox.getText();
        
        Long id = null;
        if (!createNewContent) {
          id = Long.valueOf(themeListBox.getSelectedItemValue());
        }
        createOrChangeTheme(createNewContent, id, name, lspId);
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
        deleteContent(Long.valueOf(themeListBox.getSelectedItemValue()));
      }
    };
    deleteButton.addClickHandler(deleteHandler);
  }
  
  /**
   * Make an RPC call to the server to persist a new theme entity or change an
   * existing theme entity in the datastore.
   */
  private void createOrChangeTheme(final boolean createNewTheme, Long id, String name, Long lspId) {
    AsyncCallback<Theme> callback = new AsyncCallback<Theme>() {
      public void onFailure(Throwable caught) {
        statusLabel.setText("Save not successful. Try again.");
        statusLabel.setStyleName("serverResponseLabelError");
      }
      
      public void onSuccess(Theme content) {
        statusLabel.setText("Saved!");
        statusLabel.setStyleName("serverResponseLabelSuccess");
        idToContentMap.put(content.getId(), content);
        String idAsString = String.valueOf(content.getId());
        if (createNewTheme) {
          themeListBox.addItem(content.getName(), idAsString);
        } else {
          themeListBox.renameItemWithValue(idAsString, content.getName());
        }
      }
    };
    
    livingStoryService.saveTheme(new Theme(createNewTheme ? null : id, name, lspId), callback);
  }
  
  /**
   * Make an RPC call to the server to delete an existing theme entity. After it's done, remove
   * it from the theme Listbox and clear the edit area.
   */
  private void deleteContent(final Long contentId) {
    AsyncCallback<Void> callback = new AsyncCallback<Void>() {
      public void onFailure(Throwable caught) {
        statusLabel.setText("Delete not successful. Try again.");
        statusLabel.setStyleName("serverResponseLabelError");
      }
      
      public void onSuccess(Void result) {
        statusLabel.setText("Saved!");
        statusLabel.setStyleName("serverResponseLabelSuccess");
        clearEditArea();
        themeListBox.removeItemWithValue(String.valueOf(contentId));
        idToContentMap.remove(contentId);
      }
    };
    
    livingStoryService.deleteTheme(contentId, callback);
  }
  
  private void clearEditArea() {
    nameBox.setText("");
    statusLabel.setText("");
    deleteButton.setEnabled(false);
  }
  
  /**
   * Retrieve the list of themes associated with a given LSP from the server and populate
   * the given list box with the initial snippet from each of them. 
   */
  private void populateThemeList() {
    AsyncCallback<List<Theme>> callback = new AsyncCallback<List<Theme>>() {
      public void onFailure(Throwable caught) {
        themeListBox.clear();
        themeListBox.addItem("Failed to retrieve data.");
        themeListBox.setEnabled(false);
      }
      
      public void onSuccess(List<Theme> themes) {
        themeListBox.clear();
        for (Theme theme : themes) {
          themeListBox.addItem(theme.getName(), String.valueOf(theme.getId()));
          idToContentMap.put(theme.getId(), theme);
        }
        themeListBox.setEnabled(true);
      }
    };
    
    livingStoryService.getThemesForLsp(Long.valueOf(lspSelector.getSelectedItemValue()),
        callback);
  }
  
  @Override
  public void onLspsChanged() {
    lspSelector.refresh();
  }
  
  @Override
  public void onShow() {
    lspSelector.selectCoordinatedLsp();
    lspSelectionHandler.onChange(null);
  }
}
