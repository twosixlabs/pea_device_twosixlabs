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

package com.twosixlabs.peandroid.privatedataservice;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.pal.item.Item;
import android.privatedata.DataRequest;
import android.privatedata.IMicroPALProvider;
import android.privatedata.ItemWrapper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class MicroPALProviderServiceConnection implements ServiceConnection {
	private static final String TAG = MicroPALProviderServiceConnection.class.getSimpleName();
    private static ConcurrentHashMap<String, MicroPALProviderServiceConnection> mActiveMicroPALConnections;

    private String mPackageName;
    private String mServiceName;
    private String mId;
    private Context mContext;

    private IMicroPALProvider mService;
    private DataRequest.DataType mSupportedType;

    public MicroPALProviderServiceConnection(ResolveInfo ri, Context context) {
        if(mActiveMicroPALConnections == null) {
            mActiveMicroPALConnections = new ConcurrentHashMap<>();
        }

        mPackageName = ri.serviceInfo.packageName;
        mServiceName = ri.serviceInfo.name;
        mId = mServiceName;
        mContext = context;
    }

    public MicroPALProviderServiceConnection(String packageName, String serviceName, Context context) {
        if(mActiveMicroPALConnections == null) {
            mActiveMicroPALConnections = new ConcurrentHashMap<>();
        }

        mPackageName = packageName;
        mServiceName = serviceName;
        mId = mServiceName;
        mContext = context;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        try {
            mService = IMicroPALProvider.Stub.asInterface(service);
            mSupportedType = DataRequest.DataType.valueOf(mService.getSupportedType()); // Throws IllegalArgumentException if the argument is not a valid DataType

            boolean correctId = mId.equals(mService.getId());
            if(correctId) {
                mActiveMicroPALConnections.put(mId, this);
                Log.i(TAG, String.format("Connected to %s (%d active connections now)", toString(), mActiveMicroPALConnections.size()));

            } else {
                if(!correctId) {
                    Log.e(TAG, String.format("ID mismatch between expected (%s) and what PAL declared (%s)", mId, mService.getId()));
                }
                throw new RemoteException("Error with " + toString());
            }

        } catch(RemoteException | IllegalArgumentException e) {
            e.printStackTrace();
            disconnect();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        mActiveMicroPALConnections.remove(mId);
        Log.i(TAG, String.format("Removed connection to %s (%d active connections remain)", toString(), mActiveMicroPALConnections.size()));
    }

    @Override
    public String toString() {
        return mId + ":" + mSupportedType.name();
    }

    public void connect() {
        if(!isConnected()) {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(mPackageName, mServiceName));
            mContext.bindService(intent, this, Context.BIND_AUTO_CREATE);
        } else {
            Log.w(TAG, toString() + " is already connected");
        }
    }

    public Bundle processData(DataRequest.DataType dataType, ItemWrapper privateData, Bundle palParams) throws IllegalArgumentException,RemoteException {
        if(mSupportedType == DataRequest.DataType.ANY || dataType == mSupportedType) {
            Log.i(TAG, toString() + " processing data type " + dataType.name());

            if (privateData == null) {
                throw new IllegalArgumentException("Received null privateData");
            }

            Log.i(TAG, "send privateData for dataType=" + dataType.name());
            return mService.processData(privateData, palParams);
        }

        throw new IllegalArgumentException(String.format("Invalid data type provider. Expected %s, got %s.", mSupportedType.name(), dataType.name()));
    }

    public void disconnect() {
        Log.i(TAG, "Unbinding " + toString());
        mContext.unbindService(this);
    }

    public boolean isConnected() {
        return mActiveMicroPALConnections.containsKey(mId);
    }

    public String getDescription() throws RemoteException {
        return mService.getDescription();
    }

    public static List<String> getMicroPALIdentifiers(DataRequest.DataType supportedType) {
        boolean getAllPALs = supportedType == null;

        List<String> identifiers = new ArrayList<>();
        if (mActiveMicroPALConnections != null) {
            for(MicroPALProviderServiceConnection pal : mActiveMicroPALConnections.values()) {
                boolean typeMatch = supportedType == pal.mSupportedType;
                if(getAllPALs || typeMatch) {
                    identifiers.add(pal.mId);
                }
            }
        }

        return identifiers;
    }

    public static MicroPALProviderServiceConnection getConnection(String id) {
        return mActiveMicroPALConnections.get(id);
    }
}
