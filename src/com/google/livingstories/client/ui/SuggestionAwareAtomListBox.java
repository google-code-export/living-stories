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

import com.google.livingstories.client.BaseAtom;
import com.google.livingstories.client.util.GlobalUtil;

import java.util.Collections;
import java.util.Set;

/**
 * A subclass of AtomListBox that maintains and displays a "suggested" class of atoms.
 */
public class SuggestionAwareAtomListBox extends AtomListBox {
  private Set<Long> suggestedAtomIds;
  private String SUGGESTIONS_TEXT = "Suggestions";
  
  public SuggestionAwareAtomListBox(final boolean multiSelect) {
    super(multiSelect);
    suggestedAtomIds = Collections.<Long>emptySet();
  }
  
  /**
   * Sets the suggestedAtomIds, and alters the visibility of the "suggestion" option
   * in the dropdown if appropriate.
   */
  public void setSuggestedAtomIds(Set<Long> suggestedAtomIds) {
    this.suggestedAtomIds = GlobalUtil.copySet(suggestedAtomIds);
    
    int lastIndex = filter.getItemCount() - 1;
    boolean hasSuggestionOption = filter.getValue(lastIndex).equals(SUGGESTIONS_TEXT);

    if (suggestedAtomIds.isEmpty() && hasSuggestionOption) {
      if (filter.getSelectedIndex() == lastIndex) {
        filter.setSelectedIndex(0);
        refresh();
      }
      filter.remove(SUGGESTIONS_TEXT);
    } else if (!suggestedAtomIds.isEmpty() && !hasSuggestionOption) {
      filter.addFollowing(SUGGESTIONS_TEXT);
    }
  }
  
  public void selectSuggested() {
    int lastIndex = filter.getItemCount() - 1;
    if (filter.getValue(lastIndex).equals(SUGGESTIONS_TEXT)) {
      filter.setSelectedIndex(lastIndex);
      refresh();
    }
  }
  
  @Override
  protected boolean testAtom(BaseAtom atom) {
    return (SUGGESTIONS_TEXT.equals(filter.getSelectedValue())
        ? suggestedAtomIds.contains(atom.getId()) : super.testAtom(atom));
  }
}
