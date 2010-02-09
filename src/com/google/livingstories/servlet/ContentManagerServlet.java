// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.livingstories.servlet;

import com.google.livingstories.gxps.ContentManagerHtml;
import com.google.gxp.base.GxpContext;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet for the living stories start page.
 * 
 * @author hiller@google.com (Matt Hiller)
 */
public class ContentManagerServlet extends HttpServlet {
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    ContentManagerHtml.write(
        resp.getWriter(),
        new GxpContext(req.getLocale()),
        new ExternalServiceKeyChain(getServletContext()).getMapsKey());
  }
}
