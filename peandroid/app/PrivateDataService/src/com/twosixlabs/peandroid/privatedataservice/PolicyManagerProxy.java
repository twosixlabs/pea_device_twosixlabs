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

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.policymanager.IPolicyManager;
import android.policymanager.ThreadDump;
import android.privatedata.DataRequest;
import android.util.Log;

import com.android.internal.privacy.IPrivacyManager;

import java.util.concurrent.CountDownLatch;

public class PolicyManagerProxy {
	//private static final String TAG = PolicyManagerProxy.class.getSimpleName();
	private static final String TAG = "PolicyManagerProxy2";

    private static IPrivacyManager sPolicyManagerManager;
    private static IPolicyManager sPolicyManager;

    public PolicyManagerProxy() throws RemoteException {
        initPolicyManager();
    }

    public int queryPolicyManager(String packageName, DataRequest.DataType dataType, String purpose, String pal, String palDescription) {
        try {
            initPolicyManager();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        int result = PackageManager.PERMISSION_NO_POLICY_MANAGER;

        if(sPolicyManager != null) {
            result = PackageManager.PERMISSION_DENIED;

            if(dataType == DataRequest.DataType.EMPTY || dataType == DataRequest.DataType.ANY) {
                Log.d(TAG, "Policy for 'EMPTY' or 'ANY' data type is always granted");
                result = PackageManager.PERMISSION_GRANTED;

            } else {
                String permission = DataRequest.dataTypeToPermission(dataType);

                final CountDownLatch LATCH = new CountDownLatch(1);
                final PolicyResultReceiver CALLBACK = new PolicyResultReceiver(LATCH);

                try {
                    sPolicyManager.onPrivateDataRequest(packageName, permission, purpose, pal, palDescription, CALLBACK);
                    LATCH.await();

                    if(CALLBACK.mAllowPerm) {
                        Log.d(TAG, String.format("Policy granted for package %s, permission %s, for purpose %s", packageName, permission, purpose));
                        result = PackageManager.PERMISSION_GRANTED;
                    }
                } catch(RemoteException | InterruptedException e) {
                    Log.e(TAG, "Error querying PolicyManager", e);
                }
            }
        }

        return result;
    }

    private void initPolicyManager() throws RemoteException {
        if(sPolicyManagerManager == null) {
            Log.d(TAG, "Attempting to connect to Policy Manager Manager");
            sPolicyManagerManager = IPrivacyManager.Stub.asInterface(ServiceManager.getService("privacy_manager"));
        }

        if(sPolicyManagerManager != null) {
            Log.d(TAG, "Attempting to connect to Policy Manager");
            sPolicyManager = sPolicyManagerManager.getCurrentManager();

            if(sPolicyManager != null) {
                Log.d(TAG, "Connected to Policy Manager");
            } else {
                Log.e(TAG, "Failed to connect to Policy Manager");

            }
        } else {
            Log.e(TAG, "Failed to connect to Policy Manager Manager");
        }
    }

	private class PolicyResultReceiver extends ResultReceiver {
	    boolean mAllowPerm;
	    CountDownLatch latch;

	    PolicyResultReceiver(CountDownLatch latch) {
            super(null);
            this.latch = latch;
	    }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            mAllowPerm = resultData.getBoolean("allowPerm");
            latch.countDown();
        }
	}
}
