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

package com.google.livingstories.client.lsp.views.atoms;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.livingstories.client.AssetAtom;
import com.google.livingstories.client.AssetType;
import com.google.livingstories.client.AtomType;
import com.google.livingstories.client.BaseAtom;
import com.google.livingstories.client.Importance;
import com.google.livingstories.client.Location;
import com.google.livingstories.client.NarrativeAtom;
import com.google.livingstories.client.lsp.LspMessageHolder;
import com.google.livingstories.client.lsp.event.BlockToggledEvent;
import com.google.livingstories.client.lsp.event.EventBus;
import com.google.livingstories.client.lsp.event.NarrativeLinkClickedEvent;
import com.google.livingstories.client.lsp.views.Resources;
import com.google.livingstories.client.ui.WindowScroll;
import com.google.livingstories.client.util.GlobalUtil;
import com.google.livingstories.client.util.LivingStoryControls;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Base code for an expanded container view.
 */
public abstract class LongContainerView<T extends BaseAtom> extends BaseContainerView<T> {
  private static StandardLongContainerViewUiBinder standardUiBinder =
      GWT.create(StandardLongContainerViewUiBinder.class);
  
  @SuppressWarnings("unchecked")
  @UiTemplate("StandardLongContainerView.ui.xml")
  interface StandardLongContainerViewUiBinder extends
      UiBinder<Widget, LongContainerView> {
    // This interface should theoretically use a genericized version of ShortContainerView,
    // but there's a bug in GWT that prevents that from working.  Instead, we use the raw
    // type here.  This works in most situations, though there are certain things
    // you won't be able to do (e.g. @UiHandler won't be able to bind to a method that
    // takes a parameterized type.)
    // TODO: fix this when the next version of GWT comes out and the bug is fixed.
  }
  
  private static ImportantLongContainerViewUiBinder importantUiBinder =
      GWT.create(ImportantLongContainerViewUiBinder.class);
  
  @SuppressWarnings("unchecked")
  @UiTemplate("ImportantLongContainerView.ui.xml")
  interface ImportantLongContainerViewUiBinder extends
      UiBinder<Widget, LongContainerView> {
    // This interface should theoretically use a genericized version of ShortContainerView,
    // but there's a bug in GWT that prevents that from working.  Instead, we use the raw
    // type here.  This works in most situations, though there are certain things
    // you won't be able to do (e.g. @UiHandler won't be able to bind to a method that
    // takes a parameterized type.)
    // TODO: fix this when the next version of GWT comes out and the bug is fixed.
  }
  
  @UiField FlowPanel summary;
  @UiField FlowPanel details;
  @UiField FlowPanel narrativeLinks;
  @UiField FlowPanel background;
  @UiField FlowPanel reactions;
  @UiField FlowPanel data;
  @UiField FlowPanel narratives;
  @UiField FlowPanel importantImages;
  @UiField FlowPanel importantAssets;
  @UiField FlowPanel images;
  @UiField FlowPanel assets;
  @UiField FlowPanel map;
  @UiField FlowPanel players;
  @UiField FlowPanel quotes;
  
  private Map<String, Widget> extraContentNavLinks = new HashMap<String, Widget>();
  private Map<Long, Widget> narrativeWidgetsById = new HashMap<Long, Widget>();
  private HandlerRegistration narrativeLinkClickHandler;
  
  public LongContainerView(T atom,
      Map<AtomType, List<BaseAtom>> linkedAtomsByType) {
    super(atom, linkedAtomsByType);
    
    GlobalUtil.addIfNotNull(summary, createSummary());
    GlobalUtil.addIfNotNull(details, createDetails());
    GlobalUtil.addIfNotNull(narrativeLinks, createNarrativeLinks());
    createBackground();
    createReactions();
    createData();
    createNarratives();
    createImages();
    createAssets();
    GlobalUtil.addIfNotNull(map, createMap());
    createPlayers();
    createQuotes();
  }

  @Override
  protected void bind() {
    // Bind to a different UI if this atom has important assets linked to it.
    if (hasImportantAssets()) {
      initWidget(importantUiBinder.createAndBindUi(this));
    } else {
      initWidget(standardUiBinder.createAndBindUi(this));
    }
  }

  @Override
  protected void onUnload() {
    super.onUnload();
    if (narrativeLinkClickHandler != null) {
      narrativeLinkClickHandler.removeHandler();
      narrativeLinkClickHandler = null;
    }
  }
  
  /**
   * Determines if this view has anything significant in the fields that aren't shown
   * by default in the ShortContainerView.  Note that if you add anything to this
   * view or the short view, you should reconsider whether or not some of the
   * panels should be added/removed from this method.
   */
  public boolean hasExtraContent() {
    return hasChildWidget(background, details, reactions, data, narratives,
        images, assets, map, players, quotes);
  }
  
  private boolean hasChildWidget(ComplexPanel... panels) {
    for (ComplexPanel panel : panels) {
      if (panel.getWidgetCount() > 0) {
        return true;
      }
    }
    return false;
  }

  /**
   * Gets string to widget mappings for extra content that is important enough to warrant
   * a nav link.
   */
  public Map<String, Widget> getExtraContentNavLinks() {
    return extraContentNavLinks;
  }

  protected abstract Widget createSummary();
  
  protected abstract Widget createDetails();
  
  private void createBackground() {
    for (BaseAtom backgroundAtom : linkedAtomsByType.get(AtomType.BACKGROUND)) {
      background.add(LinkedViewFactory.createView(backgroundAtom, atom.getContributorIds()));
      assignNavLinkString(background, AtomType.BACKGROUND.getNavLinkString());
    }
  }
  
  private void createReactions() {
    for (BaseAtom reactionAtom : linkedAtomsByType.get(AtomType.REACTION)) {
      reactions.add(LinkedViewFactory.createView(reactionAtom, atom.getContributorIds()));
      assignNavLinkString(reactions, AtomType.REACTION.getNavLinkString());
    }
  }

  private void createData() {
    for (BaseAtom dataAtom : linkedAtomsByType.get(AtomType.DATA)) {
      data.add(LinkedViewFactory.createView(dataAtom, atom.getContributorIds()));
      assignNavLinkString(data, AtomType.DATA.getNavLinkString());
    }
  }

  private void createNarratives() {
    for (BaseAtom narrativeAtom : linkedAtomsByType.get(AtomType.NARRATIVE)) {
      narratives.add(renderLinkedNarrative((NarrativeAtom) narrativeAtom));
    }
    EventBus.INSTANCE.addHandler(NarrativeLinkClickedEvent.TYPE,
        new NarrativeLinkClickedEvent.Handler() {
          @Override
          public void onClick(final NarrativeLinkClickedEvent e) {
            if (atom.getId().equals(e.getContainerAtomId())
                && narrativeWidgetsById.containsKey(e.getNarrativeAtomId())) {
              EventBus.INSTANCE.fireEvent(new BlockToggledEvent(true, atom.getId())
                  .setOnFinish(new Command() {
                    @Override
                    public void execute() {
                      WindowScroll.scrollTo(
                          narrativeWidgetsById.get(e.getNarrativeAtomId()).getAbsoluteTop(),
                          new Command() {
                            @Override
                            public void execute() {
                              LivingStoryControls.repositionAnchoredPanel();
                            }
                          });                  
                    }
              }));
            }
          }
        });
  }
  
  private static final EnumSet<AtomType> LINKED_TYPES_SHOWN_FOR_NARRATIVES =
      EnumSet.of(AtomType.ASSET, AtomType.PLAYER, AtomType.QUOTE);

  /**
   * A narrative that is linked to an event or another narrative and has to be rendered within it
   * has to be treated especially, because unlike with other linked atom types, we do want to 
   * show the atoms that have been linked to the narrative. We'll only show the linked atoms of 
   * the type: Multimedia, Quotes and Players.
   * 
   * TODO: Reuse the ContainerView here somehow, instead of reimplementing
   * a bunch of stuff.
   */
  private Widget renderLinkedNarrative(NarrativeAtom narrative) {
    // The left panel has the narrative headline, byline, summary and body
    Widget leftPanel = LinkedViewFactory.createView(narrative, atom.getContributorIds());
    // The right panel has multimedia, players and quotes
    FlowPanel rightPanel = new FlowPanel();
    rightPanel.addStyleName(Resources.INSTANCE.css().linkedAtomsPanel());
    
    // Create a map from the different types linked to the narrative to the widgets of atoms
    // of those types that will be rendered
    Map<AtomType, List<Widget>> linkedWidgetsMap = new HashMap<AtomType, List<Widget>>();
    for (AtomType atomType : LINKED_TYPES_SHOWN_FOR_NARRATIVES) {
      linkedWidgetsMap.put(atomType, new ArrayList<Widget>());
    }
    List<AssetAtom> linkedImages = new ArrayList<AssetAtom>();
    
    for (BaseAtom linkedAtom : narrative.getLinkedAtoms()) {
      if (linkedAtom != null) {
        AtomType linkedAtomType = linkedAtom.getAtomType();
        if (linkedAtomType == AtomType.ASSET
            && ((AssetAtom) linkedAtom).getAssetType() == AssetType.IMAGE) {
          // Collect all of the images in a list so that they can be shown in a slideshow
          linkedImages.add((AssetAtom) linkedAtom);
        } else if (LINKED_TYPES_SHOWN_FOR_NARRATIVES.contains(linkedAtomType)) {
          // Create a widget for the linked atoms and put it in the map
          linkedWidgetsMap.get(linkedAtomType).add(
              LinkedViewFactory.createView(linkedAtom, narrative.getContributorIds()));
        }
      }
    }
    // First render the images
    // TODO: this is mostly repeated from 'createImages' below, but
    // will go away once we get narratives rendering with a ContainerView as well.
    if (!linkedImages.isEmpty()) {
      List<AssetAtom> slideshowImages = new ArrayList<AssetAtom>();
      List<AssetAtom> thumbnailOnlyImages = new ArrayList<AssetAtom>();
      
      for (AssetAtom image : linkedImages) {
        if (GlobalUtil.isContentEmpty(image.getContent())) {
          thumbnailOnlyImages.add(image);
        } else {
          slideshowImages.add(image);
        }
      }
      
      if (!slideshowImages.isEmpty()) {
        AssetAtom previewImage = slideshowImages.get(0);
        previewImage.setRelatedAssets(slideshowImages);
        Widget previewPanel = LinkedViewFactory.createView(previewImage, atom.getContributorIds());
        rightPanel.add(previewPanel);
      }

      for (AssetAtom image : thumbnailOnlyImages) {
        rightPanel.add(LinkedViewFactory.createView(image, atom.getContributorIds()));
      }
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
    
    narrativeWidgetsById.put(narrative.getId(), linkedNarrativePanel);
    
    return linkedNarrativePanel;
  }

  private void createImages() {
    List<AssetAtom> linkedImages = linkedAssetsByType.get(AssetType.IMAGE);

    List<AssetAtom> slideshowImages = new ArrayList<AssetAtom>();
    List<AssetAtom> thumbnailOnlyImages = new ArrayList<AssetAtom>();
    
    for (AssetAtom image : linkedImages) {
      if (GlobalUtil.isContentEmpty(image.getContent())) {
        thumbnailOnlyImages.add(image);
      } else {
        slideshowImages.add(image);
      }
    }
    
    if (!slideshowImages.isEmpty()) {
      AssetAtom previewImage = slideshowImages.get(0);
      previewImage.setRelatedAssets(slideshowImages);
      Widget previewPanel = LinkedViewFactory.createView(previewImage, atom.getContributorIds());
      if (previewImage.getImportance() == Importance.HIGH) {
        importantImages.add(previewPanel);
      } else {
        images.add(previewPanel);
        assignNavLinkString(images, AssetType.IMAGE.getNavLinkString());
      }
    }

    for (AssetAtom image : thumbnailOnlyImages) {
      Widget previewPanel = LinkedViewFactory.createView(image, atom.getContributorIds());
      if (image.getImportance() == Importance.HIGH) {
        importantImages.add(previewPanel);
      } else {
        images.add(previewPanel);
        assignNavLinkString(images, AssetType.IMAGE.getNavLinkString());
      }
    }
  }
  
  private void createAssets() {
    for (Entry<AssetType, List<AssetAtom>> linkedAssets : linkedAssetsByType.entrySet()) {
      // Render everything except images, which we've already done elsewhere.
      if (linkedAssets.getKey() != AssetType.IMAGE) {
        for (AssetAtom assetAtom : linkedAssets.getValue()) {
          Widget view = LinkedViewFactory.createView(assetAtom, atom.getContributorIds());
          if (assetAtom.getImportance() == Importance.HIGH) {
            importantAssets.add(view);
          } else {
            assets.add(view);
            assignNavLinkString(view, linkedAssets.getKey().getNavLinkString());
          }
        }
      }
    }
  }

  /**
   * Creates a location map based on the event's location.
   * @return an appropriate MapWidget, or null if no lat/lng was specified in the location.
   */
  private LocationView createMap() {
    Location location = atom.getLocation();

    if (location.getLatitude() == null || location.getLongitude() == null) {
      return null;
    }

    assignNavLinkString(map, LspMessageHolder.consts.locationTitle());
    return new LocationView(location);
  }
  
  private void createPlayers() {
    for (BaseAtom playerAtom : linkedAtomsByType.get(AtomType.PLAYER)) {
      players.add(LinkedViewFactory.createView(playerAtom, atom.getContributorIds()));
      assignNavLinkString(players, AtomType.PLAYER.getNavLinkString());
    }
  }

  private void createQuotes() {
    for (BaseAtom quoteAtom : linkedAtomsByType.get(AtomType.QUOTE)) {
      quotes.add(LinkedViewFactory.createView(quoteAtom, atom.getContributorIds()));
      assignNavLinkString(quotes, AtomType.QUOTE.getNavLinkString());
    }
  }
  
  private void assignNavLinkString(Widget widget, String navLinkString) {
    if (navLinkString != null && extraContentNavLinks.get(navLinkString) == null) {
      extraContentNavLinks.put(navLinkString, widget);
    }
  }
}
