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

import com.google.appengine.repackaged.com.google.common.collect.ImmutableList;
import com.google.livingstories.server.AngleEntity;
import com.google.livingstories.server.BaseAtomEntityImpl;
import com.google.livingstories.server.JSONSerializable;
import com.google.livingstories.server.LivingStoryEntity;
import com.google.livingstories.server.dataservices.impl.PMF;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import javax.jdo.Extent;
import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Exports data from the appengine datastore into a json-formatted text file.
 * This works in both local and prod instances.
 */
public class DataExportServlet extends HttpServlet {
  public static List<Class<? extends JSONSerializable>> EXPORT_ENTITY_CLASSES =
    ImmutableList.<Class<? extends JSONSerializable>>of(
        LivingStoryEntity.class,
        AngleEntity.class,
        BaseAtomEntityImpl.class);

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    PersistenceManager pm = PMF.get().getPersistenceManager();

    try {
      JSONObject result = new JSONObject();

      for (Class<? extends JSONSerializable> entityClass : EXPORT_ENTITY_CLASSES) {
        addJSON(result, entityClass, pm);
      }
              
      resp.setContentType("application/json");
      resp.getOutputStream().write(result.toString().getBytes());
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
}
