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

package android.privatedata;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Bundle;
import android.util.Log;
import android.pal.item.Item;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ItemWrapper<T extends Item> implements Parcelable {
    private T mValue;
    private List<Class> mTypeClasses;

    private static final String TAG = "ItemWrapper";

    public ItemWrapper(T value) {
        mValue = value;
        mTypeClasses = new ArrayList<>();

        Type type = getClass().getGenericSuperclass();
        while (type != null) {
            if (type instanceof ParameterizedType) {
                mTypeClasses.add((Class) ((ParameterizedType) type).getRawType());
                type = ((ParameterizedType) type).getActualTypeArguments()[0];
            } else if (type instanceof Class) {
                mTypeClasses.add((Class) type);
                type = null;
            }
        }

        // get rid of first one, which is ItemWrapper;
        mTypeClasses.remove(0);
    }

    protected ItemWrapper(Parcel in) {
        readFromParcel(in);
    }

    public static final Creator<ItemWrapper> CREATOR = new Creator<ItemWrapper>() {
        @Override
        public ItemWrapper createFromParcel(Parcel in) {
            return new ItemWrapper(in);
        }

        @Override
        public ItemWrapper[] newArray(int size) {
            return new ItemWrapper[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(mTypeClasses);
        dest.writeValue(mValue);
    }

    private void readFromParcel(Parcel in) {
        mTypeClasses = (List<Class>) in.readValue(null);
        mValue = (T) in.readValue(null);
    }

    public T getValue() { return mValue; }
    public List<Class> getTypes() { return mTypeClasses; }

    // For debugging
    public String getTypeString() {
        StringBuffer typeString = new StringBuffer("");

        for (int i = 0; i < mTypeClasses.size() - 1; i++) {
            typeString.append(mTypeClasses.get(i));
            typeString.append("<");
        }
        typeString.append(mTypeClasses.get(mTypeClasses.size() - 1));
        for (int i = 0; i < mTypeClasses.size() - 1; i++) {
            typeString.append(">");
        }
        return typeString.toString();
    }

}
