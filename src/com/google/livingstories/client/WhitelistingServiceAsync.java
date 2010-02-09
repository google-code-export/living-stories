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

package com.google.livingstories.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.List;

/**
 * Async version of {@link WhitelistingService}.
 */
public interface WhitelistingServiceAsync {
  
  void addWhitelistedUser(String email, AsyncCallback<WhitelistedUser> callback);
  
  void deleteWhitelistedUser(Long id, AsyncCallback<Void> callback);
  
  void getAllWhitelistedUsers(AsyncCallback<List<WhitelistedUser>> callback);
  
  void isRestrictedToWhitelist(AsyncCallback<Boolean> callback);
  
  void setRestrictedToWhitelist(Boolean restrictedToWhitelist, AsyncCallback<Boolean> callback);
}
