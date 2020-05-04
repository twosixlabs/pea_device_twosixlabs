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

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.pal.item.CursorHelper;
import android.pal.item.ItemProvider;
import android.pal.item.ListItem;
import android.privatedata.DataRequest;
import android.privatedata.ItemWrapper;
import android.provider.Telephony;
import android.util.Log;

import java.util.ArrayList;

import static android.privatedata.DataRequest.MessageParamsBuilder.START_UTC_MILLIS;
import static android.privatedata.DataRequest.MessageParamsBuilder.END_UTC_MILLIS;

public class MessageItemProvider extends ItemProvider<ListItem<MessageItem>> {
    private static final String TAG = MessageItemProvider.class.getSimpleName();
    private static final long DEFAULT_UTC_MILLIS = -1l;

    public MessageItemProvider(Context context) {
        super(context);
    }

    @Override
    protected ItemWrapper<ListItem<MessageItem>> acquirePrivateData(Bundle params) {
        long startMillis = params.getLong(START_UTC_MILLIS);
        long endMillis = params.getLong(END_UTC_MILLIS);

        ArrayList<MessageItem> messagesList = new ArrayList<>();

        // Get all text content from SMS received in the specified time range
        Cursor cursor = queryMessages(Telephony.Sms.Inbox.CONTENT_URI, Telephony.Sms.Inbox.TYPE, Telephony.Sms.Inbox.MESSAGE_TYPE_INBOX,
                                      Telephony.Sms.Inbox.DATE, startMillis, endMillis);
        while(cursor != null && cursor.moveToNext()) {
            String type = MessageItem.TYPE_RECEIVED;
            String content = CursorHelper.getColumnAsString(cursor, Telephony.Sms.Inbox.BODY);
            String contact = CursorHelper.getColumnAsString(cursor, Telephony.Sms.Inbox.ADDRESS);
            long timestamp = CursorHelper.getColumnAsLong(cursor, Telephony.Sms.Inbox.DATE);

            MessageItem item = new MessageItem(type, content, contact, timestamp);
            messagesList.add(item);
        }
        if(cursor != null) {
            cursor.close();
        }

        // Get all context content from SMS sent in the specified time range
        cursor = queryMessages(Telephony.Sms.Sent.CONTENT_URI, Telephony.Sms.Sent.TYPE, Telephony.Sms.Sent.MESSAGE_TYPE_SENT,
                               Telephony.Sms.Sent.DATE, startMillis, endMillis);
        while(cursor != null && cursor.moveToNext()) {
            String type = MessageItem.TYPE_SENT;
            String content = CursorHelper.getColumnAsString(cursor, Telephony.Sms.Sent.BODY);
            String contact = CursorHelper.getColumnAsString(cursor, Telephony.Sms.Sent.ADDRESS);
            long timestamp = CursorHelper.getColumnAsLong(cursor, Telephony.Sms.Sent.DATE);

            MessageItem item = new MessageItem(type, content, contact, timestamp);
            messagesList.add(item);
        }
        if(cursor != null) {
            cursor.close();
        }

        // TODO Figure out how to deal with group messages. Might be a good resource:
        //      https://stackoverflow.com/questions/3012287/how-to-read-mms-data-in-android


        ListItem<MessageItem> data = new ListItem<>(messagesList);
        return new ItemWrapper<ListItem<MessageItem>>(data){};
    }

    private Cursor queryMessages(Uri table, String typeColumn, int typeValue, String timeColumn, long startMillis, long endMillis) {
        ContentResolver cr = getContext().getContentResolver();
        Cursor cursor = cr.query(table,
                                 null,
                                 String.format("%s=? AND %s>=? AND %s<=?",
                                               typeColumn, timeColumn, timeColumn),
                                 new String[]{""+typeValue,
                                              ""+startMillis,
                                              ""+endMillis},
                                 null);

        return cursor;
    }

    @Override
    public Bundle getParams() {
        Bundle params = new DataRequest.MessageParamsBuilder()
            .setStartUtcMillis(DEFAULT_UTC_MILLIS)
            .setEndUtcMillis(DEFAULT_UTC_MILLIS)
            .build();
        return params;
    }

    @Override
    protected ParamStatus checkParams(Bundle params) {
        // Run the basic checks defined in the superclass
        ParamStatus checkResult = super.checkParams(params);

        if(checkResult == ParamStatus.GOOD) {
            long startMillis = params.getLong(START_UTC_MILLIS);
            long endMillis = params.getLong(END_UTC_MILLIS);

            // Make sure that the start and end times are set, and that start < end
            boolean startValid = startMillis >= 0l;
            boolean endValid = endMillis >= 0l;
            boolean rangeValid = startMillis <= endMillis;

            boolean success = startValid && endValid && rangeValid;
            if(success) {
                return ParamStatus.GOOD;
            } else {
                if(!startValid) {
                    Log.e(TAG, "Invalid start time. Expecting a value >= 0. Got " + startMillis);
                }

                if(!endValid) {
                    Log.e(TAG, "Invalid end time. Expecting a value >= 0. Got " + endMillis);
                }

                if(!rangeValid) {
                    Log.e(TAG, String.format("Invalid time range. Start time (got %d) must be less than or equal to end time (got %d)", startMillis, endMillis));
                }

                return ParamStatus.ERROR_UNEXPECTED_PARAM_VALUE;
            }


        } else {
            return checkResult;
        }
    }
}
