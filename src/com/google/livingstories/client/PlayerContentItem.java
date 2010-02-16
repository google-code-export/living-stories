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

package com.google.livingstories.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.livingstories.client.lsp.ContentRenderer;
import com.google.livingstories.client.lsp.Page;
import com.google.livingstories.client.lsp.views.PlayerPage;
import com.google.livingstories.client.util.Constants;
import com.google.livingstories.client.util.HistoryManager;
import com.google.livingstories.client.util.LivingStoryControls;
import com.google.livingstories.client.util.HistoryManager.HistoryPages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Client-side version of a player content entity, which represents a person or an organization, not 
 * specific to a story.
 */
public class PlayerContentItem extends BaseContentItem {
  protected String name;
  protected List<String> aliases;
  protected PlayerType playerType;
  protected AssetContentItem photoContentItem;

  public PlayerContentItem() {}
  
  public PlayerContentItem(Long id, Date timestamp, Set<Long> contributorIds, String content,
      Importance importance, String name, List<String> aliases, PlayerType playerType, 
      AssetContentItem photoContentItem) {
    super(id, timestamp, ContentItemType.PLAYER, contributorIds, content, importance, null);
    this.name = name;
    this.aliases = (aliases == null ? new ArrayList<String>() : new ArrayList<String>(aliases));
    this.playerType = playerType;
    this.photoContentItem = photoContentItem;
  }

  public String getName() {
    return name;
  }
  
  public List<String> getAliases() {
    return aliases;
  }

  public PlayerType getPlayerType() {
    return playerType;
  }

  public boolean hasPhoto() {
    return photoContentItem != null;
  }
  
  public Long getPhotoContentItemId() {
    return photoContentItem == null ? null : photoContentItem.getId();
  }
  
  public AssetContentItem getPhotoContentItem() {
    return photoContentItem;
  }
  
  @Override
  public String getTitleString() {
    return "";
  }
  
  @Override
  public String getTypeString() {
    return playerType.toString();
  }
  
  public String getPreviewContentToRender() {
    return getContent().split(Constants.BREAK_TAG)[0];
  }
  
  public String getFullContentToRender() {
    return getContent();
  }
  
  @Override
  public Widget renderPreview() {
    DockPanel panel = new DockPanel();
    panel.setVerticalAlignment(DockPanel.ALIGN_TOP);
    
    if (photoContentItem != null) {
      Image photoWidget = new Image();
      photoWidget.setUrl(photoContentItem.getPreviewUrl());
      photoWidget.addStyleName("playerPhoto");
      panel.add(photoWidget, DockPanel.WEST);
    }

    Label nameLabel = new Label(getName());
    nameLabel.addStyleName("primaryLink");
    DOM.setStyleAttribute(nameLabel.getElement(), "fontWeight", "bold");
    panel.add(nameLabel, DockPanel.NORTH);
    
    // Only show the first chunk of the text description
    panel.add(new ContentRenderer(getPreviewContentToRender(), false), 
        DockPanel.CENTER);
    
    FocusPanel focusPanel = new FocusPanel(panel);
    focusPanel.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent e) {
        // TODO: this isn't great, but it works.
        // The right way to do this would probably be to store all content items in the
        // ClientCache and fire a history change event here to load the page, instead
        // of trying to hack around the history system.
        Page page = (Page) renderContent(Collections.<Long>emptySet());
        HistoryManager.newToken(page, HistoryPages.PLAYER, String.valueOf(getId()));
        LivingStoryControls.goToPage(page);
      }
    });
    focusPanel.setStylePrimaryName("clickableArea");
    focusPanel.addStyleName("wholeRenderIsLink");
    
    return focusPanel;
  }
  
  @Override
  public Widget renderContent(Set<Long> eventBlockContributorIds) {
    // For now, we don't attribute contributors to players, though in principle we could
    // (using them to describe the authorship of the bio.)
    PlayerPage playerPage = new PlayerPage();
    playerPage.load(this);
    return playerPage;
  }
  
  @Override
  public String getDisplayString() {
    return "[" + getTypeString() + "] " + getName();
  }
}
