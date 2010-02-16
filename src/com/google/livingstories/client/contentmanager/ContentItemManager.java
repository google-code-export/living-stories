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

import com.google.gwt.ajaxloader.client.AjaxLoader;
import com.google.gwt.ajaxloader.client.AjaxLoader.AjaxLoaderOptions;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.control.SmallMapControl;
import com.google.gwt.maps.client.event.MapRightClickHandler;
import com.google.gwt.maps.client.geocode.Geocoder;
import com.google.gwt.maps.client.geocode.LatLngCallback;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.user.datepicker.client.DatePicker;
import com.google.livingstories.client.AssetContentItem;
import com.google.livingstories.client.AssetType;
import com.google.livingstories.client.ContentItemType;
import com.google.livingstories.client.BackgroundContentItem;
import com.google.livingstories.client.BaseContentItem;
import com.google.livingstories.client.ContentRpcService;
import com.google.livingstories.client.ContentRpcServiceAsync;
import com.google.livingstories.client.DataContentItem;
import com.google.livingstories.client.DefaultContentItem;
import com.google.livingstories.client.EventContentItem;
import com.google.livingstories.client.Importance;
import com.google.livingstories.client.LivingStoryRpcService;
import com.google.livingstories.client.LivingStoryRpcServiceAsync;
import com.google.livingstories.client.Location;
import com.google.livingstories.client.NarrativeContentItem;
import com.google.livingstories.client.NarrativeType;
import com.google.livingstories.client.PlayerContentItem;
import com.google.livingstories.client.PlayerType;
import com.google.livingstories.client.PublishState;
import com.google.livingstories.client.QuoteContentItem;
import com.google.livingstories.client.ReactionContentItem;
import com.google.livingstories.client.StoryPlayerContentItem;
import com.google.livingstories.client.Theme;
import com.google.livingstories.client.lsp.views.contentitems.BasePlayerPreview;
import com.google.livingstories.client.lsp.views.contentitems.StreamViewFactory;
import com.google.livingstories.client.ui.ContentItemListBox;
import com.google.livingstories.client.ui.CoordinatedLivingStorySelector;
import com.google.livingstories.client.ui.EnumDropdown;
import com.google.livingstories.client.ui.ItemList;
import com.google.livingstories.client.ui.RichTextEditor;
import com.google.livingstories.client.ui.SingleContentItemSelectionPanel;
import com.google.livingstories.client.ui.SuggestionAwareContentItemListBox;
import com.google.livingstories.client.util.Constants;
import com.google.livingstories.client.util.DateUtil;
import com.google.livingstories.client.util.GlobalUtil;
import com.google.livingstories.client.util.LivingStoryData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Page to enter content items.
 * TODO: convert to using UiBinder.
 */
public class ContentItemManager extends ManagerPane {
  private static final int MAP_HEIGHT = 256;
  private static final int MAP_WIDTH = 256;
  private static final int MAP_ZOOM = 10;
  @SuppressWarnings("deprecation")
  private static final String DEFAULT_TIME_STRING = DateUtil.formatTime(new Date(0, 0, 1, 12, 0));
  
  private static final int LONG_TEXTBOX_VISIBLE_LENGTH = 60;

  private static final ContentManagerMessages msgs = GWT.create(ContentManagerMessages.class);
  
  /**
   * Create a remote service proxy to talk to the server-side content persisting service.
   */
  private final ContentRpcServiceAsync contentRpcService = GWT.create(ContentRpcService.class);
  
  /**
   * Create a remote service proxy to talk to the server-side living story persisting service.
   */
  private final LivingStoryRpcServiceAsync livingStoryService
      = GWT.create(LivingStoryRpcService.class);
  
  private DeckPanel contentPanel;
  
  private EnumDropdown<ContentItemType> contentItemTypeSelector;
  private DeckPanel specialAttributesPanel;
  private Label contentTitle;
  private RichTextEditor contentEditor;
  private Label contentItemIdLabel;
  private Label timestamp;
  private CoordinatedLivingStorySelector livingStorySelector;
  private ChangeHandler livingStorySelectionHandler;
  private EnumDropdown<Importance> importanceSelector;
  private ContentItemListBox contentItemListBox;

  // Contributor management stuff
  private HTML contributorListHtml;
  private Label clearContributorsControl;
  private PlayerSuggestAndAddPanel contributorSuggestPanel;
  private Map<Long, String> currentContributorIdsToNamesMap;
  private Map<Long, PlayerContentItem> unassignedPlayersIdToContentItemMap;
  
  private ItemList<Theme> themeListBox;
  
  // Location related stuff
  private boolean mapsKeyExists;
  private String mapsKey;
  private TextBox latitudeTextBox;
  private TextBox longitudeTextBox;
  private TextArea locationDescriptionTextArea;
  private RadioButton useDisplayedLocation;
  private RadioButton useAlternateLocation;
  private TextBox alternateTextBox;
  private RadioButton useManualLatLong;
  private Button geocodeButton;
  private Label geocoderStatus;
  private MapWidget map;
  private Marker mapMarker;
  
  // Source related stuff
  private TextBox sourceDescriptionBox;
  private SingleContentItemSelectionPanel sourceContentItemSelector;
  private DockPanel pickerPanel;
  private SuggestionAwareContentItemListBox linkedContentItemSelector;
  private ListBox selectedLinkedContentItems;
  private Label advisoryLabel;
  
  private Label publishStateLabel;
  private SaveControlsWidgetGroup topSaveControls = new SaveControlsWidgetGroup();
  private SaveControlsWidgetGroup bottomSaveControls = new SaveControlsWidgetGroup();
  private SimplePanel previewPanel;
  
  /*** Event specific attributes ***/
  private TextBox dateTrigger;
  private PopupPanel datePopup;
  private DatePicker startDatePicker;
  private TextBox startTime;
  private CheckBox hasSeparateEndDate;
  private DatePicker endDatePicker;
  private TextBox endTime;
  private TextBox updateEditor;
  private RichTextEditor summaryEditor;
  
  /*** Player specific attributes ***/
  private TextBox nameTextBox;
  private TextBox aliasesTextBox;
  private EnumDropdown<PlayerType> playerTypeSelector;
  private SingleContentItemSelectionPanel photoSelector;
  
  /*** Story Player specific attributes ***/
  private FlowPanel parentPlayerDisplayPanel;
  private Label changeParentLink;
  private Label parentSelectionInstructions;
  private PlayerSuggestAndAddPanel generalPlayerSuggestPanel;
  private PlayerContentItem parentPlayer;
  
  private DeckPanel playerAttributesPanel; 
  
  /*** Asset specific attributes ***/
  private EnumDropdown<AssetType> assetTypeSelector;
  private Label previewUrlLabel;
  private TextBox previewUrlTextBox;
  private Label captionLabel;
  private TextArea captionTextArea;
  private Label imageUrlLabel;
  private TextBox imageUrlTextBox;
  
  /*** Narrative specific attributes ***/
  private TextBox headlineTextBox;
  private EnumDropdown<NarrativeType> narrativeTypeSelector;
  private DateBox narrativeDateBox;
  private RichTextEditor narrativeSummaryTextArea;
  
  /*** Background specific attributes ***/
  private TextBox conceptNameTextBox;
  
  private Map<ContentItemType, Integer> contentItemTypeToEditorPanelMap =
      new HashMap<ContentItemType, Integer>();
  
  public ContentItemManager() {
    mapsKey = LivingStoryData.getMapsKey();
    mapsKeyExists = mapsKey != null && !mapsKey.isEmpty();
    
    HorizontalPanel container = new HorizontalPanel();
    
    container.add(createControlsPanel());
    container.add(createContentPanel());
    
    // Event handlers
    createLivingStorySelectionHandler();
    createContentItemTypeSelectionHandler();
    createContentItemSelectionHandler();
    createSaveDeleteHandlers(topSaveControls);
    createSaveDeleteHandlers(bottomSaveControls);

    initWidget(container);
  }

  private Widget createControlsPanel() {
    VerticalPanel controlsPanel = new VerticalPanel();
    controlsPanel.add(createLivingStorySelector());
    controlsPanel.add(createNewContentItemButton());
    controlsPanel.add(createContentItemListBox());
    return controlsPanel;
  }
  
  private Widget createLivingStorySelector() {
    // Our livingStorySelector extends the superclass slightly, in that when the list
    // of living stories is successfully loaded up, this triggers the list boxes
    // to load the content items and retrieve the themes for the now-selected living story.
    livingStorySelector = new CoordinatedLivingStorySelector(livingStoryService, true) {
      @Override
      public void onSuccessNextStep() {
        super.onSuccessNextStep();
        if (hasSelection()) {
          LivingStoryData.setLivingStoryId(getSelectedLivingStoryId());
          contentItemListBox.loadItemsForLivingStory(getSelectedLivingStoryId());
          linkedContentItemSelector.loadItemsForLivingStory(getSelectedLivingStoryId());
          themeListBox.refresh();
        }
      }
    };
    return livingStorySelector;
  }
  
  private Widget createNewContentItemButton() {
    Button newContentItemButton = new Button("New Content Entity");
    newContentItemButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        createOrChangeContentItem(new DefaultContentItem(
            null, livingStorySelector.getSelectedLivingStoryId()), false, null);
      }
    });
    return newContentItemButton;
  }
  
  /**
   * Create a list box for displaying all the content items for the selected living story so that
   * the user can select one to edit.
   */
  private Widget createContentItemListBox() {
    contentItemListBox = new ContentItemListBox(false);
    contentItemListBox.setVisibleItemCount(15);
    contentItemListBox.addFilterChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        contentPanel.showWidget(0);
      }
    });
    return contentItemListBox;
  }

  private Widget createContentPanel() {
    contentPanel = new DeckPanel();
    
    Label chooseContentItemLabel = new Label("Choose something to edit, or create new content.");
    chooseContentItemLabel.setStylePrimaryName("title");
    DOM.setStyleAttribute(chooseContentItemLabel.getElement(), "marginTop", "5em");
    contentPanel.add(chooseContentItemLabel);
    
    VerticalPanel editorContentPanel = new VerticalPanel();
    editorContentPanel.add(createSaveDeletePanel(topSaveControls));
    editorContentPanel.add(createEditorPanel());
    editorContentPanel.add(createSaveDeletePanel(bottomSaveControls));
    contentPanel.add(editorContentPanel);
    
    Label previewTitle = new Label("Preview");
    previewTitle.setStylePrimaryName("header");
    previewPanel = new SimplePanel();
    previewPanel.setStylePrimaryName("previewPanel");

    editorContentPanel.add(previewTitle);
    editorContentPanel.add(previewPanel);    

    contentPanel.showWidget(0);
    return contentPanel;
  }

  /**
   * Create a panel for showing the text area to enter the HTML for a content piece; selectors
   * to enter its priority and type; and the timestamp.
   */
  private Widget createEditorPanel() {
    contentItemIdLabel = new Label();
    HorizontalPanel contentItemIdPanel = new HorizontalPanel();
    contentItemIdPanel.setSpacing(2);
    contentItemIdPanel.add(new Label("Id:"));
    contentItemIdPanel.add(contentItemIdLabel);
    
    contentEditor = new RichTextEditor();
    
    timestamp = new Label();
    HorizontalPanel timestampPanel = new HorizontalPanel();
    timestampPanel.setSpacing(2);
    timestampPanel.add(new Label("Publish time:"));
    timestampPanel.add(timestamp);
    
    publishStateLabel = new Label();
    
    contentTitle = new Label("Content");
    contentTitle.setStylePrimaryName("header");
    
    VerticalPanel editorPanel = new VerticalPanel();
    editorPanel.add(contentItemIdPanel);
    editorPanel.add(createContentItemTypeSelectorPanel());
    editorPanel.add(createSpecialAttributesPanel());
    editorPanel.add(contentTitle);
    editorPanel.add(contentEditor);
    editorPanel.add(createImportanceSelectorPanel());
    editorPanel.add(createContributorSelector());
    editorPanel.add(createAdditionalPropertiesPanel());
    editorPanel.add(createLinkedContentItemsPicker());
    editorPanel.add(publishStateLabel);
    editorPanel.add(timestampPanel);

    return editorPanel;
  }
  
  /**
   * Create a panel for the content priority/importance selector.
   */
  private Widget createImportanceSelectorPanel() {
    Label enterImportanceLabel = new Label("Select priority:");
    
    importanceSelector = EnumDropdown.newInstance(Importance.class);
    importanceSelector.selectConstant(Importance.MEDIUM);
    
    HorizontalPanel importanceSelectorPanel = new HorizontalPanel();
    importanceSelectorPanel.add(enterImportanceLabel);
    importanceSelectorPanel.add(importanceSelector);
    return importanceSelectorPanel;
  }
  
  /**
   * Create a selector for the content type.
   */
  private Widget createContentItemTypeSelectorPanel() {
    Label enterTypeLabel = new Label("Select content type:");
    
    contentItemTypeSelector = EnumDropdown.newInstance(ContentItemType.class);
    
    HorizontalPanel typeSelectorPanel = new HorizontalPanel();
    DOM.setStyleAttribute(typeSelectorPanel.getElement(), "paddingBottom", "10px");
    typeSelectorPanel.add(enterTypeLabel);
    typeSelectorPanel.add(contentItemTypeSelector);
    return typeSelectorPanel;
  }
  
  private Widget createAdditionalPropertiesPanel() {
    VerticalPanel additionalPanel = new VerticalPanel();
    additionalPanel.setWidth("100%");
    
    Label title = new Label("Additional Properties");
    title.setStylePrimaryName("header");
    
    additionalPanel.add(title);
    additionalPanel.add(createThemeListBox());
    if (mapsKeyExists) {
      additionalPanel.add(createLocationPanel());
    }
    additionalPanel.add(createSourceInformationPanel());
    return additionalPanel;
  }
  
  /**
   * Create a multiselect list box for displaying all the themes that a content item is a part of.
   */
  private Widget createThemeListBox() {
    themeListBox = new ItemList<Theme>(true, false) {
      @Override
      public void loadItems() {
        try {
          Long livingStoryId = livingStorySelector.getSelectedLivingStoryId();
          if (livingStoryId != null) {
            livingStoryService.getThemesForLivingStory(
                livingStoryId, getCallback(new ThemeListAdaptor()));
          }
        } catch (UnsupportedOperationException ignored) {
        }
      }
    };
    themeListBox.setVisibleItemCount(5);

    DisclosurePanel themesPanel = new DisclosurePanel("Themes");
    themesPanel.add(themeListBox);
    return themesPanel;
  }

  private class ThemeListAdaptor extends ItemList.ListItemAdapter<Theme> {
    @Override
    public String getItemText(Theme theme) {
      return theme.getName();
    }
    
    @Override
    public String getItemValue(Theme theme) {
      return Long.toString(theme.getId());
    }
  }
  
  private Widget createLocationPanel() {
    final VerticalPanel locationPanel = new VerticalPanel();
    
    // show a map based on geocoded or manually-inputted lat-long combination
    
    HorizontalPanel descriptionPanel = new HorizontalPanel();
    descriptionPanel.add(new HTML("Location name (displayed to readers):"));
    locationDescriptionTextArea = new TextArea();
    locationDescriptionTextArea.setCharacterWidth(50);
    locationDescriptionTextArea.setHeight("60px");
    descriptionPanel.add(locationDescriptionTextArea);

    Label geocodingOptions = new Label("Geocode based on:");
    useDisplayedLocation = new RadioButton("geoGroup", "The displayed location name");
    useDisplayedLocation.setValue(true);
    useAlternateLocation = new RadioButton("geoGroup",
        "An alternate location that geocodes better: ");
    alternateTextBox = new TextBox();
    alternateTextBox.setEnabled(false);
    HorizontalPanel alternatePanel = new HorizontalPanel();
    alternatePanel.add(useAlternateLocation);
    alternatePanel.add(alternateTextBox);
    useManualLatLong = new RadioButton("geoGroup",
        "Manually entered latitude and longitude numbers (enter these below)");
    
    HorizontalPanel latLongPanel = new HorizontalPanel();
    latLongPanel.add(new HTML("Latitude:&nbsp;"));
    latitudeTextBox = new TextBox();
    latitudeTextBox.setEnabled(false);
    latLongPanel.add(latitudeTextBox);
    latLongPanel.add(new HTML("&nbsp;Longitude:&nbsp;"));
    longitudeTextBox = new TextBox();
    longitudeTextBox.setEnabled(false);
    latLongPanel.add(longitudeTextBox);

    HorizontalPanel buttonPanel = new HorizontalPanel();
    geocodeButton = new Button("Geocode location");
    geocodeButton.setEnabled(false);
    buttonPanel.add(geocodeButton);
    geocoderStatus = new Label("");
    buttonPanel.add(geocoderStatus);    
    
    AjaxLoaderOptions options = AjaxLoaderOptions.newInstance();
    options.setOtherParms(mapsKey + "&sensor=false");
    AjaxLoader.loadApi("maps", "2", new Runnable() {
      @Override
      public void run() {
        map = new MapWidget();
        map.setSize(MAP_WIDTH + "px", MAP_HEIGHT + "px");
        map.addControl(new SmallMapControl());
        map.setDoubleClickZoom(true);
        map.setDraggable(true);
        map.setScrollWheelZoomEnabled(true);
        map.setZoomLevel(MAP_ZOOM);
        map.setVisible(false);
        locationPanel.add(map);
        createLocationHandlers();
      }
    }, options);

    locationPanel.add(descriptionPanel);
    locationPanel.add(geocodingOptions);
    locationPanel.add(useDisplayedLocation);
    locationPanel.add(alternatePanel);
    locationPanel.add(useManualLatLong);
    locationPanel.add(latLongPanel);
    locationPanel.add(buttonPanel);
    locationPanel.add(
        new Label("Tip: once the map is visible, right-click a point on the map to indicate that"
            + " this is the precise location you want"));

    DisclosurePanel locationZippy = new DisclosurePanel("Location");
    locationZippy.add(locationPanel);
    return locationZippy;
  }
  
  private Widget createSourceInformationPanel() {
    VerticalPanel panel = new VerticalPanel();
    panel.add(new Label("You can enter either one or both of these fields."));
    
    sourceContentItemSelector = new SingleContentItemSelectionPanel();
    sourceDescriptionBox = new TextBox();
    sourceDescriptionBox.setVisibleLength(LONG_TEXTBOX_VISIBLE_LENGTH);
    
    Grid grid = new Grid(2, 2);
    grid.setWidget(0, 0, new Label("Content item that contains source information:"));
    grid.setWidget(0, 1, sourceContentItemSelector);
    grid.setWidget(1, 0, new Label("Description of source material:"));
    grid.setWidget(1, 1, sourceDescriptionBox);
    panel.add(grid);
    
    DisclosurePanel sourcePanel = new DisclosurePanel("Source Information");
    sourcePanel.add(panel);
    return sourcePanel;
  }
  
  private Widget createContributorSelector() {
    currentContributorIdsToNamesMap = new HashMap<Long, String>();
    
    Label title = new Label("Contributors");
    title.setStylePrimaryName("header");
    
    contributorListHtml = new HTML();
    
    clearContributorsControl = new Label("Clear current contributors");
    clearContributorsControl.setStylePrimaryName("primaryLink");
    clearContributorsControl.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        currentContributorIdsToNamesMap.clear();
        formatCurrentContributorList();
      }
    });
    
    Label instructions = new Label("Type a contributor name into the box below to add an existing"
            + " contributor to the story, or to create and add a new contributor on-the-fly:");
        
    contributorSuggestPanel = new PlayerSuggestAndAddPanel(contentRpcService, true, 
        new AsyncCallback<BaseContentItem>() {
      @Override
      public void onFailure(Throwable caught) {}
          
      @Override
      public void onSuccess(BaseContentItem result) {
        PlayerContentItem contributor = (PlayerContentItem)result;
        addUnassignedPlayer(contributor);
        currentContributorIdsToNamesMap.put(result.getId(), contributor.getName());
        formatCurrentContributorList();
      }
    });
    
    if (unassignedPlayersIdToContentItemMap == null) {
      // Asynchronously query for the contributor names:
      contentRpcService.getUnassignedPlayers(new AsyncCallback<List<PlayerContentItem>>() {
        @Override
        public void onFailure(Throwable caught) {
          contributorListHtml.setText("Could not retrieve list of available contributors");
        }

        @Override
        public void onSuccess(List<PlayerContentItem> results) {
          unassignedPlayersIdToContentItemMap = new HashMap<Long, PlayerContentItem>();
          populateUnassignedPlayersMap(results);
          populatePlayerSuggestPanel(contributorSuggestPanel);
        }
      });
    } else {
      populatePlayerSuggestPanel(contributorSuggestPanel);
    }
    
    FlowPanel contributorsPanel = new FlowPanel();
    contributorsPanel.add(title);
    contributorsPanel.add(contributorListHtml);
    contributorsPanel.add(clearContributorsControl);
    contributorsPanel.add(instructions);
    contributorsPanel.add(contributorSuggestPanel);
    return contributorsPanel;
  }

  private void formatCurrentContributorList() {
    boolean hasContributors = !currentContributorIdsToNamesMap.isEmpty();
    if (!hasContributors) {
      contributorListHtml.setHTML("<em>No contributors yet added</em>");
    } else {
      StringBuilder contributorBuilder = new StringBuilder("<ul>");
      for (String contributorName : currentContributorIdsToNamesMap.values()) {
        contributorBuilder.append("<li>").append(contributorName).append("</li>");
      }
      contributorBuilder.append("</ul>");
      contributorListHtml.setHTML(contributorBuilder.toString());
    }
    clearContributorsControl.setVisible(hasContributors);
  }

  private Widget createLinkedContentItemsPicker() {
    pickerPanel = new DockPanel();
    pickerPanel.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);

    Label title = new Label("Linked Entities");
    title.setStylePrimaryName("header");
    pickerPanel.add(title, DockPanel.NORTH);

    linkedContentItemSelector = new SuggestionAwareContentItemListBox(true);
    linkedContentItemSelector.setVisibleItemCount(10);
        
    advisoryLabel = new Label("The system has identified one or more players"
        + " that we suggest adding to the list of linked entities. These suggestions are now shown"
        + " in the area above. You may change the filter settings to revisit other linkable"
        + " entities, and may later return to these suggestions, so long as you continue to"
        + " edit only this content entity without switching to another.");
    advisoryLabel.setStylePrimaryName("serverResponseLabelSuccess");
    advisoryLabel.setWidth("475px");
    hideSuggestions();

    pickerPanel.add(advisoryLabel, DockPanel.SOUTH);
    
    FlowPanel linkedPanel = new FlowPanel();
    linkedPanel.add(linkedContentItemSelector);
    
    pickerPanel.add(linkedPanel, DockPanel.WEST);
    
    Button addButton = new Button("&raquo;");
    addButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        List<String> selectedItems = linkedContentItemSelector.getSelectedItems();
        List<String> selectedValues = linkedContentItemSelector.getSelectedValues();
        for (int i = 0; i < selectedItems.size(); i++) {
          selectedLinkedContentItems.addItem(selectedItems.get(i), selectedValues.get(i));
        }
      }
    });
    
    Button removeButton = new Button("&laquo;");
    removeButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        for (int i = selectedLinkedContentItems.getItemCount() - 1; i >= 0; i--) {
          if (selectedLinkedContentItems.isItemSelected(i)) {
            selectedLinkedContentItems.removeItem(i);
          }
        }
      }
    });
    
    VerticalPanel buttonPanel = new VerticalPanel();
    buttonPanel.add(addButton);
    buttonPanel.add(removeButton);
    pickerPanel.add(buttonPanel, DockPanel.CENTER);
    
    selectedLinkedContentItems = new ListBox(true);
    selectedLinkedContentItems.setVisibleItemCount(10);
    
    VerticalPanel selectedPanel = new VerticalPanel();
    selectedPanel.add(new Label("Selected for linking:"));
    selectedPanel.add(selectedLinkedContentItems);
    
    pickerPanel.add(selectedPanel, DockPanel.EAST);

    return pickerPanel;
  }
  
  private Widget createSpecialAttributesPanel() {
    specialAttributesPanel = new DeckPanel();
    
    specialAttributesPanel.add(createEventAttributesPanel());
    contentItemTypeToEditorPanelMap.put(ContentItemType.EVENT, 0);
    
    specialAttributesPanel.add(createAssetAttributesPanel());
    contentItemTypeToEditorPanelMap.put(ContentItemType.ASSET, 1);
    
    playerAttributesPanel = new DeckPanel();
    playerAttributesPanel.add(createStoryPlayerAttributesPanel());
    playerAttributesPanel.add(createPlayerAttributesPanel());
    playerAttributesPanel.showWidget(0);
    specialAttributesPanel.add(playerAttributesPanel);
    contentItemTypeToEditorPanelMap.put(ContentItemType.PLAYER, 2);

    specialAttributesPanel.add(createNarrativeAttributesPanel());
    contentItemTypeToEditorPanelMap.put(ContentItemType.NARRATIVE, 3);
    
    specialAttributesPanel.add(createBackgroundAttributesPanel());
    contentItemTypeToEditorPanelMap.put(ContentItemType.BACKGROUND, 4);
    
    specialAttributesPanel.showWidget(0);
    return specialAttributesPanel;
  }
  
  private Widget createEventAttributesPanel() {
    dateTrigger = new TextBox();
    dateTrigger.setVisibleLength(LONG_TEXTBOX_VISIBLE_LENGTH);
    dateTrigger.setReadOnly(true); 
    dateTrigger.setStylePrimaryName("dateTriggerBox");
    
    createDatePickerPanel();
    
    updateEditor = new TextBox();
    updateEditor.setVisibleLength(LONG_TEXTBOX_VISIBLE_LENGTH);
    summaryEditor = new RichTextEditor();

    Label updateTitle = new Label("Update");
    updateTitle.setStylePrimaryName("header");
    
    Label summaryTitle = new Label("Summary");
    summaryTitle.setStylePrimaryName("header");
    
    VerticalPanel eventPanel = new VerticalPanel();
    eventPanel.add(new Label("Event Date:"));
    eventPanel.add(dateTrigger);
    eventPanel.add(updateTitle);
    eventPanel.add(updateEditor);
    eventPanel.add(summaryTitle);
    eventPanel.add(summaryEditor);

    dateTrigger.addFocusHandler(new FocusHandler() {
      @Override
      public void onFocus(FocusEvent event) {
        datePopup.showRelativeTo(dateTrigger);
      }
    });
    dateTrigger.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        datePopup.showRelativeTo(dateTrigger);
      }
    });
    
    return eventPanel;
  }
  
  /** creates a panel of datePickers in a popup, including setting up appropriate event handling.*/
  private Widget createDatePickerPanel() {
    datePopup = new PopupPanel(false /* doesn't close if you click away */, true /* modal */);
    // TODO: Make it possible for the user to cancel, either by clicking away from
    // the popup panel or by hitting an explicit cancel button.
    startDatePicker = new DatePicker();
    startTime = new TextBox();
    hasSeparateEndDate = new CheckBox("Event has a separate end date & time");
    endDatePicker = new DatePicker();
    endTime = new TextBox();
    endTime.setEnabled(false);
    
    Grid table = new Grid(5, 2);
    table.setWidget(1, 0, new Label("Start date:"));
    table.setWidget(2, 0, startDatePicker);
    table.setWidget(3, 0, new Label("Start time:"));
    table.setWidget(4, 0, startTime);
    table.setWidget(0, 1, hasSeparateEndDate);
    table.setWidget(1, 1, new Label("End date:"));
    table.setWidget(2, 1, endDatePicker);
    table.setWidget(3, 1, new Label("End time:"));
    table.setWidget(4, 1, endTime);

    Button okButton = new Button("OK");
    final InlineLabel problemLabel = new InlineLabel("");
    problemLabel.setStylePrimaryName("serverResponseLabelError");

    FlowPanel panel = new FlowPanel();
    panel.add(table);
    panel.add(new Label("Event time may be blank, or should be entered as, e.g., 3:00 PM."));
    panel.add(okButton);
    panel.add(problemLabel);
    
    hasSeparateEndDate.addValueChangeHandler(new ValueChangeHandler<Boolean> () {
      @Override
      public void onValueChange(ValueChangeEvent<Boolean> event) {
        boolean isChecked = event.getValue().booleanValue();
        // endDatePicker.setEnabled(isChecked);  -- if only the API supported it. :-(
        endTime.setEnabled(isChecked);
      }
    });

    okButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        Date endDateTime = getEndDateTime();
        if (endDateTime != null && endDateTime.before(getStartDateTime())) {
          problemLabel.setText("End date/time cannot be before start date/time");
        } else {
          problemLabel.setText("");
          datePopup.hide();
          setDateTriggerText();
        }
      }
    });
    
    datePopup.setWidget(panel);
    return datePopup;
  }

  private void setDateTriggerText() {
    String dateString = DateUtil.formatDateTime(getStartDateTime());
    
    Date endDateTime = getEndDateTime();
    if (endDateTime != null) {
      dateString = msgs.dateRange(dateString, DateUtil.formatDateTime(endDateTime));
    }
    dateTrigger.setText(dateString);
  }

  private Date getStartDateTime() {
    return getDateTimeImpl(startDatePicker, startTime);
  }
  
  private Date getEndDateTime() {
    return hasSeparateEndDate.getValue() ? getDateTimeImpl(endDatePicker, endTime) : null;
  }
  
  private Date getDateTimeImpl(DatePicker picker, TextBox textBox) {
    Date ret = picker.getValue();
    String text = textBox.getText();
    if (text.isEmpty()) {
      text = DEFAULT_TIME_STRING;
    }
    DateUtil.parseTime(text, ret);
    return ret;
  }

  private Widget createPlayerAttributesPanel() {
    nameTextBox = new TextBox();
    aliasesTextBox = new TextBox();
    playerTypeSelector = EnumDropdown.newInstance(PlayerType.class);
    photoSelector = new SingleContentItemSelectionPanel();
    
    Grid generalPlayerAttributesPanel = new Grid(4, 2);
    generalPlayerAttributesPanel.setWidget(0, 0, new Label("Player name:"));
    generalPlayerAttributesPanel.setWidget(0, 1, nameTextBox);
    generalPlayerAttributesPanel.setWidget(1, 0, new Label("Aliases:"));
    generalPlayerAttributesPanel.setWidget(1, 1, aliasesTextBox);
    generalPlayerAttributesPanel.setWidget(2, 0, new Label("Player type:"));
    generalPlayerAttributesPanel.setWidget(2, 1, playerTypeSelector);
    generalPlayerAttributesPanel.setWidget(3, 0, new Label("Photo:"));
    generalPlayerAttributesPanel.setWidget(3, 1, photoSelector);
    return generalPlayerAttributesPanel;
  }
  
  private Widget createStoryPlayerAttributesPanel() {
    Label title = new Label("Parent player entity");
    title.setStylePrimaryName("header");
    
    parentPlayerDisplayPanel = new FlowPanel();
    parentPlayerDisplayPanel.setWidth("450px");
    changeParentLink = new Label("Change parent");
    changeParentLink.setStylePrimaryName("primaryLink");
    
    HorizontalPanel parentPlayerDisplayAndChangeLinkPanel = new HorizontalPanel();
    parentPlayerDisplayAndChangeLinkPanel.add(parentPlayerDisplayPanel);
    parentPlayerDisplayAndChangeLinkPanel.add(changeParentLink);
    
    parentSelectionInstructions = new Label("Type a player name into the box below. Select from the"
        + " list to add an existing player to the story. To add a new player to the story that"
        + " doesn't exist yet, type their name into the box and click on the button.");
    parentSelectionInstructions.setWidth("450px");
    
    generalPlayerSuggestPanel = new PlayerSuggestAndAddPanel(contentRpcService, false, 
        new AsyncCallback<BaseContentItem>() {
      @Override
      public void onFailure(Throwable caught) {
        parentPlayer = null;
        formatParentPlayerDisplay();
      }
              
      @Override
      public void onSuccess(BaseContentItem result) {
        parentPlayer = (PlayerContentItem)result;
        addUnassignedPlayer(parentPlayer);
        formatParentPlayerDisplay();
      }
    });
    
    changeParentLink.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        parentPlayer = null;
        formatParentPlayerDisplay();
      }
    });
    
    if (unassignedPlayersIdToContentItemMap == null) {
      // Asynchronously query for the contributor names:
      contentRpcService.getUnassignedPlayers(new AsyncCallback<List<PlayerContentItem>>() {
        @Override
        public void onFailure(Throwable caught) {}

        @Override
        public void onSuccess(List<PlayerContentItem> results) {
          unassignedPlayersIdToContentItemMap = new HashMap<Long, PlayerContentItem>();
          populateUnassignedPlayersMap(results);
          populatePlayerSuggestPanel(generalPlayerSuggestPanel);
        }
      });
    } else {
      populatePlayerSuggestPanel(generalPlayerSuggestPanel);
    }
    
    Label contentInstructions = new Label("Please enter the role of the selected player in this " +
        "particular story in the 'Content' box below.");
    DOM.setStyleAttribute(contentInstructions.getElement(), "marginTop", "3em");
    
    formatParentPlayerDisplay();
    
    FlowPanel storyPlayerAttributesPanel = new FlowPanel();
    storyPlayerAttributesPanel.add(title);
    storyPlayerAttributesPanel.add(parentPlayerDisplayAndChangeLinkPanel);
    storyPlayerAttributesPanel.add(parentSelectionInstructions);
    storyPlayerAttributesPanel.add(generalPlayerSuggestPanel);
    storyPlayerAttributesPanel.add(contentInstructions);
    return storyPlayerAttributesPanel;
  }
  
  private void formatParentPlayerDisplay() {
    boolean isParentNull = parentPlayer == null;
    parentPlayerDisplayPanel.clear();
    if (!isParentNull) {
      parentPlayerDisplayPanel.add(new BasePlayerPreview(parentPlayer));
    }
    parentPlayerDisplayPanel.setVisible(!isParentNull);
    changeParentLink.setVisible(!isParentNull);
    parentSelectionInstructions.setVisible(isParentNull);
    generalPlayerSuggestPanel.setVisible(isParentNull);
  }
  
  private Widget createAssetAttributesPanel() {
    assetTypeSelector = EnumDropdown.newInstance(AssetType.class);
    previewUrlLabel = new Label("Asset preview url:");
    previewUrlTextBox = new TextBox();
    imageUrlLabel = new Label("Image url (images only):");
    imageUrlTextBox = new TextBox();
    captionLabel = new Label("Asset caption:");
    captionTextArea = new TextArea();

    assetTypeSelector.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        setAssetControlVisibility();
      }
    });
    
    assetTypeSelector.selectConstant(AssetType.IMAGE);
    
    Grid assetPanel = new Grid(4, 2);
    assetPanel.setWidget(0, 0, new Label("Asset type:"));
    assetPanel.setWidget(0, 1, assetTypeSelector);
    assetPanel.setWidget(1, 0, previewUrlLabel);
    assetPanel.setWidget(1, 1, previewUrlTextBox);
    assetPanel.setWidget(2, 0, imageUrlLabel);
    assetPanel.setWidget(2, 1, imageUrlTextBox);
    assetPanel.setWidget(3, 0, captionLabel);
    assetPanel.setWidget(3, 1, captionTextArea);
    
    return assetPanel;
  }
  
  /**
   * Hides and shows labels, textareas, and other controls as appropriate for the asset
   * type a user is editing. Also may hide the main content rich text editor, if appropriate.
   */
  private void setAssetControlVisibility() {
    boolean visibleContent = true;
    boolean visiblePreviewUrl = true;
    boolean visibleImageUrl = false;
    boolean visibleCaption = true;

    if (contentItemTypeSelector.getSelectedConstant() == ContentItemType.ASSET) {
      switch (assetTypeSelector.getSelectedConstant()) {
        case AUDIO:
        case DOCUMENT:
          visiblePreviewUrl = false;
          break;
        case IMAGE:
          visibleImageUrl = true;
          visibleContent = false;
          break;
        case LINK:
          visiblePreviewUrl = false;
          visibleCaption = false;
          break;
        default:
          // nothing
          break;
      }
    }
      
    contentTitle.setVisible(visibleContent);
    contentEditor.setVisible(visibleContent);
    previewUrlLabel.setVisible(visiblePreviewUrl);
    previewUrlTextBox.setVisible(visiblePreviewUrl);
    imageUrlLabel.setVisible(visibleImageUrl);
    imageUrlTextBox.setVisible(visibleImageUrl);
    captionLabel.setVisible(visibleCaption);
    captionTextArea.setVisible(visibleCaption);
  }

  private Widget createNarrativeAttributesPanel() {
    headlineTextBox = new TextBox();
    headlineTextBox.setVisibleLength(LONG_TEXTBOX_VISIBLE_LENGTH);
    narrativeTypeSelector = EnumDropdown.newInstance(NarrativeType.class);
    narrativeDateBox = new DateBox();
    narrativeSummaryTextArea = new RichTextEditor();
    narrativeSummaryTextArea.setSize("400px", "100px");
    
    Grid narrativePanel = new Grid(4, 2);
    narrativePanel.setWidget(0, 0, new Label("Headline:"));
    narrativePanel.setWidget(0, 1, headlineTextBox);
    narrativePanel.setWidget(1, 0, new Label("Narrative type:"));
    narrativePanel.setWidget(1, 1, narrativeTypeSelector);
    narrativePanel.setWidget(2, 0, new Label("Date (optional):"));
    narrativePanel.setWidget(2, 1, narrativeDateBox);
    narrativePanel.setWidget(3, 0, new Label("Summary (optional):"));
    narrativePanel.setWidget(3, 1, narrativeSummaryTextArea);
    return narrativePanel;
  }
  
  private Widget createBackgroundAttributesPanel() {
    conceptNameTextBox = new TextBox();
    
    Grid backgroundPanel = new Grid(1, 2);
    backgroundPanel.setWidget(0, 0, new Label("Concept name:"));
    backgroundPanel.setWidget(0, 1, conceptNameTextBox);
    return backgroundPanel;
  }
  
  /**
   * Create buttons to save and delete content pieces. And a label for error messages if the
   * updates don't work.
   */
  private Widget createSaveDeletePanel(SaveControlsWidgetGroup widgets) {
    widgets.saveDraftButton = new Button("Save as draft");
    widgets.publishButton = new Button("Publish");
    widgets.republishButton = new Button("Republish, without updating time");
    widgets.republishButton.setEnabled(false);
    widgets.deleteButton = new Button("Delete");
    widgets.deleteButton.setEnabled(false);
    
    widgets.statusLabel = new Label();
    
    HorizontalPanel buttonPanel = new HorizontalPanel();
    buttonPanel.add(widgets.saveDraftButton);
    buttonPanel.add(widgets.publishButton);
    buttonPanel.add(widgets.republishButton);
    buttonPanel.add(widgets.deleteButton);
    buttonPanel.add(widgets.statusLabel);
    return buttonPanel;
  }
  
  /**
   * Create a handler to handle selection in the living story list box.
   */
  private void createLivingStorySelectionHandler() {
    livingStorySelectionHandler = new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        contentPanel.showWidget(0);
        if (livingStorySelector.hasSelection()) {
          contentItemListBox.loadItemsForLivingStory(
              livingStorySelector.getSelectedLivingStoryId());
          linkedContentItemSelector.loadItemsForLivingStory(
              livingStorySelector.getSelectedLivingStoryId());
          themeListBox.refresh();
          LivingStoryData.setLivingStoryId(livingStorySelector.getSelectedLivingStoryId());
        }
      }
    };
    livingStorySelector.addChangeHandler(livingStorySelectionHandler);
  }
  
  /**
   * Create a handler to handle selection in the 'Type' list box. If the "Event" option is
   * selected, this requires help from remote services to populate the document list box.
   */
  private void createContentItemTypeSelectionHandler() {
    ChangeHandler typeSelectionHandler = new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        ContentItemType type = contentItemTypeSelector.getSelectedConstant();
        showSpecialAttributesPanel(type);
        setAssetControlVisibility();
      }
    };
    contentItemTypeSelector.addChangeHandler(typeSelectionHandler);
  }
  
  /**
   * Create a handler to handle selection of a content piece from the content list box.
   */
  private void createContentItemSelectionHandler() {
    ChangeHandler contentSelectionHandler = new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        contentPanel.showWidget(1);
        topSaveControls.statusLabel.setText("");
        bottomSaveControls.statusLabel.setText("");
        
        BaseContentItem selectedContentItem = contentItemListBox.getSelectedContentItem();
        contentItemIdLabel.setText(String.valueOf(selectedContentItem.getId()));
        contentEditor.setContent(selectedContentItem.getContent());
        timestamp.setText(DateUtil.formatDateTime(selectedContentItem.getTimestamp()));
        importanceSelector.selectConstant(selectedContentItem.getImportance());
        contentItemTypeSelector.selectConstant(selectedContentItem.getContentItemType());
        setAssetControlVisibility();
        showSpecialAttributesPanel(selectedContentItem.getContentItemType());

        // First clear or set these fields to default values.
        // Otherwise, if the user changes the content item type, they may
        // see data from some other content item in the form fields.
        startDatePicker.setValue(DateUtil.getDateMidnight());
        startTime.setText("");
        endDatePicker.setValue(DateUtil.getDateMidnight());
        endTime.setText("");
        setDateTriggerText();
        updateEditor.setText("");
        summaryEditor.setContent("");
        nameTextBox.setText("");
        aliasesTextBox.setText("");
        playerTypeSelector.selectConstant(PlayerType.PERSON);
        photoSelector.setSelection(null);
        assetTypeSelector.selectConstant(AssetType.IMAGE);
        captionTextArea.setText("");
        previewUrlTextBox.setText("");
        imageUrlTextBox.setText("");
        headlineTextBox.setText("");
        narrativeTypeSelector.selectConstant(NarrativeType.FEATURE);
        narrativeDateBox.setValue(null);
        narrativeSummaryTextArea.setContent("");
        
        parentPlayer = null;
        formatParentPlayerDisplay();

        switch (selectedContentItem.getContentItemType()) {
          case EVENT:
            EventContentItem eventContentItem = (EventContentItem) selectedContentItem;
            Date startDate = eventContentItem.getEventStartDate();
            Date endDate = eventContentItem.getEventEndDate();
            if (startDate == null) {
              startDate = new Date();
            }
            startDatePicker.setValue(startDate);
            startDatePicker.setCurrentMonth(startDatePicker.getValue());
            startTime.setValue(DateUtil.formatTime(startDate));
            hasSeparateEndDate.setValue(endDate != null, true);
            endDatePicker.setValue(endDate == null ? startDatePicker.getValue() : endDate);
            endDatePicker.setCurrentMonth(endDatePicker.getValue());
            endTime.setText(endDate == null ? startTime.getText() : DateUtil.formatTime(endDate));
            setDateTriggerText();
            updateEditor.setText(eventContentItem.getEventUpdate());
            summaryEditor.setContent(eventContentItem.getEventSummary());
            break;
          case PLAYER:
            if (selectedContentItem.getLivingStoryId() == null) {
              PlayerContentItem playerContentItem = (PlayerContentItem) selectedContentItem;
              nameTextBox.setText(playerContentItem.getName());
              aliasesTextBox.setText(GlobalUtil.join(",", playerContentItem.getAliases()));
              playerTypeSelector.selectConstant(playerContentItem.getPlayerType());
              photoSelector.setSelection(playerContentItem.getPhotoContentItem());
            } else {
              parentPlayer =
                  ((StoryPlayerContentItem) selectedContentItem).getParentPlayerContentItem();
              formatParentPlayerDisplay();
            }
            break;
          case ASSET:
            AssetContentItem assetContentItem = (AssetContentItem) selectedContentItem;
            AssetType assetType = assetContentItem.getAssetType();
            assetTypeSelector.selectConstant(assetType);
            setAssetControlVisibility();
            captionTextArea.setText(assetContentItem.getCaption());
            previewUrlTextBox.setText(assetContentItem.getPreviewUrl());
            if (assetType == AssetType.IMAGE) {
              contentEditor.setContent("");
              imageUrlTextBox.setText(selectedContentItem.getContent());
            }
            break;
          case NARRATIVE:
            NarrativeContentItem narrativeContentItem = (NarrativeContentItem) selectedContentItem;
            headlineTextBox.setText(narrativeContentItem.getHeadline());
            narrativeTypeSelector.selectConstant(narrativeContentItem.getNarrativeType());
            narrativeDateBox.setValue(narrativeContentItem.getNarrativeDate());
            narrativeSummaryTextArea.setContent(narrativeContentItem.getNarrativeSummary());
            break;
          case BACKGROUND:
            BackgroundContentItem backgroundContentItem =
                (BackgroundContentItem) selectedContentItem;
            if (backgroundContentItem.isConcept()) {
              conceptNameTextBox.setText(backgroundContentItem.getConceptName());
            }
            break;
        }
        
        int themeCount = themeListBox.getItemCount();
        Set<Long> themesInContentItem = selectedContentItem.getThemeIds(); 
        for (int i = 0; i < themeCount; i++) {
          themeListBox.setItemSelected(i, themesInContentItem.contains(
              Long.parseLong(themeListBox.getValue(i))));
        }
        
        currentContributorIdsToNamesMap.clear();
        for (Long contributorId : selectedContentItem.getContributorIds()) {
          currentContributorIdsToNamesMap.put(contributorId, 
              unassignedPlayersIdToContentItemMap.get(contributorId).getName());
        }
        formatCurrentContributorList();
        contributorSuggestPanel.clear();
        
        if (mapsKeyExists) {
          Location location = selectedContentItem.getLocation();
          if (location != null) {
            Double latitude = location.getLatitude();
            latitudeTextBox.setText(latitude == null ? "" : latitude.toString());
            Double longitude = location.getLongitude();
            longitudeTextBox.setText(longitude == null ? "" : longitude.toString());
            if (latitude != null && longitude != null) {
              recenterMap();
            }   

            String description = location.getDescription();
            locationDescriptionTextArea.setText(description == null ? "" : description);
          }
          // Ensure that the state of the location controls are accurate for the content item data.
          adjustLocationControls();
          controlGeocodeButton();
        }
        // Set the source information related fields
        String sourceDescription = selectedContentItem.getSourceDescription();
        sourceDescriptionBox.setText(sourceDescription == null ? "" : sourceDescription);
        sourceContentItemSelector.setSelection(selectedContentItem.getSourceContentItem());
   
        updateSelectedLinkedContentItems(selectedContentItem);

        updateDisplayedPublishStatus(selectedContentItem);
        topSaveControls.deleteButton.setEnabled(true);
        bottomSaveControls.deleteButton.setEnabled(true);

        hideSuggestions();
        
        updatePreview();
      }
    };
    contentItemListBox.addSelectionChangeHandler(contentSelectionHandler);
  }
  
  private void updateDisplayedPublishStatus(BaseContentItem contentItem) {
    PublishState publishState = contentItem.getPublishState();
    boolean isPublished = publishState == PublishState.PUBLISHED;
    publishStateLabel.setText("Publish Status : " + publishState);
    topSaveControls.republishButton.setEnabled(isPublished);
    bottomSaveControls.republishButton.setEnabled(isPublished);
  }
  
  private void updateSelectedLinkedContentItems(BaseContentItem contentItem) {
    selectedLinkedContentItems.clear();
    // Initially set the linked content items list to just the ids.  Then issue
    // a request to get the actual content.
    Set<Long> linkedContentItemIds = contentItem.getLinkedContentItemIds();
    for (Long contentItemId : linkedContentItemIds) {
      selectedLinkedContentItems.addItem(String.valueOf(contentItemId));
    }
        
    if (!linkedContentItemIds.isEmpty()) {
      contentRpcService.getContentItems(linkedContentItemIds,
          new AsyncCallback<List<BaseContentItem>>() {
            @Override
            public void onFailure(Throwable caught) {}
            @Override
            public void onSuccess(List<BaseContentItem> result) {
              selectedLinkedContentItems.clear();
              for (BaseContentItem contentItem : result) {
                String content = contentItem.getDisplayString();
                if (content.length() > Constants.CONTENT_SNIPPET_LENGTH) {
                  content = content.substring(0, Constants.CONTENT_SNIPPET_LENGTH).concat("...");
                }
                selectedLinkedContentItems.addItem(content, String.valueOf(contentItem.getId()));
              }
            }
          });
    }
  }
    
  private void createSaveDeleteHandlers(final SaveControlsWidgetGroup widgets) {
    widgets.saveDraftButton.addClickHandler(new SaveHandler(false, false, widgets.statusLabel));
    widgets.publishButton.addClickHandler(new SaveHandler(true, false, widgets.statusLabel));
    widgets.republishButton.addClickHandler(new SaveHandler(true, true, widgets.statusLabel));
    ClickHandler deleteHandler = new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        deleteContentItem(contentItemListBox.getSelectedContentItem(), widgets.statusLabel);
      }
    };
    widgets.deleteButton.addClickHandler(deleteHandler);
  }
  
  /**
   * Handler for the 'Save as draft' and 'Publish' buttons.
   */
  private class SaveHandler implements ClickHandler {
    private boolean publish;
    private boolean republish;
    private Label statusLabel;
    BaseContentItem selectedContentItem;
    
    public SaveHandler(boolean publish, boolean republish, Label statusLabel) {
      this.publish = publish; 
      this.republish = republish;
      this.statusLabel = statusLabel;
    }
    
    @Override
    public void onClick(ClickEvent event) {
      doClickWork((Widget) event.getSource());
    }
    
    public void doClickWork(Widget showPromptRelativeTo) {
      selectedContentItem = contentItemListBox.getSelectedContentItem();
      long contentItemId = selectedContentItem.getId();
      Date creationDate = republish ? selectedContentItem.getTimestamp() : new Date();
      ContentItemType contentItemType = contentItemTypeSelector.getSelectedConstant();
      AssetType assetType =
        contentItemType == ContentItemType.ASSET ? assetTypeSelector.getSelectedConstant() : null;
      String content =
          contentEditor.isVisible() ? contentEditor.getContent() : imageUrlTextBox.getText();

      boolean isImage = contentItemType == ContentItemType.ASSET
          && assetTypeSelector.getSelectedConstant() == AssetType.IMAGE;
        
      if (content.isEmpty() && contentItemType != ContentItemType.EVENT && !isImage) {
        showInputError("Content cannot be empty.");
        return;
      }

      if (showPromptRelativeTo != null
          && (assetType == AssetType.AUDIO || assetType == AssetType.VIDEO
              || assetType == AssetType.INTERACTIVE)) {
        ObjectElementProofreader proofreader = new ObjectElementProofreader();
        String sanifiedContent = proofreader.proofread(content);
        
        if (sanifiedContent != null) {
          new SaveHandlerPrompt(sanifiedContent).showRelativeTo(showPromptRelativeTo);
          return;
        }
      }
      
      Importance importance = importanceSelector.getSelectedConstant();

      Long livingStoryId = livingStorySelector.getSelectedLivingStoryId();
      
      Set<Long> themeIds = new HashSet<Long>();
      for (String id : themeListBox.getSelectedItemValues()) {
        themeIds.add(Long.valueOf(id));
      }

      if (publish && contentItemType == ContentItemType.EVENT
          && currentContributorIdsToNamesMap.isEmpty()) {
        showInputError("Must select at least one contributor for publishing.");
        return;
      }
      
      Location location = new Location(null, null, "");
      // Initialize the location if it was entered
      if (mapsKeyExists) {
        Double latitude = null;
        Double longitude = null;
        String latitudeString = latitudeTextBox.getText();
        if (!latitudeString.isEmpty()) {
          String longitudeString = longitudeTextBox.getText();
          if (longitudeString.isEmpty()) {
            showInputError("Both latitude and longitude have to be entered.");
            return;
          }
          try {
            latitude = Double.valueOf(latitudeString);
            longitude = Double.valueOf(longitudeString);
            if (latitude > 90.0 || latitude < -90.0) {
              showInputError("Latitude should be between -90 and +90");
              return;
            }
            if (longitude > 180 || longitude < -180) {
              showInputError("Longitude should be between -180 and +180");
              return;
            }
          } catch (NumberFormatException e) {
            showInputError("Latitude and Longitude should be decimal numbers.");
            return;
          }
        }
        location = new Location(latitude, longitude, locationDescriptionTextArea.getText());
      }
      
      Set<Long> currentContributorIds = new HashSet<Long>(currentContributorIdsToNamesMap.keySet());

      BaseContentItem contentItem;
      switch (contentItemType) {
        case EVENT:
          Date startDate = getStartDateTime();
          Date endDate = getEndDateTime();
          if (startDate.equals(endDate)) {
            // actually, a null end-date is what we want
            endDate = null;
          }
          
          String update = updateEditor.getText().trim();
          if (update.isEmpty()) {
            showInputError("Event update cannot be empty.");
            return;
          }
          contentItem = new EventContentItem(contentItemId, creationDate, currentContributorIds,
              importance, livingStoryId, startDate, endDate, update, summaryEditor.getContent(),
              content);
          break;
        case PLAYER:
          if (livingStoryId == null) {
            String nameString = nameTextBox.getText();
            if (nameString.isEmpty()) {
              showInputError("Player name cannot be empty.");
              return;
            }
            List<String> aliasList = new ArrayList<String>();
            for (String alias : aliasesTextBox.getText().split(",")) {
              String trimmed = alias.trim();
              if (!trimmed.isEmpty()) {
                aliasList.add(trimmed);
              }
            }
            BaseContentItem photoContentItem = photoSelector.getSelection();
            if (photoContentItem != null
                && (photoContentItem.getContentItemType() != ContentItemType.ASSET
                || ((AssetContentItem) photoContentItem).getAssetType() != AssetType.IMAGE)) {
              showInputError("Player photo must be an image");
              return;
            }
            contentItem = new PlayerContentItem(contentItemId, creationDate, currentContributorIds,
                content, importance, nameString, aliasList,
                playerTypeSelector.getSelectedConstant(), (AssetContentItem) photoContentItem);
          } else {
            if (parentPlayer == null) {
              showInputError("Parent player must be chosen or created");
              return;
            }
            contentItem = new StoryPlayerContentItem(contentItemId, creationDate,
                currentContributorIds, content, importance, livingStoryId, parentPlayer);
          }
          break;
        case QUOTE:
          contentItem = new QuoteContentItem(contentItemId, creationDate, currentContributorIds,
              content, importance, livingStoryId);
          break;
        case BACKGROUND:
          contentItem = new BackgroundContentItem(contentItemId, creationDate,
              currentContributorIds, content, importance, livingStoryId,
              conceptNameTextBox.getText());
          break;
        case DATA:
          contentItem = new DataContentItem(contentItemId, creationDate, currentContributorIds,
              content, importance, livingStoryId);
          break;
        case ASSET:
          contentItem = new AssetContentItem(contentItemId, creationDate, currentContributorIds,
              content, importance, livingStoryId, assetType, captionTextArea.getText(),
              previewUrlTextBox.getText());
          break;
        case NARRATIVE:
          // There are 2 types possible for a content item that is now being saved as a narrative.
          // Either it has just been created and so selectedContentItem is a "DefaultContentItem"
          // with a type of 'Event'. In this case, the standalone value should be set to true
          // because this is the first time the item is being saved as a narrative, and hasn't
          // been linked from anything yet. The second case is when an existing narrative
          // is being resaved. In this case, we want to preserve the old value of the 
          // 'isStandalone' field.
          contentItem = new NarrativeContentItem(contentItemId, creationDate, currentContributorIds,
              content, importance, livingStoryId, headlineTextBox.getText().trim(),
              narrativeTypeSelector.getSelectedConstant(),
              selectedContentItem.getContentItemType() == ContentItemType.NARRATIVE ? 
                  ((NarrativeContentItem)selectedContentItem).isStandalone() : true,
              narrativeDateBox.getValue(), narrativeSummaryTextArea.getContent());
          break;
        case REACTION:
          contentItem = new ReactionContentItem(contentItemId, creationDate, currentContributorIds,
              content, importance, livingStoryId);
          break;
        default:
          throw new IllegalStateException("Unknown Content Item Type");
      }
      contentItem.setPublishState(publish ? PublishState.PUBLISHED : PublishState.DRAFT);
      contentItem.setThemeIds(themeIds);
      contentItem.setLocation(location);
      contentItem.setSourceDescription(sourceDescriptionBox.getText());
      contentItem.setSourceContentItem(sourceContentItemSelector.getSelection());
      
      Set<Long> linkedContentItemIds = new HashSet<Long>();
      for (int i = 0; i < selectedLinkedContentItems.getItemCount(); i++) {
        linkedContentItemIds.add(Long.valueOf(selectedLinkedContentItems.getValue(i)));
      }
      contentItem.setLinkedContentItemIds(linkedContentItemIds);
      
      createOrChangeContentItem(contentItem, publish, statusLabel);
    }
    
    private void showInputError(String errorMsg) {
      statusLabel.setText(errorMsg);
      statusLabel.setStyleName("serverResponseLabelError");
    }
    
    private class SaveHandlerPrompt extends PopupPanel {
      public SaveHandlerPrompt(final String sanifiedContent) {
        super(false /* auto-hide*/, true /* modal */);
        
        Label explanation = new Label("The content you have entered uses <object> or <embed> tags"
            + " that may cause problems in one or more browsers. We suggest you use the following"
            + " markup instead. (Your original markup is preserved as a comment below the new"
            + " suggested code.)");
        explanation.setWidth("475px");
        TextArea area = new TextArea();
        area.setCharacterWidth(60);
        area.setVisibleLines(15);
        area.setText(sanifiedContent);
        area.setReadOnly(true);
        Button useSuggested = new Button("Use suggested content");
        useSuggested.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            contentEditor.setContent(sanifiedContent);
            hide();
            doClickWork(null);
          }
        });
        Button useOriginal = new Button("Ignore suggestion; use original content");
        useOriginal.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            hide();
            doClickWork(null);
          }
        });
        
        FlowPanel panel = new FlowPanel();
        panel.add(explanation);
        FlowPanel plainDiv = new FlowPanel();
        plainDiv.add(area);
        panel.add(plainDiv);
        panel.add(useSuggested);
        panel.add(useOriginal);
        setWidget(panel);
      }
    }
  }
  
  /**
   * Creates event handlers for the Locations UI.
   */
  private void createLocationHandlers() {
    // first, set up interactions between the widgets:
    final ClickHandler radioHandler = new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        adjustLocationControls();
        controlGeocodeButton();
      }
    };
    
    final KeyUpHandler textHandler = new KeyUpHandler() {
      @Override
      public void onKeyUp(KeyUpEvent event) {
        controlGeocodeButton();
      }
    };

    useDisplayedLocation.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        radioHandler.onClick(event);
        locationDescriptionTextArea.setFocus(true);
      }
    });
    
    useAlternateLocation.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        radioHandler.onClick(event);
        alternateTextBox.setFocus(true);
      }
    });

    useManualLatLong.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        radioHandler.onClick(event);
        latitudeTextBox.setFocus(true);
      }
    });

    locationDescriptionTextArea.addKeyUpHandler(textHandler);
    
    alternateTextBox.addKeyUpHandler(textHandler);
    
    latitudeTextBox.addKeyUpHandler(textHandler);
    longitudeTextBox.addKeyUpHandler(textHandler);
    
    // Actually handle the geocode button:
    geocodeButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        if (useManualLatLong.getValue()) {
          // the latitude and longitude textboxes already have the right values in them
          recenterMap();
        } else {
          String address = (useDisplayedLocation.getValue() ? locationDescriptionTextArea
              : alternateTextBox).getText();
          geocoderStatus.setText("");
          new Geocoder().getLatLng(address, new LatLngCallback() {
            @Override
            public void onFailure() {
              geocoderStatus.setText("geocoding failed!");
            }

            @Override
            public void onSuccess(LatLng point) {
              geocoderStatus.setText("success");
              latitudeTextBox.setText(String.valueOf(point.getLatitude()));
              longitudeTextBox.setText(String.valueOf(point.getLongitude()));
              recenterMap();
            }
          });
        }
      }
    });
    
    map.addMapRightClickHandler(new MapRightClickHandler() {
      @Override
      public void onRightClick(MapRightClickEvent event) {
        LatLng clickedLatLng = map.convertContainerPixelToLatLng(event.getPoint());
        latitudeTextBox.setText(String.valueOf(clickedLatLng.getLatitude()));
        longitudeTextBox.setText(String.valueOf(clickedLatLng.getLongitude()));
        useManualLatLong.setValue(true);
        useManualLatLong.fireEvent(new ClickEvent() {});
        recenterMap();
      }
    });
  }
  
  void adjustLocationControls() {
    alternateTextBox.setEnabled(useAlternateLocation.getValue());
    boolean manualLatLong = useManualLatLong.getValue();
    latitudeTextBox.setEnabled(manualLatLong);
    longitudeTextBox.setEnabled(manualLatLong);
  }
  
  void controlGeocodeButton() {
    if (useDisplayedLocation.getValue()) {
      geocodeButton.setEnabled(!locationDescriptionTextArea.getText().isEmpty());
      geocodeButton.setText("Geocode location");
    } else if (useAlternateLocation.getValue()) {
      geocodeButton.setEnabled(!alternateTextBox.getText().isEmpty());
      geocodeButton.setText("Geocode location");
    } else {
      geocodeButton.setEnabled(!latitudeTextBox.getText().isEmpty()
          && !longitudeTextBox.getText().isEmpty());
      geocodeButton.setText("Map location");
    }
  }
  
  void recenterMap() {
    try {
      LatLng target = LatLng.newInstance(
          Double.parseDouble(latitudeTextBox.getText()),
          Double.parseDouble(longitudeTextBox.getText()));
      if (map.isVisible()) {
        map.panTo(target);
      } else {
        map.setVisible(true);
        map.setCenter(target);
        map.checkResizeAndCenter();
        // checkResizeAndCenter() call added per comments in
        // http://code.google.com/p/gwt-google-apis/issues/detail?id=223
      }
      if (mapMarker == null) {
        mapMarker = new Marker(target);
        map.addOverlay(mapMarker);
      } else {
        mapMarker.setLatLng(target);
      }
    } catch (NumberFormatException e) {
      geocoderStatus.setText("invalid latitude or longitude");
      map.setVisible(false);
    }
    // Make the copyright text smaller so it fits in the map.
    // This doesn't seem to work if it's set right when the map is created, so do it here.
    map.getElement().getFirstChildElement().getNextSiblingElement()
        .getStyle().setProperty("fontSize", "xx-small");
  }
  
  /**
   * Make an RPC call to the server to persist a new content entity or a change to an existing
   * content entity to the datastore. The timestamp is updated once the change is done if no
   * value had been entered for it before.
   */
  private void createOrChangeContentItem(
      final BaseContentItem sentContentItem, final boolean publish, final Label statusLabel) {
    
    AsyncCallback<BaseContentItem> callback = new AsyncCallback<BaseContentItem>() {
      public void onFailure(Throwable caught) {
        if (statusLabel != null) {
          statusLabel.setText("Save not successful. Try again.");
          statusLabel.setStyleName("serverResponseLabelError");
        }
      }
      
      public void onSuccess(BaseContentItem returnedContentItem) {
        if (statusLabel != null) {
          statusLabel.setText(publish ? "Published!" : "Saved as draft");
          statusLabel.setStyleName("serverResponseLabelSuccess");
        }
        timestamp.setText(DateUtil.formatDateTime(returnedContentItem.getTimestamp()));
        updateDisplayedPublishStatus(returnedContentItem);
        if (returnedContentItem.getContentItemType() == ContentItemType.PLAYER
            && returnedContentItem.getLivingStoryId() == null) {
          addUnassignedPlayer((PlayerContentItem) returnedContentItem);
        }
        
        // Set the content editor items to what was returned from the server. This is needed
        // because some changes are made to the content as entered by the user, such as adding
        // target="_blank" in links and adding player tags.
        contentEditor.setContent(returnedContentItem.getContent());
        if (returnedContentItem.getContentItemType() == ContentItemType.EVENT) {
          summaryEditor.setContent(((EventContentItem)returnedContentItem).getEventSummary());
        }
        if (returnedContentItem.getContentItemType() == ContentItemType.NARRATIVE) {
          narrativeSummaryTextArea.setContent(
              ((NarrativeContentItem) returnedContentItem).getNarrativeSummary());
        }

        // remember which linked content items were suggested, but fix up the returned content
        // item so that nothing incorrect gets cached locally.
        Set<Long> suggestionIds = GlobalUtil.copySet(returnedContentItem.getLinkedContentItemIds());
        suggestionIds.removeAll(sentContentItem.getLinkedContentItemIds());
        returnedContentItem.setLinkedContentItemIds(sentContentItem.getLinkedContentItemIds());

        contentItemListBox.addOrUpdateContentItem(returnedContentItem);
        linkedContentItemSelector.addOrUpdateContentItem(returnedContentItem);
        updatePreview();
        
        if (!suggestionIds.isEmpty()) {
          linkedContentItemSelector.setSuggestedContentItemIds(suggestionIds);
          linkedContentItemSelector.selectSuggested();
          advisoryLabel.setVisible(true);

          // scroll the picker panel into view. This should assure that both the
          // linkedContentItemSelector and advisoryLabel are fully visible (if indeed both can fit
          // onscreen at once).
          pickerPanel.getElement().scrollIntoView();
          // and, in case it doesn't fit all onscreen, prioritize display of the
          // advisory label:
          advisoryLabel.getElement().scrollIntoView();
        } else {
          hideSuggestions();
        }
      }
    };
    
    contentRpcService.createOrChangeContentItem(sentContentItem, callback);
  }
  
  private void hideSuggestions() {
    advisoryLabel.setVisible(false);
    linkedContentItemSelector.setSuggestedContentItemIds(Collections.<Long>emptySet());
  }
  
  /**
   * Make an RPC call to the server to delete an existing content entity. After it's done, remove
   * it from the content item Listbox and clear the edit area.
   */
  private void deleteContentItem(final BaseContentItem contentItem, final Label statusLabel) {
    final Long id = contentItem.getId();
    AsyncCallback<Void> callback = new AsyncCallback<Void>() {
      public void onFailure(Throwable caught) {
        statusLabel.setText("Delete not successful. Try again.");
        statusLabel.setStyleName("serverResponseLabelError");
      }
      
      public void onSuccess(Void result) {
        statusLabel.setText("Saved!");
        statusLabel.setStyleName("serverResponseLabelSuccess");
        contentPanel.showWidget(0);
        contentItemListBox.removeContentItem(id);
        if (contentItem.getLivingStoryId() == null
            && contentItem.getContentItemType() == ContentItemType.PLAYER) {
          removeUnassignedPlayer(id, ((PlayerContentItem)contentItem).getName());
        }
      }
    };
    
    contentRpcService.deleteContentItem(contentItem.getId(), callback);
  }
  
  private void showSpecialAttributesPanel(ContentItemType contentItemType) {
    Integer panelIndex = contentItemTypeToEditorPanelMap.get(contentItemType);
    if (panelIndex == null) {
      specialAttributesPanel.setVisible(false);
    } else {
      specialAttributesPanel.showWidget(panelIndex);
      specialAttributesPanel.setVisible(true);
      if (contentItemType == ContentItemType.PLAYER) {
        if (livingStorySelector.getSelectedLivingStoryId() == null) {
          // Display fields for name, aliases, player type and a photo selector
          playerAttributesPanel.showWidget(1);
        } else {
          // Display a parent player content item selector
          playerAttributesPanel.showWidget(0);
          formatParentPlayerDisplay();
        }
      }
    }
  }

  private void updatePreview() {
    BaseContentItem contentItem = contentItemListBox.getSelectedContentItem();
    if (contentItem.getDisplayString().equals("New Content Item")) {
      previewPanel.clear();
    } else {
      previewPanel.setWidget(StreamViewFactory.createView(contentItem,
          contentItemListBox.getLoadedContentItemsMap()));
    }
  }
  
  @Override
  public void onLivingStoriesChanged() {
    livingStorySelector.refresh();
    contentItemListBox.clear();
  }
  
  @Override
  public void onShow() {
    livingStorySelector.selectCoordinatedLivingStory();
    livingStorySelectionHandler.onChange(null);
    if (livingStorySelector.hasSelection()) {
      LivingStoryData.setLivingStoryId(livingStorySelector.getSelectedLivingStoryId());
    }
  }

  private void populateUnassignedPlayersMap(List<PlayerContentItem> players) {
    for (PlayerContentItem player : players) {
      unassignedPlayersIdToContentItemMap.put(player.getId(), player);
    }
  }
  
  private void populatePlayerSuggestPanel(PlayerSuggestAndAddPanel playerSuggestPanel) {
    for (PlayerContentItem player : unassignedPlayersIdToContentItemMap.values()) {
      playerSuggestPanel.addPlayer(player);
    }
  }
  
  private void addUnassignedPlayer(PlayerContentItem player) {
    if (!unassignedPlayersIdToContentItemMap.containsKey(player.getId())) {
      unassignedPlayersIdToContentItemMap.put(player.getId(), player);
      contributorSuggestPanel.addPlayer(player);
      generalPlayerSuggestPanel.addPlayer(player);
    }
  }
  
  private void removeUnassignedPlayer(Long id, String name) {
    if (unassignedPlayersIdToContentItemMap.containsKey(id)) {
      unassignedPlayersIdToContentItemMap.remove(id);
      contributorSuggestPanel.removePlayer(name);
      generalPlayerSuggestPanel.removePlayer(name);
    }
  }
  
  /**
   * Utility class for grouping and handling widgets related to saving & deleting content items
   *
   */
  private class SaveControlsWidgetGroup {
    Button saveDraftButton;
    Button publishButton;
    Button republishButton;
    Button deleteButton;
    Label statusLabel;
  }
}
