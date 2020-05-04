/*
 * This work was authored by Two Six Labs, LLC and is sponsored by a
 * subcontract agreement with Raytheon BBN Technologies Corp. under Prime
 * Contract No. FA8750-16-C-0006 with the Air Force Research Laboratory (AFRL).

 * The Government has unlimited rights to use, modify, reproduce, release,
 * perform, display, or disclose computer software or computer software
 * documentation marked with this legend. Any reproduction of technical data,
 * computer software, or portions thereof marked with this legend must also
 * reproduce this marking.

 * (C) 2020 Two Six Labs, LLC.  All rights reserved.
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

package android.pal.item.location;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;
import android.pal.item.Item;

/**
 * An Geolocation item represents a geolocation value.
 */
public class LocationItem extends Item {

    public static final String LATITUDE = "lat";
    public static final String LONGITUDE = "lon";
    public static final String PROVIDER = "provider";
    public static final String TIMESTAMP = "timestamp";
    public static final String ACCURACY = "accuracy";
    public static final String SPEED = "speed";
    public static final String BEARING = "bearing";

    /** Country level. This level's accuracy is about 100,000 meters. */
    public static final String LEVEL_COUNTRY = "country";
    static final int ACCURACY_COUNTRY = 100000;
    /** City level. This level's accuracy is about 10,000 meters. */
    public static final String LEVEL_CITY = "city";
    static final int ACCURACY_CITY = 10000;
    /** Neighborhood level. This level's accuracy is about 1,000 meters. */
    public static final String LEVEL_NEIGHBORHOOD = "neighborhood";
    static final int ACCURACY_NEIGHBORHOOD = 1000;
    /** Building level. This level's accuracy is about 100 meters. */
    public static final String LEVEL_BUILDING = "building";
    static final int ACCURACY_BUILDING = 100;
    /** Exact level. This level's accuracy is about 10 meters. */
    public static final String LEVEL_EXACT = "exact";
    static final int ACCURACY_EXACT = 10;

    public LocationItem(Location location) {
        this.setFieldValue(LATITUDE, location.getLatitude());
        this.setFieldValue(LONGITUDE, location.getLongitude());
        this.setFieldValue(PROVIDER, location.getProvider());
        this.setFieldValue(TIMESTAMP, location.getTime());
        this.setFieldValue(ACCURACY, location.getAccuracy());
        this.setFieldValue(SPEED, location.getSpeed());
        this.setFieldValue(BEARING, location.getBearing());
    }

    /**
     * Get the latitude
     * @return the latitude, in degrees
     */
    public double getLatitude() {
        return this.getValueByField(LATITUDE);
    }

    /**
     * Get the longitude
     * @return the longitude, in degrees
     */
    public double getLongitude() {
        return this.getValueByField(LONGITUDE);
    }

    /**
     * Get the provider of the location data, e.g., "gps" or "network".
     * @return the provider name
     */
    public String getProvider() {
        return this.getProvider();
    }

    /**
     * Get the timestamp of the location.
     * @return the time of fix, in ms since the epoch
     */
    public long getTime() {
        return this.getValueByField(TIMESTAMP);
    }

    /**
     * Get the accuracy
     * @return the accuracy, in meters
     */
    public float getAccuracy() {
        return this.getValueByField(ACCURACY);
    }

    /**
     * Get the speed at the location
     * @return the speed, in m/s
     */
    public float getSpeed() {
        return this.getValueByField(SPEED);
    }

    /**
     * Get the bearing at the location.
     * @return the bearing, in degrees, or 0.0 if the location does not have a bearing
     */
    public float getBearing() {
        return this.getValueByField(BEARING);
    }

    public static final Parcelable.Creator<LocationItem> CREATOR = new Parcelable.Creator<LocationItem>() {
        public LocationItem createFromParcel(Parcel in) {
            return new LocationItem(in);
        }

        public LocationItem[] newArray(int size) {
            return new LocationItem[size];
        }
    };

    private LocationItem (Parcel in) {
        super(in);
    }

}
