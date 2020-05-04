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

package android.pal.item.calendar;

import android.os.Parcel;
import android.os.Parcelable;
import android.pal.item.Item;

import java.util.Comparator;

/**
 * The meta information for a calendar event.
 */
public class CalendarEventItem extends Item {

    public static final String ID = "id";
    public static final String TITLE = "title";
    public static final String START_TIME = "start_time";
    public static final String END_TIME = "end_time";
    public static final String EVENT_LOCATION = "event_location";

    CalendarEventItem(String id, String title, long startTime, long endTime, String eventLocation) {
        this.setFieldValue(ID, id);
        this.setFieldValue(TITLE, title);
        this.setFieldValue(START_TIME, startTime);
        this.setFieldValue(END_TIME, endTime);
        this.setFieldValue(EVENT_LOCATION, eventLocation);
    }

    /**
     * Get the Event ID, as stored in Android Calendar provider.
     * @return the event id
     */
    public String getId() {
        return this.getValueByField(ID);
    }

    /**
     * Get the title of the event.
     * @return the event title
     */
    public String getTitle() {
        return this.getValueByField(TITLE);
    }

    /**
     * Get the event start time.
     * @return the start time of the event, in UTC millis since epoch
     */
    public long getStartTime() {
        return this.getValueByField(START_TIME);
    }

    /**
     * Get the event end time.
     * @return the end time of the event, in UTC millis since epoch
     */
    public long getEndTime() {
        return this.getValueByField(END_TIME);
    }

    /**
     * Get the event location
     * @return the event location
     */
    public String getLocation() {
        return this.getValueByField(EVENT_LOCATION);
    }

    public static final Parcelable.Creator<CalendarEventItem> CREATOR = new Parcelable.Creator<CalendarEventItem>() {
        public CalendarEventItem createFromParcel(Parcel in) {
            return new CalendarEventItem(in);
        }

        public CalendarEventItem[] newArray(int size) {
            return new CalendarEventItem[size];
        }
    };

    private CalendarEventItem (Parcel in) {
        super(in);
    }

    public static class StartTimeComparator implements Comparator<CalendarEventItem> {
        @Override
        public int compare(CalendarEventItem event1, CalendarEventItem event2) {
            long event1Start = event1.getStartTime();
            long event2Start = event2.getStartTime();

            int result = 0;
            if(event1Start < event2Start) {
                result = -1;
            } else if(event1Start > event2Start) {
                result = 1;
            }

            return result;
        }
    }
}
