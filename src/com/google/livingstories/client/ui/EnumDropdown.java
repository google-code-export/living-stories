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

import com.google.gwt.user.client.ui.ListBox;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A dropdown selector widget that is backed by an enum, with optional additional options at
 * the start and/or end of the list which can be set at creation-time or altered dynamically.
 */
public class EnumDropdown<T extends Enum<T>> extends ListBox {
  private Class<T> enumClass;
  
  private Set<String> reservedValues;
  
  private int precedingCount = 0;
  
  public EnumDropdown(Class<T> enumClass) {
    this(enumClass, Collections.<String>emptyList(), Collections.<String>emptyList());
  }

  public EnumDropdown(Class<T> enumClass,
      List<String> precedingItems, List<String> followingItems) {
    super();
    
    this.enumClass = enumClass;
    
    setVisibleItemCount(1);

    reservedValues = new HashSet<String>();
    for (T type : enumClass.getEnumConstants()) {
      addItem(type.toString(), type.name());
      reservedValues.add(type.name());
    }

    for (String text : precedingItems) {
      addPreceding(text);
    }
    for (String text : followingItems) {
      addFollowing(text);
    }
  }
  
  /**
   * A convenience factory; deduces the appropriate type to use at construction-time.
   */
  public static <T extends Enum<T>> EnumDropdown<T> newInstance(Class<T> enumClass) {
    return new EnumDropdown<T>(enumClass);
  }
  
  /**
   * A convenience factory for the case where there's a single leading item.
   */
  public static <T extends Enum<T>> EnumDropdown<T> newInstance(Class<T> enumClass,
      String unselectedText) {
    EnumDropdown<T> ret = new EnumDropdown<T>(enumClass);
    ret.addPreceding(unselectedText);
    ret.setSelectedIndex(0);
    return ret;
  }

  public void addPreceding(String text) {
    checkText(text);
    insertItem(text, precedingCount);
    precedingCount++;
  }
  
  public void addFollowing(String text) {
    checkText(text);
    addItem(text);
  }
  
  public void remove(String text) {
    if (reservedValues.contains(text)) {
      throw new IllegalArgumentException("cannot remove item " + text + " from the enumDropdown;"
          + " it is core to the enum, not a value added later");
    }
    
    int i;
    for (i = 0; i < getItemCount() && !text.equals(getValue(i)); i++) {}
    
    if (i == getItemCount()) {
      throw new IllegalArgumentException("there is no item with value " + text + " in the control");
    }
    removeItem(i);
    
    if (i < precedingCount) {
      precedingCount--;
    }
  }
  
  /**
   * Checks that its string argument is okay. Will either throw an exception or complete without
   * notable side effect.
   * @param text The text to check
   * @throws IllegalArgumentException
   */
  private void checkText(String text) {
    for (int i = 0; i < getItemCount(); i++) {
      if (text.equals(getItemText(i)) || text.equals(getValue(i))) {
        throw new IllegalArgumentException(text + " is already used as an option name or value");
      }
    }
  }
  
  /**
   * Returns the selected constant, or null if the item selected is not one of the
   * enum constants.
   */
  public T getSelectedConstant() {
    String value = getSelectedValue();
    return reservedValues.contains(value) ? Enum.valueOf(enumClass, value) : null;
  }
  
  public String getSelectedValue() {
    return getValue(getSelectedIndex());
  }
  
  public void selectConstant(T constant) {
    assert constant != null;

    this.setSelectedIndex(constant.ordinal() + precedingCount);
  }
}
