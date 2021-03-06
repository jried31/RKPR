/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.ridekeeper.util;

import android.content.Context;
import android.location.Location;

import com.example.ridekeeper.R;

/**
 * Defines app-wide constants and utilities
 */
public final class LocationUtils {

    // Debugging tag for the application
    public static final String GOOGLE_SERVICE = "GooglePlayServices";
    public static final String LOCATION_UPDATE = "LocationUpdate";

    // Name of shared preferences repository that stores persistent state
    public static final String SHARED_PREFERENCES =
            "com.example.ridekeeper.SHARED_PREFERENCES";

    // Key for storing the "updates requested" flag in shared preferences
    public static final String KEY_UPDATES_REQUESTED =
            "com.example.ridekeeper.KEY_UPDATES_REQUESTED";

    /*
     * Define a request code to send to Google Play services
     * This code is returned in Activity.onActivityResult
     */
    public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    /*
     * Constants for location update parameters
     */
    // Milliseconds per second
    public static final int MILLISECONDS_PER_SECOND = 1000;

    public static final int SECONDS_PER_MIN = 60;

    // The update interval
    public static final int UPDATE_INTERVAL_IN_SECONDS = 30;

    // A fast interval ceiling
    public static final int FAST_CEILING_IN_SECONDS = 10;

    // Update interval in milliseconds
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS =
            MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;

    // A fast ceiling of update intervals, used when the app is visible
    public static final long FAST_INTERVAL_CEILING_IN_MILLISECONDS =
            MILLISECONDS_PER_SECOND * FAST_CEILING_IN_SECONDS;

	public static final int LOCATION_UPDATE_RATE = MILLISECONDS_PER_SECOND * SECONDS_PER_MIN * 5; //Update Location every 5 mins 

    // Create an empty string for initializing strings
    public static final String EMPTY_STRING = new String();

    /**
     * Get the latitude and longitude from the Location object returned by
     * Location Services.
     *
     * @param currentLocation A Location object containing the current location
     * @return The latitude and longitude of the current location, or null if no
     * location is available.
     */
    public static String getLatLng(Context context, Location currentLocation) {
        // If the location is valid
        if (currentLocation != null) {

            // Return the latitude and longitude as strings
            return context.getString(
                    R.string.latitude_longitude,
                    currentLocation.getLatitude(),
                    currentLocation.getLongitude());
        } else {

            // Otherwise, return the empty string
            return EMPTY_STRING;
        }
    }
    

    private static final long MAX_LOCATION_AGE_MS = 60 * 1000; // 1 minute

    private LocationUtils() {}


    /**
     * Checks if a given location is a valid (i.e. physically possible) location
     * on Earth. Note: The special separator locations (which have latitude = 100)
     * will not qualify as valid. Neither will locations with lat=0 and lng=0 as
     * these are most likely "bad" measurements which often cause trouble.
     * 
     * @param location the location to test
     * @return true if the location is a valid location.
     */
    public static boolean isValidLocation(Location location) {
      return location != null && Math.abs(location.getLatitude()) <= 90 && Math.abs(location.getLongitude()) <= 180;
    }

    /**
     * Returns true if a location is old.
     * 
     * @param location the location
     */
    public static boolean isLocationOld(Location location) {
      return !LocationUtils.isValidLocation(location) || (System.currentTimeMillis() - location.getTime() > MAX_LOCATION_AGE_MS);
    }
}
