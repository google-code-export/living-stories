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

import com.google.gwt.user.client.ui.Widget;
import com.google.livingstories.client.Importance;

import java.util.Set;

/**
 * Interface that an element that wishes to be displayed in the atom list on the LSP has to 
 * implement.
 */
public interface AtomListElement {
  
  Long getId();
  
  Importance getImportance();
  
  Set<Long> getThemeIds();
  
  Widget render(boolean includeAtomName);
  
  boolean setExpansion(boolean expand);

  boolean setExpansion(boolean expand, boolean skipExtraActions);
  
  String getDateString();
  
  void setTimeVisible(boolean visible);
}
