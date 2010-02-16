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
 * Event fired when a 'show more' link is clicked, either on the main overview page
 * or on the player page.
 */
public class ShowMoreEvent extends GwtEvent<ShowMoreEvent.Handler> {
  // Handlers must implement this interface
  public interface Handler extends EventHandler {
    public void onShowMore(ShowMoreEvent e);
  }
  
  @Override
  protected void dispatch(ShowMoreEvent.Handler handler) {
    handler.onShowMore(this);
  }
  
  @Override
  public GwtEvent.Type<ShowMoreEvent.Handler> getAssociatedType() {
    return TYPE;
  }
  
  public static final GwtEvent.Type<ShowMoreEvent.Handler> TYPE
      = new GwtEvent.Type<ShowMoreEvent.Handler>();

  // Custom data for the event
  
  private int count;
  
  public ShowMoreEvent() {
    // Count is unused; use default.
    count = 0;
  }

  public ShowMoreEvent(int count) {
    this.count = count;
  }
  
  public int getCount() {
    return count;
  }
}
