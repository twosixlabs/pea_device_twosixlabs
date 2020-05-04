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
import android.os.Bundle;
import android.pal.item.CursorHelper;
import android.pal.item.ItemProvider;
import android.pal.item.ListItem;
import android.privatedata.DataRequest;
import android.privatedata.ItemWrapper;
import android.provider.CallLog;
import android.util.Log;

import java.util.ArrayList;

import static android.privatedata.DataRequest.MessageParamsBuilder.START_UTC_MILLIS;
import static android.privatedata.DataRequest.MessageParamsBuilder.END_UTC_MILLIS;

public class CallItemProvider extends ItemProvider<ListItem<CallItem>> {
    private static final String TAG = CallItemProvider.class.getSimpleName();
    private static final long DEFAULT_UTC_MILLIS = -1l;

    public CallItemProvider(Context context) {
        super(context);
    }

    @Override
    protected ItemWrapper<ListItem<CallItem>> acquirePrivateData(Bundle params){
        long startMillis = params.getLong(START_UTC_MILLIS);
        long endMillis = params.getLong(END_UTC_MILLIS);

        ContentResolver cr = getContext().getContentResolver();
        Cursor cursor = cr.query(CallLog.Calls.CONTENT_URI,
                                 null,
                                 String.format("%s>=? AND %s<=?",
                                               CallLog.Calls.DATE, CallLog.Calls.DATE),
                                 new String[]{""+startMillis,
                                              ""+endMillis},
                                 null);

        ArrayList<CallItem> callsList = new ArrayList<>();
        while(cursor != null && cursor.moveToNext()) {
            try {
                String id = "" + CursorHelper.getColumnAsInt(cursor, CallLog.Calls._ID);
                long timestamp = CursorHelper.getColumnAsLong(cursor, CallLog.Calls.DATE);
                String number = CursorHelper.getColumnAsString(cursor, CallLog.Calls.NUMBER);
                long duration = CursorHelper.getColumnAsLong(cursor, CallLog.Calls.DURATION);
                int type = CursorHelper.getColumnAsInt(cursor, CallLog.Calls.TYPE);

                String typeStr = "";
                switch(type) {
                    case CallLog.Calls.INCOMING_TYPE:
                        typeStr = CallItem.TYPE_INCOMING;
                        break;

                    case CallLog.Calls.OUTGOING_TYPE:
                        typeStr = CallItem.TYPE_OUTGOING;
                        break;

                    case CallLog.Calls.MISSED_TYPE:
                        typeStr = CallItem.TYPE_MISSED;
                        break;

                    default:
                        Log.w(TAG, String.format("Skipping call log ID %d because it has invalid type %d", id, type));
                        continue;
                }

                CallItem call = new CallItem(id, timestamp, number, duration, typeStr);
                callsList.add(call);

            } catch(IllegalArgumentException e) {
                e.printStackTrace();
                Log.e(TAG, "Failed to retrieve a column, skipping");
                continue;
            }
        }

        if(cursor != null) {
            cursor.close();
        }

        ListItem<CallItem> data = new ListItem<>(callsList);
        return new ItemWrapper<ListItem<CallItem>>(data){};
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
