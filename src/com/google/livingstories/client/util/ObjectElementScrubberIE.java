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

package com.google.livingstories.client.util;

import com.google.gwt.dom.client.Element;

public class ObjectElementScrubberIE extends ObjectElementScrubber {
  @Override
  public native void scrub(Element obj) /*-{
    // This approach is from SWFObject's "removeSWF" implementation.
    // http://code.google.com/p/swfobject/source/browse/trunk/swfobject/src/swfobject.js
    // I'm not sure of the exact logic behind it, but it seems to work and I will trust that the
    // implementors are more knowledgable about such things than I am.

    obj.style.display = "none";
    (function(){
      if (obj.readyState == 4) {
        for (var i in obj) {
          if (typeof obj[i] == "function") {
            obj[i] = null;
          }
        }
        obj.parentNode.removeChild(obj);
      } else {
        setTimeout(arguments.callee, 10);
      }
    })(); 
  }-*/;
}
