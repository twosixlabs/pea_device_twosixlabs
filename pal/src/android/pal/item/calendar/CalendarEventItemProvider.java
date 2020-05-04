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

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.privatedata.DataRequest;
import android.pal.item.CursorHelper;
import android.pal.item.ItemProvider;
import android.pal.item.ListItem;
import android.provider.CalendarContract;
import android.privatedata.ItemWrapper;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;

import static android.privatedata.DataRequest.CalendarParamsBuilder.START_UTC_MILLIS;
import static android.privatedata.DataRequest.CalendarParamsBuilder.END_UTC_MILLIS;

public class CalendarEventItemProvider extends ItemProvider<ListItem<CalendarEventItem>> {
    private static final String TAG = CalendarEventItemProvider.class.getSimpleName();
    private static final long DEFAULT_UTC_MILLIS = -1l;

    public CalendarEventItemProvider(Context context) {
        super(context);
    }

    @Override
    protected ItemWrapper<ListItem<CalendarEventItem>> acquirePrivateData(Bundle params) {
        long startMillis = params.getLong(START_UTC_MILLIS);
        long endMillis = params.getLong(END_UTC_MILLIS);

        ContentResolver cr = getContext().getContentResolver();
        Cursor cursor = CalendarContract.Instances.query(cr, null, startMillis, endMillis);

        ArrayList<CalendarEventItem> eventsList = new ArrayList<>();
        while(cursor != null && cursor.moveToNext()) {
            try {
                String id = "" + CursorHelper.getColumnAsInt(cursor, CalendarContract.Instances._ID);
                String title = CursorHelper.getColumnAsString(cursor, CalendarContract.Instances.TITLE);
                long instanceStart = CursorHelper.getColumnAsLong(cursor, CalendarContract.Instances.BEGIN);
                long instanceEnd = CursorHelper.getColumnAsLong(cursor, CalendarContract.Instances.END);
                String location = "";
                try {
                    location = CursorHelper.getColumnAsString(cursor, CalendarContract.Instances.EVENT_LOCATION);
                } catch (IllegalArgumentException e) {
                    // location column is probably empty
                }

                CalendarEventItem event = new CalendarEventItem(id, title, instanceStart, instanceEnd, location);
                eventsList.add(event);

            } catch(IllegalArgumentException e) {
                e.printStackTrace();
                Log.e(TAG, "Failed to retrieve a column, skipping");
                continue;
            }
        }

        if(cursor != null) {
            cursor.close();
        }

        ListItem<CalendarEventItem> data = new ListItem<>(eventsList);
        return new ItemWrapper<ListItem<CalendarEventItem>>(data){};
    }

    @Override
    public Bundle getParams() {
        Bundle params = new DataRequest.CalendarParamsBuilder()
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
