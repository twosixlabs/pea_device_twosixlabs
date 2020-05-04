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

package android.pal.item.location;

import android.content.Context;
import android.location.ILocationListener;
import android.location.ILocationManager;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationRequest;
import android.os.Bundle;
import android.privatedata.DataRequest;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.pal.item.ItemProvider;
import android.privatedata.ItemWrapper;
import android.util.Log;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.List;

import static android.privatedata.DataRequest.LocationParamsBuilder.UPDATE_MODE;
import static android.privatedata.DataRequest.LocationParamsBuilder.UPDATE_TIMEOUT_MILLIS;
import static android.privatedata.DataRequest.LocationParamsBuilder.PROVIDER;
import static android.privatedata.DataRequest.LocationParamsBuilder.MODE_LAST_LOCATION;
import static android.privatedata.DataRequest.LocationParamsBuilder.MODE_SINGLE_UPDATE;
import static android.privatedata.DataRequest.LocationParamsBuilder.MODE_UNSET;

public class LocationItemProvider extends ItemProvider<LocationItem> {
    private static final String TAG = LocationItemProvider.class.getSimpleName();
    private static final String DEFAULT_MODE = MODE_UNSET;
    private static final int DEFAULT_TIMEOUT_MILLIS = -1;

    public LocationItemProvider(Context context) {
        super(context);
    }

    @Override
    protected ItemWrapper<LocationItem> acquirePrivateData(Bundle params) {
        String mode = params.getString(UPDATE_MODE);

        Location location;
        switch(mode) {
            case MODE_LAST_LOCATION:
                Log.i(TAG, "Getting last location");
                location = getLastLocation(params.getString(PROVIDER));
                break;

            case MODE_SINGLE_UPDATE:
                Log.i(TAG, "Getting single location update");
                location = getSingleUpdate(params.getInt(UPDATE_TIMEOUT_MILLIS), params.getString(PROVIDER));
                break;

            // case MODE_CONTINUOUS_UPDATE:
            //     Log.i(TAG, "Getting continuous location updates");
            //     location = getContinuousUpdates(params.getInt(UPDATE_TIMEOUT_MILLIS));
            //     break;

            default:
                Log.w(TAG, "Got unhandled location update mode" + mode);
                location = null;
        }

        if(location != null) {
            LocationItem data = new LocationItem(location);
            return new ItemWrapper<LocationItem>(data){};
        } else {
            Log.w(TAG, "Failed to acquire location under mode " + mode);
            return null;
        }
    }

    private Location getLastLocation(String provider) {

        LocationManager lm = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        Location loc = null;

        if (provider != null) {
            try {
                loc = lm.getLastKnownLocation(provider);
            } catch (Exception e) {
                Log.e(TAG, "Unable to get last known location", e);
            }
        } else {
            // if the provider isn't specified, get the most recent location out of all available providers
            List<String> providers = lm.getAllProviders();
            long newestTime = 0;
            for (String p : providers) {
                Location providerLoc = null;
                try {
                    providerLoc = lm.getLastKnownLocation(p);
                } catch (Exception e) {
                    Log.e(TAG, "Unable to get last known location for " + p);
                }
                if (providerLoc != null && providerLoc.getTime() > newestTime) {
                    newestTime = providerLoc.getTime();
                    loc = providerLoc;
                }
            }
        }
        return loc;
    }

    private class SingleLocationListener extends ILocationListener.Stub {
        private CountDownLatch latch;
        private Location result;

        public SingleLocationListener(CountDownLatch latch) {
            this.latch = latch;
        }

        public Location getResult() {
            return result;
        }

        @Override
        public void onLocationChanged(Location location) {
            Log.i(TAG, "Received location callback");
            result = location;
            latch.countDown();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // Do nothing
        }

        @Override
        public void onProviderEnabled(String provider) {
            // Do nothing
        }

        @Override
        public void onProviderDisabled(String provider) {
            // Do nothing
        }
    }

    private Location getSingleUpdate(int timeoutMillis, String provider) {
        if (provider == null) provider = LocationManager.FUSED_PROVIDER;
        final LocationRequest REQUEST = new LocationRequest();
        REQUEST.setProvider(provider)
               .setQuality(LocationRequest.ACCURACY_FINE)
               .setInterval(0)
               .setFastestInterval(0)
               .setSmallestDisplacement(0)
               .setNumUpdates(1);

        final CountDownLatch LATCH = new CountDownLatch(1);
        final SingleLocationListener CALLBACK = new SingleLocationListener(LATCH);

		IBinder b = ServiceManager.getService(Context.LOCATION_SERVICE);
		ILocationManager lm = ILocationManager.Stub.asInterface(b);

        Location loc = null;
        try {
            String packageName = getContext().getPackageName();

            Log.d(TAG, "Requesting location update");
            lm.requestLocationUpdates(REQUEST, CALLBACK, null, packageName);

            Log.d(TAG, "Awaiting location update");
            LATCH.await(timeoutMillis, TimeUnit.MILLISECONDS);
            if(LATCH.getCount() > 0) {
                Log.d(TAG, "Location update timed out after " + timeoutMillis + " milliseconds");
            } else {
                Log.d(TAG, "Location update complete");
            }

            lm.removeUpdates(CALLBACK, null, packageName);

            loc = CALLBACK.getResult();

        } catch (RemoteException e) {
            Log.e(TAG, "Failed to call ILocationManager.getLastLocation()");
            e.printStackTrace();

        } catch (InterruptedException e) {
            Log.e(TAG, "Single location update operation was interrupted");
            e.printStackTrace();
        }

        return loc;
    }

    private Location getContinuousUpdates(int timeoutMillis) {
        // TODO Figure out how to continuous updates in this architecture
        return null;
    }

    @Override
    public Bundle getParams() {
        Bundle params = new DataRequest.LocationParamsBuilder()
            .setUpdateMode(DEFAULT_MODE)
            .setTimeoutMillis(DEFAULT_TIMEOUT_MILLIS)
            .build();
        return params;
    }

    @Override
    protected ParamStatus checkParams(Bundle params) {
        // Run the basic checks defined in the superclass
        ParamStatus checkResult = super.checkParams(params);

        if(checkResult == ParamStatus.GOOD) {
            // Make sure the update mode is set
            String mode = params.getString(UPDATE_MODE);

            if (!mode.equals(MODE_UNSET)) {
                // Make sure the timeout is set if using asynchronous modes
                if (mode.equals(MODE_SINGLE_UPDATE)) {
                    int timeoutMillis = params.getInt(UPDATE_TIMEOUT_MILLIS);
                    if (timeoutMillis >= 0) {
                        return ParamStatus.GOOD;
                    } else {
                        Log.e(TAG, "Invalid update timeout value " + timeoutMillis);
                        return ParamStatus.ERROR_UNEXPECTED_PARAM_VALUE;
                    }
                } else if (mode.equals(MODE_LAST_LOCATION)) {
                    return ParamStatus.GOOD;
                } else {
                    Log.e(TAG, "Got unexpected update mode " + mode);
                    return ParamStatus.ERROR_UNEXPECTED_PARAM_VALUE;
                }
            } else {
                Log.e(TAG, "Location update mode is unset");
                return ParamStatus.ERROR_UNEXPECTED_PARAM_VALUE;
            }
        } else {
            return checkResult;
        }
    }
}
