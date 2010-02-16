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

package com.google.livingstories.client.lsp;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;
import com.google.livingstories.client.BaseContentItem;
import com.google.livingstories.client.ClientCaches;
import com.google.livingstories.client.PlayerContentItem;
import com.google.livingstories.client.util.HistoryManager;
import com.google.livingstories.client.util.LivingStoryControls;
import com.google.livingstories.client.util.HistoryManager.HistoryPages;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Widget to create a byline from the contributors to a content item. Each of the names are linked.
 * Clicking on them takes you to the player page that lists all the contributions of that author.
 * This class also now includes a facility for prepending a widget that should be presented
 * alongside the BylineWidget, in a visually-consistent manner.
 */
public class BylineWidget extends Composite {
  private static final int MANUAL_BREAK_THRESHOLD = 8;
  
  private boolean secondaryLinkStyle;
  private FlowPanel container;
  private String leadin;
  private InlineHTML manualBreak;
  private int navLinkCount = 0;
  private int prependedWidgetCount = 0;
  private int contributorCount = 0;
  
  public BylineWidget(BaseContentItem contentItem) {
    this(contentItem, true);
  }
  
  public BylineWidget(BaseContentItem contentItem, boolean secondaryLinkStyle) {
    super();
    
    this.secondaryLinkStyle = secondaryLinkStyle;
    
    manualBreak = new InlineHTML("<br>");
    
    container = new FlowPanel();
    container.addStyleName("contributorList");
    container.add(manualBreak);
    setManualBreakVisibility();
    
    initWidget(container);
  
    leadin = contentItem.getBylineLeadin();
    ClientCaches.getContributorsById(contentItem.getContributorIds(), new ContributorsCallback());
  }
  
  public void setNavLinks(List<Widget> navLinks) {
    // remove the old prepended links first, if any.
    for (int i = prependedWidgetCount - 1; i >= 0; i++) {
      container.remove(i);
    }

    // now add all the new links & separators:
    prependedWidgetCount = 0;
    for (Widget widget : navLinks) {
      container.insert(widget, prependedWidgetCount++);
      container.insert(new InlineHTML("&nbsp;| "), prependedWidgetCount++);   // separator
    }
    navLinkCount = navLinks.size();
    setManualBreakVisibility();
  }
  
  private void setManualBreakVisibility() {
    manualBreak.setVisible(navLinkCount > 0 && contributorCount > 0
        && navLinkCount + contributorCount > MANUAL_BREAK_THRESHOLD);
  }
  
  private class ContributorsCallback implements AsyncCallback<List<PlayerContentItem>> {
    public void onFailure(Throwable t) {
      // Do nothing
    }
    
    public void onSuccess(List<PlayerContentItem> contributors) {
      if (!contributors.isEmpty()) {
        HTML leadinLabel = new InlineHTML(leadin + "&nbsp;");
        leadinLabel.addStyleName("greyFont");
        container.add(leadinLabel);
      }

      boolean notFirst = false;
      for (final PlayerContentItem contributor : contributors) {
        if (notFirst) {
          container.add(new InlineHTML(", "));
        }
        InlineLabel contributorLabel = new InlineLabel(contributor.getName());
        contributorLabel.addStyleName(secondaryLinkStyle ? "secondaryLink" : "primaryLink");
        contributorLabel.addStyleName("nowrap");
        contributorLabel.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent e) {
            // TODO: this isn't great, but it works.
            // The right way to do this would probably be to store all content items in the
            // ClientCache and fire a history change event here to load the page, instead
            // of trying to hack around the history system.
            Page page = (Page) contributor.renderContent(new HashSet<Long>());
            HistoryManager.newToken(page, HistoryPages.PLAYER, String.valueOf(contributor.getId()));
            LivingStoryControls.goToPage(page);
          }
        });
        container.add(contributorLabel);
        notFirst = true;
      }
      contributorCount = contributors.size();
      setManualBreakVisibility();
    }
  }
  
  /**
   * Conditionally creates a new byline widget to a panel for the contributors to a content item.
   * Only does this if there are contributors, and the contributors are _not the same_ as the
   * containing context's contributor set. (Partial overlap is okay.)
   * @param contentItem The content item that the byline widget is based on
   * @param containingContributorIds The contributorIds of the content eventBlock, if any.
   *   If this argument is null, the method is a no-op; if this argument is an empty
   *   set, this will serve to always add a byline widget (provided there are _some_ contributors).
   * @return the newly-created byline widget, or null if one was not created.
   */
  public static BylineWidget makeContextSensitive(
      BaseContentItem contentItem, Set<Long> containingContributorIds) {
    Set<Long> contentItemContributorIds = contentItem.getContributorIds();
    if (containingContributorIds != null && !contentItemContributorIds.isEmpty()
        && !contentItemContributorIds.equals(containingContributorIds)) {
      return new BylineWidget(contentItem);
    } else {
      return null;
    }
  }
}
