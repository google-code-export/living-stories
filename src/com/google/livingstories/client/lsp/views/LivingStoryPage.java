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
import com.google.livingstories.client.AssetAtom;
import com.google.livingstories.client.BaseAtom;
import com.google.livingstories.client.ContentRpcService;
import com.google.livingstories.client.ContentRpcServiceAsync;
import com.google.livingstories.client.Publisher;
import com.google.livingstories.client.lsp.AtomPopupWidget;
import com.google.livingstories.client.lsp.SourcePopupWidget;
import com.google.livingstories.client.lsp.views.atoms.PopupViewFactory;
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

  private final ContentRpcServiceAsync atomService = GWT.create(ContentRpcService.class);

  @UiField Image logo;
  @UiField LivingStoryManagementLinks managementLinks;
  @UiField SimplePanel canvas;
  @UiField GlassPanel glass;

  private Lightbox lightbox = new Lightbox();
  private AtomPopupWidget atomPopup = new AtomPopupWidget();
  private SourcePopupWidget sourcePopup = new SourcePopupWidget();
  
  public LivingStoryPage() {
    initWidget(uiBinder.createAndBindUi(this));

    logo.setUrl(Publisher.fromString(LivingStoryData.getLivingStoryPublisher()).getLogoPath());
    
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
  
  public void showLightbox(String title, BaseAtom atom) {
    lightbox.showItem(title, PopupViewFactory.createView(atom));
  }
  
  // pass in an int ID because JSNI doesn't work with longs.
  public void showLightboxForAtom(final String title, int atomId) {
    // TODO: Go through ClientCaches in some way rather than going right to
    // the atomService.
    atomService.getAtom((long)atomId, false, new AsyncCallback<BaseAtom>() {
      @Override
      public void onFailure(Throwable t) {}
      
      @Override
      public void onSuccess(final BaseAtom atom) {
        showLightbox(title, atom);
      }
    });
  }
  
  
  public void showAtomPopup(int atomId, final Element showRelativeTo) {
    // TODO: Go through ClientCaches in some way rather than going right to
    // the atomService.
    atomService.getAtom((long)atomId, false, new AsyncCallback<BaseAtom>() {
      public void onFailure(Throwable t) {}
      public void onSuccess(BaseAtom atom) {
        atomPopup.show(atom, showRelativeTo);
      }
    });
  }
  
  public void showSourcePopup(final String description, int atomId, final Element showRelativeTo) {
    if (atomId <= 0) {
      sourcePopup.show(description, null, showRelativeTo);
    } else {
      // TODO: Go through ClientCaches in some way rather than going right to
      // the atomService.
      atomService.getAtom((long)atomId, false, new AsyncCallback<BaseAtom>() {
        public void onFailure(Throwable t) {}
        public void onSuccess(BaseAtom atom) {
          sourcePopup.show(description, atom, showRelativeTo);
        }
      });      
    }
  }
  
  //pass in int ids because JSNI doesn't work with longs.
  public void showSlideshow(int[] atomIds) {
    List<Long> imageIds = new ArrayList<Long>();
    for (int atomId : atomIds) {
      imageIds.add((long)atomId);
    }
    atomService.getAtoms(imageIds, new AsyncCallback<List<BaseAtom>>() {
      @Override
      public void onFailure(Throwable t) {}
      
      @Override
      public void onSuccess(List<BaseAtom> atoms) {
        List<AssetAtom> images = new ArrayList<AssetAtom>(atoms.size());
        for (BaseAtom atom : atoms) {
          images.add((AssetAtom)atom);
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
    $wnd.showLightbox = function(title, atom) {
      instance.
          @com.google.livingstories.client.lsp.views.LivingStoryPage::showLightbox(Ljava/lang/String;Lcom/google/livingstories/client/BaseAtom;)
          .call(instance, title, atom);
    };
    $wnd.showLightboxForAtom = function(title, atomId) {
      instance.
          @com.google.livingstories.client.lsp.views.LivingStoryPage::showLightboxForAtom(Ljava/lang/String;I)
          .call(instance, title, atomId);
    };
    $wnd.showAtomPopup = function(atomId, showRelativeTo) {
      instance.
          @com.google.livingstories.client.lsp.views.LivingStoryPage::showAtomPopup(ILcom/google/gwt/dom/client/Element;)
          .call(instance, atomId, showRelativeTo);
    };
    $wnd.showSourcePopup = function(description, atomId, showRelativeTo) {
      instance.
          @com.google.livingstories.client.lsp.views.LivingStoryPage::showSourcePopup(Ljava/lang/String;ILcom/google/gwt/dom/client/Element;)
          .call(instance, description, atomId, showRelativeTo);
    };
    $wnd.showSlideshow = function(atomIds) {
      instance.
          @com.google.livingstories.client.lsp.views.LivingStoryPage::showSlideshow([I)
          .call(instance, atomIds);
    };
  }-*/;
}
