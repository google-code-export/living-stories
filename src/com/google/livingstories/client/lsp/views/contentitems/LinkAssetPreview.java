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
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.livingstories.client.AssetContentItem;

/**
 * Renders a link asset with a 'Resource' header.
 */
public class LinkAssetPreview extends Composite {
  private static AssetPreviewUiBinder uiBinder = GWT.create(AssetPreviewUiBinder.class);
  @UiTemplate("BaseAssetPreview.ui.xml")
  interface AssetPreviewUiBinder extends UiBinder<Widget, LinkAssetPreview> {
  }

  private static final String LINK_ICON = "/images/link_icon.gif";

  @UiField Label header;
  @UiField SimplePanel iconPanel;
  @UiField SimplePanel content;

  public LinkAssetPreview(AssetContentItem contentItem) {
    initWidget(uiBinder.createAndBindUi(this));
    header.setText("Resource");
    iconPanel.add(new Image(LINK_ICON));
    content.add(new HTML(contentItem.getContent()));
  }
  
  public LinkAssetPreview hideHeader() {
    header.setVisible(false);
    return this;
  }
}
