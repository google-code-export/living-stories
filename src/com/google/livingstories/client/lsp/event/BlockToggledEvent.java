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
import com.google.gwt.user.client.Command;

/**
 * Event fired when an event or narrative block is toggled in the event stream.
 */
public class BlockToggledEvent extends GwtEvent<BlockToggledEvent.Handler> {
  // Handlers must implement this interface
  public interface Handler extends EventHandler {
    public void onToggle(BlockToggledEvent e);
  }
  
  @Override
  protected void dispatch(BlockToggledEvent.Handler handler) {
    handler.onToggle(this);
  }
  
  @Override
  public GwtEvent.Type<BlockToggledEvent.Handler> getAssociatedType() {
    return TYPE;
  }
  
  public static final GwtEvent.Type<BlockToggledEvent.Handler> TYPE
      = new GwtEvent.Type<BlockToggledEvent.Handler>();

  // Custom data for the event
  
  private boolean opened;
  private Long contentItemId;
  private boolean animate = true;
  private boolean setHistory = true;
  private boolean scrollOnClose = true;
  private Command onFinish;
  
  public BlockToggledEvent(boolean opened, Long contentItemId) {
    this.opened = opened;
    this.contentItemId = contentItemId;
  }

  public BlockToggledEvent skipAnimation() {
    animate = false;
    return this;
  }
  
  public BlockToggledEvent skipHistory() {
    setHistory = false;
    return this;
  }

  public BlockToggledEvent skipScrollOnClose() {
    scrollOnClose = false;
    return this;
  }
  
  public BlockToggledEvent setOnFinish(Command command) {
    onFinish = command;
    return this;
  }
  
  public boolean isOpened() {
    return opened;
  }
  
  public Long getContentItemId() {
    return contentItemId;
  }
  
  public boolean shouldAnimate() {
    return animate;
  }
  
  public boolean shouldSetHistory() {
    return setHistory;
  }
  
  public boolean shouldScrollOnClose() {
    return scrollOnClose;
  }
  
  public void finish() {
    if (onFinish != null) {
      onFinish.execute();
    }
  }
}
