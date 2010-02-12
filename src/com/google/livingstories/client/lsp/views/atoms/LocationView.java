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

package com.google.livingstories.client.lsp.views.atoms;

import com.google.livingstories.client.Location;
import com.google.livingstories.client.util.LivingStoryData;
import com.google.gwt.ajaxloader.client.AjaxLoader;
import com.google.gwt.ajaxloader.client.AjaxLoader.AjaxLoaderOptions;
import com.google.gwt.core.client.GWT;
import com.google.gwt.maps.client.InfoWindow;
import com.google.gwt.maps.client.InfoWindowContent;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.control.SmallMapControl;
import com.google.gwt.maps.client.event.MarkerClickHandler;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Renders a map view with a 'Location' header.
 */
public class LocationView extends Composite {
  private static LocationViewUiBinder uiBinder = GWT.create(LocationViewUiBinder.class);
  interface LocationViewUiBinder extends UiBinder<Widget, LocationView> {
  }

  private static final int MAPS_WIDTH = 240;
  private static final int MAPS_HEIGHT = 240;
  private static final int MAPS_ZOOM = 10;

  @UiField SimplePanel content;

  private Location location;
  
  public LocationView(Location location) {
    this.location = location;
    
    initWidget(uiBinder.createAndBindUi(this));

    AjaxLoaderOptions options = AjaxLoaderOptions.newInstance();
    options.setOtherParms(LivingStoryData.getMapsKey() + "&sensor=false");
    
    // Instantiating the map via a runnable breaks horribly on firefox, for reasons
    // that are still mysterious to us. If we introduce some delay, though,
    // it works fine, and doesn't greatly hurt overall page functionality.
    AjaxLoader.loadApi("maps", "2", new Runnable() {
      @Override
      public void run() {
        new Timer() {
          @Override
          public void run() {
            content.add(createMap());
          }
        }.schedule(1000);
      }
    }, options);
  }

  private MapWidget createMap() {
    final String description = location.getDescription();
    LatLng latLng = LatLng.newInstance(location.getLatitude(), location.getLongitude());
    
    final MapWidget map = new MapWidget(latLng, MAPS_ZOOM);
    map.setSize(MAPS_WIDTH + "px", MAPS_HEIGHT + "px");
    map.addControl(new SmallMapControl());
    map.setDoubleClickZoom(true);
    map.setDraggable(true);
    map.setScrollWheelZoomEnabled(true);
    if (!description.isEmpty()) {
      final Marker marker = new Marker(latLng);
      map.addOverlay(marker);
      final InfoWindowContent iwc = new InfoWindowContent(description);
      marker.addMarkerClickHandler(new MarkerClickHandler() {
        @Override
        public void onClick(MarkerClickEvent event) {
          InfoWindow infoWindow = map.getInfoWindow();
          if (infoWindow.isVisible()) {
            infoWindow.close();
          } else {
            infoWindow.open(marker, iwc);
          }
        }
      });
      map.setTitle(description);
    }
    return map;
  }
}
