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

import android.Manifest;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.pal.item.Item;
import android.privatedata.DataRequest.DataType;
import android.privatedata.IMicroPALProvider;
import android.privatedata.ItemWrapper;
import android.util.Log;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Base class for PAL Providers. To create a provider, this class should be
 * extended and the generic type should be the type of private data the provider
 * supports
 */
public abstract class MicroPALProviderService<T extends Item> extends Service {
    public static final String PRIVACY_SERVICE_ACTION = MicroPALProviderService.class.getName();
    private static final String TAG = MicroPALProviderService.class.getSimpleName();

    private DataType mSupportedType;
    private String mId;
    private List<Class> mTypeClasses;

    /**
     * Create a new PrivateDataProviderService instance.
     */
    public MicroPALProviderService(DataType supportedType) {
        super();
        mSupportedType = supportedType;
        mId = getClass().getCanonicalName();
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
        mTypeClasses.remove(0); // get rid of first one, which is MicroPALProviderService
    }

    @Override
    public final IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        ComponentName component = new ComponentName(this, getClass());
        try {
            ServiceInfo serviceInfo = getPackageManager().getServiceInfo(component, 0 /* flags */);
            if (!Manifest.permission.PRIVATE_DATA_PROVIDER_SERVICE.equals(serviceInfo.permission)) {
                throw new IllegalStateException(component.flattenToShortString()
                        + " is not declared with the permission "
                        + "\"" + Manifest.permission.PRIVATE_DATA_PROVIDER_SERVICE + "\"");
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Can't get ServiceInfo for " + component.toShortString());
        }
    }

    /**
     *  Implementation of the private data transform.
     *  @param privateData  An item containing the private data to transform
     *  @param palExtras    A bundle containing any additional parameters for the
     *                      transformation. May be empty or null, depending on how
     *                      the transformation is implemented.
     *  @return A bundle containing transformed data derived from the private data.
     */
    public abstract Bundle onReceive(T privateData, Bundle palExtras);

    /**
     *  Description of this PAL transform. Used by the Policy Manager.
     *  @return PAL description
     */
    public abstract String getDescription();

    private final IMicroPALProvider.Stub mBinder = new IMicroPALProvider.Stub() {
        @Override
        public Bundle processData(ItemWrapper privateData, Bundle palExtras) {
            if (checkType(privateData)) {
                return MicroPALProviderService.this.onReceive(((ItemWrapper<T>) privateData).getValue(), palExtras);
            } else {
                Log.e(TAG, "Invalid data type to transform! Received: " + privateData.getTypeString() + ", " + "Expected: " + getTypeString());
            }
            return null;
        }

        @Override
        public String getId() {
            return mId;
        }

        @Override
        public String getSupportedType() {
            return mSupportedType.name();
        }

        @Override
        public String getDescription() {
            return MicroPALProviderService.this.getDescription();
        }
    };

    /* Check if the type of item contained in the ItemWrapper is compatible with this PAL */
    private boolean checkType(ItemWrapper wrappedItem) {
        List<Class> itemTypeClasses = wrappedItem.getTypes();
        for (int i = 0; i < mTypeClasses.size(); i++) {
            if (i >= itemTypeClasses.size()) {
                return false;
            }
            Class palType = mTypeClasses.get(i);
            Class itemType = itemTypeClasses.get(i);
            if (!palType.isAssignableFrom(itemType)) {
                return false;
            }
        }
        return true;
    }

    /* for debugging */
    private String getTypeString() {
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
