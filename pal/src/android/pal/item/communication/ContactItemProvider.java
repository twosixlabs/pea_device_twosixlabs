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
import android.privatedata.ItemWrapper;
import android.provider.ContactsContract;
import android.util.Log;

import java.util.ArrayList;

public class ContactItemProvider extends ItemProvider<ListItem<ContactItem>> {
    private static final String TAG = ContactItemProvider.class.getSimpleName();

    public ContactItemProvider(Context context) {
        super(context);
    }

    @Override
    protected ItemWrapper<ListItem<ContactItem>> acquirePrivateData(Bundle params) {
        ContentResolver cr = getContext().getContentResolver();
        Cursor cursor = cr.query(ContactsContract.Contacts.CONTENT_URI,
                                 null,
                                 null,
                                 null,
                                 null);

        ArrayList<ContactItem> contactList = new ArrayList<>();
        while(cursor != null && cursor.moveToNext()) {
            try {
                String id = "" + CursorHelper.getColumnAsInt(cursor, ContactsContract.Contacts._ID);
                String name = CursorHelper.getColumnAsString(cursor, ContactsContract.Contacts.DISPLAY_NAME);

                ArrayList<String> phoneNumbers = getMultiField(id,
                                                          ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                                          ContactsContract.CommonDataKinds.Phone.NUMBER);
                ArrayList<String> emailAddresses = getMultiField(id,
                                                          ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                                                          ContactsContract.CommonDataKinds.Email.ADDRESS);

                ContactItem item = new ContactItem(id, name, phoneNumbers, emailAddresses);
                contactList.add(item);

            } catch(IllegalArgumentException e) {
                e.printStackTrace();
                Log.e(TAG, "Failed to retrieve a column, skipping");
                continue;

            }
        }

        ListItem<ContactItem> data = new ListItem<ContactItem>(contactList);
        return new ItemWrapper<ListItem<ContactItem>>(data){};
    }

    private ArrayList<String> getMultiField(String contactId, Uri tableUri, String columnName) {
        ArrayList<String> result = new ArrayList<>();

        ContentResolver cr = getContext().getContentResolver();
        Cursor cursor = cr.query(tableUri,
                                 null,
                                 ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?",
                                 new String[]{contactId},
                                 null);

        while(cursor != null && cursor.moveToNext()) {
            String value = CursorHelper.getColumnAsString(cursor, columnName);
            result.add(value);
        }

        if(cursor != null) {
            cursor.close();
        }

        return result;
    }

    @Override
    public Bundle getParams() {
        return null;
    }
}
