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

package com.google.livingstories.server;

import com.google.appengine.api.datastore.Text;
import com.google.livingstories.client.AssetAtom;
import com.google.livingstories.client.AssetType;
import com.google.livingstories.client.AtomType;
import com.google.livingstories.client.BackgroundAtom;
import com.google.livingstories.client.BaseAtom;
import com.google.livingstories.client.DataAtom;
import com.google.livingstories.client.DefaultAtom;
import com.google.livingstories.client.EventAtom;
import com.google.livingstories.client.Importance;
import com.google.livingstories.client.Location;
import com.google.livingstories.client.NarrativeAtom;
import com.google.livingstories.client.NarrativeType;
import com.google.livingstories.client.PlayerAtom;
import com.google.livingstories.client.PlayerType;
import com.google.livingstories.client.PublishState;
import com.google.livingstories.client.QuoteAtom;
import com.google.livingstories.client.ReactionAtom;
import com.google.livingstories.client.StoryPlayerAtom;
import com.google.livingstories.client.util.FourthestateUtil;
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
public class BaseAtomEntityImpl implements Serializable, JSONSerializable, HasSerializableLspId {
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
  private AtomType atomType;

  // This is the HTML content.
  @Persistent
  private Text content;
  
  @Persistent
  private Importance importance = Importance.MEDIUM;

  @Persistent
  private Long livingStoryId;
  
  @Persistent
  private PublishState publishState = PublishState.DRAFT;
  
  @Persistent
  private Set<Long> contributorIds;

  @Persistent
  private Set<Long> linkedAtomIds;
  
  @Persistent
  private Set<Long> angleIds;
  
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
    private Text description;

    LocationEntity(Double latitude, Double longitude, String description) {
      this.latitude = latitude;
      this.longitude = longitude;
      this.description = new Text(description);
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
      this.description = new Text(description);
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
  private Text sourceDescription;
  
  @Persistent
  private Long sourceAtomId;
  
  /*** Event specific properties ***/
  
  @Persistent
  private Date startDate;
  
  @Persistent
  private Date endDate;
  
  @Persistent
  private Text eventUpdate;
  
  @Persistent
  private Text eventSummary;

  /*** Property shared by Background and Player types ***/
  
  @Persistent
  private String name;
  
  /*** Player specific properties ***/
  
  @Persistent
  private List<String> aliases;
  
  @Persistent
  private PlayerType playerType;
  
  @Persistent
  private Long photoAtomId; // Asset atom id
  
  /*** StoryPlayer specific properties ***/
  
  @Persistent
  private Long parentPlayerAtomId;
  
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
  private Text narrativeSummary;

  private BaseAtomEntityImpl() {}
  
  public BaseAtomEntityImpl(Date timestamp, AtomType atomType,
      String content, Importance importance, Long lspId) {
    this.timestamp = timestamp;
    this.atomType = atomType;
    this.content = new Text(content);
    this.importance = importance;
    this.livingStoryId = lspId;
  }
  
  
  /*** Methods in BaseAtomEntity ***/
  
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
    return content.getValue();
  }

  public void setContent(String content) {
    this.content = new Text(content);
  }

  public Importance getImportance() {
    return importance;
  }

  public void setImportance(Importance importance) {
    this.importance = importance;
  }

  public Long getLivingStoryId() {
    return livingStoryId;
  }

  public Long getLspId() {
    return getLivingStoryId();
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
    return FourthestateUtil.copySet(contributorIds);
  }
  
  public void setContributorIds(Set<Long> contributorIds) {
    this.contributorIds = FourthestateUtil.copySet(contributorIds);
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
  
  public Set<Long> getLinkedAtomIds() {
    return FourthestateUtil.copySet(linkedAtomIds);
  }
  
  public void setLinkedAtomIds(Set<Long> linkedAtomIds) {
    this.linkedAtomIds = FourthestateUtil.copySet(linkedAtomIds);
  }
  
  public void addLinkedAtomId(long linkedAtomId) {
    if (linkedAtomIds == null) {
      linkedAtomIds = new HashSet<Long>();
    }
    linkedAtomIds.add(linkedAtomId);
  }  

  public void removeLinkedAtomId(long atomId) {
    if (linkedAtomIds != null) {
      linkedAtomIds.remove(atomId);
    }
  }

  public Set<Long> getThemeIds() {
    return FourthestateUtil.copySet(angleIds);
  }
  
  public void setThemeIds(Set<Long> themeIds) {
    this.angleIds = FourthestateUtil.copySet(themeIds);
  }
  
  public void removeThemeId(long themeId) {
    if (angleIds != null) {
      angleIds.remove(themeId);
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
      this.sourceDescription = new Text(sourceDescription);
    }
  }

  public Long getSourceAtomId() {
    return sourceAtomId;
  }

  public void setSourceAtomId(Long sourceAtomId) {
    this.sourceAtomId = sourceAtomId;
  }

  /*** Methods in EventUpdateEntity ***/
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
    this.eventUpdate = new Text(eventUpdate);
  }
  
  public void setEventSummary(String eventSummary) {
    this.eventSummary = new Text(eventSummary);
  }
  
  /*** Methods in PlayerEntity ***/
  
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
  
  public Long getPhotoAtomId() {
    return photoAtomId;
  }
  
  public void setPhotoAtomId(Long photoAtomId) {
    this.photoAtomId = photoAtomId;
  }
  
  /*** Methods in StoryPlayerEntity ***/
  
  public Long getParentPlayerAtomId() {
    return parentPlayerAtomId;
  }
  
  public void setParentPlayerAtomId(Long parentPlayerAtomId) {
    this.parentPlayerAtomId = parentPlayerAtomId;
  }
  
  /*** Methods in AssetEntity ***/
  
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

  
  /*** Methods in NarrativeEntity ***/
  
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
    this.narrativeSummary = new Text(narrativeSummary);
  }
    
  public void copyFields(BaseAtom clientAtom) {
    setTimestamp(clientAtom.getTimestamp());
    setAtomType(clientAtom.getAtomType());
    setContent(fixLinks(trimWithBrs(clientAtom.getContent())));
    setImportance(clientAtom.getImportance());
    setLivingStoryId(clientAtom.getLivingStoryId());
    setThemeIds(clientAtom.getThemeIds());
    setContributorIds(clientAtom.getContributorIds());
    setLinkedAtomIds(clientAtom.getLinkedAtomIds());
    setPublishState(clientAtom.getPublishState());
    setLocation(clientAtom.getLocation());
    setSourceDescription(clientAtom.getSourceDescription());
    setSourceAtomId(clientAtom.getSourceAtomId());
    switch (clientAtom.getAtomType()) {
      case EVENT:
        EventAtom eventAtom = (EventAtom) clientAtom;
        setEventStartDate(eventAtom.getEventStartDate());
        setEventEndDate(eventAtom.getEventEndDate());
        setEventUpdate(fixLinks(trimWithBrs(eventAtom.getEventUpdate())));
        setEventSummary(fixLinks(trimWithBrs(eventAtom.getEventSummary())));
        break;
      case PLAYER:
        if (clientAtom.getLivingStoryId() == null) {
          PlayerAtom playerAtom = (PlayerAtom) clientAtom;
          setName(playerAtom.getName());
          setAliases(playerAtom.getAliases());
          setPlayerType(playerAtom.getPlayerType());
          setPhotoAtomId(playerAtom.getPhotoAtomId());
        } else {
          StoryPlayerAtom storyPlayerAtom = (StoryPlayerAtom) clientAtom;
          setParentPlayerAtomId(storyPlayerAtom.getParentPlayerAtom().getId());
        }
        break;
      case ASSET:
        AssetAtom assetAtom = (AssetAtom) clientAtom;
        setAssetType(assetAtom.getAssetType());
        setCaption(assetAtom.getCaption());
        setPreviewUrl(assetAtom.getPreviewUrl());
        break;
      case NARRATIVE:
        NarrativeAtom narrativeAtom = (NarrativeAtom) clientAtom;
        setHeadline(narrativeAtom.getHeadline());
        setNarrativeType(narrativeAtom.getNarrativeType());
        setIsStandalone(narrativeAtom.isStandalone());
        setNarrativeDate(narrativeAtom.getNarrativeDate());
        setNarrativeSummary(narrativeAtom.getNarrativeSummary());
        break;
      case BACKGROUND:
        BackgroundAtom backgroundAtom = (BackgroundAtom) clientAtom;
        setName(backgroundAtom.getConceptName());
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
  
  public BaseAtom toClientObject() {
    BaseAtom ret = toClientObjectImpl();
    ret.setPublishState(publishState);
    ret.setThemeIds(angleIds);
    ret.setLinkedAtomIds(linkedAtomIds);
    ret.setLocation(location.toClientObject());
    ret.setTimeElapsedSinceLastUpdate(TimeUtil.getElapsedTimeString(this.timestamp));
    
    BaseAtom sourceAtom = null;
    if (getSourceAtomId() != null) {
      try {
        sourceAtom = new ContentRpcImpl().getAtom(getSourceAtomId(), false);
      } catch (JDOException ex) {
        // leave sourceAtom as null;
      }
    }
    ret.setSourceAtom(sourceAtom);
    ret.setSourceDescription(getSourceDescription());
    
    return ret;
  }
  
  private BaseAtom toClientObjectImpl() {
    switch (getAtomType()) {
      case EVENT:
        if (getEventUpdate().isEmpty()) {
          return new DefaultAtom(getId(), getLivingStoryId());
        } else {
          return new EventAtom(getId(), getTimestamp(), getContributorIds(),
              getImportance(), getLivingStoryId(), getEventStartDate(), getEventEndDate(), 
              getEventUpdate(), getEventSummary(), getContent());
        }
      case PLAYER:
        Long livingStoryId = getLivingStoryId();
        if (livingStoryId == null) {
          AssetAtom photoAtom = null;
          if (getPhotoAtomId() != null) {
            try {
              photoAtom = (AssetAtom) new ContentRpcImpl().getAtom(getPhotoAtomId(), false);
            } catch (JDOException ex) {
              // leave photoAtom as null;
            }
          }
          return new PlayerAtom(getId(), getTimestamp(), getContributorIds(), getContent(), 
              getImportance(), getName(), getAliases(), getPlayerType(), photoAtom);
        } else {
          return new StoryPlayerAtom(getId(), getTimestamp(), getContributorIds(), getContent(),
              getImportance(), livingStoryId,
              (PlayerAtom) new ContentRpcImpl().getAtom(getParentPlayerAtomId(), false));
        }
      case QUOTE:
        return new QuoteAtom(getId(), getTimestamp(), getContributorIds(),
            getContent(), getImportance(), getLivingStoryId());
      case BACKGROUND:
        return new BackgroundAtom(getId(), getTimestamp(), getContributorIds(),
            getContent(), getImportance(), getLivingStoryId(), getName());
      case DATA:
        return new DataAtom(getId(), getTimestamp(), getContributorIds(),
            getContent(), getImportance(), getLivingStoryId());
      case ASSET:
        return new AssetAtom(getId(), getTimestamp(), getContributorIds(),
            getContent(), getImportance(), getLivingStoryId(),
            getAssetType(), getCaption(), getPreviewUrl());
      case NARRATIVE:
        return new NarrativeAtom(getId(), getTimestamp(), getContributorIds(),
            getContent(), getImportance(), getLivingStoryId(),
            getHeadline(), getNarrativeType(), isStandalone(), getNarrativeDate(), 
            getNarrativeSummary());
      case REACTION:
        return new ReactionAtom(getId(), getTimestamp(), getContributorIds(),
            getContent(), getImportance(), getLivingStoryId());
      default:
        throw new IllegalStateException("Unknown Atom Type");
    }
  }

  public static BaseAtomEntityImpl fromClientObject(BaseAtom clientAtom) {
    BaseAtomEntityImpl atomEntity = new BaseAtomEntityImpl();
    atomEntity.copyFields(clientAtom);
    return atomEntity;
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
      object.put("atomType", getAtomType().name());
      object.put("content", getContent());
      object.put("importance", getImportance().name());
      object.put("livingStoryId", getLivingStoryId());
      object.put("publishState", getPublishState().name());
      object.put("contributorIds", new JSONArray(getContributorIds()));
      object.put("linkedAtomIds", new JSONArray(getLinkedAtomIds()));
      object.put("angleIds", new JSONArray(getThemeIds()));
      object.put("location", location.toJSON());
      object.put("sourceDescription", getSourceDescription());
      object.put("sourceAtomId", getSourceAtomId());
      
      // Optional properties depending on atom type
      switch (getAtomType()) {
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
            if (photoAtomId != null) {
              object.put("photoAtomId", photoAtomId);
            }
          } else {
            object.put("parentPlayerAtomId", parentPlayerAtomId);
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
  
  public static BaseAtomEntityImpl fromJSON(JSONObject json) {
    DateFormat dateFormatter = SimpleDateFormat.getInstance();
    
    try {
      AtomType atomType = AtomType.valueOf(json.getString("atomType"));
      Long livingStoryId = json.has("livingStoryId") ? json.getLong("livingStoryId") : null;
      BaseAtomEntityImpl entity = new BaseAtomEntityImpl(
          dateFormatter.parse(json.getString("timestamp")), atomType, json.getString("content"),
          Importance.valueOf(json.getString("importance")), livingStoryId);
      
      entity.setPublishState(PublishState.valueOf(json.getString("publishState")));
      
      Set<Long> contributorIds = new HashSet<Long>();
      JSONArray contributorIdsJSON = json.getJSONArray("contributorIds");
      for (int i = 0; i < contributorIdsJSON.length(); i++) {
        contributorIds.add(contributorIdsJSON.getLong(i));
      }
      entity.setContributorIds(contributorIds);

      Set<Long> linkedAtomIds = new HashSet<Long>();
      JSONArray linkedAtomIdsJSON = json.getJSONArray("linkedAtomIds");
      for (int i = 0; i < linkedAtomIdsJSON.length(); i++) {
        linkedAtomIds.add(linkedAtomIdsJSON.getLong(i));
      }
      entity.setLinkedAtomIds(linkedAtomIds);
      
      Set<Long> themeIds = new HashSet<Long>();
      JSONArray themeIdsJSON = json.getJSONArray("angleIds");
      for (int i = 0; i < themeIdsJSON.length(); i++) {
        themeIds.add(themeIdsJSON.getLong(i));
      }
      entity.setThemeIds(themeIds);
      
      entity.setLocation(LocationEntity.fromJSON(json.getJSONObject("location")));
      
      if (json.has("sourceDescription")) {
        entity.setSourceDescription(json.getString("sourceDescription"));
      }
      if (json.has("sourceAtomId")) {
        entity.setSourceAtomId(json.getLong("sourceAtomId"));
      }
      
      // Optional properties depending on atom type
      switch (atomType) {
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
            if (json.has("photoAtomId")) {
              entity.setPhotoAtomId(json.getLong("photoAtomId"));
            }
          } else {
            entity.setParentPlayerAtomId(json.getLong("parentPlayerAtomId"));
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
