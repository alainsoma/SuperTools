/*
 * Copyright Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.superutilities;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import java.util.HashMap;

/**
 * This is a subclass of {@link Application} used to provide shared objects for this app, such as
 * the {@link Tracker}.
 */
public class AnalyticsApplication extends Application {
  private Tracker mTracker;
  // The following line should be changed to include the correct property id.
  private static final String PROPERTY_ID = "UA-74752509-2";

  /**
   * Gets the default {@link Tracker} for this {@link Application}.
   * @return tracker
   */
  synchronized public Tracker getDefaultTracker() {
    if (mTracker == null) {
      GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
      // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
      mTracker = analytics.newTracker(R.xml.globaling_tracker);

    }
    return mTracker;
  }



  /**
   * Enum used to identify the tracker that needs to be used for tracking.
   *
   * A single tracker is usually enough for most purposes. In case you do need multiple trackers,
   * storing them all in Application object helps ensure that they are created only once per
   * application instance.
   */
  public enum TrackerName {
    APP_TRACKER, // Tracker used only in this app.
    GLOBAL_TRACKER, // Tracker used by all the apps from a company. eg: roll-up tracking.
    ECOMMERCE_TRACKER, // Tracker used by all ecommerce transactions from a company.
  }

  HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();

  synchronized Tracker getTracker(TrackerName trackerId) {
    if (!mTrackers.containsKey(trackerId)) {

      GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
      Tracker t = (trackerId == TrackerName.APP_TRACKER) ? analytics.newTracker(PROPERTY_ID)
              : (trackerId == TrackerName.GLOBAL_TRACKER) ? analytics.newTracker(R.xml.globaling_tracker)
              : analytics.newTracker(R.xml.globaling_tracker);
      mTrackers.put(trackerId, t);

    }
    return mTrackers.get(trackerId);
  }

}


