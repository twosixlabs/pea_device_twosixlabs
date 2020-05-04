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

package android.pal.item;

import android.database.Cursor;
import android.util.Base64;

public class CursorHelper {
    public static String getColumnAsBase64(Cursor c, String columnName) throws IllegalArgumentException {
        int columnIndex = c.getColumnIndexOrThrow(columnName);
        byte[] raw = c.getBlob(columnIndex);

        String b64Data = Base64.encodeToString(raw, Base64.DEFAULT);
        return b64Data;
    }

    public static long getColumnAsLong(Cursor c, String columnName) throws IllegalArgumentException {
        int columnIndex = c.getColumnIndexOrThrow(columnName);
        if(c.getType(columnIndex) == Cursor.FIELD_TYPE_INTEGER) {
            long data = c.getLong(columnIndex);
            return data;
        }

        throw new IllegalArgumentException("Column " + columnName + " is not of type FIELD_TYPE_INTEGER");
    }

    public static int getColumnAsInt(Cursor c, String columnName) throws IllegalArgumentException {
        int columnIndex = c.getColumnIndexOrThrow(columnName);
        if(c.getType(columnIndex) == Cursor.FIELD_TYPE_INTEGER) {
            int data = c.getInt(columnIndex);
            return data;
        }

        throw new IllegalArgumentException("Column " + columnName + " is not of type FIELD_TYPE_INTEGER");
    }

    public static String getColumnAsString(Cursor c, String columnName) throws IllegalArgumentException {
        int columnIndex = c.getColumnIndexOrThrow(columnName);
        if(c.getType(columnIndex) == Cursor.FIELD_TYPE_STRING) {
            String data = c.getString(columnIndex);
            return data;
        }

        throw new IllegalArgumentException("Column " + columnName + " is not of type FIELD_TYPE_STRING");
    }
}
