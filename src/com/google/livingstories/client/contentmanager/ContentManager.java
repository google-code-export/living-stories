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

package com.google.livingstories.client.contentmanager;

import com.google.gwt.ajaxloader.client.AjaxLoader;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.logical.shared.BeforeSelectionEvent;
import com.google.gwt.event.logical.shared.BeforeSelectionHandler;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Entry point for the content manager to create and update documents and updates. 
 */
public class ContentManager implements EntryPoint {
  private TabPanel tabs;
  private int selectedIndex;
  
  private Widget livingStoryManager = new LivingStoryManager();
  private Widget themeManager = new ThemeManager();
  private Widget atomManager = new AtomManager();
  private Widget importManager = new ImportManager();
  
  public void onModuleLoad() {
    AjaxLoader.init();
    
    tabs = new TabPanel();
    tabs.setWidth("100%");
    
    tabs.add(atomManager, "Manage Content");
    tabs.add(livingStoryManager, "Manage Living Stories");
    tabs.add(themeManager, "Manage Themes");
    tabs.add(importManager, "Import/Export Data");
    
    tabs.selectTab(0);
    selectedIndex = 0;

    tabs.addBeforeSelectionHandler(new BeforeSelectionHandler<Integer>() {
      public void onBeforeSelection(BeforeSelectionEvent<Integer> event) {
        // TODO: this work is extra if the user didn't actually change
        // anything on the source tab.
        ManagerPane previousTab = (ManagerPane) tabs.getWidget(selectedIndex);
        boolean livingStoriesEdited = (previousTab == livingStoryManager);
        boolean atomsEdited = (previousTab == atomManager);
        for (int i = 0; i < tabs.getWidgetCount(); i++) {
          ManagerPane pane = (ManagerPane) tabs.getWidget(i);
          if (pane != previousTab) {
            if (livingStoriesEdited) {
              pane.onLivingStoriesChanged();
            }
            if (atomsEdited) {
              pane.onAtomsChanged();
            }

            pane.onShow();
          }
        }
        selectedIndex = event.getItem();
      }
    });

    RootPanel.get().add(tabs);
  }
}
