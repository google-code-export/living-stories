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

package com.google.livingstories.client.lsp;

import com.google.gwt.user.client.ui.Composite;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for page-like widgets.  This class is designed to work in conjunction
 * with the HistoryManager to provide seamless history support.
 * 
 * Users of this class should:
 * 1. Call 'onLoad' when the page is in a state where all state change events may occur.
 * 2. Provide a 'changeState' method that can handle all possible state changes to the page
 *    that should be included in the page history.
 */
public abstract class Page extends Composite {
  private List<LoadHandler> loadHandlers = new ArrayList<LoadHandler>();
  private boolean loadState = false;
  
  public void addLoadHandler(LoadHandler handler) {
    if (!isLoaded()) {
      loadHandlers.add(handler);
    }
  }
  
  protected void beginLoading() {
    loadHandlers.clear();
    loadState = false;
  }
  
  protected void finishLoading() {
    for (LoadHandler handler : loadHandlers) {
      handler.onLoad();
    }
    loadHandlers.clear();
    loadState = true;
  }
  
  public boolean isLoaded() {
    return loadState;
  }
  
  public abstract void changeState(String key, String value);
  
  public void onShow() {}
  
  public interface LoadHandler {
    void onLoad();
  }
}
