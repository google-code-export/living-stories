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

package com.google.livingstories.servlet;

import com.google.appengine.repackaged.com.google.common.base.Function;
import com.google.appengine.repackaged.com.google.common.collect.ImmutableList;
import com.google.appengine.repackaged.com.google.common.collect.ImmutableMapBuilder;
import com.google.appengine.repackaged.com.google.common.collect.Lists;
import com.google.appengine.repackaged.com.google.common.collect.Maps;
import com.google.appengine.repackaged.com.google.common.collect.Sets;
import com.google.livingstories.client.AtomType;
import com.google.livingstories.server.AngleEntity;
import com.google.livingstories.server.BaseAtomEntityImpl;
import com.google.livingstories.server.HasSerializableLspId;
import com.google.livingstories.server.JSONSerializable;
import com.google.livingstories.server.LivingStoryEntity;
import com.google.livingstories.server.dataservices.impl.PMF;
import com.google.livingstories.server.rpcimpl.Caches;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jdo.Extent;
import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet that imports JSON data from a file into the appengine datastore.
 * This uses persistent, static values, so ensure that only one person is importing at a time!
 * 
 * To overcome the timeout issue, the system will:
 * 1. Accept an input file from a page that uses an ajax file uploader.
 * 2. Begin processing the data, checking the time periodically.
 * 3. If the timeout is approaching, stop what we're doing and post the status
 *    back to the request page.
 * 4. The input page will then issue another request if the run state is still 'running'.
 */
public class DataImportServlet extends HttpServlet {
  private static final long TIMEOUT_MILLIS = 2000; // 2 seconds
  private static final int SHARD_COUNT = 5;
  
  private static final Pattern goToAtomPattern = Pattern.compile("(goToAtom\\()(\\d+)");
  private static final Pattern lightboxPattern = 
      Pattern.compile("(showLightboxForAtom\\([\"']\\w+[\"'], ?)(\\d+)");
  private static final Pattern showAtomPopupPattern = Pattern.compile("(showAtomPopup\\()(\\d+)");
  private static final Pattern showSourcePopupPattern =
      Pattern.compile("(showSourcePopup\\()[\"'].+?[\"'],\\s*(\\d+)");
  private static final Pattern atomIdPattern = Pattern.compile("(atomid=\")(\\d+)");
  
  public static List<Class<? extends HasSerializableLspId>> EXPORTED_ENTITY_CLASSES = 
    ImmutableList.<Class<? extends HasSerializableLspId>>of(
        LivingStoryEntity.class,
        AngleEntity.class,
        BaseAtomEntityImpl.class);


  private static final Map<Class<?>, Function<JSONObject, String>> identifierFunctionMap =
      new ImmutableMapBuilder<Class<?>, Function<JSONObject, String>>()
          .put(LivingStoryEntity.class, createIdentifierFunction("id"))
          .put(AngleEntity.class, createIdentifierFunction("id"))
          .put(BaseAtomEntityImpl.class, createIdentifierFunction("id"))
          .getMap();

  private static Function<JSONObject, String> createIdentifierFunction(final String parameterName) {
    return new Function<JSONObject, String>() {
      @Override
      public String apply(JSONObject object) {
        return object.optString(parameterName);
      }
    };
  }

  private enum RunState {
    RUNNING, FINISHED, ERROR;
  }
  
  private static JSONObject inputData;
  private static String message;
  private static RunState runState = RunState.FINISHED;
  private static long startTime;
  
  private static List<PersistenceManager> pms;
  private static Map<Integer, List<JSONSerializable>> shardedEntities;
  private static List<Function<Void, Boolean>> workQueue;

  private static Map<String, LivingStoryEntity> lspMap;
  private static Map<String, AngleEntity> angleMap;
  private static Map<String, BaseAtomEntityImpl> contentMap;

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.getWriter().append("Method not supported");
  }
  
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    startTime = System.currentTimeMillis();

    message = "";

    if (req.getContentType().contains("multipart/form-data")) {
      try {
        ServletFileUpload upload = new ServletFileUpload();
        JSONObject data = null;
        boolean override = false;
        FileItemIterator iter = upload.getItemIterator(req);
        while (iter.hasNext()) {
          FileItemStream item = iter.next();
          if (item.getFieldName().equals("override")) {
            override = true;
          } else if (item.getFieldName().equals("data")) {
            data = new JSONObject(Streams.asString(item.openStream()));
          }
        }
        checkRunState(override);
        inputData = data; 
        setUp();
      } catch (FileUploadException ex) {
        throw new RuntimeException(ex);
      } catch (JSONException ex) {
        throw new RuntimeException(ex);
      }
    }
    
    try {
      process();
    } catch (Exception ex) {
      Writer result = new StringWriter();
      PrintWriter printWriter = new PrintWriter(result);
      ex.printStackTrace(printWriter);
      message = result.toString();
      runState = RunState.ERROR;
    } finally {
      if (runState != RunState.RUNNING) {
        tearDown();
      }
      Caches.clearAll();
    }
    
    resp.setContentType("text/html");
    resp.getWriter().append(message + "<br>" + runState.name());
  }
  
  private synchronized void checkRunState(boolean override) {
    if (runState == RunState.RUNNING) {
      if (override) {
        return;
      } else {
        throw new RuntimeException("Servlet is already running!");
      }
    }
    runState = RunState.RUNNING;
  }
  
  private void setUp() {
    pms = Lists.newArrayListWithCapacity(SHARD_COUNT);
    shardedEntities = Maps.newHashMap();
    for (int i = 0; i < SHARD_COUNT; i++) {
      pms.add(PMF.get().getPersistenceManager());
      shardedEntities.put(i, Lists.<JSONSerializable>newArrayList());
    }
    
    lspMap = Maps.newHashMap();
    angleMap = Maps.newHashMap();
    contentMap = Maps.newHashMap();

    workQueue = Lists.newArrayList();
    workQueue.add(new DeleteAllDataFunction());
    workQueue.add(new CreateEntitiesFunction<LivingStoryEntity>(
        LivingStoryEntity.class, lspMap));
    workQueue.add(new CreateEntitiesFunction<AngleEntity>(AngleEntity.class, angleMap));
    workQueue.add(new CreateEntitiesFunction<BaseAtomEntityImpl>(
        BaseAtomEntityImpl.class, contentMap));
    workQueue.add(new MapIdsFunction());
    workQueue.add(new MapAtomIdsFunction());
    workQueue.add(new MapAtomInlineIdsFunction());
    workQueue.add(new MapLspInlineIdsFunction());
    workQueue.add(new ClosePMsFuction());
    workQueue.add(new RemoveUnusedContributorsFunction());
  }
  
  private void process() {
    if (workQueue.isEmpty()) {
      runState = RunState.FINISHED;
    } else {
      Function<Void, Boolean> task = workQueue.get(0);
      boolean timedOut = task.apply(null);
      if (!timedOut) {
        workQueue.remove(0);
      }
    }
  }
  
  private void tearDown() {
    pms = null;
    shardedEntities = null;
    inputData = null;
    workQueue = null;
    lspMap = null;
    angleMap = null;
    contentMap = null;
  }

  private boolean timeout() {
    return (System.currentTimeMillis() - startTime) > TIMEOUT_MILLIS;
  }
  
  /**
   * Deletes all data in the datastore
   */
  private class DeleteAllDataFunction implements Function<Void, Boolean> {
    public Boolean apply(Void ignore) {
      message = "Deleting entities";
      
      // We don't care which pm we use to delete data with, so just use the first.
      PersistenceManager pm = pms.get(0);
      for (Class<? extends JSONSerializable> entityClass : EXPORTED_ENTITY_CLASSES) {
        Extent<? extends JSONSerializable> extent = pm.getExtent(entityClass);
        Iterator<? extends JSONSerializable> iter = extent.iterator();
        while (iter.hasNext()) {
          pm.deletePersistent(iter.next());
          if (timeout()) {
            return true;
          }
        }
      }
      return false;
    }
  }
  
  /**
   * Creates datastore entities given the JSON data, a persistence manager,
   * and an entity class to create.
   * Returns a map of old entity ids to the created entities.
   * This map will be used by code later on to translate old entity id references
   * to the new ones in other objects.
   * 
   * Note that this method uses reflection to access a static 'fromJSON' method
   * on the JSONSerializable object.  Interfaces can't define static methods,
   * so it is up to the user to ensure that this method is defined for all entities.
   */
  private class CreateEntitiesFunction<T extends JSONSerializable>
      implements Function<Void, Boolean> {
    private static final int BATCH_SIZE = 20;
    
    private Class<T> entityClass;
    private Map<String, T> entityMap;
    private int startValue = 0;
    
    public CreateEntitiesFunction(Class<T> entityClass, Map<String, T> entityMap) {
      this.entityClass = entityClass;
      this.entityMap = entityMap;
    }
    
    public Boolean apply(Void ignore) {
      message = "Creating entities";
      boolean timedOut = false;
      for (int i = 0; i < SHARD_COUNT; i++) {
        shardedEntities.get(i).clear();
      }
      
      try {
        JSONArray json;
        try {
          json = inputData.getJSONArray(entityClass.getSimpleName());  
        } catch (JSONException allowable) {
          // if the export was for LSP-specific data, some entity classes won't be mentioned.
          return false;
        }
        for (int i = startValue; i < json.length(); i++) {
          if (timeout() || i > startValue + BATCH_SIZE) {
            startValue = i;
            timedOut = true;
            break;
          }
          JSONObject object = json.getJSONObject(i);
          try {
            @SuppressWarnings("unchecked")
            T entityToAdd =
              (T) entityClass.getMethod("fromJSON", JSONObject.class).invoke(null, object);
            entityMap.put(identifierFunctionMap.get(entityClass).apply(object), entityToAdd);
            shardedEntities.get(i % SHARD_COUNT).add(entityToAdd);
          } catch (NoSuchMethodException ex) {
            throw new RuntimeException(ex);
          } catch (InvocationTargetException ex) {
            throw new RuntimeException(ex);
          } catch (IllegalAccessException ex) {
            throw new RuntimeException(ex);
          }
        }
      } catch (JSONException ex) {
        throw new RuntimeException(ex);
      }
      
      // Shard the entities across multiple persistence managers.  This helps
      // later on when we update the entities; we can thus save a fraction of the
      // datastore in each request.
      for (int i = 0; i < SHARD_COUNT; i++) {
        pms.get(i).makePersistentAll(shardedEntities.get(i));
      }
      return timedOut;
    }
  }

  /**
   * Maps the old entity ids to the new ones.
   */
  private class MapIdsFunction implements Function<Void, Boolean> {
    public Boolean apply(Void ignore) {
      message = "Mapping IDs";

      for (AngleEntity angle : angleMap.values()) {
        angle.setLspId(lspMap.get(angle.getLspId().toString()).getId());
      }
      return false;
    }
  }
  
  /**
   * Maps the old atom ids to the new ones
   */
  private class MapAtomIdsFunction implements Function<Void, Boolean> {
    private Iterator<BaseAtomEntityImpl> iter = null;
    
    @Override
    public Boolean apply(Void arg0) {
      message = "Mapping atom IDs";
      
      if (iter == null) {
        iter = contentMap.values().iterator();
      }
      // Map all the ids in the BaseAtomEntityImpl objects to the new ones
      while (iter.hasNext()) {
        BaseAtomEntityImpl content = iter.next();
        // Living Story ids
        if (content.getLivingStoryId() != null) {
          LivingStoryEntity lsp = lspMap.get(content.getLivingStoryId().toString());
          if (lsp != null) {
            content.setLivingStoryId(lsp.getId());
          }
        }
        
        // Angle ids
        Set<Long> angleIds = Sets.newHashSet();
        for (Long angleId : content.getThemeIds()) {
          AngleEntity angle = angleMap.get(angleId.toString());
          if (angle != null) {
            angleIds.add(angle.getId());
          }
        }
        content.setThemeIds(angleIds);
        
        // Contributor ids
        Set<Long> contributorIds = Sets.newHashSet();
        for (Long contributorId : content.getContributorIds()) {
          BaseAtomEntityImpl user = contentMap.get(contributorId.toString());
          if (user != null) {
            contributorIds.add(user.getId());
          }
        }
        content.setContributorIds(contributorIds);
        
        // Linked Atom ids
        Set<Long> linkedAtomIds = Sets.newHashSet();
        for (Long linkedAtomId : content.getLinkedAtomIds()) {
          BaseAtomEntityImpl atom = contentMap.get(linkedAtomId.toString());
          if (atom != null) {
            linkedAtomIds.add(atom.getId());
          }
        }
        content.setLinkedAtomIds(linkedAtomIds);
        
        // Photo Atom id
        if (content.getAtomType() == AtomType.PLAYER && content.getPhotoAtomId() != null) {
          BaseAtomEntityImpl atom = contentMap.get(content.getPhotoAtomId().toString());
          if (atom != null) {
            content.setPhotoAtomId(atom.getId());
          } else {
            content.setPhotoAtomId(null);
          }
        }
        
        if (timeout()) {
          return true;
        }
      }
      return false;
    }
  }
  
  /**
   * Maps atom ids in inline links in the rich content atom fields to the right values. 
   */
  private class MapAtomInlineIdsFunction implements Function<Void, Boolean> {
    private Iterator<BaseAtomEntityImpl> iter = null;
    
    public Boolean apply(Void ignore) {
      message = "Mapping atom inline IDs";

      if (iter == null) {
        iter = contentMap.values().iterator();
      }
      while (iter.hasNext()) {
        BaseAtomEntityImpl atom = iter.next();
        atom.setContent(matchAll(atom.getContent()));
        if (atom.getAtomType() == AtomType.ASSET) {
          atom.setCaption(matchAll(atom.getCaption()));
        } else if (atom.getAtomType() == AtomType.EVENT) {
          atom.setEventUpdate(matchAll(atom.getEventUpdate()));
          atom.setEventSummary(matchAll(atom.getEventSummary()));
        }
        if (timeout()) {
          return true;
        }
      }
      return false;
    }
    
  }

  /**
   * Maps atom ids in inline links in the lsp summary to the right values. 
   */
  private class MapLspInlineIdsFunction implements Function<Void, Boolean> {
    public Boolean apply(Void ignore) {
      message = "Mapping lsp inline IDs";

      for (LivingStoryEntity lsp : lspMap.values()) {
        for (LivingStoryEntity.Summary revision : lsp.getAllSummaryRevisions()) {
          revision.setContent(matchAll(revision.getContent()));
        }
      }
      return false;
    }
  }
  
  private String matchAll(String content) {
    if (content != null) {
      content = doMatch(content, goToAtomPattern);
      content = doMatch(content, lightboxPattern);
      content = doMatch(content, showAtomPopupPattern);
      content = doMatch(content, showSourcePopupPattern);
      content = doMatch(content, atomIdPattern);
    }
    return content;
  }
  
  private String doMatch(String content, Pattern pattern) {
    Matcher matcher = pattern.matcher(content);
    StringBuffer sb = new StringBuffer();
    while (matcher.find()) {
      String id = matcher.group(2);
      if (contentMap.containsKey(id)) {
        matcher.appendReplacement(sb, "$1" + contentMap.get(id).getId());
      } else {
        matcher.appendReplacement(sb, "$0");
      }
    }
    matcher.appendTail(sb);
    return sb.toString();
  }
  
  /**
   * Closes the persistence managers one by one, causing the entities in each one
   * to be persisted to the datastore. 
   */
  private class ClosePMsFuction implements Function<Void, Boolean> {
    public Boolean apply(Void ignore) {
      message = "Saving entities";

      while (!pms.isEmpty()) {
        pms.get(0).close();
        pms.remove(0);
        if (timeout()) {
          return true;
        }
      }
      return false;
    }
  }
  
  /**
   * This is a final cleanup step that deletes contributors that have been orphaned
   * from deleting the old version of an lsp.
   * 
   * This MUST happen after the persistence managers are closed, otherwise
   * the new entities won't be visible to the task.
   */
  private class RemoveUnusedContributorsFunction implements Function<Void, Boolean> {
    private List<BaseAtomEntityImpl> allUnassignedAtoms = Lists.newArrayList();
    private Set<Long> allUsedUnassignedIds = Sets.newHashSet();
    
    public Boolean apply(Void ignore) {
      message = "Removing orphaned entities";
      
      PersistenceManager pm = PMF.get().getPersistenceManager();

      try {
        // Get all atoms
        Extent<BaseAtomEntityImpl> atomEntities = pm.getExtent(BaseAtomEntityImpl.class);
        for (BaseAtomEntityImpl atom : atomEntities) {
          // Put the unassigned atoms in a list
          if (atom.getLivingStoryId() == null) {
            allUnassignedAtoms.add(atom);
          }
          
          // Put the ids of the contributors and photos in a set
          Set<Long> contributorIds = atom.getContributorIds();
          if (contributorIds != null) {
            allUsedUnassignedIds.addAll(contributorIds);
          }
          Long photoAtomId = atom.getPhotoAtomId();
          if (photoAtomId != null) {
            allUsedUnassignedIds.add(photoAtomId);
          }
        }
  
        // Delete all unassigned atoms that weren't in the used set
        for (BaseAtomEntityImpl unassignedAtom : allUnassignedAtoms) {
          if (!allUsedUnassignedIds.contains(unassignedAtom.getId())) {
            pm.deletePersistent(unassignedAtom);
          }
        }
      } finally {
        pm.close();
      }
      return false;
    }
  }
}
