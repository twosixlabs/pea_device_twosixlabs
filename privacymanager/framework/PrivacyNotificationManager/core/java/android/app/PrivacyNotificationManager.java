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

package android.app;

import android.app.IPrivacyNotificationManager;
import android.content.Context;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;

public final class PrivacyNotificationManager {
    public static final String PRIVACY_NOTIFICATION_SERVICE = IPrivacyNotificationManager.class.getName();
    private static final String TAG = "PrivacyNotificationManager";

    private IPrivacyNotificationManager sPrivacyNotificationService;

    public static PrivacyNotificationManager getInstance() { return new PrivacyNotificationManager();}

    private PrivacyNotificationManager() {
        if (sPrivacyNotificationService == null) {
            Log.d(TAG, "Connecting to IPrivacyNotifcationManager by name [" + PRIVACY_NOTIFICATION_SERVICE + "]");
            sPrivacyNotificationService = IPrivacyNotificationManager.Stub.asInterface(ServiceManager.getService(PRIVACY_NOTIFICATION_SERVICE));
            if (sPrivacyNotificationService == null) {
                throw new IllegalStateException("Failed to find IPrivacyNotificationManager by name [" + PRIVACY_NOTIFICATION_SERVICE + "]");
            }
        }
    }

    /** @hide */
    public void sendPrivacyNotification(String packageName, String title, String body) {
	try {
	    sPrivacyNotificationService.sendPrivacyNotification(packageName, title, body);
	} catch (RemoteException e) {
	    Log.e(TAG, "RemoteException", e);
	}
    }
}
