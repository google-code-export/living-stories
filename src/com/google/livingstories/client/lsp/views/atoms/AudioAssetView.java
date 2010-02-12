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

package com.google.livingstories.client.lsp.views.atoms;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.livingstories.client.AssetAtom;
import com.google.livingstories.client.util.GlobalUtil;

/**
 * Renders an audio asset inline, without popping up a lightbox to show the full content.
 * This is useful since audio assets will tend to either be small flash players
 * or links to audio files.
 */
public class AudioAssetView extends Composite {
  private static AudioAssetViewUiBinder uiBinder = GWT.create(AudioAssetViewUiBinder.class);
  interface AudioAssetViewUiBinder extends UiBinder<Widget, AudioAssetView> {
  }

  private static final String AUDIO_ICON = "/images/audio_icon.gif";

  @UiField Label header;
  @UiField SimplePanel iconPanel;
  @UiField HTML content;
  @UiField HTML caption;

  public AudioAssetView(AssetAtom atom) {
    initWidget(uiBinder.createAndBindUi(this));
    iconPanel.add(new Image(AUDIO_ICON));
    content.setHTML(atom.getContent());
    String captionText = atom.getCaption();
    if (!GlobalUtil.isContentEmpty(captionText)) {
      caption.setHTML(captionText);
    }
  }
  
  public AudioAssetView hideHeader() {
    header.setVisible(false);
    return this;
  }
}
