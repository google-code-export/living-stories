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

package com.google.livingstories.client.util;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.livingstories.client.FilterSpec;
import com.google.livingstories.client.lsp.Page;
import com.google.livingstories.client.lsp.Page.LoadHandler;
import com.google.livingstories.client.lsp.views.PlayerPage;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Class that manages history tokens and navigation.
 * 
 * Note that we use Window.Location.getHash() to get the history token everywhere
 * in this class, rather than History.getToken().  We do this because IE has issues
 * with firing history change events when the hash is updated programmatically, which
 * causes History.getToken to return out of date results.
 */
public class HistoryManager {
  public enum HistoryPages {
    OVERVIEW {
      @Override
      public Page getPage(String[] tokens) {
        return PageCache.getOverviewPage(new FilterSpec(tokens[1]),
            tokens[2].equals("null") ? null : Long.valueOf(tokens[2]));
      }
      @Override
      public String createToken(String... params) {
        // name:filterParams:focusedContentItemId (for filterParams see FilterSpec.java)
        return name() + ":" + params[0] + ":" + params[1];
      }
      @Override
      public Map<String, String> getDefaultState() {
        return new HashMap<String, String>();
      }
    },
    PLAYER {
      @Override
      public Page getPage(String[] tokens) {
        PlayerPage playerPage = new PlayerPage();
        playerPage.load(Long.valueOf(tokens[1]));
        return playerPage;
      }
      @Override
      public String createToken(String... params) {
        // name:contentItemId
        return name() + ":" + params[0];
      }
      @Override
      public Map<String, String> getDefaultState() {
        return new HashMap<String, String>();
      }
    };
    
    public abstract Page getPage(String[] pageTokens);
    
    public abstract String createToken(String... params);
    
    public abstract Map<String, String> getDefaultState();
  }
  
  public static class HistoryChangeHandler implements ValueChangeHandler<String> {
    @Override
    public void onValueChange(ValueChangeEvent<String> e) {
      if (e.getValue().isEmpty()) {
        // Handles the case where the history token is empty (e.g. first visit)
        loadMainPage();
        return;
      }
      final String[] newTokens = e.getValue().split(";");
      String newPageToken = newTokens[0];
      if (page == null || !pageToken.equals(newPageToken)) {
        pageToken = newPageToken;
        String[] pageParams = newPageToken.split(":");
        HistoryPages historyPage = HistoryPages.valueOf(pageParams[0]);
        Page newPage = historyPage.getPage(pageParams);
        pageState = historyPage.getDefaultState();
        if (newPage != page) {
          page = newPage;
          LivingStoryControls.goToPage(page);
        }
      }
      changeState(newTokens);
      if (page.isLoaded()) {
        page.onShow();
        executeState();
      } else {
        page.addLoadHandler(new LoadHandler() {
          public void onLoad() {
            page.onShow();
            executeState();
          }
        });
      }
    }
    
    private void changeState(String[] tokens) {
      pageState.clear();
      for (int i = 1; i < tokens.length; i++) {
        String[] state = tokens[i].split(":");
        pageState.put(state[0], state[1]);
      }
    }
    
    private void executeState() {
      // Prevent state change operations from setting extra tokens
      beginBatchStateChange();
      for (Entry<String, String> state : pageState.entrySet()) {
        page.changeState(state.getKey(), state.getValue());
      }
      endBatchStateChange();
    }
  }

  private static HistoryChangeHandler historyChangeHandler;
  private static String pageToken;
  private static Page page;
  private static Map<String, String> pageState;
  private static boolean batchStateChange = false;
  private static boolean initialized = false;

  /**
   * Convenience method for creating a new history page with the default state.
   * This method is a hack to let the player page transition work properly. 
   */
  public static void newToken(Page page, HistoryPages historyPage, String... params) {
    HistoryManager.page = page;
    pageToken = historyPage.createToken(params);
    pageState = historyPage.getDefaultState();
    setToken();
  }

  /**
   * Convenience method for updating the pageToken without changing the page.
   */
  public static void newToken(HistoryPages historyPage, String... params) {
    pageToken = historyPage.createToken(params);
    pageState = historyPage.getDefaultState();
    setToken();
  }

  /**
   * Convenience method for creating a new history page with the default state and
   * firing a history changed event.
   */
  public static void newTokenWithEvent(HistoryPages page, String... params) {
    String token = page.createToken(params) + ";";
    if (!token.equals(getHash())) {
      History.newItem(token);
    }
  }
  
  /**
   * Sets a single state key/value pair without changing the current page.
   * Pass in a null as the state value to unset a key.
   */
  public static void changeState(String key, String value) {
    if (value == null) {
      if (value != pageState.remove(key)) {
        setToken();
      }
    } else {
      if (!value.equals(pageState.put(key, value))) {
        setToken();
      }
    }
  }
  
  public static void beginBatchStateChange() {
    batchStateChange = true;
  }

  public static void endBatchStateChange() {
    batchStateChange = false;
    setToken();
  }

  public static String getState(String key) {
    return pageState.get(key);
  }
  
  public static String getLink(HistoryPages page, String... params) {
    String href = Window.Location.getHref().split("#")[0];
    return href.concat("#" + getToken(page.createToken(params), page.getDefaultState()));
  }
  
  public static String getTokenStringForFocusedContentItem(Long focusedContentItemId) {
    return "#" + HistoryPages.OVERVIEW.createToken(
        getDefaultFilterSpec().getFilterParams(), focusedContentItemId.toString())
        + ";";
  }
  
  private static String getToken(String pageToken, Map<String, String> pageState) {
    StringBuilder sb = new StringBuilder(pageToken).append(";");
    for (Entry<String, String> entry : pageState.entrySet()) {
      sb.append(entry.getKey()).append(":").append(entry.getValue()).append(";");
    }
    return sb.toString();    
  }
  
  private static void setToken() {
    if (batchStateChange) {
      return;
    }
    String token = getToken(pageToken, pageState);
    if (!token.equals(getHash())) {
      History.newItem(token, false);
    }
  }
  
  private static String getHash() {
    // In IE, we need to use the location.hash value, since it doesn't fire history
    // change events properly so that History.getToken returns the right value.
    // In chrome, we can't use location.hash because THAT doesn't return the right value,
    // apparently.
    return isIE() ? Window.Location.getHash().replaceAll("#", "") : History.getToken();
  }
  
  public static void loadMainPage() {
    String currentUrl = Window.Location.getHref().split("#")[0];
    final String token = HistoryPages.OVERVIEW.createToken(
        getDefaultFilterSpec().getFilterParams(), null) + ";";
    Window.Location.replace(currentUrl + "#" + token);
    Command fireValueChange = new Command() {
      @Override
      public void execute() {
        historyChangeHandler.onValueChange(new ValueChangeEvent<String>(token){});
      }
    };
    if (isIE()) {
      // In IE, we need to fire this immediately or we get into an infinite redirect loop.
      fireValueChange.execute();
    } else {
      // In chrome, we need to delay this, otherwise getHash() doesn't return the right value.
      DeferredCommand.addCommand(fireValueChange);
    }
  }
  
  private static FilterSpec getDefaultFilterSpec() {
    String defaultPageParams = LivingStoryData.getDefaultPage();
    return (defaultPageParams == null || defaultPageParams.isEmpty())  ?
        new FilterSpec() : new FilterSpec(defaultPageParams);
  }
  
  public static void initialize() {
    historyChangeHandler = new HistoryManager.HistoryChangeHandler();
    History.addValueChangeHandler(historyChangeHandler);
    if (getHash().isEmpty()) {
      loadMainPage();
    } else {
      History.fireCurrentHistoryState();
    }
    initialized = true;
  }
  
  public static boolean isInitialized() {
    return initialized;
  }
  
  private static native boolean isIE() /*-{
    return $wnd.navigator.appName == "Microsoft Internet Explorer";
  }-*/;
}
