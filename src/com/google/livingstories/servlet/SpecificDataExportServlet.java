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
import com.google.appengine.repackaged.com.google.common.collect.Lists;
import com.google.appengine.repackaged.com.google.common.collect.Sets;
import com.google.livingstories.client.LivingStory;
import com.google.livingstories.server.AngleEntity;
import com.google.livingstories.server.BaseAtomEntityImpl;
import com.google.livingstories.server.HasSerializableLspId;
import com.google.livingstories.server.LivingStoryEntity;
import com.google.livingstories.server.dataservices.impl.PMF;
import com.google.livingstories.server.rpcimpl.LivingStoryRpcImpl;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.jdo.Extent;
import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Exports data for specific LSPs into a json-formatted text file.
 */
public class SpecificDataExportServlet extends HttpServlet {
  public static String LSP_URLS_PARAM = "lspUrls";

  public static List<Class<? extends HasSerializableLspId>> EXPORT_LSP_ENTITY_CLASSES = 
    ImmutableList.<Class<? extends HasSerializableLspId>>of(
        LivingStoryEntity.class,
        AngleEntity.class,
        BaseAtomEntityImpl.class);

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    PersistenceManager pm = PMF.get().getPersistenceManager();
    String lspsParameter = req.getParameter(LSP_URLS_PARAM);

    if (lspsParameter == null) {
      lspsParameter = "";
    }
    
    try {
      JSONObject result = new JSONObject();
    
      List<String> validLspUrls = Lists.newArrayList();
      Set<Long> lspIds = Sets.newHashSet();
      LivingStoryRpcImpl serviceImpl = new LivingStoryRpcImpl();

      for (String lspUrl : lspsParameter.split(",")) {
        LivingStory story = serviceImpl.getLivingStoryByUrl(lspUrl);
        if (story != null) {
          validLspUrls.add(lspUrl);
          lspIds.add(story.getId());
        }
      }
      
      if (lspIds.isEmpty()) {
        resp.setContentType("text/plain");
        resp.getOutputStream().println(LSP_URLS_PARAM + " parameter not specified or invalid.");
        return;
      }

      try {
        result.put(LSP_URLS_PARAM, new JSONArray(validLspUrls));
      } catch (JSONException ex) {
        throw new RuntimeException(ex);
      }

      for (Class<? extends HasSerializableLspId> entityClass : EXPORT_LSP_ENTITY_CLASSES) {
        addLspAppropriateJSON(result, entityClass, pm, lspIds);
      }

      resp.setContentType("application/json");
      resp.getOutputStream().write(result.toString().getBytes());
    } finally {
      pm.close();
    }
  }

  private <T extends HasSerializableLspId> void addLspAppropriateJSON(
      JSONObject result, Class<T> entityClass, PersistenceManager pm, Set<Long> lspIds) {
    Extent<T> entities = pm.getExtent(entityClass);

    try {
      JSONArray json = new JSONArray();
      for (T entity : entities) {
        // Output the entity if it belongs to one of the specified lspIds, or doesn't belong to
        // any lsp at all.
        Long lspId = entity.getLspId();
        if (lspId == null || lspIds.contains(entity.getLspId())) {
          json.put(entity.toJSON());
        }
      }
      result.put(entityClass.getSimpleName(), json);

    } catch (JSONException ex) {
      throw new RuntimeException(ex);
    } finally {
      entities.closeAll();
    }
  }
}
