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
import com.google.livingstories.client.BaseAtom;
import com.google.livingstories.client.EventAtom;
import com.google.livingstories.client.lsp.AtomRenderer;
import com.google.livingstories.client.util.FourthestateUtil;

import java.util.Date;
import java.util.Map;

/**
 * An event atom as a complex element in the atom list.
 */
public class EventAtomListElement extends ComplexAtomListElement {
  private EventAtom event;
  
  public EventAtomListElement(BaseAtom atom, Map<Long, BaseAtom> allAtomsMap,
      boolean noHistoryOnToggle) {
    super(atom, allAtomsMap, noHistoryOnToggle);
    event = (EventAtom)atom;
  }
  
  @Override
  public Date getStartDate() {
    return event.getEventStartDate();
  }

  @Override
  public Date getEndDate() {
    return event.getEventEndDate();
  }
  
  @Override
  public String getHeadlineHtml() {
    return event.getEventUpdate();
  }

  @Override
  public SummarySnippetWidget getSummarySnippetWidget() {
    if (FourthestateUtil.isContentEmpty(event.getEventSummary())) {
      return null; 
    } else { 
      return new SummarySnippetWidget(event, alreadySeen); 
    }
  }
  
  @Override
  public Widget getDetailsWidget() {
    Widget eventDetailsWidget = null;
    if (!FourthestateUtil.isContentEmpty(event.getContent())) {
      eventDetailsWidget = new AtomRenderer(event, true, false, event.getContributorIds());
    }
    return eventDetailsWidget;
  }
}
