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

package com.google.livingstories.client.lsp.views.contentitems;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.livingstories.client.AssetContentItem;
import com.google.livingstories.client.AssetType;
import com.google.livingstories.client.ContentItemType;
import com.google.livingstories.client.BaseContentItem;
import com.google.livingstories.client.Importance;
import com.google.livingstories.client.Location;
import com.google.livingstories.client.NarrativeContentItem;
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
public abstract class LongContainerView<T extends BaseContentItem> extends BaseContainerView<T> {
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
  
  public LongContainerView(T contentItem,
      Map<ContentItemType, List<BaseContentItem>> linkedContentItemsByType) {
    super(contentItem, linkedContentItemsByType);
    
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
    // Bind to a different UI if this content item has important assets linked to it.
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
    for (BaseContentItem backgroundContentItem
        : linkedContentItemsByType.get(ContentItemType.BACKGROUND)) {
      background.add(LinkedViewFactory.createView(
          backgroundContentItem, contentItem.getContributorIds()));
      assignNavLinkString(background, ContentItemType.BACKGROUND.getNavLinkString());
    }
  }
  
  private void createReactions() {
    for (BaseContentItem reactionContentItem
        : linkedContentItemsByType.get(ContentItemType.REACTION)) {
      reactions.add(LinkedViewFactory.createView(
          reactionContentItem, contentItem.getContributorIds()));
      assignNavLinkString(reactions, ContentItemType.REACTION.getNavLinkString());
    }
  }

  private void createData() {
    for (BaseContentItem dataContentItem : linkedContentItemsByType.get(ContentItemType.DATA)) {
      data.add(LinkedViewFactory.createView(dataContentItem, contentItem.getContributorIds()));
      assignNavLinkString(data, ContentItemType.DATA.getNavLinkString());
    }
  }

  private void createNarratives() {
    for (BaseContentItem narrativeContentItem
        : linkedContentItemsByType.get(ContentItemType.NARRATIVE)) {
      narratives.add(renderLinkedNarrative((NarrativeContentItem) narrativeContentItem));
    }
    EventBus.INSTANCE.addHandler(NarrativeLinkClickedEvent.TYPE,
        new NarrativeLinkClickedEvent.Handler() {
          @Override
          public void onClick(final NarrativeLinkClickedEvent e) {
            if (contentItem.getId().equals(e.getContainerContentItemId())
                && narrativeWidgetsById.containsKey(e.getNarrativeContentItemId())) {
              EventBus.INSTANCE.fireEvent(new BlockToggledEvent(true, contentItem.getId())
                  .setOnFinish(new Command() {
                    @Override
                    public void execute() {
                      WindowScroll.scrollTo(
                          narrativeWidgetsById.get(e.getNarrativeContentItemId()).getAbsoluteTop(),
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
  
  private static final EnumSet<ContentItemType> LINKED_TYPES_SHOWN_FOR_NARRATIVES =
      EnumSet.of(ContentItemType.ASSET, ContentItemType.PLAYER, ContentItemType.QUOTE);

  /**
   * A narrative that is linked to an event or another narrative and has to be rendered within it
   * has to be treated especially, because unlike with other linked content item types, we do want to 
   * show the content items that have been linked to the narrative. We'll only show the linked content items of 
   * the type: Multimedia, Quotes and Players.
   * 
   * TODO: Reuse the ContainerView here somehow, instead of reimplementing
   * a bunch of stuff.
   */
  private Widget renderLinkedNarrative(NarrativeContentItem narrative) {
    // The left panel has the narrative headline, byline, summary and body
    Widget leftPanel = LinkedViewFactory.createView(narrative, contentItem.getContributorIds());
    // The right panel has multimedia, players and quotes
    FlowPanel rightPanel = new FlowPanel();
    rightPanel.addStyleName(Resources.INSTANCE.css().linkedContentItemsPanel());
    
    // Create a map from the different types linked to the narrative to the widgets of content items
    // of those types that will be rendered
    Map<ContentItemType, List<Widget>> linkedWidgetsMap = new HashMap<ContentItemType, List<Widget>>();
    for (ContentItemType contentItemType : LINKED_TYPES_SHOWN_FOR_NARRATIVES) {
      linkedWidgetsMap.put(contentItemType, new ArrayList<Widget>());
    }
    List<AssetContentItem> linkedImages = new ArrayList<AssetContentItem>();
    
    for (BaseContentItem linkedContentItem : narrative.getLinkedContentItems()) {
      if (linkedContentItem != null) {
        ContentItemType linkedContentItemType = linkedContentItem.getContentItemType();
        if (linkedContentItemType == ContentItemType.ASSET
            && ((AssetContentItem) linkedContentItem).getAssetType() == AssetType.IMAGE) {
          // Collect all of the images in a list so that they can be shown in a slideshow
          linkedImages.add((AssetContentItem) linkedContentItem);
        } else if (LINKED_TYPES_SHOWN_FOR_NARRATIVES.contains(linkedContentItemType)) {
          // Create a widget for the linked content items and put it in the map
          linkedWidgetsMap.get(linkedContentItemType).add(
              LinkedViewFactory.createView(linkedContentItem, narrative.getContributorIds()));
        }
      }
    }
    // First render the images
    // TODO: this is mostly repeated from 'createImages' below, but
    // will go away once we get narratives rendering with a ContainerView as well.
    if (!linkedImages.isEmpty()) {
      List<AssetContentItem> slideshowImages = new ArrayList<AssetContentItem>();
      List<AssetContentItem> thumbnailOnlyImages = new ArrayList<AssetContentItem>();
      
      for (AssetContentItem image : linkedImages) {
        if (GlobalUtil.isContentEmpty(image.getContent())) {
          thumbnailOnlyImages.add(image);
        } else {
          slideshowImages.add(image);
        }
      }
      
      if (!slideshowImages.isEmpty()) {
        AssetContentItem previewImage = slideshowImages.get(0);
        previewImage.setRelatedAssets(slideshowImages);
        Widget previewPanel = LinkedViewFactory.createView(previewImage, contentItem.getContributorIds());
        rightPanel.add(previewPanel);
      }

      for (AssetContentItem image : thumbnailOnlyImages) {
        rightPanel.add(LinkedViewFactory.createView(image, contentItem.getContributorIds()));
      }
    }
    // Then render the rest of the linked content items
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
    List<AssetContentItem> linkedImages = linkedAssetsByType.get(AssetType.IMAGE);

    List<AssetContentItem> slideshowImages = new ArrayList<AssetContentItem>();
    List<AssetContentItem> thumbnailOnlyImages = new ArrayList<AssetContentItem>();
    
    for (AssetContentItem image : linkedImages) {
      if (GlobalUtil.isContentEmpty(image.getContent())) {
        thumbnailOnlyImages.add(image);
      } else {
        slideshowImages.add(image);
      }
    }
    
    if (!slideshowImages.isEmpty()) {
      AssetContentItem previewImage = slideshowImages.get(0);
      previewImage.setRelatedAssets(slideshowImages);
      Widget previewPanel = LinkedViewFactory.createView(previewImage, contentItem.getContributorIds());
      if (previewImage.getImportance() == Importance.HIGH) {
        importantImages.add(previewPanel);
      } else {
        images.add(previewPanel);
        assignNavLinkString(images, AssetType.IMAGE.getNavLinkString());
      }
    }

    for (AssetContentItem image : thumbnailOnlyImages) {
      Widget previewPanel = LinkedViewFactory.createView(image, contentItem.getContributorIds());
      if (image.getImportance() == Importance.HIGH) {
        importantImages.add(previewPanel);
      } else {
        images.add(previewPanel);
        assignNavLinkString(images, AssetType.IMAGE.getNavLinkString());
      }
    }
  }
  
  private void createAssets() {
    for (Entry<AssetType, List<AssetContentItem>> linkedAssets : linkedAssetsByType.entrySet()) {
      // Render everything except images, which we've already done elsewhere.
      if (linkedAssets.getKey() != AssetType.IMAGE) {
        for (AssetContentItem assetContentItem : linkedAssets.getValue()) {
          Widget view = LinkedViewFactory.createView(assetContentItem, contentItem.getContributorIds());
          if (assetContentItem.getImportance() == Importance.HIGH) {
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
    Location location = contentItem.getLocation();

    if (location.getLatitude() == null || location.getLongitude() == null) {
      return null;
    }

    assignNavLinkString(map, LspMessageHolder.consts.locationTitle());
    return new LocationView(location);
  }
  
  private void createPlayers() {
    for (BaseContentItem playerContentItem : linkedContentItemsByType.get(ContentItemType.PLAYER)) {
      players.add(LinkedViewFactory.createView(playerContentItem, contentItem.getContributorIds()));
      assignNavLinkString(players, ContentItemType.PLAYER.getNavLinkString());
    }
  }

  private void createQuotes() {
    for (BaseContentItem quoteContentItem : linkedContentItemsByType.get(ContentItemType.QUOTE)) {
      quotes.add(LinkedViewFactory.createView(quoteContentItem, contentItem.getContributorIds()));
      assignNavLinkString(quotes, ContentItemType.QUOTE.getNavLinkString());
    }
  }
  
  private void assignNavLinkString(Widget widget, String navLinkString) {
    if (navLinkString != null && extraContentNavLinks.get(navLinkString) == null) {
      extraContentNavLinks.put(navLinkString, widget);
    }
  }
}
