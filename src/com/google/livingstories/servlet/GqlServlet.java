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

import com.google.common.collect.ImmutableList;
import com.google.gxp.base.GxpContext;
import com.google.livingstories.gxps.GqlServletHtml;
import com.google.livingstories.server.dataservices.impl.PMF;
import com.google.livingstories.server.rpcimpl.Caches;

import java.io.IOException;
import java.util.List;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GqlServlet extends HttpServlet {
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    render(req, resp, "", null);
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String query = req.getParameter("query");
    String delete = req.getParameter("delete");
    
    PersistenceManager pm = PMF.get().getPersistenceManager();
    Query q = pm.newQuery(query);
    
    try {
      List<Object> results;
      if (delete != null) {
        long numDeleted = q.deletePersistentAll();
        results = ImmutableList.<Object>of("Deleted " + numDeleted + " entities");
        Caches.clearAll();
      } else {
        results = ImmutableList.copyOf((List<Object>) q.execute());
      }
      render(req, resp, query, results);
    } finally {
      q.closeAll();
      pm.close();
    }
  }

  private void render(HttpServletRequest req, HttpServletResponse resp,
      String query, List<Object> results) throws IOException {
    GqlServletHtml.write(resp.getWriter(), new GxpContext(req.getLocale()), query, results);
  }
}
