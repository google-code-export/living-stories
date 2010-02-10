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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.livingstories.client.AssetAtom;
import com.google.livingstories.client.AssetType;
import com.google.livingstories.client.AtomType;
import com.google.livingstories.client.BaseAtom;
import com.google.livingstories.client.Importance;
import com.google.livingstories.client.NarrativeAtom;
import com.google.livingstories.client.lsp.LspMessageHolder;
import com.google.livingstories.client.lsp.event.EventBus;
import com.google.livingstories.client.lsp.event.NarrativeLinkClickedEvent;
import com.google.livingstories.client.lsp.views.Resources;
import com.google.livingstories.client.util.AtomComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A 'container' view is a view that renders other atom views within it.
 * Event stream views and narrative stream views are examples of container
 * views.  This class has common code used in containers.
 */
public abstract class BaseContainerView<T extends BaseAtom> extends Composite {
  protected T atom;
  protected Map<AtomType, List<BaseAtom>> linkedAtomsByType;
  protected Map<AssetType, List<AssetAtom>> linkedAssetsByType;
  
  private boolean hasImportantAssets = false;
  
  public BaseContainerView(T atom, Map<AtomType, List<BaseAtom>> linkedAtomsByType) {
    this.atom = atom;
    this.linkedAtomsByType = linkedAtomsByType;

    // Sort atoms within their categories by their importance followed by their timestamp
    for (AtomType type : linkedAtomsByType.keySet()) {
      Collections.sort(linkedAtomsByType.get(type), new AtomComparator());
    }

    List<BaseAtom> linkedAssets = linkedAtomsByType.get(AtomType.ASSET);
    if (!linkedAssets.isEmpty()) {
      // Determine if this atom has important assets.
      // Since the asset list is sorted by importance, we can just check if the first
      // asset is important.
      hasImportantAssets = linkedAssets.get(0).getImportance() == Importance.HIGH;
    }
    
    // Create a map of linked asset atoms to their type.  The lists of assets
    // in this map will also naturally be sorted by importance and timestamp,
    // since the source list is already sorted.
    linkedAssetsByType = new HashMap<AssetType, List<AssetAtom>>();
    for (AssetType type : AssetType.values()) {
      linkedAssetsByType.put(type, new ArrayList<AssetAtom>());
    }
    for (BaseAtom linkedAtom : linkedAssets) {
      AssetAtom asset = (AssetAtom) linkedAtom;
      linkedAssetsByType.get(asset.getAssetType()).add(asset);
    }
    
    bind();
  }

  protected abstract void bind();

  protected T getAtom() {
    return atom;
  }
  
  protected Widget createNarrativeLinks() {
    List<BaseAtom> linkedNarratives = linkedAtomsByType.get(AtomType.NARRATIVE);
    if (linkedNarratives == null || linkedNarratives.isEmpty()) {
      return null;
    } else {
      FlowPanel panel = new FlowPanel();
      panel.setWidth("100%");
      DOM.setStyleAttribute(panel.getElement(), "margin", "10px 0");
      Label relatedLabel = new Label(LspMessageHolder.consts.related());
      relatedLabel.setStylePrimaryName("atomHeader");
      panel.add(relatedLabel);
      
      for (final BaseAtom linkedAtom : linkedNarratives) {
        NarrativeAtom narrative = (NarrativeAtom)linkedAtom;
        
        FlowPanel row = new FlowPanel();
        InlineLabel narrativeHeadline = new InlineLabel(narrative.getHeadline());
        narrativeHeadline.addStyleName(Resources.INSTANCE.css().clickable());
        InlineHTML narrativeType = new InlineHTML("&nbsp;-&nbsp;" 
            + narrative.getNarrativeType().toString());
        narrativeType.addStyleName("greyFont");
        row.add(narrativeHeadline);
        row.add(narrativeType);
        
        FocusPanel clickableRow = new FocusPanel();
        clickableRow.add(row);
        clickableRow.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent e) {
            EventBus.INSTANCE.fireEvent(
                new NarrativeLinkClickedEvent(atom.getId(), linkedAtom.getId()));
          }
        });
        panel.add(clickableRow);
      }
      return panel;
    }
  }
  
  public boolean hasImportantAssets() {
    return hasImportantAssets;
  }
}
