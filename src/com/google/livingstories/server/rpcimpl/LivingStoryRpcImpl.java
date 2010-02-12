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

package com.google.livingstories.server.rpcimpl;

import com.google.appengine.repackaged.com.google.common.collect.Lists;
import com.google.appengine.repackaged.com.google.common.collect.Maps;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.livingstories.client.AssetAtom;
import com.google.livingstories.client.AssetType;
import com.google.livingstories.client.AtomType;
import com.google.livingstories.client.AtomTypesBundle;
import com.google.livingstories.client.BaseAtom;
import com.google.livingstories.client.LivingStory;
import com.google.livingstories.client.LivingStoryRpcService;
import com.google.livingstories.client.NarrativeAtom;
import com.google.livingstories.client.PublishState;
import com.google.livingstories.client.Publisher;
import com.google.livingstories.client.StartPageBundle;
import com.google.livingstories.client.Theme;
import com.google.livingstories.client.util.DateUtil;
import com.google.livingstories.server.dataservices.LivingStoryDataService;
import com.google.livingstories.server.dataservices.ThemeDataService;
import com.google.livingstories.server.dataservices.impl.DataImplFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Implementation of the RPC calls related to getting data for living stories and themes.
 */
public class LivingStoryRpcImpl extends RemoteServiceServlet implements LivingStoryRpcService {
  private LivingStoryDataService livingStoryDataService;
  private ThemeDataService themeDataService;
  private ContentRpcImpl contentRpcService;
  private UserRpcImpl userRpcService;
  
  public LivingStoryRpcImpl() {
    this.livingStoryDataService = DataImplFactory.getLivingStoryService();
    this.themeDataService = DataImplFactory.getThemeService();
    this.contentRpcService = new ContentRpcImpl();
    this.userRpcService = new UserRpcImpl();
  }

  
  private static final Logger logger =
      Logger.getLogger(LivingStoryRpcImpl.class.getCanonicalName());

  @Override
  public synchronized LivingStory createLivingStory(String url, String title) {
    LivingStory story = livingStoryDataService.save(null, url, title, PublishState.DRAFT, 
        Publisher.NYT, "");
    Caches.clearLivingStories();
    Caches.clearStartPageBundle();
    return story;
  }
  
  @Override
  public synchronized List<LivingStory> getAllLivingStories(boolean onlyPublished) {
    List<LivingStory> allLivingStories = Caches.getLivingStories();
    if (allLivingStories == null) {
      allLivingStories = livingStoryDataService.retrieveAll(null, true);
      Caches.setLivingStories(allLivingStories);
    }

    if (!onlyPublished) {
      return allLivingStories;
    } else {
      List<LivingStory> publishedLivingStories = Lists.newArrayList();
      for (LivingStory story : allLivingStories) {
        if (story.getPublishState() == PublishState.PUBLISHED) {
          publishedLivingStories.add(story);
        }
      }
      return publishedLivingStories;
    }
  }
  
  @Override
  public synchronized List<LivingStory> getLivingStoriesForContentManager() {
    return livingStoryDataService.retrieveByPublisher(userRpcService.getPublisherForAdminUser(),
        null, true);
  }
  
  @Override
  public LivingStory getLivingStoryById(long id, boolean allSummaryRevisions) {
    return livingStoryDataService.retrieveById(id, !allSummaryRevisions);
  }

  @Override
  public LivingStory getLivingStoryByUrl(String url) {
    return livingStoryDataService.retrieveByUrlName(url, true);
  }
  
  @Override
  public synchronized LivingStory saveLivingStory(long id, String url, String title, 
      Publisher publisher, PublishState publishState, String summary) {
    LivingStory story = livingStoryDataService.save(id, url, title, publishState, publisher, 
        summary);
    Caches.clearLivingStories();
    Caches.clearStartPageBundle();
    return story;
  }
  
  @Override
  public synchronized void deleteLivingStory(long id) {
    livingStoryDataService.delete(id);
    Caches.clearLivingStories();
    Caches.clearLivingStoryAtoms(id);
    Caches.clearLivingStoryThemes(id);
    Caches.clearLivingStoryThemeInfo(id);
    Caches.clearStartPageBundle();
  }

  @Override
  public synchronized List<Theme> getThemesForLivingStory(long livingStoryId) {
    List<Theme> themes = Caches.getLivingStoryThemes(livingStoryId);
    if (themes == null) {
      themes = themeDataService.retrieveByLivingStory(livingStoryId);
      Caches.setLivingStoryThemes(livingStoryId, themes);
    }
    return themes;
  }

  /**
   * Returns a map from theme id to AtomTypesBundles. It lists the published atom and asset types
   * for the story, broken down by theme id. Each AtomTypeBundle includes an theme name field,
   * so this call will give client-facing code all the information it needs to present
   * themes and filters to the user.
   * @param livingStoryId living story id
   * @return a map of AtomTypeBundles appropriately filled in.
   */
  @Override
  public synchronized Map<Long, AtomTypesBundle> getThemeInfoForLivingStory(long livingStoryId) {
    Map<Long, AtomTypesBundle> result = Caches.getLivingStoryThemeInfo(livingStoryId); 
    if (result != null) {
      return result;
    }

    result = Maps.newHashMap();
    AtomTypesBundle globalBundle = new AtomTypesBundle("");
    result.put(null, globalBundle);

    // put an entry in the map for each theme, too. Since some themes may have no atoms,
    // we should not do this on-demand.
    for (Theme theme : getThemesForLivingStory(livingStoryId)) {
      result.put(theme.getId(), new AtomTypesBundle(theme.getName()));
    }
    
    // In principle, we could try to track when we've found every atom type that we care about, in
    // every possible theme, but it's such a micro-optimization at this point that it's not
    // worthwhile.
    List<BaseAtom> allAtoms = contentRpcService.getAtomsForLivingStory(livingStoryId, true);
    
    for (BaseAtom atom : allAtoms) {
      if (!addAtomToTypesBundle(atom, globalBundle)) {
        continue;
      }
      
      for (Long themeId : atom.getThemeIds()) {
        AtomTypesBundle themeBundle = result.get(themeId);
        if (themeBundle == null) {
          logger.warning("atom " + atom.getId() + " refers to themeId " + themeId + ", but this"
              + "theme does not appear to be in the story.");
        } else {
          addAtomToTypesBundle(atom, themeBundle);
        }
      }
    }

    Caches.setLivingStoryThemeInfo(livingStoryId, result);
    return result;
  }      

  /**
   * Adds information on BaseAtom atom to bundle.
   * @return false if this is an atom that should be ignored completely. Handy to the caller,
   * which can then avoid adding the atom to other, theme-specific types bundles.
   */
  private boolean addAtomToTypesBundle(BaseAtom atom, AtomTypesBundle bundle) {
    AtomType atomType = atom.getAtomType();
    
    // We don't want to show background and reaction atoms in the filters, so skip those entirely
    if (atomType == AtomType.BACKGROUND || atomType == AtomType.REACTION) {
      return false;
    }
    
    if (atomType == AtomType.NARRATIVE && ((NarrativeAtom)atom).isOpinion()) {
      bundle.opinionAvailable = true;
    } else {
      bundle.availableAtomTypes.add(atomType);
      if (atomType == AtomType.ASSET) {
        AssetType assetType = ((AssetAtom) atom).getAssetType();
        if (assetType == AssetType.DOCUMENT) {
          assetType = AssetType.LINK;
        }
        bundle.availableAssetTypes.add(assetType);
      }
    }
    
    return true;
  }

  @Override
  public synchronized Theme getThemeById(long id) {
    return themeDataService.retrieveById(id);
  }
  
  @Override
  public synchronized Theme saveTheme(Theme theme) {
    Theme result = themeDataService.save(theme);
    // Clear caches
    Caches.clearLivingStoryThemes(theme.getLivingStoryId());
    Caches.clearLivingStoryThemeInfo(theme.getLivingStoryId());
    return result;
  }
  
  @Override
  public synchronized void deleteTheme(long id) {
    themeDataService.delete(id);
    // Clear caches
    Caches.clearLivingStoryThemes(id);
    Caches.clearLivingStoryThemeInfo(id);
  }
  
  @Override
  public StartPageBundle getStartPageBundle() {
    StartPageBundle bundle = Caches.getStartPageBundle();
    if (bundle == null) {
      List<LivingStory> unsortedLivingStories = getAllLivingStories(true);
      Map<Long, List<BaseAtom>> storyIdToUpdateMap = new HashMap<Long, List<BaseAtom>>();
      List<LivingStoryAndLastUpdateTime> livingStoriesAndUpdateTimes = 
        new ArrayList<LivingStoryAndLastUpdateTime>();
      // For each living story, get the last 3 updates - the updates are sorted in reverse
      // chronological order
      for (LivingStory livingStory : unsortedLivingStories) {
        List<BaseAtom> updates = contentRpcService.getUpdatesForStartPage(livingStory.getId());
        storyIdToUpdateMap.put(livingStory.getId(), updates);
        livingStoriesAndUpdateTimes.add(
            new LivingStoryAndLastUpdateTime(livingStory,
                updates.isEmpty() ? null : updates.get(0).getTimestamp()));
      }
      
      // After we have the updates for each story, we want to sort them such that the story with
      // the latest update is first
      Collections.sort(livingStoriesAndUpdateTimes, LivingStoryAndLastUpdateTime.getComparator());
      List<LivingStory> sortedLivingStories = new ArrayList<LivingStory>();
      for (LivingStoryAndLastUpdateTime story : livingStoriesAndUpdateTimes) {
        sortedLivingStories.add(story.livingStory);
      }
      bundle = new StartPageBundle(sortedLivingStories, storyIdToUpdateMap);
      Caches.setStartPageBundle(bundle);
    }
    return bundle;
  }
  
  private static class LivingStoryAndLastUpdateTime {
    public LivingStory livingStory;
    public Date timeOfLatestUpdate;
    
    public LivingStoryAndLastUpdateTime(LivingStory livingStory, Date timeOfLatestUpdate) {
      this.livingStory = livingStory;
      this.timeOfLatestUpdate = timeOfLatestUpdate;
    }
    
    public Date timeOfLastChange() {
      return DateUtil.laterDate(livingStory.getLastChangeTimestamp(), timeOfLatestUpdate);
    }

    public static Comparator<LivingStoryAndLastUpdateTime> getComparator() {
      return new Comparator<LivingStoryAndLastUpdateTime>() {
        @Override
        public int compare(LivingStoryAndLastUpdateTime lhs, LivingStoryAndLastUpdateTime rhs) {
          Date lhsTime = lhs.timeOfLastChange();
          Date rhsTime = rhs.timeOfLastChange();
          // Sort stories with no updates to the end.
          if (rhsTime == null && lhsTime == null) {
            return 0;
          } else if (rhsTime == null) {
            return 1;
          } else if (lhsTime == null) {
            return -1;
          } else {
            return rhsTime.compareTo(lhsTime);
          }
        }
      };
    }
  }
}
