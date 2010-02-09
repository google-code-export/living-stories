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
 * Enum for the different types of suggestions that a user can submit.
 */
public enum SuggestionType {

  TYPO("Spelling error"),
  GRAMMAR("Grammatical error"),
  UNCLEAR("Unclear language"),
  MISSING_SOURCE("Source citation needed"),
  DEFINITION("Definition of term needed"),
  MORE_INFO("More information needed");
  
  private String displayPhrase;
  
  private SuggestionType(String displayPhrase) {
    this.displayPhrase = displayPhrase;
  }
  
  @Override
  public String toString() {
    return displayPhrase;
  }
}
