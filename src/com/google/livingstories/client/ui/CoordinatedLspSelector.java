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

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.livingstories.client.LivingStoryRpcServiceAsync;

/**
 * An LspSelector variant that keeps track of what LSPs, by ID, that the user
 * has actively selected from among all CoordinatedLspSelector instances.
 * When reloading or newly showing this LspSelector, choose the most recent
 * actively-selected LSP by default.
 */
public class CoordinatedLspSelector extends LspSelector {
  // shared among all CoordinatedLspSelector instances
  private static Long coordinatedLspId = -1L;
  
  public CoordinatedLspSelector(LivingStoryRpcServiceAsync livingStoryService,
      boolean showUnassigned) {
    super(livingStoryService, showUnassigned);
    addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        setCoordinatedLspIdFromSelection();
      }
    });
  }
  
  public CoordinatedLspSelector(LivingStoryRpcServiceAsync livingStoryService) {
    this(livingStoryService, false);
  }
  
  public void selectCoordinatedLsp() {
    if (coordinatedLspId == null) {
      selectItemWithValue(UNASSIGNED);
    } else if (coordinatedLspId != -1) {
      selectItemWithValue(String.valueOf(coordinatedLspId));
    }
    if (!hasSelection() && getItemCount() > 0) {
      // in that case, just select the first item by default
      setItemSelected(0, true);
      setCoordinatedLspIdFromSelection();
    }
  }
  
  public void setCoordinatedLspIdFromSelection() {
    coordinatedLspId = hasSelection() ? getSelectedLspId() : Long.valueOf(-1L);
    // the Long.valueOf prevents unboxing of the getSelectedLspId() result.
  }
  
  public void clearCoordinatedLspId() {
    coordinatedLspId = -1L;
  }
  
  @Override
  protected void onSuccessNextStep() {
    super.onSuccessNextStep();
    selectCoordinatedLsp();
  }
}
