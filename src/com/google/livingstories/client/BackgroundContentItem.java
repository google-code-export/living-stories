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

package com.google.livingstories.client;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.google.livingstories.client.util.GlobalUtil;
import com.google.livingstories.client.util.SnippetUtil;
import com.google.livingstories.client.util.dom.GwtNodeAdapter;

import java.util.Date;
import java.util.Set;

/**
 * Client-side version of background content entity
 */
public class BackgroundContentItem extends BaseContentItem {
  private String conceptName;
  
  public BackgroundContentItem() {}
  
  public BackgroundContentItem(Long id, Date timestamp, Set<Long> contributorIds, String content,
      Importance importance, Long livingStoryId, String conceptName) {
    super(id, timestamp, ContentItemType.BACKGROUND, contributorIds, content, importance,
        livingStoryId);
    this.conceptName = conceptName;
  }
  
  @Override
  public Widget renderTiny() {
    return new HTML(SnippetUtil.createSnippet(GwtNodeAdapter.fromHtml(getContent()),
        TINY_SNIPPET_LENGTH));
  }
  
  public boolean isConcept() {
    return !GlobalUtil.isContentEmpty(conceptName);
  }
  
  public String getConceptName() {
    return conceptName;
  }
  
  @Override
  public String getTypeString() {
    return isConcept() ? ClientMessageHolder.consts.contentTypeStringConcept()
        : getContentItemType().toString();
  }
  
  // Superclass implementation of renderContext & getBylineLeadin should be fine.
}
