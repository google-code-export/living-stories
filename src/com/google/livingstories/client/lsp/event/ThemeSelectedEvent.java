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
 * Event that is fired when an theme is selected.
 */
public class ThemeSelectedEvent extends GwtEvent<ThemeSelectedEvent.Handler> {
  // Handlers must implement this interface
  public interface Handler extends EventHandler {
    public void onThemeSelected(ThemeSelectedEvent e);
  }
  
  @Override
  protected void dispatch(ThemeSelectedEvent.Handler handler) {
    handler.onThemeSelected(this);
  }
  
  @Override
  public GwtEvent.Type<ThemeSelectedEvent.Handler> getAssociatedType() {
    return TYPE;
  }
  
  public static final GwtEvent.Type<ThemeSelectedEvent.Handler> TYPE
      = new GwtEvent.Type<ThemeSelectedEvent.Handler>();

  // Custom data for the event
  
  private Long themeId;
  
  public ThemeSelectedEvent(Long themeId) {
    this.themeId = themeId;
  }

  public Long getThemeId() {
    return themeId;
  }
}
