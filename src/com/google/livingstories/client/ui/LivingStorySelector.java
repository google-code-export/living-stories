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

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.livingstories.client.LivingStory;
import com.google.livingstories.client.LivingStoryRpcServiceAsync;

public class LivingStorySelector extends ItemList<LivingStory> {
  public static final String UNASSIGNED = "Unassigned";
  
  private final LivingStoryRpcServiceAsync livingStoryService;
  private boolean showUnassigned;

  private HorizontalPanel containingPanel;

  public LivingStorySelector(LivingStoryRpcServiceAsync livingStoryService,
      boolean showUnassigned) {
    // we don't load items on init, not until this.livingStoryService is set
    // below.
    super(false, false);
    this.livingStoryService = livingStoryService;
    this.showUnassigned = showUnassigned;
    setVisibleItemCount(1);
    loadItems();
  }
  
  private class LivingStoryAdapter extends ListItemAdapter<LivingStory> {
    @Override
    public String getItemText(LivingStory item) {
      return item.getTitle();
    }
    @Override
    public String getItemValue(LivingStory item) {
      return Long.toString(item.getId());
    }        
  }
  
  @Override
  public void loadItems() {
    livingStoryService.getLivingStoriesForContentManager(getCallback(new LivingStoryAdapter()));
  }
  
  /**
   * Returns "this" embedded in a new horizontal panel. May only be called once per
   * instance.
   * @return the horizontal panel
   */
  public Widget makeContainingPanel() {
    if (containingPanel != null) {
      throw new IllegalStateException("Containing panel has already been made.");
    }
    
    containingPanel = new HorizontalPanel();
    containingPanel.add(new Label("Select living story:"));
    containingPanel.add(this);
    
    return containingPanel;
  }
  
  @Override
  protected void onSuccessNextStep() {
    if (showUnassigned) {
      addItem(UNASSIGNED);
    }
  }

  /**
   * Returns the selected living story id, or null if the user has selected "unassigned"
   */
  public Long getSelectedLivingStoryId() {
    String selectedItemValue = getSelectedItemValue();
    if (selectedItemValue == null) {  // no selection
      throw new UnsupportedOperationException("no living story selected");
    }
    return selectedItemValue.equals(UNASSIGNED) ? null : Long.valueOf(selectedItemValue);
  }
  
  /**
   * Returns true if the user has selected "unassigned". Mostly a convenience wrapper around
   * @link{getSelectedLivingStoryId}.
   */
  public boolean isUnassignedSelected() {
    return (hasSelection() && getSelectedLivingStoryId() == null);
  }

  public void selectUnassigned() {
    selectItemWithValue(UNASSIGNED);
  }
  
  /**
   * A simple accessor to the containing panel, if it has already been made.
   * @return the containing horizontal panel, or null if none has been made.
   */
  public Widget getContainingPanel() {
    return containingPanel;
  }
}
