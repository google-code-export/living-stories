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

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Widget;
import com.google.livingstories.client.lsp.ContentRenderer;
import com.google.livingstories.client.util.GlobalUtil;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * This is the client version of a {@link com.google.livingstories.server.BaseAtomEntityImpl}.
 * The {@link ContentRpcService} converts AtomEntities to
 * and from this class which is used by the client.
 * 
 * It also includes implementation relevant to actually rendering Atoms, as GWT widgets. 
 */
public abstract class BaseAtom implements Serializable {
  protected static final int TINY_SNIPPET_LENGTH = 100;
  
  private Long id;
  private String content;
  private Date timestamp;
  private AtomType atomType;
  private Importance importance;
  private Long livingStoryId;
  private PublishState publishState = PublishState.DRAFT;
  private Set<Long> contributorIds;
  private Set<Long> linkedAtomIds;
  private List<BaseAtom> linkedAtoms;
  private Set<Long> themeIds;
  private Location location;
  private String sourceDescription;
  private BaseAtom sourceAtom;
  private String timeElapsedSinceLastUpdate;
  protected boolean renderAsSeen;
  
  /**
   * Comparator for sorting a mixed list of atoms. Relies on proper getDateSortKey() polymorphic
   * implementations.
   */
  public static final Comparator<BaseAtom> COMPARATOR = new Comparator<BaseAtom>() {
    @Override
    public int compare(BaseAtom lhs, BaseAtom rhs) {
      return lhs.getDateSortKey().compareTo(rhs.getDateSortKey());
    }
  };

  public static final Comparator<BaseAtom> REVERSE_COMPARATOR =
    Collections.reverseOrder(COMPARATOR);
  
  // No-arg constructor to keep gwt happy
  public BaseAtom() {}
  
  public BaseAtom(Long id, Date timestamp, AtomType atomType, Set<Long> contributorIds,
      String content, Importance importance, Long livingStoryId) {
    this.id = id;
    this.timestamp = timestamp;
    this.atomType = atomType;
    this.contributorIds = contributorIds;
    this.content = content;
    this.importance = importance;
    this.livingStoryId = livingStoryId;
  }
  
  public Long getId() {
    return id;
  }

  public Date getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }

  public AtomType getAtomType() {
    return atomType;
  }

  public void setAtomType(AtomType atomType) {
    this.atomType = atomType;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public Long getLivingStoryId() {
    return livingStoryId;
  }

  public void setLivingStoryId(Long livingStoryId) {
    this.livingStoryId = livingStoryId;
  }

  public Importance getImportance() {
    return importance;
  }

  public void setImportance(Importance importance) {
    this.importance = importance;
  }

  public PublishState getPublishState() {
    return publishState;
  }

  public void setPublishState(PublishState publishState) {
    this.publishState = publishState;
  }

  public Set<Long> getContributorIds() {
    return GlobalUtil.copySet(contributorIds);
  }

  public void setContributorIds(Set<Long> contributorIds) {
    this.contributorIds = GlobalUtil.copySet(contributorIds);
  }

  public Set<Long> getLinkedAtomIds() {
    return GlobalUtil.copySet(linkedAtomIds);
  }

  public void setLinkedAtomIds(Set<Long> linkedAtomIds) {
    this.linkedAtomIds = GlobalUtil.copySet(linkedAtomIds);
  }
  
  public void addAllLinkedAtomIds(Collection<Long> newLinkedAtomIds) {
    linkedAtomIds.addAll(newLinkedAtomIds);
  }
  
  public List<BaseAtom> getLinkedAtoms() {
    return linkedAtoms;
  }

  public void setLinkedAtoms(List<BaseAtom> linkedAtoms) {
    this.linkedAtoms = linkedAtoms;
  }
  
  public Set<Long> getThemeIds() {
    return GlobalUtil.copySet(themeIds);
  }
  
  public void setThemeIds(Set<Long> themeIds) {
    this.themeIds = GlobalUtil.copySet(themeIds);
  }
  
  public Location getLocation() {
    return location;
  }

  public void setLocation(Location location) {
    this.location = location;
  }
  
  public String getSourceDescription() {
    return sourceDescription;
  }

  public void setSourceDescription(String sourceDescription) {
    this.sourceDescription = sourceDescription;
  }

  public BaseAtom getSourceAtom() {
    return sourceAtom;
  }
  
  public Long getSourceAtomId() {
    return sourceAtom == null ? null : sourceAtom.getId();
  }

  public void setSourceAtom(BaseAtom sourceAtom) {
    this.sourceAtom = sourceAtom;
  }
  
  public boolean hasSourceInformation() {
    return sourceAtom != null || !GlobalUtil.isContentEmpty(sourceDescription);
  }

  public String getTypeString() {
    return atomType.toString();
  }
  
  public String getTitleString() {
    return getTypeString();
  }
  
  public String getNavLinkString() {
    return atomType.getNavLinkString();
  }
  
  public String getTimeElapsedSinceLastUpdate() {
    return timeElapsedSinceLastUpdate;
  }

  public void setTimeElapsedSinceLastUpdate(String timeElapsedSinceLastUpdate) {
    this.timeElapsedSinceLastUpdate = timeElapsedSinceLastUpdate;
  }

  /**
   * Evaluates whether this is renderable or not. May be overridden by some subclasses.
   * @return whether this is renderable
   */
  public boolean renderable() {
    return true;
  }
  
  public Widget renderTiny() {
    return new ContentRenderer(content, false);
  }
  
  /**
   * The string to use when introducing a byline for this atom.
   * @return the string to use
   */
  public String getBylineLeadin() {
    return Holder.msgs.bylineLeadinBaseAtom();
  }
  
  public String getDisplayString() {
    return "[" + getTypeString() + "] " + getContent();
  }
  
  /**
   * Returns the date to use when sorting events by date, or filtering based on date
   */
  public Date getDateSortKey() {
    return getTimestamp();
  }
  
  /**
   * Returns true if an atom should be considered for toplevel display in the UI's
   * "events" view.
   */
  public boolean displayTopLevel() {
    return false;
  }
  
  @Override
  public boolean equals(Object o) {
    return (o instanceof BaseAtom) && this.getId() == ((BaseAtom) o).getId();
  }
  
  public void getDimensionsAsync(DimensionHandler dimensionHandler) {
    dimensionHandler.onSuccess(null);
  }
  
  /**
   * Set this to true if you are following up with a call to one of the render* methods,
   * and it's important to style the rendering such that the item has already been seen.
   */
  public void setRenderAsSeen(boolean renderAsSeen) {
    this.renderAsSeen = renderAsSeen;
  }
  
  public boolean getRenderAsSeen() {
    return renderAsSeen;
  }
  
  // For access to client strings by BaseAtom and its subclasses. Using the inner
  // class here allows for use of the BaseAtom hierarchy on the server-side so long as
  // Holder is never accessed by any server-side code path.
  protected static class Holder {
    static ClientConstants msgs = GWT.create(ClientConstants.class);
  }
}
