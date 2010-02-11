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

package com.google.livingstories.client.lsp.views.atoms;

import com.google.livingstories.client.AtomType;
import com.google.livingstories.client.BaseAtom;
import com.google.livingstories.client.EventAtom;
import com.google.livingstories.client.NarrativeAtom;
import com.google.livingstories.client.atomlist.AtomListElement;
import com.google.livingstories.client.util.LivingStoryData;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Factory that creates stream views based on the content type.
 */
public class StreamViewFactory {
  public static AtomListElement createView(BaseAtom atom, Map<Long, BaseAtom> idToAtomMap) {
    Map<AtomType, List<BaseAtom>> linkedAtomsByType;
    switch (atom.getAtomType()) {
      case EVENT:
        linkedAtomsByType = processAtom(atom, idToAtomMap);
        return new EventStreamView((EventAtom) atom, linkedAtomsByType);
      case NARRATIVE:
        linkedAtomsByType = processAtom(atom, idToAtomMap);
        return new NarrativeStreamView((NarrativeAtom) atom, linkedAtomsByType);
      default:
        throw new IllegalArgumentException("Atom type " + atom.getAtomType()
            + " does not have a stream view defined.");
    }
  }
  
  /**
   * Processes an atom by setting its read state and returning a map of atoms that
   * are linked to it.
   */
  private static Map<AtomType, List<BaseAtom>> processAtom(
      BaseAtom atom, Map<Long, BaseAtom> idToAtomMap) {
    Date lastVisitDate = LivingStoryData.getLastVisitDate();
    if (lastVisitDate != null) {
      atom.setRenderAsSeen(atom.getTimestamp().before(lastVisitDate));
    }
    
    Map<AtomType, List<BaseAtom>> linkedAtomsByType = new HashMap<AtomType, List<BaseAtom>>();
    for (AtomType type : AtomType.values()) {
      linkedAtomsByType.put(type, new ArrayList<BaseAtom>());
    }
    for (Long atomId : atom.getLinkedAtomIds()) {
      BaseAtom linkedAtom = idToAtomMap.get(atomId);
      if (linkedAtom != null) {
        linkedAtomsByType.get(linkedAtom.getAtomType()).add(linkedAtom);

        if (atom.getRenderAsSeen() && lastVisitDate != null
            && linkedAtom.getTimestamp().after(lastVisitDate)) {
          atom.setRenderAsSeen(false);
        }
      }
    }
    for (BaseAtom narrative : linkedAtomsByType.get(AtomType.NARRATIVE)) {
      List<BaseAtom> linkedAtoms = new ArrayList<BaseAtom>();
      for (Long atomId : narrative.getLinkedAtomIds()) {
        linkedAtoms.add(idToAtomMap.get(atomId));
      }
      narrative.setLinkedAtoms(linkedAtoms);
    }
    return linkedAtomsByType;
  }
}
