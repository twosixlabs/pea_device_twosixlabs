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

package android.pal.item.communication;

import android.os.Parcel;
import android.os.Parcelable;
import android.pal.item.Item;

import java.util.Comparator;

/**
 * The information of a phone call.
 */
public class CallItem extends Item {

    public static final String TIMESTAMP = "timestamp";
    public static final String CONTACT = "contact";
    public static final String DURATION = "duration";
    public static final String TYPE = "type";

    public static final String TYPE_INCOMING = "incoming";
    public static final String TYPE_OUTGOING = "outgoing";
    public static final String TYPE_MISSED = "missed";

    CallItem(String id, Long timestamp, String phone_number, Long duration, String call_type) {
        this.setFieldValue(TIMESTAMP, timestamp);
        this.setFieldValue(CONTACT, phone_number);
        this.setFieldValue(DURATION, duration);
        this.setFieldValue(TYPE, call_type);
    }

    /**
     * Get the timestamp of when the phone call happened.
     * @return the timestamp, in UTC millis since epoch
     */
    public long getTimestamp() {
        return this.getValueByField(TIMESTAMP);
    }

    /**
     * Get the contact (phone number or name) of the phone call.
     * @return the name or phone number of the call
     */
    public String getContact() {
        return this.getValueByField(CONTACT);
    }

    /**
     * Get the duration of the phone call.
     * @return the duration of the phone call, in milliseconds
     */
    public long getDuration() {
        return this.getValueByField(DURATION);
    }

    /**
     * Get the type of the phone call.
     * @return the type of the phone call, value is one of {@value #TYPE_INCOMING},
     *         {@value #TYPE_OUTGOING} or {@value @#TYPE_MISSED}
     */
    public String getCallType() {
        return this.getValueByField(TYPE);
    }

    public static final Parcelable.Creator<CallItem> CREATOR = new Parcelable.Creator<CallItem>() {
        public CallItem createFromParcel(Parcel in) {
            return new CallItem(in);
        }

        public CallItem[] newArray(int size) {
            return new CallItem[size];
        }
    };

    private CallItem (Parcel in) {
        super(in);
    }

    public static class TimestampComparator implements Comparator<CallItem> {
        @Override
        public int compare(CallItem call1, CallItem call2) {
            long message1Timestamp = call1.getTimestamp();
            long message2Timestamp = call2.getTimestamp();

            int result = 0;
            if(message1Timestamp < message2Timestamp) {
                result = -1;
            } else if(message1Timestamp > message2Timestamp) {
                result = 1;
            }

            return result;
        }
    }
}
