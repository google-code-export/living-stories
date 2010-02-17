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

package com.google.livingstories.server.dataservices;

import com.google.livingstories.client.Theme;

import java.util.List;

/**
 * Interface to create, modify and retrieve "Themes" for living stories from the datastore.
 */
public interface ThemeDataService {
  
  /**
   * Persist a Theme object to the datastore. It can either be new, in which case the id will be
   * null, or an existing theme, in which case the name of the theme will be updated. The
   * living story id cannot be updated for existing themes. The name of the theme should be unique
   * within the same living story.
   * @param theme the client theme each of whose fields should be persisted to the datastore. The
   * id can be null if this is a new Theme that hasn't been persisted yet. 
   * @throws IllegalArgumentException thrown if the theme name is empty or if another theme already
   * exists in the database for the same living story with the same name
   */
  Theme save(Theme theme) throws IllegalArgumentException;
  
  /**
   * Delete a Theme object from the datastore.
   * @param id database id of the theme to be deleted
   */
  void delete(Long id);
  
  /**
   * Delete all the Theme objects for a given living story.
   * @param livingStoryId database id of the Living Story for which all themes should be deleted
   */
  void deleteThemesForLivingStory(Long livingStoryId);
  
  /**
   * Fetch a theme from the datastore given its id. Returns null if no theme with the given id
   * is found.
   * @param id database id of the theme to be retrieved
   */
  Theme retrieveById(Long id);
  
  /**
   * Return all the themes for a living story.
   * @param livingStoryId database id of the living story whose themes should be returned
   */
  List<Theme> retrieveByLivingStory(Long livingStoryId);
}
