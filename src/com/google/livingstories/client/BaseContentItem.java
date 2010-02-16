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
 * This is the client version of a {@link com.google.livingstories.server.BaseContentEntity}.
 * The {@link ContentRpcService} converts BaseContentEntities to
 * and from subtypes of this class, which are used by the client.
 * 
 * It also includes implementation relevant to actually rendering content items, as GWT widgets. 
 */
public abstract class BaseContentItem implements Serializable {
  protected static final int TINY_SNIPPET_LENGTH = 100;
  
  private Long id;
  private String content;
  private Date timestamp;
  private ContentItemType contentItemType;
  private Importance importance;
  private Long livingStoryId;
  private PublishState publishState = PublishState.DRAFT;
  private Set<Long> contributorIds;
  private Set<Long> linkedContentItemIds;
  private List<BaseContentItem> linkedContentItems;
  private Set<Long> themeIds;
  private Location location;
  private String sourceDescription;
  private BaseContentItem sourceContentItem;
  private String timeElapsedSinceLastUpdate;
  protected boolean renderAsSeen;
  
  /**
   * Comparator for sorting a mixed list of content items. Relies on proper getDateSortKey()
   * polymorphic implementations.
   */
  public static final Comparator<BaseContentItem> COMPARATOR = new Comparator<BaseContentItem>() {
    @Override
    public int compare(BaseContentItem lhs, BaseContentItem rhs) {
      return lhs.getDateSortKey().compareTo(rhs.getDateSortKey());
    }
  };

  public static final Comparator<BaseContentItem> REVERSE_COMPARATOR =
    Collections.reverseOrder(COMPARATOR);
  
  // No-arg constructor to keep gwt happy
  public BaseContentItem() {}
  
  public BaseContentItem(Long id, Date timestamp, ContentItemType contentItemType,
      Set<Long> contributorIds, String content, Importance importance, Long livingStoryId) {
    this.id = id;
    this.timestamp = timestamp;
    this.contentItemType = contentItemType;
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

  public ContentItemType getContentItemType() {
    return contentItemType;
  }

  public void setContentItemType(ContentItemType contentItemType) {
    this.contentItemType = contentItemType;
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

  public Set<Long> getLinkedContentItemIds() {
    return GlobalUtil.copySet(linkedContentItemIds);
  }

  public void setLinkedContentItemIds(Set<Long> linkedContentItemIds) {
    this.linkedContentItemIds = GlobalUtil.copySet(linkedContentItemIds);
  }
  
  public void addAllLinkedContentItemIds(Collection<Long> newLinkedContentItemIds) {
    linkedContentItemIds.addAll(newLinkedContentItemIds);
  }
  
  public List<BaseContentItem> getLinkedContentItems() {
    return linkedContentItems;
  }

  public void setLinkedContentItems(List<BaseContentItem> linkedContentItems) {
    this.linkedContentItems = linkedContentItems;
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

  public BaseContentItem getSourceContentItem() {
    return sourceContentItem;
  }
  
  public Long getSourceContentItemId() {
    return sourceContentItem == null ? null : sourceContentItem.getId();
  }

  public void setSourceContentItem(BaseContentItem sourceContentItem) {
    this.sourceContentItem = sourceContentItem;
  }
  
  public boolean hasSourceInformation() {
    return sourceContentItem != null || !GlobalUtil.isContentEmpty(sourceDescription);
  }

  public String getTypeString() {
    return contentItemType.toString();
  }
  
  public String getTitleString() {
    return getTypeString();
  }
  
  public String getNavLinkString() {
    return contentItemType.getNavLinkString();
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
   * The string to use when introducing a byline for this content item.
   * @return the string to use
   */
  public String getBylineLeadin() {
    return ClientMessageHolder.consts.bylineLeadinBaseContentItem();
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
   * Returns true if this item should be considered for toplevel display in the UI's
   * "events" view.
   */
  public boolean displayTopLevel() {
    return false;
  }
  
  @Override
  public boolean equals(Object o) {
    return (o instanceof BaseContentItem) && this.getId() == ((BaseContentItem) o).getId();
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
}
