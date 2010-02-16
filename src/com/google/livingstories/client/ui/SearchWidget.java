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

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.livingstories.client.BaseContentItem;
import com.google.livingstories.client.ContentRpcService;
import com.google.livingstories.client.ContentRpcServiceAsync;
import com.google.livingstories.client.contentitemlist.ContentItemClickHandler;
import com.google.livingstories.client.contentmanager.SearchResultsList;
import com.google.livingstories.client.contentmanager.SearchTerms;
import com.google.livingstories.client.ui.SearchPanel.SearchHandler;

import java.util.List;

/**
 * Page that hooks up a search panel to a content item list, allowing search
 * over the entire corpus of content items.
 */
public class SearchWidget extends Composite {
  private final ContentRpcServiceAsync contentService = GWT.create(ContentRpcService.class);
  
  private VerticalPanel contentPanel;
  private SearchPanel searchPanel;
  private SearchResultsList contentItemList;
  
  public SearchWidget(ContentItemClickHandler handler) {
    contentPanel = new VerticalPanel();
    contentPanel.add(createSearchPanel());
    contentItemList = new SearchResultsList(handler);
    
    contentPanel.add(contentItemList);
    initWidget(contentPanel);
  }
  
  private Widget createSearchPanel() {
    searchPanel = new SearchPanel();
    searchPanel.addSearchHandler(new SearchHandler() {
      public void onSearch(SearchTerms searchTerms) {
        contentService.executeSearch(searchTerms, new AsyncCallback<List<BaseContentItem>>() {
          public void onFailure(Throwable t) {
            // Ignore;
          }
          public void onSuccess(List<BaseContentItem> contentItems) {
            contentItemList.load(contentItems);
          }
        });
      }
    });
    return searchPanel;    
  }
  
  public void clear() {
    contentItemList.clear();
  }
}
