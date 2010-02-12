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

package com.google.livingstories.client.ui;

import com.google.gwt.core.client.JavaScriptException;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.livingstories.client.util.LivingStoryData;

/**
 * Widget that displays the friend connect comment box.
 * The javascript in 'loadCommentsBoxInternal' is just copied
 * from the generated snippet in friend connect.
 */
public class FCCommentsBox extends Composite {
  private SimplePanel container;
  private boolean loaded = false;
  
  public FCCommentsBox() {
    container = new SimplePanel();
    initWidget(container);
  }
  
  /**
   * Load the comments box with the given ID.
   * 
   * You probably want to schedule calls to this with a timer, since
   * the friend connect code uses getElementById internally to get the
   * container div, and that won't be available until the page fully loads.
   */
  public void loadCommentsBox(String instanceId) {
    try {
      if (!loaded) {
        String siteId = LivingStoryData.getFriendConnectSiteId();
        
        if (!siteId.isEmpty()) {
          container.getElement().setId("FCCommentsBox" + instanceId);
          loadCommentsBoxInternal(siteId, instanceId);
          loaded = true;
        }
      }
    } catch (JavaScriptException e) {
      // Do nothing
    }
  }
  
  private native void loadCommentsBoxInternal(
      String friendConnectSiteId, String instanceId) /*-{
    var skin = {};
    skin['BORDER_COLOR'] = '#cccccc';
    skin['ENDCAP_BG_COLOR'] = '#e0ecff';
    skin['ENDCAP_TEXT_COLOR'] = '#333333';
    skin['ENDCAP_LINK_COLOR'] = '#0000cc';
    skin['ALTERNATE_BG_COLOR'] = '#ffffff';
    skin['CONTENT_BG_COLOR'] = '#ffffff';
    skin['CONTENT_LINK_COLOR'] = '#0000cc';
    skin['CONTENT_TEXT_COLOR'] = '#333333';
    skin['CONTENT_SECONDARY_LINK_COLOR'] = '#7777cc';
    skin['CONTENT_SECONDARY_TEXT_COLOR'] = '#666666';
    skin['CONTENT_HEADLINE_COLOR'] = '#333333';
    skin['DEFAULT_COMMENT_TEXT'] = '- add your comment here -';
    skin['HEADER_TEXT'] = 'Comments';
    skin['POSTS_PER_PAGE'] = '5';
    $wnd.google.friendconnect.container.setParentUrl('/');
    $wnd.google.friendconnect.container.renderWallGadget(
        { id: "FCCommentsBox" + instanceId,
          site: friendConnectSiteId,
          'view-params': {
            "disableMinMax": "false",
            "scope": "ID",
            "features": "video,comment",
            "docId": instanceId,
            "startMaximized": "false"
          }
        },
        skin);
  }-*/;
}
