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
 * A LivingStorySelector variant that keeps track of what living story, by ID, that the user
 * has actively selected from among all CoordinatedLivingStorySelector instances.
 * When reloading or newly showing this LivingStorySelector, choose the most recent
 * actively-selected living story by default.
 */
public class CoordinatedLivingStorySelector extends LivingStorySelector {
  // shared among all CoordinatedLivingStorySelector instances
  private static Long coordinatedLivingStoryId = -1L;
  
  public CoordinatedLivingStorySelector(LivingStoryRpcServiceAsync livingStoryService,
      boolean showUnassigned) {
    super(livingStoryService, showUnassigned);
    addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        setCoordinatedLivingStoryIdFromSelection();
      }
    });
  }
  
  public CoordinatedLivingStorySelector(LivingStoryRpcServiceAsync livingStoryService) {
    this(livingStoryService, false);
  }
  
  public void selectCoordinatedLivingStory() {
    if (coordinatedLivingStoryId == null) {
      selectItemWithValue(UNASSIGNED);
    } else if (coordinatedLivingStoryId != -1) {
      selectItemWithValue(String.valueOf(coordinatedLivingStoryId));
    }
    if (!hasSelection() && getItemCount() > 0) {
      // in that case, just select the first item by default
      setItemSelected(0, true);
      setCoordinatedLivingStoryIdFromSelection();
    }
  }
  
  public void setCoordinatedLivingStoryIdFromSelection() {
    coordinatedLivingStoryId = hasSelection() ? getSelectedLivingStoryId() : Long.valueOf(-1L);
    // the Long.valueOf prevents unboxing of the getSelectedivingStoryId() result.
  }
  
  public void clearCoordinatedLivingStoryId() {
    coordinatedLivingStoryId = -1L;
  }
  
  @Override
  protected void onSuccessNextStep() {
    super.onSuccessNextStep();
    selectCoordinatedLivingStory();
  }
}
