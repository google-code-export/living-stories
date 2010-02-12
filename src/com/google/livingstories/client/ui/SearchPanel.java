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

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.livingstories.client.AssetType;
import com.google.livingstories.client.AtomType;
import com.google.livingstories.client.Importance;
import com.google.livingstories.client.NarrativeType;
import com.google.livingstories.client.PlayerType;
import com.google.livingstories.client.PublishState;
import com.google.livingstories.client.contentmanager.SearchTerms;
import com.google.livingstories.client.util.DateUtil;
import com.google.livingstories.client.util.LivingStoryData;

import java.util.EnumSet;

/**
 * Search interface for atoms.
 * To use this widget, add it to the page where you want it shown.
 * Then create and add a {@link com.google.livingstories.client.ui.SearchPanel.SearchHandler}
 * object that will get passed a QueryFilter.  This SearchHandler can then add
 * additional arguments to the QueryFilter if desired, and then execute the appropriate query,
 * updating your UI however you like once the search results return.
 */
public class SearchPanel extends Composite {
  private static final EnumSet<AtomType> atomTypesWithSubtypes =
      EnumSet.of(AtomType.PLAYER, AtomType.ASSET, AtomType.NARRATIVE);
  
  private VerticalPanel contentPanel;
  private Grid filterGrid;
  private EnumDropdown<AtomType> atomType;
  private int atomSubtypeRow;
  private EnumDropdown<PlayerType> playerType;
  private EnumDropdown<AssetType> assetType;
  private EnumDropdown<NarrativeType> narrativeType;
  private TextBox beforeDate;
  private TextBox afterDate;
  private EnumDropdown<Importance> importance;
  private EnumDropdown<PublishState> publishState;
  private Button submitButton;

  private SearchHandler handler;

  public SearchPanel() {
    contentPanel = new VerticalPanel();
    
    filterGrid = new Grid(0, 2);
    createPublishStateFilter();
    createAtomTypeFilter();
    createAtomSubtypeFilters();
    createBeforeDateFilter();
    createAfterDateFilter();
    createImportanceFilter();

    contentPanel.add(new Label("Search content entities:"));
    contentPanel.add(filterGrid);
    contentPanel.add(createSubmitButton());
    
    initWidget(contentPanel);
  }

  private void createPublishStateFilter() {
    publishState = EnumDropdown.newInstance(PublishState.class);
    publishState.selectConstant(PublishState.PUBLISHED);
    
    int row = filterGrid.insertRow(filterGrid.getRowCount());
    filterGrid.setWidget(row, 0, new Label("Publish state:"));
    filterGrid.setWidget(row, 1, publishState);
  }

  private void createAtomTypeFilter() {
    atomType = EnumDropdown.newInstance(AtomType.class, "All");
    atomType.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        AtomType selectedType = atomType.getSelectedConstant();
        if (atomTypesWithSubtypes.contains(selectedType)) {
          filterGrid.getRowFormatter().setVisible(atomSubtypeRow, true);
          playerType.setVisible(selectedType == AtomType.PLAYER);
          assetType.setVisible(selectedType == AtomType.ASSET);
          narrativeType.setVisible(selectedType == AtomType.NARRATIVE);
        } else {
          filterGrid.getRowFormatter().setVisible(atomSubtypeRow, false);
        }
      }
    });
    
    int row = filterGrid.insertRow(filterGrid.getRowCount());
    filterGrid.setWidget(row, 0, new Label("Content entity type:"));
    filterGrid.setWidget(row, 1, atomType);
  }

  private void createAtomSubtypeFilters() {
    playerType = EnumDropdown.newInstance(PlayerType.class, "All");
    playerType.setVisible(false);
    assetType = EnumDropdown.newInstance(AssetType.class, "All");
    assetType.setVisible(false);
    narrativeType = EnumDropdown.newInstance(NarrativeType.class, "All");
    narrativeType.setVisible(false);
    
    FlowPanel specialFiltersPanel = new FlowPanel();
    specialFiltersPanel.add(playerType);
    specialFiltersPanel.add(assetType);
    specialFiltersPanel.add(narrativeType);

    atomSubtypeRow = filterGrid.getRowCount();
    int row = filterGrid.insertRow(filterGrid.getRowCount());
    filterGrid.setWidget(row, 0, new Label("Content entity subtype:"));
    filterGrid.setWidget(row, 1, specialFiltersPanel);
    filterGrid.getRowFormatter().setVisible(atomSubtypeRow, false);
  }
  
  private void createBeforeDateFilter() {
    beforeDate = new TextBox();

    int row = filterGrid.insertRow(filterGrid.getRowCount());
    filterGrid.setWidget(row, 0, new Label("Created before:"));
    filterGrid.setWidget(row, 1, beforeDate);
  }

  private void createAfterDateFilter() {
    afterDate = new TextBox();

    int row = filterGrid.insertRow(filterGrid.getRowCount());
    filterGrid.setWidget(row, 0, new Label("Created after:"));
    filterGrid.setWidget(row, 1, afterDate);
  }

  private void createImportanceFilter() {
    importance = EnumDropdown.newInstance(Importance.class, "All");

    int row = filterGrid.insertRow(filterGrid.getRowCount());
    filterGrid.setWidget(row, 0, new Label("Importance:"));
    filterGrid.setWidget(row, 1, importance);
  }

  private Widget createSubmitButton() {
    submitButton = new Button("Search");
    
    submitButton.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent e) {
        handler.onSearch(getSearchTerms());
      }
    });
    
    return submitButton;
  }

  public SearchTerms getSearchTerms() {
    SearchTerms searchTerms = new SearchTerms();
    searchTerms.livingStoryId = LivingStoryData.getLspId();
    searchTerms.atomType = atomType.getSelectedConstant();
    searchTerms.assetType = assetType.getSelectedConstant();
    if (!afterDate.getValue().isEmpty()) {
      DateUtil.parseShortDate(afterDate.getValue(), searchTerms.afterDate);
    }
    if (!beforeDate.getValue().isEmpty()) {
      DateUtil.parseShortDate(beforeDate.getValue(), searchTerms.beforeDate);
    }
    searchTerms.importance = importance.getSelectedConstant();
    searchTerms.publishState = publishState.getSelectedConstant();
    return searchTerms;
  }

  public void addSearchHandler(SearchHandler handler) {
    this.handler = handler;
  }
  
  public static interface SearchHandler {
    void onSearch(SearchTerms searchTerms);
  }
}
