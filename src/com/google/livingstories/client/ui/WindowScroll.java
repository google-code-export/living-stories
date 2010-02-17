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

import com.google.gwt.animation.client.Animation;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.livingstories.client.util.Constants;

/**
 * Animation effect that scrolls the window to the specified position.
 */
public class WindowScroll {
  private static ScrollAnimation animation = new ScrollAnimation();
  private static Command completionCommand = null;
  
  public static void scrollTo(int scrollTo) {
    animation.cancel();
    animation.setScrollTo(scrollTo);
    animation.run(Constants.ANIMATION_DURATION);
  }
  
  public static void scrollTo(int scrollTo, Command onComplete) {
    scrollTo(scrollTo);
    // Set this after calling scrollTo(), since animation.cancel() will clear any existing
    // completionCommand first.
    completionCommand = onComplete;
  }
  
  private static class ScrollAnimation extends Animation {
    private int start;
    private int delta;
    private int end;
    
    public void setScrollTo(int scrollTo) {
      start = Window.getScrollTop();
      delta = scrollTo - start;
      end = scrollTo;
    }
    
    @Override
    public void onUpdate(double progress) {
      progress = interpolate(progress);
      Window.scrollTo(0, (int) (start + progress * delta));
    }
    
    @Override
    public void onComplete() {
      Window.scrollTo(0, end);
      if (completionCommand != null) {
        completionCommand.execute();
        completionCommand = null;
      }
    }
    
    @Override
    public void onCancel() {
      completionCommand = null;
    }
  }
}
