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
import java.util.ArrayList;

/**
 * The information of a contact.
 */
public class ContactItem extends Item {

    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String PHONES = "phones";
    public static final String EMAILS = "emails";

    ContactItem(String id, String name, ArrayList<String> phones, ArrayList<String> emails) {
        this.setFieldValue(ID, id);
        this.setFieldValue(NAME, name);
        this.setStringArrayListFieldValue(PHONES, phones);
        this.setStringArrayListFieldValue(EMAILS, emails);
    }

    /**
     * Get the contact's unique ID, as stored in Android database.
     * @return the contact id
     */
    public String getId() {
        return this.getValueByField(ID);
    }

    /**
     * Get the contact name.
     * @return the contact's name
     */
    public String getName() {
        return this.getValueByField(NAME);
    }

    /**
     * Get the phone numbers of the contact.
     * @return an ArrayList of known phone numbers for the contact
     */
    public ArrayList<String> getPhoneNumbers() {
        return this.getValueByField(PHONES);
    }

    /**
     * Get the email addresses of the contact
     * @return  an ArrayList of known email addresses for the contact
     */
    public ArrayList<String> getEmailAddresses() {
        return this.getValueByField(EMAILS);
    }

    public static final Parcelable.Creator<ContactItem> CREATOR = new Parcelable.Creator<ContactItem>() {
        public ContactItem createFromParcel(Parcel in) {
            return new ContactItem(in);
        }

        public ContactItem[] newArray(int size) {
            return new ContactItem[size];
        }
    };

    private ContactItem (Parcel in) {
        super(in);
    }
}
