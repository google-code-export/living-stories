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
import com.google.gwt.dom.client.Element;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.livingstories.client.AssetContentItem;
import com.google.livingstories.client.BaseContentItem;
import com.google.livingstories.client.ContentRpcService;
import com.google.livingstories.client.ContentRpcServiceAsync;
import com.google.livingstories.client.lsp.ContentItemPopupWidget;
import com.google.livingstories.client.lsp.SourcePopupWidget;
import com.google.livingstories.client.lsp.views.contentitems.PopupViewFactory;
import com.google.livingstories.client.ui.GlassPanel;
import com.google.livingstories.client.ui.Lightbox;
import com.google.livingstories.client.ui.Slideshow;
import com.google.livingstories.client.util.HistoryManager;
import com.google.livingstories.client.util.LivingStoryData;

import java.util.ArrayList;
import java.util.List;

/**
 * Chrome around a living story.
 */
public class LivingStoryPage extends Composite {
  private static LivingStoryPageUiBinder uiBinder = GWT.create(LivingStoryPageUiBinder.class);
  interface LivingStoryPageUiBinder extends UiBinder<Widget, LivingStoryPage> {
  }

  private final ContentRpcServiceAsync contentService = GWT.create(ContentRpcService.class);

  @UiField Image logo;
  @UiField LivingStoryManagementLinks managementLinks;
  @UiField SimplePanel canvas;
  @UiField GlassPanel glass;

  private Lightbox lightbox = new Lightbox();
  private ContentItemPopupWidget contentItemPopup = new ContentItemPopupWidget();
  private SourcePopupWidget sourcePopup = new SourcePopupWidget();
  
  public LivingStoryPage() {
    initWidget(uiBinder.createAndBindUi(this));

    String logoLocation = LivingStoryData.getLogoLocation();
    if (logoLocation != null) {
      logo.setUrl(logoLocation);
    }
    
    exportMethods();
  }

  public Widget getCurrentPage() {
    return canvas.getWidget();
  }
  
  public void goToPage(Widget page) {
    Window.scrollTo(0, 0);
    canvas.setWidget(page);
  }

  public void goToOverview() {
    HistoryManager.loadMainPage();
  }

  public void showGlass(boolean show) {
    glass.setVisible(show);
  }
  
  public void showLightbox(String title, BaseContentItem contentItem) {
    lightbox.showItem(title, PopupViewFactory.createView(contentItem));
  }
  
  // pass in an int ID because JSNI doesn't work with longs.
  public void showLightboxForContentItem(final String title, int contentItemId) {
    // TODO: Go through ClientCaches in some way rather than going right to
    // the contentService.
    contentService.getContentItem((long)contentItemId, false, new AsyncCallback<BaseContentItem>() {
      @Override
      public void onFailure(Throwable t) {}
      
      @Override
      public void onSuccess(final BaseContentItem contentItem) {
        showLightbox(title, contentItem);
      }
    });
  }
  
  
  public void showContentItemPopup(int contentItemId, final Element showRelativeTo) {
    // TODO: Go through ClientCaches in some way rather than going right to
    // the contentService.
    contentService.getContentItem((long)contentItemId, false, new AsyncCallback<BaseContentItem>() {
      public void onFailure(Throwable t) {}
      public void onSuccess(BaseContentItem contentItem) {
        contentItemPopup.show(contentItem, showRelativeTo);
      }
    });
  }
  
  public void showSourcePopup(final String description, int contentItemId,
      final Element showRelativeTo) {
    if (contentItemId <= 0) {
      sourcePopup.show(description, null, showRelativeTo);
    } else {
      // TODO: Go through ClientCaches in some way rather than going right to
      // the contentService.
      contentService.getContentItem((long)contentItemId, false,
          new AsyncCallback<BaseContentItem>() {
            public void onFailure(Throwable t) {}
            public void onSuccess(BaseContentItem contentItem) {
              sourcePopup.show(description, contentItem, showRelativeTo);
            }
          });      
    }
  }
  
  //pass in int ids because JSNI doesn't work with longs.
  public void showSlideshow(int[] contentItemIds) {
    List<Long> imageIds = new ArrayList<Long>();
    for (int contentItemId : contentItemIds) {
      imageIds.add((long)contentItemId);
    }
    contentService.getContentItems(imageIds, new AsyncCallback<List<BaseContentItem>>() {
      @Override
      public void onFailure(Throwable t) {}
      
      @Override
      public void onSuccess(List<BaseContentItem> contentItems) {
        List<AssetContentItem> images = new ArrayList<AssetContentItem>(contentItems.size());
        for (BaseContentItem contentItem : contentItems) {
          images.add((AssetContentItem)contentItem);
        }
        new Slideshow(images).show(0);
      }
    });
  }
  
  public native void exportMethods() /*-{
    var instance = this;
    $wnd.getCurrentPage = function() {
      return instance.@com.google.livingstories.client.lsp.views.LivingStoryPage::getCurrentPage()
          .call(instance);
    };
    $wnd.goToPage = function(widget) {
      instance.
      @com.google.livingstories.client.lsp.views.LivingStoryPage::goToPage(Lcom/google/gwt/user/client/ui/Widget;)
          .call(instance, widget);
    };
    $wnd.goToOverview = function() {
      instance.@com.google.livingstories.client.lsp.views.LivingStoryPage::goToOverview()
          .call(instance);
    };
    $wnd.showGlass = function(show) {
      instance.
          @com.google.livingstories.client.lsp.views.LivingStoryPage::showGlass(Z)
          .call(instance, show);
    };    
    $wnd.showLightbox = function(title, contentItem) {
      instance.
          @com.google.livingstories.client.lsp.views.LivingStoryPage::showLightbox(Ljava/lang/String;Lcom/google/livingstories/client/BaseContentItem;)
          .call(instance, title, contentItem);
    };
    $wnd.showLightboxForContentItem = function(title, contentItemId) {
      instance.
          @com.google.livingstories.client.lsp.views.LivingStoryPage::showLightboxForContentItem(Ljava/lang/String;I)
          .call(instance, title, contentItemId);
    };
    // for legacy purposes:
    $wnd.showLightboxForAtom = $wnd.showLightboxForContentItem;
    $wnd.showContentItemPopup = function(contentItemId, showRelativeTo) {
      instance.
          @com.google.livingstories.client.lsp.views.LivingStoryPage::showContentItemPopup(ILcom/google/gwt/dom/client/Element;)
          .call(instance, contentItemId, showRelativeTo);
    };
    // for legacy purposes:
    $wnd.showAtomPopup = $wnd.showContentItemPopup;
    $wnd.showSourcePopup = function(description, contentItemId, showRelativeTo) {
      instance.
          @com.google.livingstories.client.lsp.views.LivingStoryPage::showSourcePopup(Ljava/lang/String;ILcom/google/gwt/dom/client/Element;)
          .call(instance, description, contentItemId, showRelativeTo);
    };
    $wnd.showSlideshow = function(contentItemIds) {
      instance.
          @com.google.livingstories.client.lsp.views.LivingStoryPage::showSlideshow([I)
          .call(instance, contentItemIds);
    };
  }-*/;
}
