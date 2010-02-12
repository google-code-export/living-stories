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

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.livingstories.client.util.LivingStoryData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Class that manages data on the client side.  Having a centralized place
 * for all data means that different widgets can share the same backing
 * data without having to pass it around to each other or doing multiple requests.
 */
public class ClientCaches {
  private static final ContentRpcServiceAsync atomService = GWT.create(ContentRpcService.class);
  
  private static class Request<T> implements AsyncCallback<T>{
    private Boolean succeeded;
    private T result;
    private Throwable t;
    private List<AsyncCallback<T>> callbacks = new ArrayList<AsyncCallback<T>>();

    public Request(AsyncCallback<T> callback) {
      callbacks.add(callback);
    }
    
    public void getResult(AsyncCallback<T> callback) {
      if (succeeded == Boolean.TRUE) {
        callback.onSuccess(result);
      } else if (succeeded == Boolean.FALSE) {
        callback.onFailure(t);
      } else {
        callbacks.add(callback);
      }
    }
    
    @Override
    public void onFailure(Throwable t) {
      succeeded = false;
      this.t = t;
      for (AsyncCallback<T> callback : callbacks) {
        callback.onFailure(t);
      }
      callbacks.clear();
    }
    
    public void onSuccess(T result) {
      succeeded = true;
      this.result = result;
      for (AsyncCallback<T> callback : callbacks) {
        callback.onSuccess(result);
      }
      callbacks.clear();
    }
  }
  
  private static Request<List<EventAtom>> importantEventsCache;
  
  public static void getImportantEvents(AsyncCallback<List<EventAtom>> callback) {
    if (importantEventsCache == null) {
      importantEventsCache = new Request<List<EventAtom>>(callback);
      atomService.getImportantEventsForLivingStory(LivingStoryData.getLivingStoryId(),
          importantEventsCache);
    } else {
      importantEventsCache.getResult(callback);
    }
  }
  
  private static Request<List<PlayerAtom>> importantPlayersCache;
  
  public static void getImportantPlayers(AsyncCallback<List<PlayerAtom>> callback) {
    if (importantPlayersCache == null) {
      importantPlayersCache = new Request<List<PlayerAtom>>(callback);
      atomService.getImportantPlayersForLivingStory(LivingStoryData.getLivingStoryId(),
          importantPlayersCache);
    } else {
      importantPlayersCache.getResult(callback);
    }
  }
  
  private static Request<Map<Long, PlayerAtom>> contributorsCache;
  
  public static void getContributors(AsyncCallback<Map<Long, PlayerAtom>> callback) {
    if (contributorsCache == null) {
      contributorsCache = new Request<Map<Long, PlayerAtom>>(callback);
      atomService.getContributorsByIdForLivingStory(LivingStoryData.getLivingStoryId(),
          contributorsCache);
    } else {
      contributorsCache.getResult(callback);
    }
  }
  
  public static void getContributorsById(final Set<Long> contributorIds,
      final AsyncCallback<List<PlayerAtom>> callback) {
    AsyncCallback<Map<Long, PlayerAtom>> contributorsCallback =
      new AsyncCallback<Map<Long, PlayerAtom>>() {
        @Override
        public void onFailure(Throwable caught) {
          callback.onFailure(caught);
        }
        @Override
        public void onSuccess(Map<Long, PlayerAtom> result) {
          List<PlayerAtom> contributors = new ArrayList<PlayerAtom>();
          for (Long contributorId : contributorIds) {
            PlayerAtom contributor = result.get(contributorId);
            if (contributor != null) {
              contributors.add(contributor);
            }
          }
          callback.onSuccess(contributors);
        }
      };
    getContributors(contributorsCallback);
  }
}
