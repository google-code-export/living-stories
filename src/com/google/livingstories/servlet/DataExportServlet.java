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

import com.google.livingstories.server.dataservices.entities.BaseContentEntity;
import com.google.livingstories.server.dataservices.entities.JSONSerializable;
import com.google.livingstories.server.dataservices.entities.LivingStoryEntity;
import com.google.livingstories.server.dataservices.entities.ThemeEntity;
import com.google.livingstories.server.dataservices.impl.PMF;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import javax.jdo.Extent;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Exports data from the appengine datastore into a json-formatted text file.
 * This works in both local and prod instances.
 */
public class DataExportServlet extends HttpServlet {
  
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    PersistenceManager pm = PMF.get().getPersistenceManager();

    try {
      JSONObject result = new JSONObject();
      
      // Write the json for living story entities and theme entities first
      addJSON(result, LivingStoryEntity.class, pm);
      addJSON(result, ThemeEntity.class, pm);
      
      // Then write the json for content entities without living stories followed by content
      // entities with living stories
      JSONArray contentJson = new JSONArray();
      processContentEntities(contentJson, pm, true);
      processContentEntities(contentJson, pm, false);
      result.put(BaseContentEntity.class.getSimpleName(), contentJson);
      
      resp.setContentType("application/json");
      resp.getOutputStream().write(result.toString().getBytes());
    } catch (JSONException ex) {
      throw new RuntimeException(ex);
    } finally {
      pm.close();
    }
  }
  
  private <T extends JSONSerializable> void addJSON(
      JSONObject result, Class<T> entityClass, PersistenceManager pm) {
    Extent<T> entities = pm.getExtent(entityClass);

    try {
      JSONArray json = new JSONArray();
      for (T entity : entities) {
        json.put(entity.toJSON());
      }
      result.put(entityClass.getSimpleName(), json);

    } catch (JSONException ex) {
      throw new RuntimeException(ex);
    } finally {
      entities.closeAll();
    }
  }
  
  private void processContentEntities(JSONArray json, PersistenceManager pm,
      boolean nullLivingStory) {
    Query query = pm.newQuery(BaseContentEntity.class);
    if (nullLivingStory) {
      query.setFilter("livingStoryId == null");
    } else {
      query.setFilter("livingStoryId != null");
    }
    @SuppressWarnings("unchecked")
    List<BaseContentEntity> entities = (List<BaseContentEntity>) query.execute();
    try {
      for (BaseContentEntity entity : entities) {
        json.put(entity.toJSON());
      }
    } finally {
      query.closeAll();
    }
  }
  
}
