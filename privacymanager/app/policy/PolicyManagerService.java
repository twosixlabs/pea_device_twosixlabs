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

package android.app.policy;

import android.Manifest;
import android.app.PrivacyNotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.policymanager.IPolicyManager;
import android.policymanager.PrivacySettingInfo;
import android.policymanager.ThreadDump;
import android.util.Log;

import java.util.ArrayList;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.HashMap;

/**
 * This abstract class is used to create a policy manager on the system.
 * Subclasses will create their own implementation for how to manage
 * permission requests
 */
public abstract class PolicyManagerService extends Service {

	private static final String TAG = PolicyManagerService.class.getSimpleName();
	//public final static int PE_Android_Build_Version = Build.VERSION_CODES.PE_Android;
	/**
	 * Flag to indicate that the request came from a system app
	 */
	public final static int FROM_SYS_APP_REQ = 1 << 0;
	/**
	 * Flag to indicate that the request came from a privileged system app, and may lead to system instability if denied
	 */
	public final static int FROM_PRIV_APP_REQ = 1 << 1;
	/**
	 * Flag to indicate that the request came from the Android system on behalf of an app, i.e. {READ,WRITE}_EXTERNAL_STORAGE
	 */
	public final static int FROM_ANDROID_REQ = 1 << 2;

	// key is setting id
	private LinkedHashMap<String, PrivacySettingInfo> mQuickSettings;
	private Map<String, PrivacySettingListener> mQuickSettingsCallbacks;


    /**
     * Get the off device policy for a package
     * @param packageName package for which to get the policy
     * @return the policy
     */
	public String getOffDevicePolicy(String packageName) {

		try {
			PackageInfo pi = getPackageManager().getPackageInfo(packageName, 0);
			return pi.offDevicePolicy;
		} catch (PackageManager.NameNotFoundException e) {
			Log.e(TAG, "Can't get PackageInfo for " + packageName);
		}

		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		ComponentName component = new ComponentName(this, getClass());
		try {
			ServiceInfo serviceInfo = getPackageManager().getServiceInfo(component, 0 /* flags */);
			if (!Manifest.permission.POLICY_MANAGER_SERVICE.equals(serviceInfo.permission)) {
				throw new IllegalStateException(component.flattenToShortString()
						+ " is not declared with the permission "
						+ "\"" + Manifest.permission.POLICY_MANAGER_SERVICE + "\"");
			}
		} catch (PackageManager.NameNotFoundException e) {
			Log.e(TAG, "Can't get ServiceInfo for " + component.toShortString());
		}
		mQuickSettings = new LinkedHashMap<>();
		mQuickSettingsCallbacks = new HashMap<>();
	}

	@Override
	public final IBinder onBind(Intent intent) {
		return mBinder;
	}

	private final IPolicyManager.Stub mBinder = new IPolicyManager.Stub() {
		@Override
		public boolean onAppInstall(String packageName, String odp) {
		    return PolicyManagerService.this.onAppInstall(packageName, odp);
		}

		@Override
		public void onPrivateDataRequest(String packageName, String permission, String purpose, String pal, String description, ResultReceiver recv) {
		    PolicyManagerService.this.onPrivateDataRequest(packageName, permission, purpose, pal, description, recv);
		}

		@Override
		public void onDangerousPermissionRequest(String packageName, String permission, String purpose, ThreadDump threadDump, int flags, ComponentName callingComponent, ComponentName topActivity, ResultReceiver recv) {
		    PolicyManagerService.this.onDangerousPermissionRequest(packageName, permission, purpose, (threadDump == null) ? null : threadDump.getStackTraces(), flags, callingComponent, topActivity, recv);
		}

		@Override
		public void onPrivacyQuickSettingSelected(String settingId) {
		    PrivacySettingInfo setting = mQuickSettings.get(settingId);
		    PrivacySettingListener callback = mQuickSettingsCallbacks.get(settingId);
		    if (callback != null) {
			// toggle state and report update via callback
			boolean currentState = setting.getEnabled();
			setPrivacyQuickSettingEnabled(settingId, !currentState);
			callback.onSettingChanged(settingId, !currentState);
		    } else {
			Log.e(TAG, "No callback for setting " + settingId);
		    }
		}

		@Override
		public List<PrivacySettingInfo> getPrivacyQuickSettings() {
		    return new ArrayList<PrivacySettingInfo>(mQuickSettings.values());
		}
	};

    /**
     * Interface for receiving updates when privacy quick setting tiles are pressed
     */
    public interface PrivacySettingListener {
        /**
         * Called when a privacy quick settings tile is pressed
         * @param settingName Id of the setting that was pressed
         * @param enabled The new state of the setting after being pressed
         */
        public void onSettingChanged(String settingName, boolean enabled);
    }

    /**
     * Add a new privacy quick setting tile.
     * @param id A unique string identifier for the new setting.
     * @param text Text to be displayed with this setting
     * @param enabledIcon Icon to display when the setting is enabled
     * @param disabledIcon Icon to display when the setting is disabled.
     *        If null, the enabled Icon will be used
     * @param callback A {@link PolicyManagerService.PrivacySettingListener PrivacySettingListener used to detect when the setting is pressed
     */
    protected final void addPrivacyQuickSetting(String id, String text, Drawable enabledIcon, Drawable disabledIcon, PrivacySettingListener callback) {
        Log.d(TAG, "Adding privacy setting " + id);

        // Draw Bitmap for enabled icon
        Bitmap enabledBitmap = Bitmap.createBitmap(enabledIcon.getIntrinsicWidth(), enabledIcon.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(enabledBitmap);
        enabledIcon.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        enabledIcon.draw(canvas);

        // Draw Bitmap for optional disabled icon. If it is null, just use enabled icon
        Bitmap disabledBitmap;
        if (disabledIcon != null) {
            disabledBitmap = Bitmap.createBitmap(disabledIcon.getIntrinsicWidth(), disabledIcon.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            canvas = new Canvas(disabledBitmap);
            disabledIcon.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            disabledIcon.draw(canvas);
        } else {
            disabledBitmap = enabledBitmap;
        }

        mQuickSettings.put(id, new PrivacySettingInfo(id, text, true, enabledBitmap, disabledBitmap));
        mQuickSettingsCallbacks.put(id, callback);
        notifyQuickSettingsChanged();
    }

    /**
     * Set the state of an existing privacy quick setting tile.
     * @param id Id of the setting to change
     * @param enabled Sets the setting to enabled or disabled
     */
    protected final void setPrivacyQuickSettingEnabled(String id, boolean enabled) {
        PrivacySettingInfo setting = mQuickSettings.get(id);
        setting.setEnabled(enabled);
        notifyQuickSettingsChanged();
    }

    /**
     * Remove a privacy quick setting.
     * @param id Id of the setting to remove
     */
    protected final void removePrivacyQuickSetting(String id) {
        Log.d(TAG, "Remove privacy setting " + id);
        mQuickSettings.remove(id);
        mQuickSettingsCallbacks.remove(id);
        notifyQuickSettingsChanged();
    }

    /* Send intent to notify PrivacySettingsController in SystemUI that the list of quick settings has changed */
    private void notifyQuickSettingsChanged() {
        Intent notifyIntent = new Intent("android.intent.action.PRIVACY_SETTINGS_CHANGED");
        sendBroadcast(notifyIntent);
	}

    /**
     * Send a privacy alert notification to a package. This can be used if the
     * policy manager wants to alert the user about an app's behavior
     * @param packageName Package name of the app to send the notification for
     * @param title Title text of the notification
     * @param body Body text of the notification
     */
    protected final void sendPrivacyNotification(String packageName, String title, String body) {
        PrivacyNotificationManager pnm = PrivacyNotificationManager.getInstance();
        if (pnm != null) {
            Log.d(TAG, "Sending privacy notification for " + packageName);
            pnm.sendPrivacyNotification(packageName, title, body);
        } else {
            Log.d(TAG, "Unable to get instance of PrivacyNotificationManager");
        }
    }

    /**
     * Called when an app is installed and its declared permissions and purposes are being parsed
     * @param packageName The package name of the app being installed
     * @param odp The string representation of the entire Off Device Policy, a.k.a. App policy
     * @return True if the app is allowed to be installed, False if not
     */
    public abstract boolean onAppInstall(String packageName, String odp);

    /**
     * Called whenever a new Private Data Request is received.
     * @param packageName The name of the package that made the private data request
     * @param permission The name of the permission
     * @param purpose The purpose of this private data request
     * @param description A description of the transform
     * @param recv A RequestReceiver object that should be utilized to return the permission decision along with any associated policy for the request.
     *             The result of the permission request should be set via putting the boolean allow/deny into the Bundle with key 'allowPerm'. The policy
     *             should be returned to the caller by setting the String to the policy for tag 'ODP'. See code snippet below as an indication for the expected
     *             return parameters.
     *
     *             public void onPrivateDataRequest(String packageName, String permission, String purpose, String description, ResultReceiver recv) {
     *                 ...
     *                 Bundle b = new Bundle();
     *                 b.putBoolean("allowPerm", true);
     *                 recv.send(0, b);
     *              }
     */
    public abstract void onPrivateDataRequest(String packageName, String permission, String purpose, String pal, String description, ResultReceiver recv);

    /**
     * Called whenever a new dangerous permission request occurs.
     * @param packageName The name of the package that made the dangerous request
     * @param permission The name of the permission
     * @param purpose The purpose of this permission request
     * @param stackTraceElements A stack trace of the permission call
     * @param flags A bitmask of flags indicating the origin of the permission request
     * @param callingComponent The calling component from the Application making the request. If it can not be determined it will be null
     * @param topActivity The top activity that is displayed on the device.
	 * @param recv A RequestReceiver object that should be utilized to return the permission decision along with any associated policy for the request.
	 *             The result of the permission request should be set via putting the boolean allow/deny into the Bundle with key 'allowPerm'. The policy
	 *             should be returned to the caller by setting the String to the policy for tag 'ODP'. See code snippet below as an indication for the expected
	 *             return parameters.
	 *
	 *             public void onDangerousPermissionRequest(String packageName, String permission, String purpose, List{@literal <}StackTraceElement[]{@literal >}{ stackTrace, int flags, ResultReceiver recv) {
	 *                 ...
	 *                 Bundle b = new Bundle();
	 *                 b.putBoolean("allowPerm", true);
	 *                 b.putString("ODP", "This is the policy...");
	 *                 recv.send(0, b);
	 *              }
     */
    public abstract void onDangerousPermissionRequest(String packageName, String permission, String purpose, List<StackTraceElement[]> stackTraceElements, int flags, ComponentName callingComponent, ComponentName topActivity, ResultReceiver recv);
}
