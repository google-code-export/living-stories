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

public class PopupImageLoadedEvent extends GwtEvent<PopupImageLoadedEvent.Handler> {
  // Handlers must implement this interface
  public interface Handler extends EventHandler {
    public void onImageLoaded(PopupImageLoadedEvent e);
  }
  
  @Override
  protected void dispatch(PopupImageLoadedEvent.Handler handler) {
    handler.onImageLoaded(this);
  }
  
  @Override
  public GwtEvent.Type<PopupImageLoadedEvent.Handler> getAssociatedType() {
    return TYPE;
  }
  
  public static final GwtEvent.Type<PopupImageLoadedEvent.Handler> TYPE
      = new GwtEvent.Type<PopupImageLoadedEvent.Handler>();

  // Custom data for the event
  
  private int width;
  private int height;
  
  public PopupImageLoadedEvent(int width, int height) {
    this.width = width;
    this.height = height;
  }
  
  public int getWidth() {
    return width;
  }
  
  public int getHeight() {
    return height;
  }
}
