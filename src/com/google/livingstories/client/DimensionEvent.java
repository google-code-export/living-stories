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

/**
 * A wrapper class that allows objects to return data about the size at which they should
 * render to a calling routine, asynchronously.
 */
public class DimensionEvent {
  private int width;
  private int height;
  private boolean heuristic;
  
  public DimensionEvent(int width, int height, boolean heuristic) {
    this.width = width;
    this.height = height;
    this.heuristic = heuristic;
  }
  
  public int getWidth() {
    return width;
  }
  
  public int getHeight() {
    return height;
  }
  
  public boolean isHeuristic() {
    return heuristic;
  }
}
