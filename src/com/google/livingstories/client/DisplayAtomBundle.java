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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents the atoms that are relevant for events in a particular time range
 * for a living story, including information on whether the client can request earlier or later
 * events.
 */
public class DisplayAtomBundle implements Serializable {
  // Implementation note: class makes wide use of defensive copying.
  private List<BaseAtom> coreAtoms;
  private Set<BaseAtom> linkedAtoms;

  /**
   * if we had returned more atoms, what would the event date of the next atom
   * returned have been? This can be useful for knowing when we've bridged the gap between
   * 2 timespans of atoms. Is null if there were no additional atoms to return.
   */
  private Date nextDateInSequence;
  
  private FilterSpec adjustedFilterSpec;
  
  // empty constructor to make GWT happy
  public DisplayAtomBundle() {}

  public DisplayAtomBundle(List<BaseAtom> coreAtoms, Set<BaseAtom> linkedAtoms,
      Date nextDateInSequence, FilterSpec adjustedFilterSpec) {
    this.coreAtoms = new ArrayList<BaseAtom>(coreAtoms);
    this.linkedAtoms = new HashSet<BaseAtom>(linkedAtoms);
    this.nextDateInSequence =
        (nextDateInSequence == null ? null : new Date(nextDateInSequence.getTime()));
    this.adjustedFilterSpec = adjustedFilterSpec;
  }
  
  public List<BaseAtom> getCoreAtoms() {
    return new ArrayList<BaseAtom>(coreAtoms);
  }
  
  public Set<BaseAtom> getLinkedAtoms() {
    return new HashSet<BaseAtom>(linkedAtoms);
  }
  
  public Date getNextDateInSequence() {
    return (nextDateInSequence == null ? null : new Date(nextDateInSequence.getTime()));
  }
  
  public FilterSpec getAdjustedFilterSpec() {
    return new FilterSpec(adjustedFilterSpec);
  }
}
