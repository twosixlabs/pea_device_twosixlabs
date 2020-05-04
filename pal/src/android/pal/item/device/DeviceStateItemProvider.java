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

package android.pal.item.device;

import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.pal.item.ItemProvider;
import android.privatedata.ItemWrapper;
import android.telecom.PhoneAccount;
import android.util.Log;

import com.android.internal.telephony.IPhoneSubInfo;
import com.android.internal.telephony.ITelephony;
import com.android.internal.telecom.ITelecomService;

import java.util.List;

public class DeviceStateItemProvider extends ItemProvider<DeviceStateItem> {
    private static final String TAG = DeviceStateItemProvider.class.getSimpleName();

    public DeviceStateItemProvider(Context context) {
        super(context);
    }

    @Override
    protected ItemWrapper<DeviceStateItem> acquirePrivateData(Bundle params) {
		IBinder b = ServiceManager.getService(Context.TELECOM_SERVICE);
		ITelecomService telecom = ITelecomService.Stub.asInterface(b);

        IBinder b2 = ServiceManager.getService(Context.TELEPHONY_SERVICE);
        ITelephony telephony = ITelephony.Stub.asInterface(b2);

        IBinder b3 = ServiceManager.getService("iphonesubinfo");
        IPhoneSubInfo phoneInfo = IPhoneSubInfo.Stub.asInterface(b3);

        DeviceStateItem data = null;
        try {
            List<PhoneAccount> accounts = telecom.getAllPhoneAccounts();
            int numAccounts = accounts.size();

            PhoneAccount acct = null;
            if(numAccounts == 0) {
                Log.w(TAG, "No phone accounts found. Some fields will be left default.");
            } else if(numAccounts == 1) {
                acct = accounts.get(0);
            } else {
                // TODO Handle multiple PhoneAccounts
                Log.w(TAG, numAccounts + " phone accounts found. Setting fields using only the first one.");
                acct = accounts.get(0);
            }

            if(acct != null) {
                String packageName = getContext().getPackageName();
                int subId = telephony.getSubIdForPhoneAccount(acct);

                String phoneNumber = phoneInfo.getLine1Number(packageName);
                String cellCarrier = telephony.getSubscriptionCarrierName(subId);
                int cellCarrierId = telephony.getSubscriptionCarrierId(subId);
                int signalStrength = telephony.getSignalStrength(subId).getLevel();
                String simId = phoneInfo.getIccSerialNumberForSubscriber(subId, packageName);
                String imei = telephony.getDeviceId(packageName);
                String meid = telephony.getDeviceId(packageName);

                data = new DeviceStateItem();

                data.setFieldValue(DeviceStateItem.PHONE_NUMBER, phoneNumber);
                data.setFieldValue(DeviceStateItem.CELL_CARRIER, cellCarrier);
                data.setFieldValue(DeviceStateItem.CELL_CARRIER_ID, cellCarrierId);
                data.setFieldValue(DeviceStateItem.CELL_SIGNAL_STRENGTH, signalStrength);
                data.setFieldValue(DeviceStateItem.SIM_ID, simId);
                data.setFieldValue(DeviceStateItem.IMEI, imei);
                data.setFieldValue(DeviceStateItem.MEID, meid);
            }

        } catch (RemoteException e) {
            Log.w(TAG, "Failed to communicate with ITelecomService, ITelephony, or IPhoneSubInfo");
            e.printStackTrace();
        }

        return new ItemWrapper<DeviceStateItem>(data){};
    }

    @Override
    public Bundle getParams() {
        Log.d(TAG, "No parameters are accepted");
        return null;
    }
}
