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

package com.google.livingstories.client.lsp;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.livingstories.client.AssetType;
import com.google.livingstories.client.AtomType;
import com.google.livingstories.client.AtomTypesBundle;
import com.google.livingstories.client.ClientConstants;
import com.google.livingstories.client.FilterSpec;
import com.google.livingstories.client.UserRpcService;
import com.google.livingstories.client.UserRpcServiceAsync;
import com.google.livingstories.client.lsp.event.EventBus;
import com.google.livingstories.client.lsp.event.ThemeSelectedEvent;
import com.google.livingstories.client.util.AnalyticsUtil;
import com.google.livingstories.client.util.GlobalUtil;
import com.google.livingstories.client.util.HistoryManager;
import com.google.livingstories.client.util.LivingStoryData;
import com.google.livingstories.client.util.NullCallback;
import com.google.livingstories.client.util.HistoryManager.HistoryPages;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Widget that contains different types of filters for the events and atoms displayed on the LSP.
 */
public class FilterWidget extends Composite {
  private static final UserRpcServiceAsync userService = GWT.create(UserRpcService.class); 
  private static final AtomType[] ATOM_TYPE_PRESENTATION_ORDER = {
    AtomType.EVENT, AtomType.NARRATIVE, AtomType.PLAYER, AtomType.QUOTE,
    AtomType.DATA, AtomType.REACTION   // asset deliberately skipped 
  };
  private static final String OPINION_TEXT;
  static {
    // oddly, we can't do evaluate this as an expression without a cast: 
    ClientConstants consts = GWT.create(ClientConstants.class);
    OPINION_TEXT = consts.contentFilterNameNarrativeOpinion();
  }
  
  private VerticalPanel filterPanel;
  
  private Map<String, FilterRow> keyToFilterRowMap;
  private FilterRow allImportanceFilter;
  private FilterRow mostImportantFilter;
  private FilterRow newestFirstFilter;
  private FilterRow oldestFirstFilter;
  private Label setAsDefaultLabel;
  private Label defaultViewLabel;
  
  private Map<Long, AtomTypesBundle> atomTypesBundles;
  
  private FilterSpec currentFilters;
  private Long selectedThemeId;
  private boolean loaded = false;
  
  public FilterWidget() {
    super();
    
    keyToFilterRowMap = new HashMap<String, FilterRow>();
    
    filterPanel = new VerticalPanel();
    DOM.setStyleAttribute(filterPanel.getElement(), "paddingTop", "5px");
    filterPanel.setWidth("125px");

    initWidget(filterPanel);
    
    EventBus.INSTANCE.addHandler(ThemeSelectedEvent.TYPE, new ThemeSelectedEvent.Handler() {
      @Override
      public void onThemeSelected(ThemeSelectedEvent e) {
        setSelectedTheme(e.getThemeId());
      }
    });
  }
    
  public void load(Map<Long, AtomTypesBundle> atomTypesBundles, Long selectedThemeId) {
    this.atomTypesBundles = atomTypesBundles;
    populateFilterPanel();
    refreshFilters();
    if (selectedThemeId != null) {
      setAtomTypeFilterVisibility(selectedThemeId);
    }
    loaded = true;
  }
  
  /**
   * Populate the left column with links for the available atom types and filters for the
   * importance.
   */
  private void populateFilterPanel() {
    createAtomTypeFilters();
    addSeparator();
    createImportanceFilters();
    addSeparator();
    createTimeSortControls();
    if (LivingStoryData.isLoggedIn()) {
      addSeparator();
      createSetAsDefaultLink();
    }
  }

  /**
   * Informs the filter widget that the user has requested a change in selected theme.
   * Apart from side effects, also sets a history token with an adjusted filter spec.
   * This lets us get different atoms if the current filter isn't available in the
   * new theme.
   * @param selectedThemeId the new selectedThemeId
   */
  public void setSelectedTheme(Long selectedThemeId) {
    boolean isChange = !GlobalUtil.equal(this.selectedThemeId, selectedThemeId);
    this.selectedThemeId = selectedThemeId;
    FilterSpec filterCopy = copyCurrentFilterSpec();

    if (isChange) {
      if (loaded) {
        setAtomTypeFilterVisibility(selectedThemeId);
      }
      filterCopy.themeId = selectedThemeId;
      
      if (!filterAppliesToTheme(filterCopy, selectedThemeId)) {
        filterCopy.atomType = null;
        filterCopy.assetType = null;
        filterCopy.opinion = false;
      }
    }

    HistoryManager.newTokenWithEvent(
        HistoryPages.OVERVIEW, filterCopy.getFilterParams(), null);
  }
  
  /**
   * Answers queries on whether the theme the user might like to switch to can be filtered
   * according to the current type filter. If not, the caller should likely switch it back to the
   * "all" view.
   */
  private boolean filterAppliesToTheme(FilterSpec filterSpec, Long newThemeId) {
    AtomTypesBundle bundle = atomTypesBundles.get(newThemeId);
    if (bundle == null) {
      throw new IllegalArgumentException("filterAppliesToTheme given an unknown theme id argument");
    }
    return getTuplesForTypesBundle(bundle).contains(new TypeFilterTuple(
        "", filterSpec.atomType, filterSpec.assetType, filterSpec.opinion));
  }
  
  private void createAtomTypeFilters() {
    for (TypeFilterTuple tuple : getTuplesForTypesBundle(atomTypesBundles.get(null))) {
      createFilterRow(tuple);
    }
    
    // Invisible spacer element that will have the separator attached to it (via styling).
    // Can't attach directly to filters since they can appear/disappear for different themes.
    SimplePanel spacer = new SimplePanel();
    spacer.setPixelSize(10, 0);
    spacer.getElement().getStyle().setProperty("lineHeight", "0");
    filterPanel.add(spacer);
  }
  
  private void setAtomTypeFilterVisibility(Long themeId) {
    Set<TypeFilterTuple> allTuples = getTuplesForTypesBundle(atomTypesBundles.get(null));
    Set<TypeFilterTuple> visibleTuples =
      themeId == null ? null : getTuplesForTypesBundle(atomTypesBundles.get(themeId));
    // There's no need to appeal to visibleTuples at all if themeId is null.
    
    for (TypeFilterTuple tuple : allTuples) {
      setFilterRowVisibility(tuple, themeId == null ? true : visibleTuples.contains(tuple));
    }
  }
  
  private void createFilterRow(TypeFilterTuple tuple) {
    String key = getAtomFilterKey(tuple.atomType, tuple.assetType, tuple.isOpinion);
    FilterRow row = new FilterRow(tuple.name,
        new AtomTypeFilterHandler(tuple.atomType, tuple.assetType, tuple.isOpinion, tuple.name));
    filterPanel.add(row);
    keyToFilterRowMap.put(key, row);
  }
  
  private void setFilterRowVisibility(TypeFilterTuple tuple, boolean visible) {
    keyToFilterRowMap.get(
        getAtomFilterKey(tuple.atomType, tuple.assetType, tuple.isOpinion)).setVisible(visible);
  }
  
  private Set<TypeFilterTuple> getTuplesForTypesBundle(AtomTypesBundle atomTypesBundle) {
    Set<TypeFilterTuple> tuples = new LinkedHashSet<TypeFilterTuple>();
    
    tuples.add(new TypeFilterTuple(LspMessageHolder.consts.allTypes(), null, null));
    for (AtomType atomType : ATOM_TYPE_PRESENTATION_ORDER) {
      if (atomTypesBundle.availableAtomTypes.contains(atomType)) {
        tuples.add(new TypeFilterTuple(atomType.getFilterString(), atomType, null));
        if (atomType == AtomType.NARRATIVE && atomTypesBundle.opinionAvailable) {
          tuples.add(new TypeFilterTuple(OPINION_TEXT, atomType, null, true));
        }
      }
    }
    
    // Now handle assets, except for links.
    if (atomTypesBundle.availableAtomTypes.contains(AtomType.ASSET)) {
      for (AssetType assetType : EnumSet.complementOf(EnumSet.of(AssetType.DOCUMENT))) {
        if (atomTypesBundle.availableAssetTypes.contains(assetType)) {
          tuples.add(new TypeFilterTuple(assetType.getPluralPresentationString(),
              AtomType.ASSET, assetType));
        }
      }
    }
    
    return tuples;
  }
  
  private class AtomTypeFilterHandler implements ClickHandler {
    private AtomType atomType;
    private AssetType assetType;
    private boolean opinion;
    private String filterName;
    
    public AtomTypeFilterHandler(AtomType atomType, AssetType assetType, boolean opinion, 
        String filterName) {
      this.atomType = atomType;
      this.assetType = assetType;
      this.opinion = opinion;
      this.filterName = filterName;
    }
    
    @Override
    public void onClick(ClickEvent event) {
      if (currentFilters.atomType != atomType || currentFilters.assetType != assetType
          || currentFilters.opinion != opinion) {
        FilterSpec newFilter = copyCurrentFilterSpec();
        newFilter.atomType = atomType;
        newFilter.assetType = assetType;
        newFilter.opinion = opinion;
        HistoryManager.newTokenWithEvent(HistoryPages.OVERVIEW,
            newFilter.getFilterParams(), null);
        AnalyticsUtil.trackFilterClick(LivingStoryData.getLivingStoryUrl(), filterName);
      }
    }
  }
  
  /**
   * Return a key that is used for identifying an atom type filter option. The key is composed
   * of the atom type and the asset type if the atom is an asset.
   */
  private String getAtomFilterKey(AtomType atomType, AssetType assetType, boolean opinion) {
    if (atomType == null) {
      return "";
    } else if (atomType == AtomType.ASSET && assetType != null) {
      return atomType.name() + "," + assetType.name();
    } else if (atomType == AtomType.NARRATIVE) {
      return atomType.name() + ",," + opinion;
    } else {
      return atomType.name();
    }
  }
  
  /**
   * Create options in the filter panel to switch between 'all' and the 'most important' items
   */
  private void createImportanceFilters() {
    allImportanceFilter = new FilterRow(LspMessageHolder.consts.allImportance(), 
        new ImportanceFilterHandler(false));
    filterPanel.add(allImportanceFilter);
    keyToFilterRowMap.put(getImportanceFilterKey(false), allImportanceFilter);
    
    mostImportantFilter = new FilterRow(LspMessageHolder.consts.highImportance(), 
        new ImportanceFilterHandler(true));
    filterPanel.add(mostImportantFilter);
    keyToFilterRowMap.put(getImportanceFilterKey(true), mostImportantFilter);
  }
  
  private class ImportanceFilterHandler implements ClickHandler {
    private boolean importantOnly;
    
    public ImportanceFilterHandler(boolean importantOnly) {
      this.importantOnly = importantOnly;
    }
    
    @Override
    public void onClick(ClickEvent event) {
      if (importantOnly != currentFilters.importantOnly) {
        FilterSpec newFilter = copyCurrentFilterSpec();
        newFilter.importantOnly = importantOnly;
        HistoryManager.newTokenWithEvent(HistoryPages.OVERVIEW,
            newFilter.getFilterParams(), null);
        AnalyticsUtil.trackFilterClick(LivingStoryData.getLivingStoryUrl(), 
            importantOnly ? "Most important" : "All importance");
      }
    }
  }
  
  private String getImportanceFilterKey(boolean importantOnly) {
    return "importance:" + importantOnly;
  }
  
  /**
   * Create options in the filter panel to switch the sorting of the items by time.
   */
  private void createTimeSortControls() {
    newestFirstFilter = new FilterRow(LspMessageHolder.consts.newestFirst(),
        new TimeSortHandler(false));
    filterPanel.add(newestFirstFilter);
    keyToFilterRowMap.put(getTimeSortKey(false), newestFirstFilter);
    
    oldestFirstFilter = new FilterRow(LspMessageHolder.consts.oldestFirst(),
        new TimeSortHandler(true));
    filterPanel.add(oldestFirstFilter);
    keyToFilterRowMap.put(getTimeSortKey(true), oldestFirstFilter);
  }
    
  private class TimeSortHandler implements ClickHandler {
    private boolean oldestFirst;
    
    public TimeSortHandler(boolean oldestFirst) {
      this.oldestFirst = oldestFirst;
    }
    
    @Override
    public void onClick(ClickEvent event) {
      if (oldestFirst != currentFilters.oldestFirst) {
        FilterSpec newFilter = copyCurrentFilterSpec();
        newFilter.oldestFirst = oldestFirst;
        HistoryManager.newTokenWithEvent(HistoryPages.OVERVIEW,
            newFilter.getFilterParams(), null);
        AnalyticsUtil.trackFilterClick(LivingStoryData.getLivingStoryUrl(), 
            oldestFirst ? "Oldest first" : "Newest first");
      }
    }
  }
  
  private String getTimeSortKey(boolean oldestFirst) {
    return "oldestFirst:" + oldestFirst;
  }

  /**
   * Create a 'set default view' link in the options panel so the user can
   * set how they want to view lsps in the future.
   */
  private void createSetAsDefaultLink() {
    setAsDefaultLabel = new Label(LspMessageHolder.consts.setAsDefault());
    setAsDefaultLabel.setStylePrimaryName("unselectedToolbeltFilter");
    setAsDefaultLabel.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        // Get a copy of the current filters, but set themeId to null since we don't
        // care about that here.
        FilterSpec filterCopy = copyCurrentFilterSpec();
        filterCopy.themeId = null;
        userService.setDefaultStoryView(filterCopy, new NullCallback<Void>());
        setAsDefaultLabel.setVisible(false);
        defaultViewLabel.setVisible(true);
        LivingStoryData.setDefaultPage(filterCopy.getFilterParams());
      }
    });
    setAsDefaultLabel.setVisible(false);
    filterPanel.add(setAsDefaultLabel);
    
    defaultViewLabel = new Label(LspMessageHolder.consts.defaultView());
    defaultViewLabel.setStylePrimaryName("selectedToolbeltFilter");
    defaultViewLabel.setVisible(false);
    
    filterPanel.add(defaultViewLabel);
  }

  private void addSeparator() {
    filterPanel.getWidget(filterPanel.getWidgetCount() - 1).addStyleName("toolbeltSeparator");
  }
  
  private void refreshFilters() {
    if (currentFilters == null || keyToFilterRowMap.isEmpty()) {
      // Don't do this if the filters haven't been set yet, or if the filter data
      // hasn't loaded yet.
      return;
    }
    
    keyToFilterRowMap.get(getAtomFilterKey(
        currentFilters.atomType, currentFilters.assetType, currentFilters.opinion)).select();
    keyToFilterRowMap.get(getImportanceFilterKey(currentFilters.importantOnly)).select();
    keyToFilterRowMap.get(getTimeSortKey(currentFilters.oldestFirst)).select();
    
    String defaultView = LivingStoryData.getDefaultPage();
    FilterSpec defaultFilterSpec = (defaultView == null || defaultView.length() == 0 ? 
        new FilterSpec() : new FilterSpec(defaultView));
    if (LivingStoryData.isLoggedIn()) {
      if (filtersEqual(currentFilters, defaultFilterSpec)) {
        setAsDefaultLabel.setVisible(false);
        defaultViewLabel.setVisible(true);
      } else {
        setAsDefaultLabel.setVisible(true);
        defaultViewLabel.setVisible(false);
      }
    }
  }

  public FilterSpec getFilter() {
    return currentFilters == null ? null : new FilterSpec(currentFilters);
  }
  
  /**
   * Sets the selected filter rows based on the specified filterSpec.
   */
  public void setFilter(FilterSpec filter) {
    // Set the current filters to the provided filter spec, and refreshes which items
    // are shown as selected.
    if (currentFilters != null && !filtersEqual(filter, currentFilters)) {
      keyToFilterRowMap.get(getAtomFilterKey(
          currentFilters.atomType, currentFilters.assetType, currentFilters.opinion)).unselect();
      keyToFilterRowMap.get(getImportanceFilterKey(currentFilters.importantOnly)).unselect();
      keyToFilterRowMap.get(getTimeSortKey(currentFilters.oldestFirst)).unselect();
    }
    currentFilters = filter;
    refreshFilters();
  }

  private FilterSpec copyCurrentFilterSpec() {
    return currentFilters == null ? new FilterSpec() : new FilterSpec(currentFilters);
  }
  
  /**
   * Compares two filter specs, ignoring the irrelevant fields (themeId).
   */
  private boolean filtersEqual(FilterSpec filter1, FilterSpec filter2) {
    return filter1.importantOnly == filter2.importantOnly
        && filter1.oldestFirst == filter2.oldestFirst
        && filter1.opinion == filter2.opinion
        && filter1.atomType == filter2.atomType
        && filter1.assetType == filter2.assetType;
  }
  
  /**
   * A row in the filter widget that corresponds to an option and contains an arrow and the label.
   */
  private class FilterRow extends Composite {
    public HTML arrow;
    public Label label;
    
    public FilterRow(String labelName, ClickHandler handler) {
      super();
      arrow = new HTML("&nbsp;&nbsp;");
      arrow.addStyleName("bold");
      
      label = new Label(labelName);
      label.setStylePrimaryName("unselectedToolbeltFilter");
      label.addClickHandler(handler);
      
      HorizontalPanel row = new HorizontalPanel();
      row.add(arrow);
      row.add(label);
      initWidget(row);
    }
    
    public void select() {
      arrow.setHTML("&#8250;&nbsp;");
      label.setStylePrimaryName("selectedToolbeltFilter");
    }
    
    public void unselect() {
      arrow.setHTML("&nbsp;&nbsp;");
      label.setStylePrimaryName("unselectedToolbeltFilter");
    }
  }
  
  private class TypeFilterTuple {
    String name;
    public AtomType atomType;
    public AssetType assetType;
    public boolean isOpinion;
    
    public TypeFilterTuple(String name, AtomType atomType, AssetType assetType,
        boolean isOpinion) {
      this.name = name;
      this.atomType = atomType;
      this.assetType = assetType;
      this.isOpinion = isOpinion;
    }
    
    public TypeFilterTuple(String name, AtomType atomType, AssetType assetType) {
      this(name, atomType, assetType, false);
    }
    
    // methods below generated by eclipse, with some streamlining. Also, we don't care about the
    // name as far as equality goes.
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + getOuterType().hashCode();
      result = prime * result + ((assetType == null) ? 0 : assetType.hashCode());
      result = prime * result + ((atomType == null) ? 0 : atomType.hashCode());
      result = prime * result + (isOpinion ? 1231 : 1237);
      return result;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (getClass() != obj.getClass()) return false;
      TypeFilterTuple other = (TypeFilterTuple) obj;
      if (!getOuterType().equals(other.getOuterType())) return false;
      if (assetType == null) {
        if (other.assetType != null) return false;
      } else if (assetType != other.assetType) return false;
      if (atomType == null) {
        if (other.atomType != null) return false;
      } else if (atomType != other.atomType) return false;
      if (isOpinion != other.isOpinion) return false;
      return true;
    }
    
    private FilterWidget getOuterType() {
      return FilterWidget.this;
    }
  }
}
