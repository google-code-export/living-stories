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

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.livingstories.client.ClientCaches;
import com.google.livingstories.client.PlayerContentItem;

import java.util.List;

public class ImportantPlayersList extends Composite {
  private static final int MAX_PLAYER_COUNT = 5;
  
  private FlowPanel contentPanel;
  
  public ImportantPlayersList() {
    contentPanel = new FlowPanel();
    contentPanel.add(getHeader());
    contentPanel.setWidth("250px");
    
    initWidget(contentPanel);
  }

  public void load() {
    ClientCaches.getImportantPlayers(new ImportantPlayersCallback());
  }
  
  private Widget getHeader() {
    Label header = new Label("People");
    header.setStylePrimaryName("sectionHeader");
    return header;
  }
  
  private class ImportantPlayersCallback implements AsyncCallback<List<PlayerContentItem>> {
    @Override
    public void onFailure(Throwable caught) {
      contentPanel.add(new Label("Failed to load player list"));
    }
    
    @Override
    public void onSuccess(List<PlayerContentItem> result) {
      if (result.isEmpty()) {
        contentPanel.setVisible(false);
      } else {
        for (int i = 0; i < result.size(); i++) {
          if (i == MAX_PLAYER_COUNT) {
            break;
          }
          Widget playerElement = result.get(i).renderPreview();
          playerElement.setStylePrimaryName("rightColumnElement");
          contentPanel.add(playerElement);
        }
      }
    }
  }
}
