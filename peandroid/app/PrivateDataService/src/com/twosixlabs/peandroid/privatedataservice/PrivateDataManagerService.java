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

import android.annotation.Nullable;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.pal.item.Item;
import android.pal.item.ItemProvider;
import android.pal.item.calendar.CalendarEventItemProvider;
import android.pal.item.communication.CallItemProvider;
import android.pal.item.communication.ContactItemProvider;
import android.pal.item.communication.MessageItemProvider;
import android.pal.item.device.DeviceStateItemProvider;
import android.pal.item.empty.EmptyItemProvider;
import android.pal.item.location.LocationItemProvider;
import android.privatedata.DataRequest;
import android.privatedata.IPrivateDataManagerService;
import android.privatedata.ItemWrapper;
import android.privatedata.MicroPALProviderService;
import android.privatedata.PrivateDataManager;
import android.util.Log;

import java.util.List;
import java.util.ArrayList;

class PrivateDataManagerService extends IPrivateDataManagerService.Stub {
    private static final String TAG = PrivateDataManagerService.class.getSimpleName();
    private Context mContext;
    private BroadcastReceiver mInstallReceiver;
    private BroadcastReceiver mUninstallReceiver;
    private PolicyManagerProxy mPolicyManager;

    public PrivateDataManagerService(Context context) {
        mContext = context;
        init();
    }

    private void init() {
        // Listen for uPAL installation
	    IntentFilter installFilter = new IntentFilter();
	    installFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
		installFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
		installFilter.addDataScheme("package");

        BroadcastReceiver mInstallReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String packageName = intent.getData().getEncodedSchemeSpecificPart();
                bindPALsFromPackage(packageName);
            }
        };
		mContext.registerReceiver(mInstallReceiver, installFilter);

        // Listen for uPAL uninstallation
		IntentFilter uninstallFilter = new IntentFilter();
		uninstallFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		uninstallFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
		uninstallFilter.addDataScheme("package");

        BroadcastReceiver mUninstallReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String packageName = intent.getData().getEncodedSchemeSpecificPart();

            }
        };
		mContext.registerReceiver(mUninstallReceiver, uninstallFilter);

        // Bind to all uPALs
        bindPALsFromPackage(null);

        // Look for the Policy Manager
        try {
            mPolicyManager = new PolicyManagerProxy();

        } catch(RemoteException e) {
            Log.e(TAG, "Failed to connect to Policy Manager", e);
        }
    }

    private void bindPALsFromPackage(@Nullable String packageName) {
        Intent intent = new Intent();
        intent.setAction(MicroPALProviderService.PRIVACY_SERVICE_ACTION);

        boolean bindAllPackages = packageName == null;
        if(bindAllPackages) {
            Log.d(TAG, "Attempting to bind all available uPALs");
        } else {
            Log.d(TAG, "Attempting to bind uPALs in package " + packageName);
            intent.setPackage(packageName);
        }

        PackageManager pm = mContext.getPackageManager();
        List<ResolveInfo> resolveInfos = pm.queryIntentServices(intent, 0);

        if(resolveInfos != null) {
            Log.d(TAG, String.format("Found %d uPAL providers",  resolveInfos.size()));

            for(ResolveInfo ri : resolveInfos) {
                String foundUPALPackage = ri.serviceInfo.packageName;
                String foundUPALIdentifier = ri.serviceInfo.name;
                Log.d(TAG, "Found uPAL " + foundUPALIdentifier);

                if(bindAllPackages || packageName.equals(foundUPALPackage)) {
                    Log.d(TAG, "Attempting to connect to uPAL " + foundUPALIdentifier);
                    MicroPALProviderServiceConnection connection = new MicroPALProviderServiceConnection(ri, mContext);
                    connection.connect();
                }
            }
        }
    }

    private ItemWrapper getPrivateData(DataRequest.DataType dataType, Bundle dataTypeExtras) throws IllegalArgumentException {
        ItemProvider provider = null;
        switch(dataType) {
            // TODO Implement ACCOUNTS type in the future, perhaps
            //case ACCOUNTS:
            //    provider = null;            // TODO Get accounts data
            //    break;

            case CALENDAR:
                provider = new CalendarEventItemProvider(mContext);
                break;

            case CALL_LOGS:
                provider = new CallItemProvider(mContext);
                break;

            case CONTACTS:
                provider = new ContactItemProvider(mContext);
                break;

            case LOCATION:
                provider = new LocationItemProvider(mContext);
                break;

            case PHONE_STATE:
                provider = new DeviceStateItemProvider(mContext);
                break;

            case SMS:
                provider = new MessageItemProvider(mContext);
                break;

            case EMPTY:
            case ANY:
                provider = new EmptyItemProvider(mContext);
                break;

            default:
                provider = null;         // Not getting any data at all
        }

        ItemWrapper privateData = null;
        if(provider != null) {
            privateData = provider.getPrivateData(dataTypeExtras);
        }

        return privateData;
    }

    private Bundle processData(DataRequest.DataType dataType, ItemWrapper privateData, String palProvider, Bundle palExtras) {
        MicroPALProviderServiceConnection pal = MicroPALProviderServiceConnection.getConnection(palProvider);
        if(pal != null) {
            try {
                Log.i(TAG, String.format("Attempting to process dataType=%s on palProvider=%s",
                                         dataType.name(), palProvider));
                Log.i(TAG, "Item type is " + privateData.getTypeString());
                Bundle processed = pal.processData(dataType, privateData, palExtras);
                return processed;

            } catch(IllegalArgumentException | RemoteException e) {
                Log.e(TAG, "Failed to connect to PAL provider " + palProvider, e);
                return null;
            }
        }

        Log.e(TAG, "Requested PAL provider " + palProvider + " not found");
        return null;
    }

    private class AsyncProcessor extends AsyncTask<Object, Void, Bundle> {
        private DataRequest.DataType dt;
        private Bundle dataTypeExtras;
        private String palProvider;
        private Bundle palExtras;
        private ResultReceiver receiver;

        public AsyncProcessor(DataRequest.DataType dt, Bundle dataTypeExtras, String palProvider, Bundle palExtras, ResultReceiver receiver) {
            this.dt = dt;
            this.dataTypeExtras = dataTypeExtras;
            this.palProvider = palProvider;
            this.palExtras = palExtras;
            this.receiver = receiver;
        }

        @Override
        protected Bundle doInBackground(Object... params) {
            Bundle processed = null;
            try {
                ItemWrapper privateData = getPrivateData(dt, dataTypeExtras);

                if (privateData != null) {
                    // Process data in uPAL synchronously, then report result to callback
                    processed = processData(dt, privateData, palProvider, palExtras);
                }

            } catch(IllegalArgumentException e) {
                Log.e(TAG, "Failed to obtain data of type " + dt.name(), e);
            }

            return processed;
        }

        @Override
        protected void onPostExecute(Bundle result) {
            int resultCode = (result != null) ? PrivateDataManager.RESULT_SUCCESS : PrivateDataManager.RESULT_UNABLE_TO_ACQUIRE_DATA;
            Log.i(TAG, "Async processing complete, sending result back up to the PDM");
            receiver.send(resultCode, result);
        }
    }

    @Override
    public void requestData(String callingPackage, String dataType, Bundle dataTypeExtras, String palProvider, Bundle palExtras, String purpose, ResultReceiver receiver) {
        DataRequest.DataType dt = DataRequest.DataType.valueOf(dataType);

        // Query the policy manager if it's a dangerous permission
        MicroPALProviderServiceConnection pal = MicroPALProviderServiceConnection.getConnection(palProvider);
        int policyResult = PackageManager.PERMISSION_NO_POLICY_MANAGER;
        if(mPolicyManager != null) {
            if(pal != null) {
                try {
                    String palDescription = pal.getDescription();
                    policyResult = mPolicyManager.queryPolicyManager(callingPackage, dt, purpose, palProvider, palDescription);
                } catch(RemoteException e) {
                    Log.e(TAG, "Failed to get description from PAL ID " + palProvider, e);
                }
            } else {
                Log.e(TAG, "PAL ID " + palProvider + " not found");
            }
        } else {
            Log.e(TAG, "Attempting to request data with no Policy Manager present");
        }

        // Get private data
        if(policyResult == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, String.format("Permission granted to app %s for data type %s with purpose %s",
                                     callingPackage, dataType, purpose));

            AsyncProcessor processor = new AsyncProcessor(dt, dataTypeExtras, palProvider, palExtras, receiver);
            processor.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);        // NOTE(irwin): Parallelization should be OK because these are all read operations


        } else {
            Log.e(TAG, String.format("Policy Manager denied package %s for data type %s with purpose %s. Error code %d",
                                     callingPackage, dataType, purpose, policyResult));
            receiver.send(PrivateDataManager.RESULT_REQUEST_DENIED, null);
        }
    }

    @Override
    public List<String> getPALProviders(String dataType) {
        try {
            DataRequest.DataType type = null;
            if(dataType != null) {
                type = DataRequest.DataType.valueOf(dataType);
            }

            return MicroPALProviderServiceConnection.getMicroPALIdentifiers(type);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Invalid PAL data type string " + dataType, e);
            return null;
        }
    }
}
