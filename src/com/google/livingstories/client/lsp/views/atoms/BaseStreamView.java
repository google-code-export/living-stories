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

import com.google.livingstories.client.AssetAtom;
import com.google.livingstories.client.BaseAtom;
import com.google.livingstories.client.Importance;
import com.google.livingstories.client.PlayerAtom;
import com.google.livingstories.client.QuoteAtom;
import com.google.livingstories.client.atomlist.AtomListElement;
import com.google.livingstories.client.lsp.views.DateTimeRangeWidget;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import java.util.Set;

/**
 * Class that renders all element types (other than event and narrative) in the stream view.
 * Mostly reuses existing classes that render previews of each content type. 
 */
public class BaseStreamView extends AtomListElement {

  private static BaseStreamViewUiBinder uiBinder = GWT.create(BaseStreamViewUiBinder.class);

  interface BaseStreamViewUiBinder extends UiBinder<Widget, BaseStreamView> {
  }

  @UiField DateTimeRangeWidget timestamp;
  @UiField SimplePanel content;

  private BaseAtom atom;
  
  public BaseStreamView(BaseAtom atom) {
    this.atom = atom;
    
    initWidget(uiBinder.createAndBindUi(this));

    timestamp.setDateTime(atom.getTimestamp(), null);

    switch (atom.getAtomType()) {
      case BACKGROUND:
      case DATA:
      case REACTION:
        content.setWidget(new BaseAtomPreview(atom));
        break;
      case QUOTE:
        content.setWidget(new QuoteAtomView((QuoteAtom) atom).hideHeader());
        break;
      case PLAYER:
        content.setWidget(new BasePlayerPreview((PlayerAtom) atom).hideHeader());
        break;
      case ASSET:
        AssetAtom asset = (AssetAtom) atom;
        switch (asset.getAssetType()) {
          case LINK:
            content.setWidget(new LinkAssetPreview(asset).hideHeader());
            break;
          case DOCUMENT:
            content.setWidget(new DocumentAssetPreview(asset).hideHeader());
            break;
          case AUDIO:
            content.setWidget(new AudioAssetView(asset).hideHeader());
            break;
          case INTERACTIVE:
            content.setWidget(new GraphicAssetPreview(asset).hideHeader());
            break;
          case VIDEO:
            content.setWidget(new VideoAssetPreview(asset).hideHeader());
            break;
          case IMAGE:
            content.setWidget(new ImageAssetPreview(asset).hideHeader());
            break;
          default:
            throw new IllegalArgumentException("Asset type " + asset.getAssetType()
                + " does not have a stream view defined.");
        }
        break;
      default:
        throw new IllegalArgumentException("Atom type " + atom.getAtomType()
            + " does not have a stream view defined.");        
    }
  }

  @Override
  public Long getId() {
    return atom.getId();
  }
  
  @Override
  public BaseAtom getAtom() {
    return atom;
  }
  
  @Override
  public Set<Long> getThemeIds() {
    return atom.getThemeIds();
  }

  @Override
  public Importance getImportance() {
    return atom.getImportance();
  }

  @Override
  public String getDateString() {
    return timestamp.getDateString();
  }
  
  @Override
  public void setTimeVisible(boolean visible) {
    timestamp.setTimeVisible(visible);
  }
  
  @Override
  public boolean setExpansion(boolean expand) {
    // Do nothing
    return false;
  }
}
