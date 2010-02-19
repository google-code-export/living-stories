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

package com.google.livingstories.server.dataservices.entities;

import com.google.livingstories.client.AssetContentItem;
import com.google.livingstories.client.AssetType;
import com.google.livingstories.client.BackgroundContentItem;
import com.google.livingstories.client.BaseContentItem;
import com.google.livingstories.client.ContentItemType;
import com.google.livingstories.client.DataContentItem;
import com.google.livingstories.client.DefaultContentItem;
import com.google.livingstories.client.EventContentItem;
import com.google.livingstories.client.Importance;
import com.google.livingstories.client.Location;
import com.google.livingstories.client.NarrativeContentItem;
import com.google.livingstories.client.NarrativeType;
import com.google.livingstories.client.PlayerContentItem;
import com.google.livingstories.client.PlayerType;
import com.google.livingstories.client.PublishState;
import com.google.livingstories.client.QuoteContentItem;
import com.google.livingstories.client.ReactionContentItem;
import com.google.livingstories.client.StoryPlayerContentItem;
import com.google.livingstories.client.util.GlobalUtil;
import com.google.livingstories.server.rpcimpl.ContentRpcImpl;
import com.google.livingstories.server.util.TimeUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jdo.JDOException;
import javax.jdo.annotations.Embedded;
import javax.jdo.annotations.EmbeddedOnly;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * This class represents a piece of content for a living story.
 */
@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class BaseContentEntity
    implements Serializable, JSONSerializable, HasSerializableLivingStoryId {
  private static final Pattern EXTERNAL_LINK_PATTERN =
      Pattern.compile("<a\\b[^>]+?\\bhref=\"(?!javascript:)[^>]+?>",
          Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
  private static final Pattern TARGET_ATTR_PATTERN =
      Pattern.compile("\\btarget=", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
  private static final String DEFAULT_LINK_TARGET = " target=\"_blank\"";

  @PrimaryKey
  @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
  private Long id;
  
  @Persistent
  private Date timestamp;
  
  // Enum for the classification of this content such as "Fact", "Context", "Analysis", etc.
  @Persistent
  private ContentItemType contentItemType;

  // This is the HTML content.
  @Persistent
  private LongStringHolder content;
  
  @Persistent
  private Importance importance = Importance.MEDIUM;

  @Persistent
  private Long livingStoryId;
  
  @Persistent
  private PublishState publishState = PublishState.DRAFT;
  
  @Persistent
  private Set<Long> contributorIds;

  @Persistent
  private Set<Long> linkedContentEntityIds;
  
  @Persistent
  private Set<Long> themeIds;
  
  @Persistent
  @Embedded
  private LocationEntity location;
  
  @PersistenceCapable
  @EmbeddedOnly
  public static class LocationEntity implements Serializable, JSONSerializable {
    // The appengine datastore actually supports a core value type of 'GeoPt' that consists of a
    // lat, long. But unfortunately, this has not been implemented in the Java API. So for now,
    // we'll just have to store them separately ourselves.
    @Persistent
    private Double latitude;
    
    @Persistent
    private Double longitude;
    
    @Persistent
    private LongStringHolder description;

    LocationEntity(Double latitude, Double longitude, String description) {
      this.latitude = latitude;
      this.longitude = longitude;
      this.description = new LongStringHolder(description);
    }

    public Double getLatitude() {
      return latitude;
    }

    public Double getLongitude() {
      return longitude;
    }

    public String getDescription() {
      return description.getValue();
    }
    
    public void setLatitude(Double latitude) {
      this.latitude = latitude;
    }

    public void setLongitude(Double longitude) {
      this.longitude = longitude;
    }

    public void setDescription(String description) {
      this.description = new LongStringHolder(description);
    }

    public Location toClientObject() {
      return new Location(latitude, longitude, description.getValue());
    }
    
    @Override
    public JSONObject toJSON() {
      JSONObject object = new JSONObject();
      try {
        object.put("latitude", latitude);
        object.put("longitude", longitude);
        object.put("description", description.getValue());
      } catch (JSONException ex) {
        throw new RuntimeException(ex);
      }
      return object;
    }
    
    public static LocationEntity fromJSON(JSONObject json) {
      try {
        return new LocationEntity(json.has("latitude") ? json.getDouble("latitude") : null,
            json.has("longitude") ? json.getDouble("longitude") : null,
            json.getString("description"));
      } catch (JSONException ex) {
        throw new RuntimeException(ex);
      }
    }
    
  }
  
  /*** Fields related to the source ***/
  @Persistent
  private LongStringHolder sourceDescription;
  
  @Persistent
  private Long sourceContentEntityId;
  
  /*** Event specific properties ***/
  
  @Persistent
  private Date startDate;
  
  @Persistent
  private Date endDate;
  
  @Persistent
  private LongStringHolder eventUpdate;
  
  @Persistent
  private LongStringHolder eventSummary;

  /*** Property shared by Background and Player types ***/
  
  @Persistent
  private String name;
  
  /*** Player specific properties ***/
  
  @Persistent
  private List<String> aliases;
  
  @Persistent
  private PlayerType playerType;
  
  @Persistent
  private Long photoContentEntityId;  // id of an associated asset entity
  
  /*** StoryPlayer specific properties ***/
  
  @Persistent
  private Long parentPlayerContentEntityId;
  
  /*** Asset specific properties ***/
  
  @Persistent
  private AssetType assetType;
  
  @Persistent
  private String caption;
  
  @Persistent
  private String previewUrl;
  
  
  /*** Narrative specific properties ***/
  
  @Persistent
  private String headline;
  
  @Persistent
  private NarrativeType narrativeType;
  
  @Persistent
  private Boolean isStandalone = true;
  
  @Persistent
  private Date narrativeDate;
  
  @Persistent
  private LongStringHolder narrativeSummary;

  private BaseContentEntity() {}
  
  public BaseContentEntity(Date timestamp, ContentItemType contentItemType,
      String content, Importance importance, Long livingStoryId) {
    this.timestamp = timestamp;
    this.contentItemType = contentItemType;
    this.content = new LongStringHolder(content);
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
    return content.getValue();
  }

  public void setContent(String content) {
    this.content = new LongStringHolder(content);
  }

  public Importance getImportance() {
    return importance;
  }

  public void setImportance(Importance importance) {
    this.importance = importance;
  }

  @Override
  public Long getLivingStoryId() {
    return livingStoryId;
  }

  public void setLivingStoryId(Long livingStoryId) {
    this.livingStoryId = livingStoryId;
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

  public void addContributorId(long contributorId) {
    if (contributorIds == null) {
      contributorIds = new HashSet<Long>();
    }
    contributorIds.add(contributorId);
  }
  
  public void removeContributorId(long contributorId) {
    if (contributorIds != null) {
      contributorIds.remove(contributorId);
    }
  }
  
  public Set<Long> getLinkedContentEntityIds() {
    return GlobalUtil.copySet(linkedContentEntityIds);
  }
  
  public void setLinkedContentEntityIds(Set<Long> linkedContentEntityIds) {
    this.linkedContentEntityIds = GlobalUtil.copySet(linkedContentEntityIds);
  }
  
  public void addLinkedContentEntityId(long linkedContentEntityId) {
    if (linkedContentEntityIds == null) {
      linkedContentEntityIds = new HashSet<Long>();
    }
    linkedContentEntityIds.add(linkedContentEntityId);
  }  

  public void removeLinkedContentEntityId(long linkedContentEntityId) {
    if (linkedContentEntityIds != null) {
      linkedContentEntityIds.remove(linkedContentEntityId);
    }
  }

  public Set<Long> getThemeIds() {
    return GlobalUtil.copySet(themeIds);
  }
  
  public void setThemeIds(Set<Long> themeIds) {
    this.themeIds = GlobalUtil.copySet(themeIds);
  }
  
  public void removeThemeId(long themeId) {
    if (themeIds != null) {
      themeIds.remove(themeId);
    }
  }
  
  public Location getLocation() {
    return location.toClientObject();
  }
  
  public void setLocation(Location location) {
    if (this.location == null) {
      this.location = new LocationEntity(location.getLatitude(), location.getLongitude(), 
          location.getDescription());
    } else {
      this.location.setLatitude(location.getLatitude());
      this.location.setLongitude(location.getLongitude());
      this.location.setDescription(location.getDescription());
    }
  }
  
  private void setLocation(LocationEntity location) {
    this.location = location;
  }

  public String getSourceDescription() {
    return sourceDescription == null ? null : sourceDescription.getValue();
  }

  public void setSourceDescription(String sourceDescription) {
    if (sourceDescription != null) {
      this.sourceDescription = new LongStringHolder(sourceDescription);
    }
  }

  public Long getSourceContentEntityId() {
    return sourceContentEntityId;
  }

  public void setSourceContentEntityId(Long sourceContentEntityId) {
    this.sourceContentEntityId = sourceContentEntityId;
  }

  public Date getEventStartDate() {
    return startDate;
  }
  
  public Date getEventEndDate() {
    return endDate;
  }
  
  public String getEventUpdate() {
    return eventUpdate.getValue();
  }

  public String getEventSummary() {
    return eventSummary.getValue();
  }
  
  public void setEventStartDate(Date eventStartDate) {
    this.startDate = eventStartDate;
  }
  
  public void setEventEndDate(Date eventEndDate) {
    this.endDate = eventEndDate;
  }

  public void setEventUpdate(String eventUpdate) {
    this.eventUpdate = new LongStringHolder(eventUpdate);
  }
  
  public void setEventSummary(String eventSummary) {
    this.eventSummary = new LongStringHolder(eventSummary);
  }
  
  public String getName() {
    return name;
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
  public List<String> getAliases() {
    return aliases;
  }
  
  public void setAliases(List<String> aliases) {
    this.aliases = aliases;
  }
  
  public PlayerType getPlayerType() {
    return playerType;
  }
  
  public void setPlayerType(PlayerType playerType) {
    this.playerType = playerType;
  }
  
  public Long getPhotoContentEntityId() {
    return photoContentEntityId;
  }
  
  public void setPhotoContentEntityId(Long photoContentEntityId) {
    this.photoContentEntityId = photoContentEntityId;
  }
  
  public Long getParentPlayerContentEntityId() {
    return parentPlayerContentEntityId;
  }
  
  public void setParentPlayerContentEntityId(Long parentPlayerContentEntityId) {
    this.parentPlayerContentEntityId = parentPlayerContentEntityId;
  }
  
  public AssetType getAssetType() {
    return assetType;
  }
  
  public void setAssetType(AssetType assetType) {
    this.assetType = assetType;
  }
  
  public String getCaption() {
    return caption;
  }
  
  public void setCaption(String caption) {
    this.caption = caption;
  }

  public String getPreviewUrl() {
    return previewUrl;
  }
  
  public void setPreviewUrl(String previewUrl) {
    this.previewUrl = previewUrl;
  }

  
  public String getHeadline() {
    return headline;
  }
  
  public void setHeadline(String headline) {
    this.headline = headline;
  }
  
  public NarrativeType getNarrativeType() {
    return narrativeType;
  }
  
  public void setNarrativeType(NarrativeType narrativeType) {
    this.narrativeType = narrativeType;
  }

  public boolean isStandalone() {
    return isStandalone;
  }

  public void setIsStandalone(boolean isStandalone) {
    this.isStandalone = isStandalone;
  }

  public Date getNarrativeDate() {
    return narrativeDate;
  }

  public void setNarrativeDate(Date narrativeDate) {
    this.narrativeDate = narrativeDate;
  }

  public String getNarrativeSummary() {
    return narrativeSummary == null ? null : narrativeSummary.getValue();
  }

  public void setNarrativeSummary(String narrativeSummary) {
    this.narrativeSummary = new LongStringHolder(narrativeSummary);
  }
    
  public void copyFields(BaseContentItem clientContentItem) {
    setTimestamp(clientContentItem.getTimestamp());
    setContentItemType(clientContentItem.getContentItemType());
    setContent(fixLinks(trimWithBrs(clientContentItem.getContent())));
    setImportance(clientContentItem.getImportance());
    setLivingStoryId(clientContentItem.getLivingStoryId());
    setThemeIds(clientContentItem.getThemeIds());
    setContributorIds(clientContentItem.getContributorIds());
    setLinkedContentEntityIds(clientContentItem.getLinkedContentItemIds());
    setPublishState(clientContentItem.getPublishState());
    setLocation(clientContentItem.getLocation());
    setSourceDescription(clientContentItem.getSourceDescription());
    setSourceContentEntityId(clientContentItem.getSourceContentItemId());
    switch (clientContentItem.getContentItemType()) {
      case EVENT:
        EventContentItem eventContentItem = (EventContentItem) clientContentItem;
        setEventStartDate(eventContentItem.getEventStartDate());
        setEventEndDate(eventContentItem.getEventEndDate());
        setEventUpdate(fixLinks(trimWithBrs(eventContentItem.getEventUpdate())));
        setEventSummary(fixLinks(trimWithBrs(eventContentItem.getEventSummary())));
        break;
      case PLAYER:
        if (clientContentItem.getLivingStoryId() == null) {
          PlayerContentItem playerContentItem = (PlayerContentItem) clientContentItem;
          setName(playerContentItem.getName());
          setAliases(playerContentItem.getAliases());
          setPlayerType(playerContentItem.getPlayerType());
          setPhotoContentEntityId(playerContentItem.getPhotoContentItemId());
        } else {
          StoryPlayerContentItem storyPlayerContentItem =
              (StoryPlayerContentItem) clientContentItem;
          setParentPlayerContentEntityId(
              storyPlayerContentItem.getParentPlayerContentItem().getId());
        }
        break;
      case ASSET:
        AssetContentItem assetContentItem = (AssetContentItem) clientContentItem;
        setAssetType(assetContentItem.getAssetType());
        setCaption(assetContentItem.getCaption());
        setPreviewUrl(assetContentItem.getPreviewUrl());
        break;
      case NARRATIVE:
        NarrativeContentItem narrativeContentItem = (NarrativeContentItem) clientContentItem;
        setHeadline(narrativeContentItem.getHeadline());
        setNarrativeType(narrativeContentItem.getNarrativeType());
        setIsStandalone(narrativeContentItem.isStandalone());
        setNarrativeDate(narrativeContentItem.getNarrativeDate());
        setNarrativeSummary(narrativeContentItem.getNarrativeSummary());
        break;
      case BACKGROUND:
        BackgroundContentItem backgroundContentItem = (BackgroundContentItem) clientContentItem;
        setName(backgroundContentItem.getConceptName());
        break;
    }
  }
  
 /**
  * Returns a form of input that's trimmed, including the removal of any leading or trailing <br/>s
  * @input 
  * @return trimmed input
  */
 private static String trimWithBrs(String input) {
   return input.replaceFirst("^(\\s|<br/?>|<br></br>)+", "")
       .replaceFirst("(\\s|<br/?>|<br></br>)+$", "").trim();
 }
  
  /**
   * Examines the content for anchor tags that look like they point to external pages
   *   (in general, any link that doesn't start with 'javascript:', and adds a
   *   target="_blank" attribute to them, if there isn't a target attribute already.
   * @param content Content to fix up.
   * @return The modified content string, with links fixed to pop up new windows.
   */
  private String fixLinks(String content) {
    Matcher matcher = EXTERNAL_LINK_PATTERN.matcher(content);
    StringBuffer sb = new StringBuffer();
    while (matcher.find()) {
      String link = matcher.group(0);
      if (!TARGET_ATTR_PATTERN.matcher(link).find()) {
        link = link.replace(">", DEFAULT_LINK_TARGET + ">");
      }
      matcher.appendReplacement(sb, Matcher.quoteReplacement(link));
    }
    matcher.appendTail(sb);
    return sb.toString();
  }
  
  public BaseContentItem toClientObject() {
    BaseContentItem ret = toClientObjectImpl();
    ret.setPublishState(publishState);
    ret.setThemeIds(themeIds);
    ret.setLinkedContentItemIds(linkedContentEntityIds);
    ret.setLocation(location.toClientObject());
    ret.setTimeElapsedSinceLastUpdate(TimeUtil.getElapsedTimeString(this.timestamp));
    
    BaseContentItem sourceContentItem = null;
    Long sourceContentEntityId = getSourceContentEntityId();
    if (sourceContentEntityId != null) {
      try {
        sourceContentItem = new ContentRpcImpl().getContentItem(sourceContentEntityId, false);
      } catch (JDOException ex) {
        // leave sourceContentEntity as null;
      }
    }
    ret.setSourceContentItem(sourceContentItem);
    ret.setSourceDescription(getSourceDescription());
    
    return ret;
  }
  
  private BaseContentItem toClientObjectImpl() {
    switch (getContentItemType()) {
      case EVENT:
        if (getEventUpdate().isEmpty()) {
          return new DefaultContentItem(getId(), getLivingStoryId());
        } else {
          return new EventContentItem(getId(), getTimestamp(), getContributorIds(),
              getImportance(), getLivingStoryId(), getEventStartDate(), getEventEndDate(), 
              getEventUpdate(), getEventSummary(), getContent());
        }
      case PLAYER:
        Long livingStoryId = getLivingStoryId();
        if (livingStoryId == null) {
          AssetContentItem photoContentItem = null;
          if (getPhotoContentEntityId() != null) {
            try {
              photoContentItem = (AssetContentItem) new ContentRpcImpl().getContentItem(
                  getPhotoContentEntityId(), false);
            } catch (JDOException ex) {
              // leave photoContentItem as null;
            }
          }
          return new PlayerContentItem(getId(), getTimestamp(), getContributorIds(), getContent(), 
              getImportance(), getName(), getAliases(), getPlayerType(), photoContentItem);
        } else {
          return new StoryPlayerContentItem(getId(), getTimestamp(), getContributorIds(),
              getContent(), getImportance(), livingStoryId,
              (PlayerContentItem) new ContentRpcImpl().getContentItem(
                  getParentPlayerContentEntityId(), false));
        }
      case QUOTE:
        return new QuoteContentItem(getId(), getTimestamp(), getContributorIds(),
            getContent(), getImportance(), getLivingStoryId());
      case BACKGROUND:
        return new BackgroundContentItem(getId(), getTimestamp(), getContributorIds(),
            getContent(), getImportance(), getLivingStoryId(), getName());
      case DATA:
        return new DataContentItem(getId(), getTimestamp(), getContributorIds(),
            getContent(), getImportance(), getLivingStoryId());
      case ASSET:
        return new AssetContentItem(getId(), getTimestamp(), getContributorIds(),
            getContent(), getImportance(), getLivingStoryId(),
            getAssetType(), getCaption(), getPreviewUrl());
      case NARRATIVE:
        return new NarrativeContentItem(getId(), getTimestamp(), getContributorIds(),
            getContent(), getImportance(), getLivingStoryId(),
            getHeadline(), getNarrativeType(), isStandalone(), getNarrativeDate(), 
            getNarrativeSummary());
      case REACTION:
        return new ReactionContentItem(getId(), getTimestamp(), getContributorIds(),
            getContent(), getImportance(), getLivingStoryId());
      default:
        throw new IllegalStateException("Unknown Content Item Type");
    }
  }

  public static BaseContentEntity fromClientObject(BaseContentItem clientContentItem) {
    BaseContentEntity contentEntity = new BaseContentEntity();
    contentEntity.copyFields(clientContentItem);
    return contentEntity;
  }
  
  @Override
  public String toString() {
    try {
      return toJSON().toString(2);
    } catch (JSONException ex) {
      throw new RuntimeException(ex);
    }
  }
  
  @Override
  public JSONObject toJSON() {
    JSONObject object = new JSONObject();
    try {
      object.put("id", getId());
      object.put("timestamp", SimpleDateFormat.getInstance().format(getTimestamp()));
      object.put("contentItemType", getContentItemType().name());
      object.put("content", getContent());
      object.put("importance", getImportance().name());
      object.put("livingStoryId", getLivingStoryId());
      object.put("publishState", getPublishState().name());
      object.put("contributorIds", new JSONArray(getContributorIds()));
      object.put("linkedContentEntityIds", new JSONArray(getLinkedContentEntityIds()));
      object.put("themeIds", new JSONArray(getThemeIds()));
      object.put("location", location.toJSON());
      object.put("sourceDescription", getSourceDescription());
      object.put("sourceContentEntityId", getSourceContentEntityId());
      
      // Optional properties depending on contentItemType
      switch (getContentItemType()) {
        case EVENT:
          if (startDate != null) {
            object.put("startDate", SimpleDateFormat.getInstance().format(startDate));
          }
          if (endDate != null) {
            object.put("endDate", SimpleDateFormat.getInstance().format(endDate));
          }
          object.put("eventUpdate", getEventUpdate());
          object.put("eventSummary", getEventSummary());
          break;
        case PLAYER:
          if (getLivingStoryId() == null) {
            object.put("name", name);
            object.put("playerType", playerType.name());
            if (aliases != null && !aliases.isEmpty()) {
              object.put("aliases", new JSONArray(getAliases()));
            }
            if (photoContentEntityId != null) {
              object.put("photoContentEntityId", photoContentEntityId);
            }
          } else {
            object.put("parentPlayerContentEntityId", parentPlayerContentEntityId);
          }
          break;
        case ASSET:
          object.put("assetType", assetType.name());
          object.put("caption", getCaption());
          object.put("previewUrl", getPreviewUrl());
          break;
        case NARRATIVE:
          object.put("headline", getHeadline());
          object.put("narrativeType", narrativeType.name());
          object.put("isStandalone", isStandalone);
          if (narrativeDate != null) {
            object.put("narrativeDate", SimpleDateFormat.getInstance().format(narrativeDate));
          }
          object.put("narrativeSummary", getNarrativeSummary());
          break;
        case BACKGROUND:
          object.put("name", name);
          break;
      }
    } catch (JSONException ex) {
      throw new RuntimeException(ex);
    }
    return object;
  }
  
  public static BaseContentEntity fromJSON(JSONObject json) {
    DateFormat dateFormatter = SimpleDateFormat.getInstance();
    
    try {
      ContentItemType contentItemType = ContentItemType.valueOf(json.getString("contentItemType"));
      Long livingStoryId = json.has("livingStoryId") ? json.getLong("livingStoryId") : null;
      BaseContentEntity entity = new BaseContentEntity(
          dateFormatter.parse(json.getString("timestamp")), contentItemType,
          json.getString("content"), Importance.valueOf(json.getString("importance")),
          livingStoryId);
      
      entity.setPublishState(PublishState.valueOf(json.getString("publishState")));
      
      Set<Long> contributorIds = new HashSet<Long>();
      JSONArray contributorIdsJSON = json.getJSONArray("contributorIds");
      for (int i = 0; i < contributorIdsJSON.length(); i++) {
        contributorIds.add(contributorIdsJSON.getLong(i));
      }
      entity.setContributorIds(contributorIds);

      Set<Long> linkedContentEntityIds = new HashSet<Long>();
      JSONArray linkedContentEntityIdsJSON = json.getJSONArray("linkedContentEntityIds");
      for (int i = 0; i < linkedContentEntityIdsJSON.length(); i++) {
        linkedContentEntityIds.add(linkedContentEntityIdsJSON.getLong(i));
      }
      entity.setLinkedContentEntityIds(linkedContentEntityIds);
      
      Set<Long> themeIds = new HashSet<Long>();
      JSONArray themeIdsJSON = json.getJSONArray("themeIds");
      for (int i = 0; i < themeIdsJSON.length(); i++) {
        themeIds.add(themeIdsJSON.getLong(i));
      }
      entity.setThemeIds(themeIds);
      
      entity.setLocation(LocationEntity.fromJSON(json.getJSONObject("location")));
      
      if (json.has("sourceDescription")) {
        entity.setSourceDescription(json.getString("sourceDescription"));
      }
      if (json.has("sourceContentEntityId")) {
        entity.setSourceContentEntityId(json.getLong("sourceContentEntityId"));
      }
      
      // Optional properties depending on contentItemType
      switch (contentItemType) {
        case EVENT:
          if (json.has("startDate")) {
            entity.setEventStartDate(dateFormatter.parse(json.getString("startDate")));
          }
          if (json.has("endDate")) {
            entity.setEventEndDate(dateFormatter.parse(json.getString("endDate")));
          }
          entity.setEventUpdate(json.getString("eventUpdate"));
          entity.setEventSummary(json.getString("eventSummary"));
          break;
        case PLAYER:
          if (livingStoryId == null) {
            entity.setName(json.getString("name"));
            entity.setPlayerType(PlayerType.valueOf(json.getString("playerType")));
            if (json.has("aliases")) {
              List<String> aliases = new ArrayList<String>();
              JSONArray aliasesJSON = json.getJSONArray("aliases");
              for (int i = 0; i < aliasesJSON.length(); i++) {
                aliases.add(aliasesJSON.getString(i));
              }
              entity.setAliases(aliases);
            }
            if (json.has("photoContentEntityId")) {
              entity.setPhotoContentEntityId(json.getLong("photoContentEntityId"));
            }
          } else {
            entity.setParentPlayerContentEntityId(json.getLong("parentPlayerContentEntityId"));
          }
          break;
        case ASSET:
          entity.setAssetType(AssetType.valueOf(json.getString("assetType")));
          entity.setCaption(json.getString("caption"));
          entity.setPreviewUrl(json.getString("previewUrl"));
          break;
        case NARRATIVE:
          entity.setHeadline(json.getString("headline"));
          entity.setNarrativeType(NarrativeType.valueOf(json.getString("narrativeType")));
          // To convert exports that may have been done before the isStandalone field was
          // introduced, set the value to 'false' if the field is not present
          entity.setIsStandalone(
              json.has("isStandalone") ? json.getBoolean("isStandalone") : false);
          if (json.has("narrativeDate")) {
            entity.setNarrativeDate(dateFormatter.parse(json.getString("narrativeDate")));
          }
          if (json.has("narrativeSummary")) {
            entity.setNarrativeSummary(json.getString("narrativeSummary"));
          }
          break;
        case BACKGROUND:
          if (json.has("name")) {
            entity.setName(json.getString("name"));
          }
          break;
      }
      
      return entity;
    } catch (JSONException ex) {
      throw new RuntimeException(ex);
    } catch (ParseException ex) {
      throw new RuntimeException(ex);
    }
  }
}
