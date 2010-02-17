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
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;
import com.google.livingstories.client.BaseContentItem;
import com.google.livingstories.client.Importance;
import com.google.livingstories.client.lsp.event.BlockToggledEvent;
import com.google.livingstories.client.lsp.event.EventBus;
import com.google.livingstories.client.lsp.views.Resources;
import com.google.livingstories.client.lsp.views.ShareLinkWidget;
import com.google.livingstories.client.ui.ToggleDisclosurePanel;
import com.google.livingstories.client.ui.WindowScroll;
import com.google.livingstories.client.util.LivingStoryControls;

import java.util.Map;

/**
 * Footer to display in a container view.
 * Generally consists of a 'Read more' link and either some navigation links
 * that open the item and jump to an element, or a 'Share' link that lets the
 * user copy/paste a permalink for the entry. 
 */
public class ContainerStreamViewFooter extends Composite {
  private static ContainerStreamViewFooterUiBinder uiBinder =
      GWT.create(ContainerStreamViewFooterUiBinder.class);
  interface ContainerStreamViewFooterUiBinder extends UiBinder<Widget, ContainerStreamViewFooter> {
  }

  @UiField DeckPanel text;
  @UiField DeckPanel links;
  @UiField FlowPanel navLinks;
  
  private Long contentItemId;
  private Importance importance;
  private HandlerRegistration toggleEventHandler;

  public ContainerStreamViewFooter(BaseContentItem contentItem) {
    contentItemId = contentItem.getId();
    importance = contentItem.getImportance();
    
    initWidget(uiBinder.createAndBindUi(this));
    setOpen(false);
    
    toggleEventHandler = EventBus.INSTANCE.addHandler(BlockToggledEvent.TYPE,
        new BlockToggledEvent.Handler() {
          @Override
          public void onToggle(BlockToggledEvent e) {
            if (contentItemId.equals(e.getContentItemId())) {
              setOpen(e.isOpened());
            }
          }
        });
  }
  
  @UiFactory ShareLinkWidget makeShareLink() {
    return new ShareLinkWidget(contentItemId);
  }
  
  @Override
  protected void onUnload() {
    super.onUnload();
    if (toggleEventHandler != null) {
      toggleEventHandler.removeHandler();
      toggleEventHandler = null;
    }
  }

  public void addNavLinks(Map<String, Widget> typeStringToNavLinkTarget,
      final ToggleDisclosurePanel disclosurePanel) {
    for (Map.Entry<String, Widget> e : typeStringToNavLinkTarget.entrySet()) {
      InlineLabel link = new InlineLabel(e.getKey());
      link.setStylePrimaryName(Resources.INSTANCE.css().clickable());
      
      if (navLinks.getWidgetCount() > 1) {
        navLinks.add(new InlineHTML("&nbsp;|&nbsp;"));   // separator
      }
      navLinks.add(link);
      
      final Widget linkTarget = e.getValue();
      link.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          // Consume this event, so that it doesn't cause the disclosurePanel to register the
          // click
          event.stopPropagation();
          EventBus.INSTANCE.fireEvent(new BlockToggledEvent(true, contentItemId)
              .setOnFinish(new Command() {
                @Override
                public void execute() {
                  WindowScroll.scrollTo(linkTarget.getAbsoluteTop(),
                      new Command() {
                        @Override
                        public void execute() {
                          LivingStoryControls.repositionAnchoredPanel();
                        }
                      });                  
                }
              }));
        }
      });
    }
    if (!typeStringToNavLinkTarget.isEmpty()) {
      navLinks.removeStyleName(Resources.INSTANCE.css().hidden());
    }
  }
  
  public void setOpen(boolean opened) {
    if (opened) {
      setVisible(true);
    } else if (importance == Importance.LOW) {
      setVisible(false);
    }
    int showIndex = opened ? 1 : 0;
    text.showWidget(showIndex);
    links.showWidget(showIndex);
  }
  
}
