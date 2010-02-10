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

package com.google.livingstories.client.atomlist;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.livingstories.client.BaseAtom;
import com.google.livingstories.client.lsp.LspMessageHolder;
import com.google.livingstories.client.ui.WindowScroll;
import com.google.livingstories.client.util.LivingStoryControls;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

/**
 * Widget that displays a list of {@link AtomListElement} objects in a vertical list. There
 * is also a method for reversing whether the items are shown in chronological or
 * reverse-chronological order. The atoms know how to render their content and their day and time.
 */
public class AtomList extends Composite {
  private Map<BaseAtom, AtomListRow> currentAtomToRowMap;
  private Set<Long> atomIdsInPanel;
  private Map<Long, BaseAtom> idToAtomMap;
  
  private Label statusLabel;
  private VerticalPanel contentPanel;
  
  private static final String NO_ITEMS_TEXT = LspMessageHolder.consts.atomListNoItemsText();
  
  private boolean includeAtomName;
  private AtomClickHandler handler;
  private boolean noHistoryOnToggle;
  
  private boolean chronological = false;
  
  private AtomList(boolean includeAtomName, AtomClickHandler handler, boolean noHistoryOnToggle) {
    super();
    this.includeAtomName = includeAtomName;
    this.handler = handler;
    this.noHistoryOnToggle = noHistoryOnToggle;
    atomIdsInPanel = new HashSet<Long>();
    currentAtomToRowMap = new TreeMap<BaseAtom, AtomListRow>(new Comparator<BaseAtom>() {
      public int compare(BaseAtom a1, BaseAtom a2) {
        int ret = a1.getDateSortKey().compareTo(a2.getDateSortKey());
        return ret == 0 ? ((int) Math.signum(a1.getId() - a2.getId())) : ret;
      }
    });
    statusLabel = new Label("");
    statusLabel.setStylePrimaryName("greyFont");
    
    contentPanel = new VerticalPanel();
    contentPanel.setWidth("100%");
    
    VerticalPanel container = new VerticalPanel();
    container.setWidth("100%");
    container.add(statusLabel);
    container.add(contentPanel);
    
    initWidget(container);
  }
  
  public static AtomList create(boolean includeAtomName) {
    return new AtomList(includeAtomName, null, false);
  }
  
  public static AtomList createWithHandler(boolean includeAtomName, AtomClickHandler handler) {
    return new AtomList(includeAtomName, handler, false);
  }
  
  public static AtomList createNoHistoryOnToggle(boolean includeAtomName) {
    return new AtomList(includeAtomName, null, true);
  }
  
  /**
   * Sets the atom click handler object, separately from the construction-time handler.
   * Should only be called on an AtomList that is presently empty
   */
  public void setAtomClickHandler(AtomClickHandler handler) {
    assert currentAtomToRowMap.isEmpty();
    this.handler = handler;
  }

  /**
   * @param atoms atoms to create the AtomList for
   * @param idToAtomMap Map from atom ids to the atom objects that should contain entries for 
   * at least the atoms linked from all the atoms in the list passed in the first parameter, but
   * can contain more. If the map is null, empty or doesn't contain the linked atoms, the complex
   * atoms will be displayed in a degenerate form without their linked atoms.
   */
  public void load(List<BaseAtom> atoms, Map<Long, BaseAtom> idToAtomMap) {
    clear();    
    appendAtoms(atoms, idToAtomMap);
  }
    
  /**
   * @param atoms atoms that should be added to the AtomList
   * @param idToAtomMap a complete idToAtomMap for all atoms relevant to the current reporting.
   */
  public void appendAtoms(List<BaseAtom> atoms, Map<Long, BaseAtom> idToAtomMap) {
    this.idToAtomMap = idToAtomMap;
    
    for (BaseAtom atom : atoms) {
      addAtom(atom);
    }

    if (currentAtomToRowMap.isEmpty()) {
      statusLabel.setText(NO_ITEMS_TEXT);
    } else {
      statusLabel.setText("");
    }
    
    updateContentPanel();
  }

  /**
   * Updates the content panel contents based on the current contents of currentAtomToRowMap.
   */
  private void updateContentPanel() {
    AtomListElement previousElement = null;
        
    int insertionPosition = 0;
    for (BaseAtom atom : getAtomList()) {
      AtomListRow row = currentAtomToRowMap.get(atom);
      if (!atomIdsInPanel.contains(atom.getId())) {
        // the appropriate widget is not in the contentPanel. Add it!
        contentPanel.insert(row.elementWidget, insertionPosition);
        atomIdsInPanel.add(atom.getId());
      }
      
      setTimeVisibility(row.atomListElement, previousElement);
      previousElement = row.atomListElement;
      insertionPosition++;
    }
  }
  
  /**
   * Adds a new atom to currentAtomToRowMap, if it's not already there.
   * Doesn't alter atomIdsInPanel.
   */
  public void addAtom(BaseAtom atom) {
    if (!currentAtomToRowMap.containsKey(atom)) {
      AtomListElement element = AtomListElementFactory.createAtomListElement(
          atom, idToAtomMap, handler, noHistoryOnToggle);
      currentAtomToRowMap.put(atom, new AtomListRow(element, element.render(includeAtomName)));
    }
  }
  
  /**
   * Removes an atom from the list if it is currently present.
   */
  public void removeAtom(Long atomId) {
    for (Entry<BaseAtom, AtomListRow> entry : currentAtomToRowMap.entrySet()) {
      if (entry.getKey().getId() == atomId) {
        contentPanel.remove(entry.getValue().elementWidget);
        currentAtomToRowMap.remove(entry.getKey());
        atomIdsInPanel.remove(entry.getKey().getId());
        break;
      }
    }
  }
  
  public Set<BaseAtom> getAtomSet() {
    return currentAtomToRowMap.keySet();
  }
  
  public int getAtomCount() {
    return currentAtomToRowMap.size();
  }
  
  public List<BaseAtom> getAtomList() {
    // This would be simpler if GWT emulated TreeMap.descendingKeySet().
    List<BaseAtom> ret = new ArrayList<BaseAtom>(currentAtomToRowMap.keySet());
    if (!chronological) {
      Collections.reverse(ret);
    }
    return ret;
  }
  
  public void clear() {
    contentPanel.clear();
    currentAtomToRowMap.clear();
    atomIdsInPanel.clear();
  }
  
  public void adjustTimeOrdering(boolean chronological) {
    if (this.chronological == chronological) {
      return;
    }
    this.chronological = chronological;
    contentPanel.clear();
    for (AtomListRow row : currentAtomToRowMap.values()) {
      if (chronological) {
        contentPanel.add(row.elementWidget);
      } else {
        contentPanel.insert(row.elementWidget, 0);
      }
    }
  }
  
  public boolean getChronological() {
    return chronological;
  }
  
  /**
   * Opens atoms from the given set that are present in the current view. If only a single atom
   * is passed in, "jumps to it" i.e. scrolls it into view and opens its contents. All other
   * previously expanded atoms are collapsed.
   * @return true if all requested atoms were opened; false if one or more could not be found
   */
  public boolean openElements(Set<Long> atomIds) {
    /* Implementation notes:
     * - To avoid unintended scrolling to elements that this routine closes, we suppress extra
     *   expansion actions. (We've put matching code into ComplexAtomListElement.)
     * - To avoid scrolling to the position of a single target element that then changes
     *   due to contractions elsewhere in the list, we save the relevant AtomListRow in the
     *   loop, but actually act on this knowledge only after the loop is done. */
    int countFound = 0;
    AtomListRow singleRowToScrollTo = null;

    for (Entry<BaseAtom, AtomListRow> entry : currentAtomToRowMap.entrySet()) {
      AtomListRow row = entry.getValue();
      AtomListElement listElement = row.atomListElement;

      if (atomIds.contains(entry.getKey().getId())) {
        listElement.setExpansion(true);
        if (atomIds.size() == 1) {
          singleRowToScrollTo = row;
        }
        countFound++;
      } else {
        listElement.setExpansion(false);
      }
    }
    
    if (singleRowToScrollTo != null) {
      WindowScroll.scrollTo(singleRowToScrollTo.elementWidget.getAbsoluteTop(),
          new Command() {
            @Override
            public void execute() {
              LivingStoryControls.repositionAnchoredPanel();
            }        
          });
    }
    
    return countFound == atomIds.size();
  }
  
  private void setTimeVisibility(AtomListElement currentElement, AtomListElement previousElement) {
    if (previousElement != null 
        && previousElement.getDateString().equals(currentElement.getDateString())) {
      previousElement.setTimeVisible(true);
      currentElement.setTimeVisible(true);
    } else {
      currentElement.setTimeVisible(false);
    }
  }
  
  private class AtomListRow {
    public AtomListElement atomListElement;
    public Widget elementWidget;
    
    public AtomListRow(AtomListElement atomListElement, Widget elementWidget) {
      this.atomListElement = atomListElement;
      this.elementWidget = elementWidget;
    }
  }
}
