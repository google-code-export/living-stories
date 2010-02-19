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
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.livingstories.client.ContentItemTypesBundle;
import com.google.livingstories.client.ContentRpcService;
import com.google.livingstories.client.ContentRpcServiceAsync;
import com.google.livingstories.client.DisplayContentItemBundle;
import com.google.livingstories.client.FilterSpec;
import com.google.livingstories.client.LivingStoryRpcService;
import com.google.livingstories.client.LivingStoryRpcServiceAsync;
import com.google.livingstories.client.lsp.ContentRenderer;
import com.google.livingstories.client.lsp.FilterWidget;
import com.google.livingstories.client.lsp.LspContentItemListWidget;
import com.google.livingstories.client.lsp.Page;
import com.google.livingstories.client.lsp.RecentEventsList;
import com.google.livingstories.client.lsp.ThemeListWidget;
import com.google.livingstories.client.lsp.event.BlockToggledEvent;
import com.google.livingstories.client.lsp.event.EventBus;
import com.google.livingstories.client.ui.AnchoredPanel;
import com.google.livingstories.client.ui.FCCommentsBox;
import com.google.livingstories.client.ui.UpdateCountWidget;
import com.google.livingstories.client.util.HistoryManager;
import com.google.livingstories.client.util.LivingStoryControls;
import com.google.livingstories.client.util.LivingStoryData;
import com.google.livingstories.client.util.HistoryManager.HistoryPages;

import java.util.Map;

/**
 * Page that displays the summary and the content grid.
 */
public class OverviewPage extends Page {
  private static OverviewPageUiBinder uiBinder = GWT.create(OverviewPageUiBinder.class);
  interface OverviewPageUiBinder extends UiBinder<Widget, OverviewPage> {
  }

  private static final String SCROLL_POSITION_STATE = "sp";
  
  private final ContentRpcServiceAsync contentService = GWT.create(ContentRpcService.class);
  private final LivingStoryRpcServiceAsync livingStoryService =
    GWT.create(LivingStoryRpcService.class);

  @UiField Label title; 
  @UiField UpdateCountWidget updateCount;
  @UiField SimplePanel summary;
  @UiField ThemeListWidget themeList;
  @UiField FilterWidget filterList;
  @UiField LspContentItemListWidget contentItemList;
  @UiField AnchoredPanel rightPanel;
  @UiField RecentEventsList recentEvents;
  @UiField FCCommentsBox comments;
  
  private HandlerRegistration toggleEventHandler;
  
  public OverviewPage() {
    initWidget(uiBinder.createAndBindUi(this));

    title.setText(LivingStoryData.getLivingStoryTitle());
    updateCount.load(LivingStoryData.getLivingStoryId(), LivingStoryData.getLastVisitDate());
    
    summary.add(new ContentRenderer(LivingStoryData.getSummary(), true, true));
    
    recentEvents.load();
    
    livingStoryService.getThemeInfoForLivingStory(LivingStoryData.getLivingStoryId(),
        new AsyncCallback<Map<Long, ContentItemTypesBundle>>() {
          @Override
          public void onFailure(Throwable caught) {}
          
          @Override
          public void onSuccess(Map<Long, ContentItemTypesBundle> results) {
            themeList.load(results);
            filterList.load(results, themeList.getSelectedThemeId());
          }
        });
    
    exportMethods();
  }
  
  /**
   * Sets off an asynchronous call that fills in data for the event list. In some circumstances,
   * can do its work synchronously.
   */
  public void update(FilterSpec filter, Long focusedContentItemId) {
    FilterSpec oldFilter = filterList.getFilter();
    if (!filter.equals(oldFilter)) {
      boolean simpleReversal = !contentItemList.hasMore() && filter.isReverseOf(oldFilter);
      filterList.setFilter(filter);
      if (simpleReversal) {
        contentItemList.doSimpleReversal(filter.oldestFirst);
      } else {
        contentService.getDisplayContentItemBundle(LivingStoryData.getLivingStoryId(), filter,
            focusedContentItemId, null, new ContentItemCallback(focusedContentItemId));
        contentItemList.clear();
        contentItemList.beginLoading();
        beginLoading();
      }
    } else if (focusedContentItemId != null) {
      highlightEvent(focusedContentItemId, false);
    }
  }
  
  @Override
  public void changeState(String key, String value) {
    if (key.equals(SCROLL_POSITION_STATE)) {
      Window.scrollTo(0, Integer.valueOf(value));
      repositionAnchoredPanel();
    }
  }
  
  @Override
  public void onShow() {
    repositionAnchoredPanel();
  }
  
  @Override
  protected void onLoad() {
    super.onLoad();
    toggleEventHandler = EventBus.INSTANCE.addHandler(BlockToggledEvent.TYPE,
        new BlockToggledEvent.Handler() {
          @Override
          public void onToggle(BlockToggledEvent e) {
            if (e.isOpened() && e.shouldSetHistory()) {
              // Set this content item as the focused contentitem in the history.
              // When the user navigates away from this and then clicks back, this item will
              // appear expanded, and the viewport will be scrolled to its position.
              HistoryManager.newToken(HistoryPages.OVERVIEW,
                  LivingStoryControls.getCurrentFilterSpec().getFilterParams(),
                  String.valueOf(e.getContentItemId()));
            }
          }
        });
  }
  
  @Override
  public void onUnload() {
    super.onUnload();
    if (toggleEventHandler != null) {
      toggleEventHandler.removeHandler();
      toggleEventHandler = null;
    }
  }

  private class ContentItemCallback implements AsyncCallback<DisplayContentItemBundle> {
    private Long focusedContentItemId;

    public ContentItemCallback() {
      this(null);
    }
    
    public ContentItemCallback(Long focusedContentItemId) {
      this.focusedContentItemId = focusedContentItemId;
    }
    
    public void onFailure(Throwable t) {
      contentItemList.showError();
    }
    
    public void onSuccess(DisplayContentItemBundle bundle) {
      themeList.setSelectedThemeId(bundle.getAdjustedFilterSpec().themeId);
      if (!filterList.getFilter().equals(bundle.getAdjustedFilterSpec())) {
        filterList.setFilter(bundle.getAdjustedFilterSpec());
        contentItemList.clear();
      }
      contentItemList.finishLoading(bundle);
      if (focusedContentItemId != null) {
        contentItemList.goToContentItem(focusedContentItemId);
      }
      finishLoading();
      comments.loadCommentsBox("LSID:" + LivingStoryData.getLivingStoryId());
      rightPanel.setVisible(true);
    }
  }
  
  /**
   * "Jumps to" the event indicated by contentItemId, scrolling it into view and opening its
   * contents.
   * Only works for event and standalone narrative content items. 
   * Sets a history token when called.
   */
  public void highlightEvent(int contentItemId) {
    // Save the current scroll position in the history so that when the user clicks 'back',
    // they're scrolled back here.
    HistoryManager.changeState(SCROLL_POSITION_STATE, String.valueOf(Window.getScrollTop()));
    highlightEvent(contentItemId, true);
  }
  
  /**
   * Internal version of highlightEvent.  Lets the update() method above select an
   * event without setting an extra history token.
   */
  private void highlightEvent(long contentItemId, boolean setHistoryToken) {
    boolean finished = contentItemList.goToContentItem(contentItemId);
    if (!finished) {
      contentService.getDisplayContentItemBundle(LivingStoryData.getLivingStoryId(),
          filterList.getFilter(), contentItemId, contentItemList.getNextDateInSequence(),
          new ContentItemCallback(contentItemId));
    }
    
    if (setHistoryToken) {
      // Set this content item as the focused item in the history.
      // When the user navigates away from this and then clicks back, this content item will
      // appear expanded, and the viewport will be scrolled to its position.
      HistoryManager.newToken(
          HistoryPages.OVERVIEW, LivingStoryControls.getCurrentFilterSpec().getFilterParams(),
          String.valueOf(contentItemId));
    }
  }
  
  public void getMoreContentItems() {
    contentService.getDisplayContentItemBundle(
        LivingStoryData.getLivingStoryId(), filterList.getFilter(), null,
        contentItemList.getNextDateInSequence(), new ContentItemCallback());
    contentItemList.beginLoading();
  }
  
  public void repositionAnchoredPanel() {
    rightPanel.reposition();
  }

  public FilterSpec getCurrentFilterSpec() {
    return filterList.getFilter();
  }
  
  private native void exportMethods() /*-{
    var instance = this;
    $wnd.goToContentItem = function(contentItemId) {
      instance.@com.google.livingstories.client.lsp.views.OverviewPage::highlightEvent(I)
          .call(instance, contentItemId);
    }
    $wnd.repositionAnchoredPanel = function(contentItemId) {
      instance.@com.google.livingstories.client.lsp.views.OverviewPage::repositionAnchoredPanel()
          .call(instance);
    }
    $wnd.getMoreContentItems = function() {
      instance.@com.google.livingstories.client.lsp.views.OverviewPage::getMoreContentItems()
          .call(instance);
    }
    $wnd.getCurrentFilterSpec = function() {
      return instance.
          @com.google.livingstories.client.lsp.views.OverviewPage::getCurrentFilterSpec()
          .call(instance);
    }
  }-*/;
}
