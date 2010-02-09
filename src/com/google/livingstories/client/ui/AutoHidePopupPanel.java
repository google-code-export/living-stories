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
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.livingstories.client.lsp.views.Resources;
import com.google.livingstories.client.util.LivingStoryControls;
import com.google.livingstories.client.util.ObjectElementScrubber;

/**
 * A PopupPanel that hides itself whenever the user clicks outside of it (optionally)
 * or on a link inside of it.
 */
public class AutoHidePopupPanel extends PopupPanel {
  public static String NON_HIDING_CLASS = "nonhiding";
  
  private boolean wholePopupIsLink;

  private ObjectElementScrubber scrubber = GWT.create(ObjectElementScrubber.class);
  private boolean scrubbed;
  
  public AutoHidePopupPanel() {
    this(true);
  }
  
  public AutoHidePopupPanel(boolean autoHide) {
    super(autoHide);
  }

  @Override
  public void setWidget(Widget w) {
    super.setWidget(w);
    scrubbed = false;
  }
  
  // the superclass add() method calls into setWidget, and thus needn't be overridden separately.
  
  @Override
  public void show() {
    if (scrubbed) {
      if (GWT.isScript()) {
        // just hide the dialog. The user's encountered a bug, but we don't want to put them
        // in a position where we need to instruct them what to do next.
        LivingStoryControls.showGlass(false);
        super.hide();
      } else {
        // show a better error to developers:
        setAutoHideEnabled(true);
        super.setWidget(new Label("Error: the widget within this element was cleaned and cannot"
            + " safely be reshown without having its content reset first"));
      }
    } else {
      super.show();
    }
  }
  
  @Override
  public void hide() {
    if (getWidget() != null) {
      // clear the "data" attribute of all HTML objects within the widget, so that no playback
      // persists.
      NodeList<Element> objects = getWidget().getElement().getElementsByTagName("object");
      for (int i = 0, len = objects.getLength(); i < len; i++) {
        scrubber.scrub(objects.getItem(i));
        scrubbed = true;
        // in point of fact, we're safe in FF, but this indicates an implementation problem where
        // a popup is reused that shouldn't be, and we want to catch this on _all_ browsers.
      }
    }
    super.hide();
  }
  
  public void setWholePopupIsLink(boolean wholePopupIsLink) {
    this.wholePopupIsLink = wholePopupIsLink;
  }
  
  @Override
  protected void onPreviewNativeEvent(NativePreviewEvent event) {
    Event nativeEvent = Event.as(event.getNativeEvent());
    if (nativeEvent.getTypeInt() == Event.ONCLICK) {
      Element source = Element.as(nativeEvent.getEventTarget());
      if (source.getClassName().contains(NON_HIDING_CLASS)) {
        return;
      }
      if (wholePopupIsLink
          || source.getTagName().equalsIgnoreCase("A") 
          || source.getClassName().contains("primaryLink")
          || source.getClassName().contains("secondaryLink")
          || source.getClassName().contains(Resources.INSTANCE.css().clickable())) {
        // Need to use a deferred command here because if we hide the panel immediately,
        // it somehow prevents the link from seeing the event.
        DeferredCommand.addCommand(new Command() {
          @Override
          public void execute() {
            hide();
          }
        });
      }
    } else if (nativeEvent.getTypeInt() == Event.ONKEYUP
        && nativeEvent.getKeyCode() == KeyCodes.KEY_ESCAPE) {
      // also hide the dialog if that happens. Handy to provide this, in case the close
      // button somehow gets hidden offscreen.
      hide();
    }    
  }
}
