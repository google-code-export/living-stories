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

package com.google.livingstories.client.atomlist;

import com.google.gwt.ajaxloader.client.AjaxLoader;
import com.google.gwt.ajaxloader.client.AjaxLoader.AjaxLoaderOptions;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.maps.client.InfoWindow;
import com.google.gwt.maps.client.InfoWindowContent;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.control.SmallMapControl;
import com.google.gwt.maps.client.event.MarkerClickHandler;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.livingstories.client.AssetAtom;
import com.google.livingstories.client.AssetType;
import com.google.livingstories.client.AtomType;
import com.google.livingstories.client.BaseAtom;
import com.google.livingstories.client.Importance;
import com.google.livingstories.client.Location;
import com.google.livingstories.client.NarrativeAtom;
import com.google.livingstories.client.lsp.BylineWidget;
import com.google.livingstories.client.lsp.DateTimeRangeWidget;
import com.google.livingstories.client.lsp.LspMessageHolder;
import com.google.livingstories.client.lsp.views.ShareLinkWidget;
import com.google.livingstories.client.lsp.views.atoms.LinkedViewFactory;
import com.google.livingstories.client.ui.PartialDisclosurePanel;
import com.google.livingstories.client.ui.WindowScroll;
import com.google.livingstories.client.util.AnalyticsUtil;
import com.google.livingstories.client.util.GlobalUtil;
import com.google.livingstories.client.util.HistoryManager;
import com.google.livingstories.client.util.LivingStoryControls;
import com.google.livingstories.client.util.LivingStoryData;
import com.google.livingstories.client.util.HistoryManager.HistoryPages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * For complex elements in the atom list such as events and stand-alone narratives, this
 * class provides most of the implementation for rendering an individual element.
 */
public abstract class ComplexAtomListElement implements AtomListElement {
  private static final String READ_MORE = LspMessageHolder.consts.readMore();
  private static final String SHOW_LESS = LspMessageHolder.consts.showLess();
  private static final String LOCATION_TITLE = LspMessageHolder.consts.locationTitle();
  
  private static final Comparator<BaseAtom> ATOM_COMPARATOR = new Comparator<BaseAtom>() {
    @Override
    public int compare(BaseAtom lhs, BaseAtom rhs) {
      // Sort by importance
      int result = lhs.getImportance().compareTo(rhs.getImportance());
      if (result != 0) {
        return result;
      }
      // Sort reverse chronologically
      result = -lhs.getTimestamp().compareTo(rhs.getTimestamp());
      if (result != 0) {
        return result;
      }
      return lhs.getId().compareTo(rhs.getId());
    }
  };

  private static final int MAPS_WIDTH = 240;
  private static final int MAPS_HEIGHT = 240;
  private static final int MAPS_ZOOM = 10;
  private static final int RIGHT_PANEL_WIDTH = 210;

  private BaseAtom atom;
  private VerticalPanel atomBlock;
  
  protected List<Widget> toggledWidgets;
  protected boolean alreadySeen;
  
  private Map<Long, BaseAtom> allAtomsMap;
  private Map<AtomType, List<BaseAtom>> typeToLinkedAtomsMap;
  private Map<String, Widget> typeStringToNavLinkTargetMap;
  private List<BaseAtom> expandedAssets;
  private List<AssetAtom> linkedImages;
  private Set<Long> idsOfExpandedAssets;
  private Map<Long, Widget> narrativeIdToWidgetMap;

  private boolean noHistoryOnToggle;
  
  private HTML headlineWidget;
  private DateTimeRangeWidget dateTimeWidget;
  private BylineWidget bylineWidget;
  private SummarySnippetWidget summaryWidget;
  private VerticalPanel mapPanel;
  private MapWidget mapWidget;
  private PartialDisclosurePanel disclosurePanel;
  private Label disclosurePanelText;

  private boolean suppressScrollOnClose;

  public ComplexAtomListElement(BaseAtom atom, Map<Long, BaseAtom> allAtomsMap,
      boolean noHistoryOnToggle) {
    this.atom = atom;
    this.allAtomsMap = allAtomsMap;
    this.noHistoryOnToggle = noHistoryOnToggle;
    
    // Keep track of the linked assets that are of highest importance because they will be expanded
    // by default
    expandedAssets = new ArrayList<BaseAtom>();
    idsOfExpandedAssets = new HashSet<Long>();

    // Keep a list of widgets that should be shown/hidden by the partial disclosure panel.
    toggledWidgets = new ArrayList<Widget>();
    
    // Create lists for the different types of linked atoms and put them in a map keyed by their
    // type
    typeToLinkedAtomsMap = new HashMap<AtomType, List<BaseAtom>>();
    for (AtomType type : AtomType.values()) {
      typeToLinkedAtomsMap.put(type, new ArrayList<BaseAtom>());
    }
    typeStringToNavLinkTargetMap = new LinkedHashMap<String, Widget>();
    
    linkedImages = new ArrayList<AssetAtom>();
    
    Date lastVisitDate = LivingStoryData.getLastVisitDate();
    Date lastEventBlockUpdateTime = atom.getTimestamp();
    // Process the linked atoms if an all-atom map has been sent
    if (allAtomsMap != null && !allAtomsMap.isEmpty()) {
      for (Long linkedAtomId : atom.getLinkedAtomIds()) {
        BaseAtom linkedAtom = allAtomsMap.get(linkedAtomId);
        if (linkedAtom != null) {
          AtomType linkedAtomType = linkedAtom.getAtomType();

          if (linkedAtomType == AtomType.ASSET
              && ((AssetAtom) linkedAtom).getAssetType() == AssetType.IMAGE) {
            linkedImages.add((AssetAtom) linkedAtom);
          } else {
            // Put each of the atoms linked from the update into the corresponding list in the map
            typeToLinkedAtomsMap.get(linkedAtomType).add(linkedAtom);
          }

          // Find the latest time when anything within the event block was updated
          Date atomTimestamp = linkedAtom.getTimestamp();
          if (atomTimestamp.after(lastEventBlockUpdateTime)) {
            lastEventBlockUpdateTime = atomTimestamp;
          }

          // For high importance linked multimedia, we want to show them in the expanded view
          // next to the summary for high and medium importance elements.
          if (getImportance() != Importance.LOW
              && linkedAtom.getImportance() == Importance.HIGH
              && linkedAtom.getAtomType() == AtomType.ASSET) {
            expandedAssets.add(linkedAtom);
            idsOfExpandedAssets.add(linkedAtom.getId());
          }
        }
      }
    }
    
    // Sort atoms within their categories by their importance followed by their timestamp
    for (AtomType type : typeToLinkedAtomsMap.keySet()) {
      Collections.sort(typeToLinkedAtomsMap.get(type), ATOM_COMPARATOR);
    }
    
    // Sort the images by importance and timestamp as well
    Collections.sort(linkedImages, ATOM_COMPARATOR);

    this.alreadySeen = lastVisitDate != null && lastEventBlockUpdateTime.before(lastVisitDate);
  }
  
  public abstract String getHeadlineHtml();
  
  public abstract SummarySnippetWidget getSummarySnippetWidget();
  
  public abstract Widget getDetailsWidget();
  
  public abstract Date getStartDate();
  
  public abstract Date getEndDate();
  
  @Override
  public Long getId() {
    return atom.getId();
  }
  
  @Override
  public Set<Long> getThemeIds() {
    return atom.getThemeIds();
  }

  @Override
  public Importance getImportance() {
    return atom.getImportance();
  }

  
  @Override
  public Widget render(boolean includeAtomName) {
    atomBlock = new VerticalPanel();
    atomBlock.addStyleName("eventBlock");
    if (alreadySeen) {
      atomBlock.addStyleName("alreadySeenBlockText");
    } else {
      atomBlock.addStyleName("newBlockText");
    }
    atomBlock.add(createBlockHeader());
    
    summaryWidget = getSummarySnippetWidget();
    Widget narrativePreviewWidget = getNarrativePreviewWidget();
    FlowPanel leftPanel = getLeftPanel();
    FlowPanel rightPanel = getRightPanel();
    
    FlowPanel contentPanel = new FlowPanel();
    
    if (summaryWidget != null) {
      // in most cases, this margin will collapse with the content below.
      DOM.setStyleAttribute(summaryWidget.getElement(), "marginBottom", "0.9em");
      contentPanel.add(summaryWidget);
    }
    if (narrativePreviewWidget != null) {
      contentPanel.add(narrativePreviewWidget);
    }
    if (rightPanel.getWidgetCount() > 0) {
      if (expandedAssets.isEmpty()) {
        contentPanel.add(rightPanel);
      } else {
        contentPanel.insert(rightPanel, 0);
      }
    }
    if (leftPanel.getWidgetCount() > 0) {
      contentPanel.add(leftPanel);
    }

    if (toggledWidgets.isEmpty()) {
      // There's nothing to toggle, so the atom isn't expandable.
      atomBlock.add(contentPanel);
      if (alreadySeen) {
        headlineWidget.addStyleName("greyFont");
      }
    } else {
      atomBlock.add(getDisclosurePanel(contentPanel));
    }

    // The following wrapper is necessary because trying to apply padding directly to
    // a table element blows IE's mind, causing it to add lots of extra padding everywhere.
    SimplePanel atomBlockWrapper = new SimplePanel();
    atomBlockWrapper.setStylePrimaryName("eventBlockWrapper");
    atomBlockWrapper.setWidget(atomBlock);

    return atomBlockWrapper;
  }

  private Widget createBlockHeader() {
    headlineWidget = new HTML(getHeadlineHtml());
    if (getImportance() != Importance.LOW) {
      headlineWidget.addStyleName("title");
    }
    
    bylineWidget = BylineWidget.makeContextSensitive(atom, new HashSet<Long>());    
    if (bylineWidget != null) { 
      if (collapseCompletely()) { 
        bylineWidget.setVisible(false); 
      }   
    }
    
    dateTimeWidget = new DateTimeRangeWidget(getStartDate(), getEndDate());
    dateTimeWidget.addStyleName("floatRight");
    if (getImportance() == Importance.LOW) {
      dateTimeWidget.setTimeVisible(false);
    }
    
    VerticalPanel headlineAndBylinePanel = new VerticalPanel(); 
    headlineAndBylinePanel.setWidth("100%");    
    headlineAndBylinePanel.add(headlineWidget); 
    if (bylineWidget != null) { 
      headlineAndBylinePanel.add(bylineWidget);   
    }
    
    HorizontalPanel headerPanel = new HorizontalPanel();
    headerPanel.setWidth("100%");
    headerPanel.add(headlineAndBylinePanel);
    headerPanel.add(dateTimeWidget);
    headerPanel.setCellHorizontalAlignment(dateTimeWidget, HasHorizontalAlignment.ALIGN_RIGHT);
    headerPanel.setCellWidth(dateTimeWidget, "100px");
    return headerPanel;
  }
  
  /**
   * If there are narratives attached to an event, put their headlines below the summary. Clicking
   * on the headline expands the block and scrolls to the full narrative.
   */
  private Widget getNarrativePreviewWidget() {
    if (getImportance() == Importance.LOW) {
      return null;
    }
    List<BaseAtom> linkedNarratives = typeToLinkedAtomsMap.get(AtomType.NARRATIVE);
    if (linkedNarratives == null || linkedNarratives.isEmpty()) {
      return null;
    } else {
      FlowPanel panel = new FlowPanel();
      panel.setWidth("100%");
      DOM.setStyleAttribute(panel.getElement(), "margin", "10px 0");
      Label relatedLabel = new Label("Related");
      relatedLabel.setStylePrimaryName("atomHeader");
      panel.add(relatedLabel);
      
      for (final BaseAtom linkedAtom : linkedNarratives) {
        NarrativeAtom narrative = (NarrativeAtom)linkedAtom;
        
        FlowPanel row = new FlowPanel();
        InlineLabel narrativeHeadline = new InlineLabel(narrative.getHeadline());
        narrativeHeadline.addStyleName(alreadySeen ? "secondaryLink" : "primaryLink");
        InlineHTML narrativeType = new InlineHTML("&nbsp;-&nbsp;" 
            + narrative.getNarrativeType().toString());
        narrativeType.addStyleName("greyFont");
        row.add(narrativeHeadline);
        row.add(narrativeType);
        
        FocusPanel clickableRow = new FocusPanel();
        clickableRow.add(row);
        clickableRow.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent e) {
            Widget narrativeWidget = narrativeIdToWidgetMap.get(linkedAtom.getId());
            disclosurePanel.scrollToContainedWidget(narrativeWidget);
            setHistoryToken(atom.getId());
          }
        });
        panel.add(clickableRow);
      }
      return panel;
    }
  }
  
  @Override
  public String getDateString() {
    return dateTimeWidget.getDateString();
  }
  
  @Override
  public void setTimeVisible(boolean visible) {
    if (getImportance() != Importance.LOW) {
      dateTimeWidget.setTimeVisible(visible);
    }
  }
  
  private FlowPanel getLeftPanel() {
    FlowPanel leftPanel = new FlowPanel();

    // Background atoms come first
    for (BaseAtom backgroundAtom : typeToLinkedAtomsMap.get(AtomType.BACKGROUND)) {
      addAtom(leftPanel, backgroundAtom, false);
    }
    
    // Then event details if there are any
    Widget details = getDetailsWidget();
    if (details != null) {
      details.setVisible(false);
      toggledWidgets.add(details);
      leftPanel.add(details);
    }
    
    // Then add the reactions
    for (BaseAtom reactionAtom : typeToLinkedAtomsMap.get(AtomType.REACTION)) {
      addAtom(leftPanel, reactionAtom, false);
    }
    
    // Then some other atom types
    narrativeIdToWidgetMap = new HashMap<Long, Widget>();
    for (BaseAtom articleAtom : typeToLinkedAtomsMap.get(AtomType.NARRATIVE)) {
      Widget widget = addAtom(leftPanel, articleAtom, false);
      narrativeIdToWidgetMap.put(articleAtom.getId(), widget);
    }
    
    for (BaseAtom dataAtom : typeToLinkedAtomsMap.get(AtomType.DATA)) {
      addAtom(leftPanel, dataAtom, false);
    }
    
    return leftPanel;
  }
  
  private FlowPanel getRightPanel() {
    final FlowPanel rightPanel = new FlowPanel();
    rightPanel.setWidth(RIGHT_PANEL_WIDTH + "px");
    rightPanel.addStyleName("eventBlockRightColumn");
    
    boolean imagesRendered = false;
    // First render the images, if there is an important image
    // Note that the linked images are sorted by importance, so we only need to check if
    // the first image is expanded to determine if any images are important.
    if (!linkedImages.isEmpty() && idsOfExpandedAssets.contains(linkedImages.get(0).getId())) {
      renderImages(rightPanel, linkedImages, true);
      imagesRendered = true;
    }
    
    // Then render the other important assets
    for (BaseAtom importantAsset : expandedAssets) {
      if (((AssetAtom)importantAsset).getAssetType() != AssetType.IMAGE) {
        addAtom(rightPanel, importantAsset, true);
      }
    }
    
    // Then render the unimportant images, if any
    if (!imagesRendered && !linkedImages.isEmpty()) {
      renderImages(rightPanel, linkedImages, true);
    }
    
    // Then render the non-image and non-important assets
    for (BaseAtom assetAtom : typeToLinkedAtomsMap.get(AtomType.ASSET)) {
      if (!idsOfExpandedAssets.contains(assetAtom.getId())) {
        addAtom(rightPanel, assetAtom, true);
      }
    }

    if (hasLocationMap()) {
      mapPanel = new VerticalPanel();
      mapPanel.addStyleName("baseAtomPanel");
      Label locationTitleLabel = new Label(LOCATION_TITLE);
      locationTitleLabel.addStyleName("atomHeader");
      mapPanel.add(locationTitleLabel);
      AjaxLoaderOptions options = AjaxLoaderOptions.newInstance();
      options.setOtherParms(LivingStoryData.getMapsKey() + "&sensor=false");
      
      // Instantiating the map via a runnable breaks horribly on firefox, for reasons
      // that are still mysterious to us. If we introduce some delay, though,
      // it works fine, and doesn't greatly hurt overall page functionality.
      final Timer t = new Timer() {
        @Override
        public void run() {
          mapWidget = makeLocationMap();
          mapPanel.add(mapWidget);
        }
      };
      AjaxLoader.loadApi("maps", "2", new Runnable() {
        @Override
        public void run() {
          t.schedule(1000);
        }
      }, options);
      assignNavLinkString(mapPanel, "Location");
      mapPanel.setVisible(false);
      rightPanel.add(mapPanel);
      rightPanel.setWidth(Math.max(RIGHT_PANEL_WIDTH, MAPS_WIDTH) + "px");
      toggledWidgets.add(mapPanel);
    }
    
    for (BaseAtom playerAtom : typeToLinkedAtomsMap.get(AtomType.PLAYER)) {
      addAtom(rightPanel, playerAtom, true);
    }
    
    for (BaseAtom quoteAtom : typeToLinkedAtomsMap.get(AtomType.QUOTE)) {
      addAtom(rightPanel, quoteAtom, false);
    }

    return rightPanel;
  }
  
  private Widget getDisclosurePanel(Widget contentPanel) {
    String linkStyle = alreadySeen ? "secondaryLink" : "primaryLink";
    // Make the headline a link and clicking on it should open the disclosure panel
    headlineWidget.addStyleName(linkStyle);
    headlineWidget.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        boolean opening = !disclosurePanel.isOpen();
        disclosurePanel.setOpen(opening);
        if (HistoryManager.isInitialized()) {
          if (opening) {
            setHistoryToken(atom.getId());
          } else {
            setHistoryToken(null);
          }
        }
      }
    });
    
    disclosurePanel = new PartialDisclosurePanel(false);
    disclosurePanelText = new Label(READ_MORE);
    disclosurePanelText.addStyleName(linkStyle);
    
    ListElementNavLinks navLinks = new ListElementNavLinks(
        disclosurePanel, typeStringToNavLinkTargetMap, alreadySeen,
        new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            setHistoryToken(atom.getId());
          }
        });
    ShareLinkWidget shareLink = new ShareLinkWidget(atom.getId(), linkStyle);
    final DeckPanel navLinksOrShare = new DeckPanel();
    navLinksOrShare.add(navLinks);
    navLinksOrShare.add(shareLink);
    navLinksOrShare.showWidget(0);
    
    HorizontalPanel disclosurePanelHeader = new HorizontalPanel();
    disclosurePanelHeader.setWidth("100%");
    disclosurePanelHeader.add(disclosurePanelText);
    disclosurePanelHeader.add(navLinksOrShare);
    disclosurePanelHeader.setCellHorizontalAlignment(
        disclosurePanelText, HorizontalPanel.ALIGN_LEFT);
    disclosurePanelHeader.setCellHorizontalAlignment(
        navLinksOrShare, HorizontalPanel.ALIGN_RIGHT);
    
    disclosurePanel.setHeader(disclosurePanelHeader);
    disclosurePanel.setAnimationEnabled(true);
    disclosurePanel.getHeader().setVisible(!collapseCompletely());
    setDisclosurePanelHeaderStyle(false);
    disclosurePanel.setContent(contentPanel, toggledWidgets);
    disclosurePanel.getContent().setVisible(!collapseCompletely());

    disclosurePanel.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        boolean opening = !disclosurePanel.isOpen();
        if (HistoryManager.isInitialized()) {
          if (opening) {
            setHistoryToken(atom.getId());            
          } else {
            setHistoryToken(null);
          }
        }
      }
    });
    disclosurePanel.addOpenHandler(new OpenHandler<PartialDisclosurePanel>() {
      @Override
      public void onOpen(OpenEvent<PartialDisclosurePanel> e) {
        disclosurePanelText.setText(SHOW_LESS);
        setDisclosurePanelHeaderStyle(true);
        if (collapseCompletely()) {
          disclosurePanel.getHeader().setVisible(true);
          disclosurePanel.getContent().setVisible(true);
          if (bylineWidget != null) {  
            bylineWidget.setVisible(true);  
          }
        }
        if (summaryWidget != null) {
          summaryWidget.expand();
        }
        navLinksOrShare.showWidget(1);
        AnalyticsUtil.trackOpenEventAction(LivingStoryData.getLspUrl(), atom.getId());
      }
    });
    disclosurePanel.addCloseHandler(new CloseHandler<PartialDisclosurePanel>() {
      @Override
      public void onClose(CloseEvent<PartialDisclosurePanel> t) {
        disclosurePanelText.setText(READ_MORE);
        setDisclosurePanelHeaderStyle(false);
        if (collapseCompletely()) {
          disclosurePanel.getHeader().setVisible(false);
          disclosurePanel.getContent().setVisible(false);
          if (bylineWidget != null) {   
            bylineWidget.setVisible(false); 
          }
        }
        if (summaryWidget != null) {
          summaryWidget.collapse();
        }
        navLinksOrShare.showWidget(0);
        
        if (!suppressScrollOnClose) {
          int blockPosition = atomBlock.getAbsoluteTop();
          if (blockPosition < Window.getScrollTop()) {
            WindowScroll.scrollTo(blockPosition,
                new Command() {
                  @Override
                  public void execute() {
                    LivingStoryControls.repositionAnchoredPanel();
                  }
                });
          }
        }
      }
    });
    // on IE, animating the opening and closing of the event block seems to
    // mess with the visibility of the map. Toggle it back and forth to fix this, and
    // asynchronously schedule a call to checkResizeAndCenter().
    disclosurePanel.setAnimationCompletionCommand(new Command() {
      @Override
      public void execute() {
        // IE has issues with floated, absolute-positioned images vanishing when they are
        // suddenly shown. Walk the DOM to find and fix this.
        // See http://ryanfait.com/resources/disappearing-images-in-ie/ for a description of
        // the problem, though note that the solution detailed there won't work.
        Element atomBlockElt = atomBlock.getElement();
        NodeList<Element> images = atomBlockElt.getElementsByTagName("img");

        for (int i = 0, imageCount = images.getLength(); i < imageCount; i++) {
          Style imageStyle = images.getItem(i).getStyle();
          String initialDisplay = imageStyle.getProperty("display");
          
          if (!initialDisplay.equals("none")
              && imageStyle.getProperty("position").equals("absolute")) {
            // get the layout engine to rerun by making the image invisible, then back to
            // normal:
            imageStyle.setProperty("display", "none");
            imageStyle.setProperty("display", initialDisplay);
          }
        }
        
        if (mapWidget != null) {
          // The same issue with absolute positioning of images and divs within a floated
          // element is what causes maps to vanish on IE, but the code above doesn't seem to
          // fix this on its own. So we just toggle the visibility of the whole maps panel.
          mapPanel.setVisible(!mapPanel.isVisible());   // toggle once
          mapPanel.setVisible(!mapPanel.isVisible());   // toggle back
          mapWidget.checkResizeAndCenter();
          // can't do this synchronously; might as well do it here:
          mapWidget.getElement().getFirstChildElement().getNextSiblingElement()
              .getStyle().setProperty("fontSize", "xx-small");
        }
      }
    });
    
    return disclosurePanel;
  }
  
  private void setDisclosurePanelHeaderStyle(boolean expanded) {
    DOM.setStyleAttribute(disclosurePanel.getHeader().getElement(), "color", 
        expanded ? "#2200cc" : (alreadySeen ? "#7777cc" : "#2200cc"));
  }
  
  private void setHistoryToken(Long atomId) {
    if (noHistoryOnToggle) {
      return;
    }
    // Set this atom as the focused atom in the history.
    // When the user navigates away from this and then clicks back, this atom will
    // appear expanded, and the viewport will be scrolled to its position.
    HistoryManager.newToken(HistoryPages.OVERVIEW,
        LivingStoryControls.getCurrentFilterSpec().getFilterParams(), String.valueOf(atomId));
  }
  
  private void renderImages(ComplexPanel panel, final List<AssetAtom> images, boolean addNavLink) {
    List<AssetAtom> slideshowImages = new ArrayList<AssetAtom>();
    List<AssetAtom> thumbnailOnlyImages = new ArrayList<AssetAtom>();
    
    for (AssetAtom image : images) {
      image.setRenderAsSeen(alreadySeen);
      if (GlobalUtil.isContentEmpty(image.getContent())) {
        thumbnailOnlyImages.add(image);
      } else {
        slideshowImages.add(image);
      }
    }

    if (!slideshowImages.isEmpty()) {
      AssetAtom previewImage = slideshowImages.get(0);
      previewImage.setRelatedAssets(slideshowImages);
      
      Widget previewPanel = LinkedViewFactory.createView(previewImage, new HashSet<Long>());
      panel.add(previewPanel);

      if (addNavLink && !idsOfExpandedAssets.contains(previewImage.getId())) {
        previewPanel.setVisible(false);
        toggledWidgets.add(previewPanel);
        // Add a nav link for multimedia only if the image panel is hidden
        assignNavLinkString(previewPanel, previewImage.getNavLinkString());
      }
    }

    if (!thumbnailOnlyImages.isEmpty()) {
      for (AssetAtom previewImage : thumbnailOnlyImages) {
        Widget preview = LinkedViewFactory.createView(previewImage, new HashSet<Long>());
        panel.add(preview);

        if (addNavLink && !idsOfExpandedAssets.contains(previewImage.getId())) {
          preview.setVisible(false);
          toggledWidgets.add(preview);
          // Add a nav link for multimedia only if the image panel is hidden
          assignNavLinkString(preview, previewImage.getNavLinkString());
        }
      }
    }
  }
  
  private Widget addAtom(ComplexPanel panel, BaseAtom subsidiaryAtom, boolean renderPreview) {
    Widget widget = null;
    subsidiaryAtom.setRenderAsSeen(alreadySeen);
    if (subsidiaryAtom.getAtomType() == AtomType.NARRATIVE) {
      widget = renderLinkedNarrative((NarrativeAtom)subsidiaryAtom);
    } else {
      widget = LinkedViewFactory.createView(subsidiaryAtom, atom.getContributorIds());
    }
    panel.add(widget);
    if (!idsOfExpandedAssets.contains(subsidiaryAtom.getId())) {
      widget.setVisible(false);
      toggledWidgets.add(widget);
      // Add a nav link for hidden atoms only.
      assignNavLinkString(widget, subsidiaryAtom.getNavLinkString());
    }
    return widget;
  }
  
  private void assignNavLinkString(Widget widget, String navLinkString) {
    if (navLinkString != null && typeStringToNavLinkTargetMap.get(navLinkString) == null) {
      typeStringToNavLinkTargetMap.put(navLinkString, widget);
    }
  }
  
  /**
   * @param expand if true, will open the disclosure panel; if false, will close it.
   * If there is no disclosure panel, because the atom is not expandable, will not do
   * anything.
   * @return true if the state of the disclosure panel changed, false otherwise.
   */
  @Override
  public boolean setExpansion(boolean expand) {
    boolean ret = disclosurePanel != null && disclosurePanel.isOpen() != expand;
    if (ret) {
      disclosurePanel.setAnimationEnabled(false);
      disclosurePanel.setOpen(expand);
      disclosurePanel.setAnimationEnabled(true);
    }
    return ret;
  }
  
  @Override
  public boolean setExpansion(boolean expand, boolean skipExtraActions) {
    boolean ret = false;
    try {
      if (skipExtraActions) {
        suppressScrollOnClose = true;
      }
      ret = setExpansion(expand);
    } finally {
      if (skipExtraActions) {
        suppressScrollOnClose = false;
      }
    }
    return ret;
  }
  
  /**
   * Return true if the element should be collapsed completely, which means the only thing
   * visible is the headline
   */
  private boolean collapseCompletely() {
    return getImportance() == Importance.LOW;
  }

  private boolean hasLocationMap() {
    if (LivingStoryData.getMapsKey().isEmpty()) {
      return false;
    }
    Location location = atom.getLocation();
    Double latitude = location == null ? null : location.getLatitude();
    Double longitude = location == null ? null : location.getLongitude();
    return latitude != null && longitude != null;
  }
  
  /**
   * Creates a location map based on the event's location.
   * @return an appropriate MapWidget, or null if no lat/lng was specified in the location.
   */
  private MapWidget makeLocationMap() {
    Location location = atom.getLocation();
    if (!hasLocationMap()) {
      return null;
    }
    
    final String description = location.getDescription();
    LatLng latLng = LatLng.newInstance(location.getLatitude(), location.getLongitude());
    
    final MapWidget map = new MapWidget(latLng, MAPS_ZOOM);
    map.setSize(MAPS_WIDTH + "px", MAPS_HEIGHT + "px");
    map.addControl(new SmallMapControl());
    map.setDoubleClickZoom(true);
    map.setDraggable(true);
    map.setScrollWheelZoomEnabled(true);
    if (!description.isEmpty()) {
      final Marker marker = new Marker(latLng);
      map.addOverlay(marker);
      final InfoWindowContent iwc = new InfoWindowContent(description);
      marker.addMarkerClickHandler(new MarkerClickHandler() {
        @Override
        public void onClick(MarkerClickEvent event) {
          InfoWindow infoWindow = map.getInfoWindow();
          if (infoWindow.isVisible()) {
            infoWindow.close();
          } else {
            infoWindow.open(marker, iwc);
          }
        }
      });
      map.setTitle(description);
    }
    return map;
  }
  
  private static final EnumSet<AtomType> linkedTypesShownForNarratives =
      EnumSet.of(AtomType.ASSET, AtomType.PLAYER, AtomType.QUOTE);

  /**
   * A narrative that is linked to an event or another narrative and has to be rendered within it
   * has to be treated especially, because unlike with other linked atom types, we do want to 
   * show the atoms that have been linked to the narrative. We'll only show the linked atoms of 
   * the type: Multimedia, Quotes and Players.
   */
  private Widget renderLinkedNarrative(NarrativeAtom narrative) {
    // The left panel has the narrative headline, byline, summary and body
    Widget leftPanel = LinkedViewFactory.createView(narrative, atom.getContributorIds());
    // The right panel has multimedia, players and quotes
    FlowPanel rightPanel = new FlowPanel();
    rightPanel.setWidth(RIGHT_PANEL_WIDTH + "px");
    rightPanel.addStyleName("eventBlockRightColumn");
    
    // Create a map from the different types linked to the narrative to the widgets of atoms
    // of those types that will be rendered
    Map<AtomType, List<Widget>> linkedWidgetsMap = new HashMap<AtomType, List<Widget>>();
    for (AtomType atomType : linkedTypesShownForNarratives) {
      linkedWidgetsMap.put(atomType, new ArrayList<Widget>());
    }
    List<AssetAtom> linkedImages = new ArrayList<AssetAtom>();
    
    for (Long linkedAtomId : narrative.getLinkedAtomIds()) {
      BaseAtom linkedAtom = allAtomsMap.get(linkedAtomId);
      if (linkedAtom != null) {
        AtomType linkedAtomType = linkedAtom.getAtomType();
        if (linkedAtomType == AtomType.ASSET
            && ((AssetAtom) linkedAtom).getAssetType() == AssetType.IMAGE) {
          // Collect all of the images in a list so that they can be shown in a slideshow
          linkedImages.add((AssetAtom) linkedAtom);
        } else if (linkedTypesShownForNarratives.contains(linkedAtomType)) {
          // Create a widget for the linked atoms and put it in the map
          linkedWidgetsMap.get(linkedAtomType).add(
              LinkedViewFactory.createView(linkedAtom, narrative.getContributorIds()));
        }
      }
    }
    // First render the images
    if (!linkedImages.isEmpty()) {
      renderImages(rightPanel, linkedImages, false);
    }
    // Then render the rest of the linked atoms
    for (List<Widget> widgetList : linkedWidgetsMap.values()) {
      for (Widget widget : widgetList) {
        rightPanel.add(widget);
      }
    }
    
    FlowPanel linkedNarrativePanel = new FlowPanel();
    if (rightPanel.getWidgetCount() > 0) {
      linkedNarrativePanel.add(rightPanel);
    }
    linkedNarrativePanel.add(leftPanel);
    return linkedNarrativePanel;
  }
}
