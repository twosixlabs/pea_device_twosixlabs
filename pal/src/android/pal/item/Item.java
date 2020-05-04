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

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.ArrayList;
import java.util.Locale;

import static android.pal.item.utils.Assertions.cast;

/**
 * An Item is a basic element in a stream.
 * This class is the base class of all type of personal data items in PrivacyStream.
 */
public class Item implements Parcelable {

    private static final String TAG = "Item";

    /**
     * The timestamp of when this item is created.
     * It is a general field for all items.
     */
    public static final String TIME_CREATED = "time_created";

    private final Bundle itemMap;

    public Item() {
        this(new Bundle());
    }

    public Item(Bundle itemMap) {
        this.itemMap = itemMap;
        this.setFieldValue(TIME_CREATED, System.currentTimeMillis());
    }

    public String toDebugString() {
        String itemStr = "ITEM {\n";
        for (String fieldKey : this.itemMap.keySet()) {
            Object fieldValue = this.itemMap.get(fieldKey);
            if (fieldValue == null) {
                itemStr += String.format(Locale.getDefault(),
                        "\t\"%s\": null\n",
                        fieldKey);
            } else {
                String fieldValueClass = fieldValue.getClass().getSimpleName();
                itemStr += String.format(Locale.getDefault(),
                        "\t%s \"%s\": %s\n",
                        fieldValueClass, fieldKey, fieldValue.toString());
            }
        }
        itemStr += "}";
        return itemStr;
    }

    public Bundle getBundle() {
        return this.itemMap;
    }

    /**
     * Get the value of a field in the item.
     *
     * @param fieldName the name of the field
     * @param <TValue>  the type of field value
     * @return the field value
     */
    public <TValue> TValue getValueByField(String fieldName) {
        if (itemMap.containsKey(fieldName)) {
            return cast(fieldName, itemMap.get(fieldName));
        }
        Log.e(TAG, "Unknown field: \"" + fieldName + "\" in " + this.toString());
        return null;
    }

    /**
     * Get the value of a String field in the item.
     *
     * @param fieldName the name of the field
     * @return the field value
     */
    public String getAsString(String fieldName) {
        return getValueByField(fieldName);
    }

    /**
     * Get the value of a Boolean field in the item.
     *
     * @param fieldName the name of the field
     * @return the field value
     */
    public Boolean getAsBoolean(String fieldName) {
        return getValueByField(fieldName);
    }

    /**
     * Get the value of a Integer field in the item.
     *
     * @param fieldName the name of the field
     * @return the field value
     */
    public Integer getAsInteger(String fieldName) {
        return getValueByField(fieldName);
    }

    /**
     * Get the value of a Long field in the item.
     *
     * @param fieldName the name of the field
     * @return the field value
     */
    public Long getAsLong(String fieldName) {
        return getValueByField(fieldName);
    }

    /**
     * Get the value of a Double field in the item.
     *
     * @param fieldName the name of the field
     * @return the field value
     */
    public Double getAsDouble(String fieldName) {
        return getValueByField(fieldName);
    }

    /**
     * Get the value of a Float field in the item.
     *
     * @param fieldName the name of the field
     * @return the field value
     */
    public Float getAsFloat(String fieldName) {
        return getValueByField(fieldName);
    }

    /**
     * Set a value to a field in the Item
     *
     * @param fieldName the name of the field
     * @param value     the value of the field
     */
    public void setFieldValue(String fieldName, String value) {
        this.itemMap.putString(fieldName, value);
    }

    /**
     * Set a value to a field in the Item
     *
     * @param fieldName the name of the field
     * @param value     the value of the field
     */
    public void setFieldValue(String fieldName, boolean value) {
        this.itemMap.putBoolean(fieldName, value);
    }

    /**
     * Set a value to a field in the Item
     *
     * @param fieldName the name of the field
     * @param value     the value of the field
     */
    public void setFieldValue(String fieldName, int value) {
        this.itemMap.putInt(fieldName, value);
    }

    /**
     * Set a value to a field in the Item
     *
     * @param fieldName the name of the field
     * @param value     the value of the field
     */
    public void setFieldValue(String fieldName, long value) {
        this.itemMap.putLong(fieldName, value);
    }

    /**
     * Set a value to a field in the Item
     *
     * @param fieldName the name of the field
     * @param value     the value of the field
     */
    public void setFieldValue(String fieldName, double value) {
        this.itemMap.putDouble(fieldName, value);
    }

    /**
     * Set a value to a field in the Item
     *
     * @param fieldName the name of the field
     * @param value     the value of the field
     */
    public void setFieldValue(String fieldName, float value) {
        this.itemMap.putFloat(fieldName, value);
    }

    /**
     * Set a value to a field in the Item
     *
     * @param fieldName the name of the field
     * @param value     the value of the field
     */
    public void setFieldValue(String fieldName, Parcelable value) {
        this.itemMap.putParcelable(fieldName, value);
    }

    /**
     * Set an ArrayList<String> value to a field in the Item
     *
     * @param fieldName the name of the field
     * @param value     the value of the field
     */
    public void setStringArrayListFieldValue(String fieldName, ArrayList<String> value) {
        this.itemMap.putStringArrayList(fieldName, value);
    }

    /**
     * Set an ArrayList<Parcelable> value to a field in the Item
     *
     * @param fieldName the name of the field
     * @param value     the value of the field
     */
    public void setParcelableArrayListFieldValue(String fieldName, ArrayList<? extends Parcelable> value) {
        this.itemMap.putParcelableArrayList(fieldName, value);
    }

    /**
     * Test if current item contains a field
     *
     * @param fieldName the field name to test
     * @return true if the item contains the field, otherwise false
     */
    public boolean containsField(String fieldName) {
        return this.itemMap.containsKey(fieldName);
    }

    public static final Parcelable.Creator<Item> CREATOR = new Parcelable.Creator<Item>() {
        public Item createFromParcel(Parcel in) {
            return new Item(in);
        }

        public Item[] newArray(int size) {
            return new Item[size];
        }
    };

    protected Item (Parcel in) {
        this.itemMap = in.readParcelable(null);
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeParcelable(this.itemMap, 0);
    }

    @Override
    public int describeContents() {
        return 0;
    }

}
