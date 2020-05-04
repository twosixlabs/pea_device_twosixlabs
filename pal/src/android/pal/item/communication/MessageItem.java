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
 * A text message. It could be from SMS, WhatsApp, Facebook, etc.
 */
public class MessageItem extends Item {

    public static final String CONTENT = "content";
    public static final String CONTACT = "contact";
    public static final String TIMESTAMP = "timestamp";
    public static final String TYPE = "type";

    public static final String TYPE_RECEIVED = "received";
    public static final String TYPE_SENT = "sent";

    MessageItem(String type, String content, String contact, long timestamp){
        this.setFieldValue(TYPE, type);
        this.setFieldValue(CONTENT, content);
        this.setFieldValue(CONTACT, contact);
        this.setFieldValue(TIMESTAMP, timestamp);
    }

    /**
     * Get the message type
     * @return the message type, value is one of {@value #TYPE_RECEIVED}, {@value #TYPE_SENT},
     *         {@value #TYPE_DRAFT}, {@value #TYPE_PENDING}, {@value #TYPE_UNKNOWN}
     */
    public String getType() {
        return this.getValueByField(TYPE);
    }

    /**
     * Get the message content.
     * @return the message content
     */
    public String getContent() {
        return this.getValueByField(CONTENT);
    }

    /**
     * Get the contact (name or phone number) of the message
     * @return the contact of the message
     */
    public String getContact() {
        return this.getValueByField(CONTACT);
    }

    /**
     * Get the timestamp of when the message is sent or received.
     * @return the timestamp, in UTC millis since epoch
     */
    public long getTimestamp() {
        return this.getValueByField(TIMESTAMP);
    }

    public static final Parcelable.Creator<MessageItem> CREATOR = new Parcelable.Creator<MessageItem>() {
        public MessageItem createFromParcel(Parcel in) {
            return new MessageItem(in);
        }

        public MessageItem[] newArray(int size) {
            return new MessageItem[size];
        }
    };

    private MessageItem (Parcel in) {
        super(in);
    }

    public static class TimestampComparator implements Comparator<MessageItem> {
        @Override
        public int compare(MessageItem message1, MessageItem message2) {
            long message1Timestamp = message1.getTimestamp();
            long message2Timestamp = message2.getTimestamp();

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
