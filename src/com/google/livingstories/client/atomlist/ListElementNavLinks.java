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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;
import com.google.livingstories.client.lsp.LspMessageHolder;
import com.google.livingstories.client.ui.PartialDisclosurePanel;

import java.util.Map;

/**
 * Widget to show a list of content that is hidden for an element in the list. Eg.
 * "Background | Articles | Multimedia | Resources".
 */
public class ListElementNavLinks extends Composite {
  private FlowPanel navLinkPanel;
  private ClickHandler navLinkClickHandler;
  private static String JUMP_TO = LspMessageHolder.consts.jumpTo();
  
  public ListElementNavLinks(final PartialDisclosurePanel disclosurePanel,
      Map<String, Widget> typeStringToNavLinkTarget, boolean alreadySeen,
      ClickHandler navLinkClickHandler) {
    super();
    navLinkPanel = new FlowPanel();
    this.navLinkClickHandler = navLinkClickHandler;
    populate(disclosurePanel, typeStringToNavLinkTarget, alreadySeen);
    initWidget(navLinkPanel);
  }
  
  public void populate(final PartialDisclosurePanel disclosurePanel,
      Map<String, Widget> typeStringToNavLinkTarget, boolean alreadySeen) {
    if (!typeStringToNavLinkTarget.isEmpty()) {
      InlineHTML jumpToLabel = new InlineHTML(JUMP_TO + "&nbsp;");
      jumpToLabel.addStyleName("greyFont");
      navLinkPanel.add(jumpToLabel);
    }
    for (Map.Entry<String, Widget> e : typeStringToNavLinkTarget.entrySet()) {
      InlineLabel link = new InlineLabel(e.getKey());
      link.setStylePrimaryName(alreadySeen ? "secondaryLink" : "primaryLink");
      
      if (navLinkPanel.getWidgetCount() > 1) {
        navLinkPanel.add(new InlineHTML("&nbsp;|&nbsp;"));   // separator
      }
      navLinkPanel.add(link);
      
      final Widget linkTarget = e.getValue();
      link.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          // Consume this event, so that it doesn't cause the disclosurePanel to register the
          // click
          event.stopPropagation();
          disclosurePanel.scrollToContainedWidget(linkTarget);
          navLinkClickHandler.onClick(event);
        }
      });
    }
  }
}
