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
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.livingstories.client.ContentItemTypesBundle;
import com.google.livingstories.client.lsp.event.EventBus;
import com.google.livingstories.client.lsp.event.ThemeSelectedEvent;
import com.google.livingstories.client.ui.ButtonListWidget;
import com.google.livingstories.client.util.AnalyticsUtil;
import com.google.livingstories.client.util.LivingStoryData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Displays a vertical list of theme names.  Positioned correctly, this can be made
 * to look like tabs to a pane full of updates, filtered by the appropriate theme.
 */
public class ThemeListWidget extends Composite {
  private ButtonListWidget contentPanel;
  private Map<Long, Integer> themeIdToThemeRowMap = new HashMap<Long, Integer>();
  private Long selectedThemeId = null;
  private String ALL_THEMES = LspMessageHolder.consts.allThemes();
  
  public ThemeListWidget() {
    contentPanel = new ButtonListWidget();
    DOM.setStyleAttribute(contentPanel.getElement(), "marginBottom", "10px");
    initWidget(contentPanel);
    setVisible(false);
  }
  
  public void load(Map<Long, ContentItemTypesBundle> themesMap) {
    if (themesMap.size() <= 1) {
      // there will generally be 1 entry in the map which contains a ContentItemTypesBundle
      // for the story overall; all themes
      setVisible(false);
    } else {
      setVisible(true);
      int rows = 0;
      // The themes map is ordered arbitrarily. (Previous implementation used a LinkedHashmap,
      // but this breaks in a production deployment.) Sort it based on the themeName (which is
      // "" for "All coverage", and thus will sort to the front, as we want.)
      List<Map.Entry<Long, ContentItemTypesBundle>> entries =
          new ArrayList<Map.Entry<Long, ContentItemTypesBundle>>(themesMap.entrySet());
      Collections.sort(entries, new Comparator<Map.Entry<Long, ContentItemTypesBundle>>() {
        @Override
        public int compare(Entry<Long, ContentItemTypesBundle> o1,
            Entry<Long, ContentItemTypesBundle> o2) {
          return o1.getValue().themeName.compareTo(o2.getValue().themeName);
        }
      });
          
      for (Map.Entry<Long, ContentItemTypesBundle> entry : entries) {
        Long themeId = entry.getKey();
        String displayName = (themeId == null) ? ALL_THEMES : entry.getValue().themeName;
        String analyticsName = (themeId == null) ? "All" : displayName;
        Widget themeRow = createThemeBlock(displayName);
        contentPanel.addItem(themeRow, new ThemeClickHandler(themeId, analyticsName), false);
        themeIdToThemeRowMap.put(themeId, rows);
        rows++;
      }
    }
  }

  public Long getSelectedThemeId() {
    return selectedThemeId;
  }
  
  public void setSelectedThemeId(Long themeId) {
    selectedThemeId = themeId;
    if (isVisible()) {
      Integer themeRow = themeIdToThemeRowMap.get(themeId);
      contentPanel.selectItem(themeRow);
    }
    EventBus.INSTANCE.fireEvent(new ThemeSelectedEvent(themeId));
  }
  
  private Widget createThemeBlock(String name) {
    SimplePanel panel = new SimplePanel();
    Label themeName = new Label(name);
    panel.add(themeName);
    return panel;
  }
  
  private class ThemeClickHandler implements ClickHandler {
    private Long themeId;
    private String name;
    
    public ThemeClickHandler(Long themeId, String name) {
      this.themeId = themeId;
      this.name = name;
    }
    
    public void onClick(ClickEvent e) {
      setSelectedThemeId(themeId);
      AnalyticsUtil.trackThemeClick(LivingStoryData.getLivingStoryUrl(), name);
    }
  }
}
