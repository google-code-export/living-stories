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

package com.google.livingstories.client.lsp.views;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.livingstories.client.BaseContentItem;
import com.google.livingstories.client.ContentRpcService;
import com.google.livingstories.client.ContentRpcServiceAsync;
import com.google.livingstories.client.DisplayContentItemBundle;
import com.google.livingstories.client.PlayerContentItem;
import com.google.livingstories.client.lsp.ContentRenderer;
import com.google.livingstories.client.lsp.Page;
import com.google.livingstories.client.lsp.PlayerPageContentItemListWidget;
import com.google.livingstories.client.lsp.event.EventBus;
import com.google.livingstories.client.lsp.event.ShowMoreEvent;
import com.google.livingstories.client.util.LivingStoryControls;

import java.util.Date;

/**
 * Renders the full page version of the player bio.
 */
public class PlayerPage extends Page {
  private static PlayerPageUiBinder uiBinder = GWT.create(PlayerPageUiBinder.class);
  interface PlayerPageUiBinder extends UiBinder<Widget, PlayerPage> {
  }

  private final ContentRpcServiceAsync contentService = GWT.create(ContentRpcService.class);

  @UiField Label name;
  @UiField Label backLink;
  @UiField SimplePanel photo;
  @UiField SimplePanel content;
  @UiField PlayerPageContentItemListWidget relatedContent;
  
  private PlayerContentItem player;
  private Date nextCutoff = null;
  private HandlerRegistration showMoreHandler;

  public PlayerPage() {
    initWidget(uiBinder.createAndBindUi(this));
  }
  
  public PlayerPage(PlayerContentItem player) {
    this();
    load(player);
  }
  
  public void load(PlayerContentItem player) {
    this.player = player;
    
    name.setText(player.getName());
    backLink.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        History.back();
      }
    });
    
    if (player.hasPhoto()) {
      Image photoWidget = new Image(player.getPhotoContentItem().getContent());
      photoWidget.addStyleName("playerPhoto");
      photo.add(photoWidget);
    }
    content.add(new ContentRenderer(player.getFullContentToRender(), false));

    // For now, we don't attribute contributors to player content items, though in principle we
    // could (using them to describe the authorship of the bio.)
    
    loadMore();
    
    onShow();
  }

  public void load(Long contentItemId) {
    contentService.getContentItem(contentItemId, false, new AsyncCallback<BaseContentItem>() {
      @Override
      public void onFailure(Throwable t) {
        LivingStoryControls.goToPage(new ErrorPage(t.getMessage()));
      }
      @Override
      public void onSuccess(BaseContentItem result) {
        load((PlayerContentItem) result);
      }
    });
  }

  @Override
  public void changeState(String key, String value) {
    // No valid state change events yet.
  }

  private void loadMore() {
    relatedContent.beginLoading();
    
    // If the player is not in a living story, we get items the player has authored.
    // Otherwise, we get items the player was linked to.
    // TODO: this doesn't necessarily makes sense anymore if general players
    // (and not just contributors) also have null living story ids.
    boolean byContribution = player.getLivingStoryId() == null;
    contentService.getRelatedContentItems(player.getId(), byContribution, nextCutoff,
        new AsyncCallback<DisplayContentItemBundle>() {
          public void onFailure(Throwable t) {
            relatedContent.showError();
          }
          public void onSuccess(DisplayContentItemBundle bundle) {
            relatedContent.finishLoading(bundle);
            nextCutoff = bundle.getNextDateInSequence();
          }
        });    
  }
  
  @Override
  protected void onLoad() {
    showMoreHandler = EventBus.INSTANCE.addHandler(ShowMoreEvent.TYPE, new ShowMoreEvent.Handler() {
      @Override
      public void onShowMore(ShowMoreEvent e) {
        loadMore();
      }
    });
  }

  @Override
  protected void onUnload() {
    showMoreHandler.removeHandler();
    showMoreHandler = null;
  }
}
