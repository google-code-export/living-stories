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

import com.google.appengine.api.users.UserServiceFactory;
import com.google.livingstories.client.UserRpcService;
import com.google.livingstories.client.WhitelistingService;
import com.google.livingstories.server.rpcimpl.UserRpcImpl;
import com.google.livingstories.server.rpcimpl.WhitelistingRpcImpl;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Filter to restrict the access to all user-facing pages, files and RPC services to whitelisted
 * and internal users only.
 */
public class PreviewAccessFilter implements Filter {
  private UserRpcService userInfoService;
  private WhitelistingService whitelistService;

  public PreviewAccessFilter() {
    this.userInfoService = new UserRpcImpl();
    this.whitelistService = new WhitelistingRpcImpl();
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest req = (HttpServletRequest) request;
    HttpServletResponse res = (HttpServletResponse) response;
    
    if (whitelistService.isRestrictedToWhitelist()) {
      if (userInfoService.isUserLoggedIn()) {
        if (userInfoService.isWhitelisted()) {
          // Good to go
          chain.doFilter(request, response);
        } else {
          // Cannot access because user is external and not on the whitelist
          res.sendError(HttpServletResponse.SC_FORBIDDEN, "Access forbidden.");
        }
      } else {
        // User needs to login first
        res.sendRedirect(UserServiceFactory.getUserService().createLoginURL(
            req.getRequestURL().toString()));
      }
    } else {
      // Good to go
      chain.doFilter(request, response);
    }
  }

  @Override
  public void init(FilterConfig filterConfig) {
  }
  
  @Override
  public void destroy() {
  }
}
