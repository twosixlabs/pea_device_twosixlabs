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

package com.twosixlabs.peandroid.privacynotificationservice;

import android.app.INotificationManager;
import android.app.IPrivacyNotificationManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ParceledListSlice;
import android.graphics.Color;
import android.os.IBinder;
import android.os.UserHandle;
import android.os.ServiceManager;
import android.os.RemoteException;
import android.util.Log;

import com.android.internal.privacy.IPrivacyManager;

import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;

class IPrivacyNotificationServiceImpl extends IPrivacyNotificationManager.Stub {
	private Context mContext;
	private static final String TAG = IPrivacyNotificationServiceImpl.class.getSimpleName();
	private static final String CHANNEL_ID = "peandroid_privacy_channel";
	private static final String TEST_PACKAGE = "com.twosix.android.permissionstest";

	private INotificationManager notificationManager;

	private Set<String> mPackagesWithChannels;
	private int mNotificationId;

	public IPrivacyNotificationServiceImpl(Context context) {

		mContext = context;
		mPackagesWithChannels = new HashSet<>();

		IBinder b = ServiceManager.getService(Context.NOTIFICATION_SERVICE);
		notificationManager = INotificationManager.Stub.asInterface(b);

		/* Set up receiver to detect when new package is installed */
		IntentFilter installFilter = new IntentFilter();
		installFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
		installFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
		installFilter.addDataScheme("package");

		mContext.registerReceiver(mInstallReceiver, installFilter);
		mNotificationId = 0;
	}

    @Override
    public void sendPrivacyNotification(String packageName, String title, String body) {
	Log.d(TAG, "Sending privacy notification for " + packageName);
	if (!mPackagesWithChannels.contains(packageName)) {
	    Log.e(TAG, "Package " + packageName + " does not have notification channel, creating one");
	    boolean created = createPrivacyChannelForPackage(packageName);
	    if (!created) {
		Log.e(TAG, "Unable to send privacy notification: could not create channel");
		return;
	    }
	}

	Notification.Builder notification = new Notification.Builder(mContext, CHANNEL_ID)
	    .setContentTitle(title)
	    .setContentText(body)
	    .setSmallIcon(com.android.internal.R.drawable.emergency_icon)
        .setStyle(new Notification.BigTextStyle().bigText(body));

	try {
	    IPrivacyManager privacyManagerService = IPrivacyManager.Stub.asInterface(ServiceManager.getService("privacy_manager"));
	    ComponentName currentManager = privacyManagerService.getCurrentManagerName();
	    Intent launchPolicyManagerIntent = mContext.getPackageManager().getLaunchIntentForPackage(currentManager.getPackageName());
	    PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, launchPolicyManagerIntent, 0);
	    notification.setContentIntent(pendingIntent);
	} catch (RemoteException e) {
	    Log.e(TAG, "Unable to set intent on notification");
	}

	try {
	    notificationManager.enqueueNotificationWithTag(packageName, packageName, null, mNotificationId++, notification.build(), 0 /* UserHandle.USER_SYSTEM */);
	    Log.d(TAG, "Sent notification");
	} catch (RemoteException e) {
	    Log.e(TAG, "Failed to post notification");
	    e.printStackTrace();
	}
    }

    private boolean createPrivacyChannelForPackage(String packageName) {
	int packageUid = 0;
	try {
	    packageUid = mContext.getPackageManager().getApplicationInfo(packageName, 0).uid;
	} catch (PackageManager.NameNotFoundException e) {
	    Log.e(TAG, "Error creating privacy channel", e);
	    return false;
	}

	NotificationChannel privacyChannel = new NotificationChannel(CHANNEL_ID, "Privacy Notifications", NotificationManager.IMPORTANCE_HIGH);
	privacyChannel.enableVibration(true);
	privacyChannel.enableLights(true);
	privacyChannel.setLightColor(Color.RED);
	privacyChannel.setShowBadge(true);
	privacyChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
	try {
	    notificationManager.createNotificationChannelsForPackage(packageName, packageUid, new ParceledListSlice(Arrays.asList(privacyChannel)));
	    mPackagesWithChannels.add(packageName);
	} catch (RemoteException e) {
	    Log.e(TAG, "Error creating privacy channel", e);
	    return false;
	}
	return true;
    }

	private BroadcastReceiver mInstallReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			String packageName = intent.getData().getEncodedSchemeSpecificPart();
			int packageUid = 0;
			try {
				packageUid = context.getPackageManager().getApplicationInfo(packageName, 0).uid;
			} catch (PackageManager.NameNotFoundException e) {
				e.printStackTrace();
			}

			Log.d(TAG, "Creating privacy notification channel for package " + packageName);

			NotificationChannel privacyChannel = new NotificationChannel(CHANNEL_ID, "Privacy Notifications", NotificationManager.IMPORTANCE_HIGH);
			privacyChannel.enableVibration(true);
			privacyChannel.enableLights(true);
			privacyChannel.setLightColor(Color.RED);
			privacyChannel.setShowBadge(true);
			privacyChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
			try {
				notificationManager.createNotificationChannelsForPackage(packageName, packageUid, new ParceledListSlice(Arrays.asList(privacyChannel)));
				mPackagesWithChannels.add(packageName);
			} catch (RemoteException e) {
				Log.d(TAG, "Failed to create notification channel");
				e.printStackTrace();
			}

			Log.d(TAG, "Finished creating privacy notification channel");
		}
	};

}
