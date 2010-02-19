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

package com.google.livingstories.client.lsp.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 * Event fired when an narrative link is clicked in the event stream.
 */
public class NarrativeLinkClickedEvent extends GwtEvent<NarrativeLinkClickedEvent.Handler> {
  // Handlers must implement this interface
  public interface Handler extends EventHandler {
    public void onClick(NarrativeLinkClickedEvent e);
  }
  
  @Override
  protected void dispatch(NarrativeLinkClickedEvent.Handler handler) {
    handler.onClick(this);
  }
  
  @Override
  public GwtEvent.Type<NarrativeLinkClickedEvent.Handler> getAssociatedType() {
    return TYPE;
  }
  
  public static final GwtEvent.Type<NarrativeLinkClickedEvent.Handler> TYPE
      = new GwtEvent.Type<NarrativeLinkClickedEvent.Handler>();

  // Custom data for the event

  private Long containerContentItemId;
  private Long narrativeContentItemId;
  
  public NarrativeLinkClickedEvent(Long containerContentItemId, Long narrativeContentItemId) {
    this.containerContentItemId = containerContentItemId;
    this.narrativeContentItemId = narrativeContentItemId;
  }
  
  public Long getContainerContentItemId() {
    return containerContentItemId;
  }
  
  public Long getNarrativeContentItemId() {
    return narrativeContentItemId;
  }
}
