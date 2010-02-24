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

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ListBox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Widget that extends listbox functionality.
 * Users of this class should define the 'loadItems' method.
 */
public abstract class ItemList<T> extends ListBox {
  private AsyncCallback<List<T>> callback;
  private boolean sortByText;
  
  public ItemList() {
    this(false, true, false);
  }
  
  public ItemList(boolean multiSelect) {
    this(multiSelect, true, false);
  }
  
  public ItemList(boolean multiSelect, boolean loadItemsOnInit) {
    this(multiSelect, loadItemsOnInit, false);
  }
  
  public ItemList(boolean multiSelect, boolean loadItemsOnInit, boolean sortByText) {
    super(multiSelect);
    this.sortByText = sortByText;
    if (loadItemsOnInit) {
      loadItems();
    }
  }
  
  public abstract void loadItems();

  /**
   * Use this method to get a callback for use with the
   * persistence services, if you need.
   * 
   * @return a callback to use with the persistence services.
   */
  protected AsyncCallback<List<T>> getCallback(final ListItemAdapter<T> adapter) {
    if (callback == null) {
      callback = new AsyncCallback<List<T>>() {
        public void onFailure(Throwable caught) {
          addItem("call to get item list failed");
          setEnabled(false);
          onFailureNextStep();
        }
        
        public void onSuccess(List<T> items) {
          if (sortByText) {
            adapter.sortListByText(items);
          }
          for (T item : items) {
            if (adapter.includeItem(item)) {
              addItem(adapter.getItemText(item), adapter.getItemValue(item));
            }
          }
          setEnabled(true);
          onSuccessNextStep();          
        }
      };
    }
    return callback;
  }
  
  protected AsyncCallback<List<T>> getCallback(final ListItemAdapter<T> adapter, 
      final String selectedItemValue) {
    if (callback == null) {
      callback = new AsyncCallback<List<T>>() {
        public void onFailure(Throwable caught) {
          addItem("call to get item list failed");
          setEnabled(false);
          onFailureNextStep();
        }
        
        public void onSuccess(List<T> items) {
          if (sortByText) {
            adapter.sortListByText(items);
          }
          for (T item : items) {
            if (adapter.includeItem(item)) {
              addItem(adapter.getItemText(item), adapter.getItemValue(item));
            }
          }
          setEnabled(true);
          selectItemWithValue(selectedItemValue);
          onSuccessNextStep();
        }
      };
    }
    return callback;
  }
  
  /**
   * Provide an implementation of this if there's followon work to do after the
   * callback function fails
   */
  protected void onFailureNextStep() { }

  /**
   * Provide an implementation of this if there's followon work to do after the
   * callback function succeeds
   */
  protected void onSuccessNextStep() { }

  /**
   * Convenience method to use the default adapter.
   * @return a callback that can be used with the persistence service
   *     to populate the item list.
   */
  protected AsyncCallback<List<T>> getCallback() {
    return getCallback(new ListItemAdapter<T>());
  }
  
  public boolean hasSelection() {
    return getSelectedIndex() >= 0;
  }
  
  public void selectItemWithText(String item) {
    int index = -1;
    for (int i = 0; i < getItemCount(); i++) {
      if (getItemText(i).equals(item)) {
        index = i;
        break;
      }
    }
    setSelectedIndex(index);
  }
  
  public void selectItemWithValue(String value) {
    int index = -1;
    for (int i = 0; i < getItemCount(); i++) {
      if (getValue(i).equals(value)) {
        index = i;
        break;
      }
    }
    setSelectedIndex(index);
  }
  
  public String getSelectedItemText() {
    return hasSelection() ? getItemText(getSelectedIndex()) : null;
  }
  
  public List<String> getSelectedItems() {
    if (!isMultipleSelect()) {
      throw new UnsupportedOperationException("Must be a multi-select");
    } else {
      List<String> items = new ArrayList<String>();
      for (int i = 0; i < getItemCount(); i++) {
        if (isItemSelected(i)) {
          items.add(getItemText(i));
        }
      }
      return items;
    }
  }
  
  public List<String> getSelectedItemValues() {
    if (!isMultipleSelect()) {
      throw new UnsupportedOperationException("Must be a multi-select");
    } else {
      List<String> items = new ArrayList<String>();
      for (int i = 0; i < getItemCount(); i++) {
        if (isItemSelected(i)) {
          items.add(getValue(i));
        }
      }
      return items;
    }
  }
  
  public String getSelectedItemValue() {
    return hasSelection() ? getValue(getSelectedIndex()) : null;
  }
  
  public void removeItemWithText(String item) {
    for (int i = 0; i < getItemCount(); i++) {
      if (getItemText(i).equals(item)) {
        removeItem(i);
        return;
      }
    }
  }
  
  public void removeItemWithValue(String value) {
    for (int i = 0; i < getItemCount(); i++) {
      if (getValue(i).equals(value)) {
        removeItem(i);
        return;
      }
    }
  }
  
  public void renameItemWithValue(String value, String newName) {
    for (int i = 0; i < getItemCount(); i++) {
      if (getValue(i).equals(value)) {
        setItemText(i, newName);
        return;
      }
    }
  }
  
  public boolean hasItemWithValue(String value) {
    for (int i = 0; i < getItemCount(); i++) {
      if (getValue(i).equals(value)) {
        return true;
      }
    }
    return false;
  }
  
  public int getSelectedCount() {
    int count = 0;
    for (int i = 0; i < getItemCount(); i++) {
      if (isItemSelected(i)) {
        count++;
      }
    }
    return count;    
  }
  
  public void refresh() {
    clear();
    loadItems();
  }
  
  public void clearSelection() {
    setSelectedIndex(-1);
  }

  public static class ListItemAdapter<T> {
    public boolean includeItem(T item) {
      return true;
    }
    
    public String getItemText(T item) {
      return item.toString();
    }
    public String getItemValue(T item) {
      return getItemText(item);
    }
    
    public final void sortListByText(List<T> items) {
      Collections.sort(items, new Comparator<T> () {
        @Override
        public int compare(T o1, T o2) {
          return getItemText(o1).compareTo(getItemText(o2));
        }
      });
    }
  }
}
