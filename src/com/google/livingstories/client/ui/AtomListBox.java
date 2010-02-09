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

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.livingstories.client.AtomType;
import com.google.livingstories.client.BaseAtom;
import com.google.livingstories.client.ContentRpcService;
import com.google.livingstories.client.ContentRpcServiceAsync;
import com.google.livingstories.client.util.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Widget that loads a list of atoms based on an lsp id, and has a built-in
 * filtering mechanism.
 * The underlying storage for the atoms is a LinkedHashMap. To get incrementally-added items
 * to appear at the top of the list, rather than at the bottom, the order in which items are
 * stored in the LinkedHashMap is actually opposite to the display order.
 */
public class AtomListBox extends Composite {
  /**
   * Create a remote service proxy to talk to the server-side content persisting service.
   */
  private final ContentRpcServiceAsync atomService = GWT.create(ContentRpcService.class);

  private ItemList<BaseAtom> itemList;
  protected EnumDropdown<AtomType> filter;
  private Map<Long, BaseAtom> loadedAtomsMap = new LinkedHashMap<Long, BaseAtom>();

  public AtomListBox(final boolean multiSelect) {
    filter = EnumDropdown.newInstance(AtomType.class, "All");
    filter.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        refresh();
      }
    });
    
    HorizontalPanel filterPanel = new HorizontalPanel();
    filterPanel.add(new Label("Filter:"));
    filterPanel.add(filter);
    
    itemList = new ItemList<BaseAtom>(multiSelect) {
      @Override
      public void loadItems() {
        if (!loadedAtomsMap.isEmpty()) {
          // loads the items in reverse order from how they're stored.
          List<BaseAtom> atoms = new ArrayList<BaseAtom>(loadedAtomsMap.values());
          Collections.reverse(atoms);

          for (BaseAtom atom : atoms) {
            if (testAtom(atom)) {
              String content = atom.getDisplayString();
              if (content.length() > Constants.CONTENT_SNIPPET_LENGTH) {
                content = content.substring(0, Constants.CONTENT_SNIPPET_LENGTH).concat("...");
              }
              addItem(content, String.valueOf(atom.getId()));
            }
          }
        }
      }
    };

    VerticalPanel contentPanel = new VerticalPanel();
    contentPanel.add(filterPanel);
    contentPanel.add(itemList);
    initWidget(contentPanel);
  }
  
  /**
   * Tests whether an atom should be included in the displayed list, based on the filter
   * setting.
   */
  protected boolean testAtom(BaseAtom atom) {
    AtomType type = filter.getSelectedConstant();
    return type == null || atom.getAtomType().equals(type) || isSelected(atom);
  }
  
  public void loadItemsForLsp(Long lspId) {
    itemList.setSelectedIndex(-1);
    loadedAtomsMap.clear();
    atomService.getAtomsForLsp(lspId, false, new AsyncCallback<List<BaseAtom>>() {
      @Override
      public void onFailure(Throwable caught) {
        itemList.clear();
        itemList.addItem("Callback failed, please try again");
      }
      @Override
      public void onSuccess(List<BaseAtom> result) {
        // Put result on loadedAtomsMap in reverse order. Can't use useful Google Collections
        // stuff for it, so:
        for (int i = result.size() - 1; i >= 0; i--) {
          BaseAtom atom = result.get(i);
          loadedAtomsMap.put(atom.getId(), atom);
        }
        refresh();
      }
    });
  }
  
  public void setVisibleItemCount(int count) {
    itemList.setVisibleItemCount(count);
  }

  public void addSelectionChangeHandler(ChangeHandler handler) {
    itemList.addChangeHandler(handler);
  }
  
  public void addFilterChangeHandler(ChangeHandler handler) {
    filter.addChangeHandler(handler);
  }
  
  public void clear() {
    itemList.clear();
  }
  
  public void refresh() {
    itemList.refresh();
  }
  
  public Long getSelectedAtomId() {
    return itemList.hasSelection() ? Long.valueOf(itemList.getSelectedItemValue()) : null;
  }
  
  public BaseAtom getSelectedAtom() {
    return itemList.hasSelection() ? loadedAtomsMap.get(getSelectedAtomId()) : null;
  }

  public List<String> getSelectedItems() {
    return itemList.getSelectedItems();
  }
  
  public List<String> getSelectedValues() {
    return itemList.getSelectedItemValues();
  }
  
  public void setSelectedAtomIds(Set<Long> selectedAtomIds) {
    for (int i = 0; i < itemList.getItemCount(); i++) {
      itemList.setItemSelected(i,
          selectedAtomIds.contains(Long.valueOf(itemList.getValue(i))));
    }
  }
  
  public List<BaseAtom> getSelectedAtoms() {
    List<BaseAtom> result = new ArrayList<BaseAtom>();
    for (String atomId : itemList.getSelectedItemValues()) {
      result.add(loadedAtomsMap.get(Long.valueOf(atomId)));
    }
    return result;
  }
  
  public Map<Long, BaseAtom> getLoadedAtomsMap() {
    return loadedAtomsMap;
  }
  
  public void addOrUpdateAtom(BaseAtom atom) {
    boolean isAdd = !loadedAtomsMap.containsKey(atom.getId());
    loadedAtomsMap.put(atom.getId(), atom);
    // Change the filter if necessary so that the added/updated atom
    // is visible and selectable.
    if (filter.getSelectedConstant() != null
        && !atom.getAtomType().equals(filter.getSelectedConstant())) {
      filter.selectConstant(null);
    }
    itemList.refresh();
    itemList.selectItemWithValue(String.valueOf(atom.getId()));
    if (isAdd) {
      itemList.fireEvent(new ChangeEvent(){});
    }
  }
  
  public void addAtoms(List<BaseAtom> atoms) {
    // Add the new atoms to loadedAtomsMap in reverse order to how they were specified.
    for (int i = atoms.size() - 1; i >= 0; i--) {
      BaseAtom atom = atoms.get(i);
      loadedAtomsMap.put(atom.getId(), atom);
    }
    itemList.refresh();
  }
  
  public void removeAtom(long atomId) {
    itemList.removeItemWithValue(String.valueOf(atomId));
    loadedAtomsMap.remove(atomId);
  }
  
  private boolean isSelected(BaseAtom atom) {
    if (itemList.isMultipleSelect()) {
      for (String value : itemList.getSelectedItemValues()) {
        if (Long.valueOf(value).equals(atom.getId())) {
          return true;
        }
      }
      return false;
    } else {
      return itemList.getSelectedItemValue() != null
          && Long.valueOf(itemList.getSelectedItemValue()).equals(atom.getId());
    }
  }
}
