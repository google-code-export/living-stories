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

package com.google.livingstories.client.lsp.views.contentitems;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.livingstories.client.ContentItemType;
import com.google.livingstories.client.BaseContentItem;
import com.google.livingstories.client.NarrativeContentItem;
import com.google.livingstories.client.lsp.ContentRenderer;
import com.google.livingstories.client.util.GlobalUtil;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * The stream view for a narrative entity.  Displays the content of the narrative
 * as well as linked content.
 */
public class NarrativeStreamView extends ContainerStreamView<NarrativeContentItem> {
  public NarrativeStreamView(NarrativeContentItem narrative,
      Map<ContentItemType, List<BaseContentItem>> linkedContentItemsByType) {
    super(narrative, linkedContentItemsByType);
  }

  @Override
  protected String getHeadline() {
    return contentItem.getHeadline() + "<span style=\"color: #777;\">&nbsp;-&nbsp;" 
        + contentItem.getNarrativeType().toString() + "</span>";
  }
  
  @Override
  protected Date getStartDate() {
    return contentItem.getDateSortKey();
  }
  
  @Override
  protected Date getEndDate() {
    return null;
  }
  
  @Override
  protected LongContainerView<NarrativeContentItem> getLongContainerView() {
    return new LongContainerView<NarrativeContentItem>(contentItem, linkedContentItemsByType) {
      @Override
      protected Widget createSummary() {
        if (GlobalUtil.isContentEmpty(contentItem.getNarrativeSummary())) {
          return null; 
        } else { 
          return new ContentRenderer(contentItem.getNarrativeSummary(), false);
        }
      }
      
      @Override
      protected Widget createDetails() {
        if (GlobalUtil.isContentEmpty(contentItem.getContent())) {
          return null;
        } else {
          FlowPanel details = new FlowPanel();
          details.add(new Label("--"));
          details.add(new BaseContentItemPreview(contentItem));
          return details;
        }
      }      
    };
  }
}
