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

import android.os.Bundle;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.privatedata.DataRequest;
import android.privatedata.IPrivateDataManagerService;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that manages all Privacy transform interactions between an app and
 * the corresponding PAL implementation.
 *
 */
public class PrivateDataManager {
    public static final int RESULT_SUCCESS = 0;
    public static final int RESULT_UNABLE_TO_ACQUIRE_DATA = 1;
    public static final int RESULT_REQUEST_DENIED = 2;

    private static final String TAG = PrivateDataManager.class.getSimpleName();
    private static final String PDMS = IPrivateDataManagerService.class.getName();

    private static IPrivateDataManagerService sPDMS;

    /**
     * Get new instance of PrivateDataManager
     * @return the instance
     */
    public static PrivateDataManager getInstance() {
        return new PrivateDataManager();
    }

    private PrivateDataManager() {
        if(sPDMS == null) {
            Log.d(TAG, "Connecting to PDMS " + PDMS);
            sPDMS = IPrivateDataManagerService.Stub.asInterface(ServiceManager.getService(PDMS));

            if(sPDMS == null) {
                throw new IllegalStateException("Failed to connect to PDMS " + PDMS);
            }
        }
    }

    /**
     *  Request sensitive data obtained asynchronously. For example,
     *  current location, location updates, and other streaming types.
     *  Data returned via callback.
     *  @param request An object specifying the data type, target PAL, parameters, and callback.
     */
    public void requestData(DataRequest request) {
        String callingPackage = request.getContext().getPackageName();
        String dataType = request.getDataType().name();
        Bundle dataTypeExtras = request.getDataTypeExtras();
        String pal = request.getPalProvider();
        Bundle palExtras = request.getPalExtras();
        String purpose = request.getPurpose().toString();
        ResultReceiver callback = request.getReceiver();

        try {
            sPDMS.requestData(callingPackage, dataType, dataTypeExtras, pal, palExtras, purpose, callback);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get a list of all installed PAL providers for a given type
     * @param type The type that the data provider should secure. "ANY" will return providers that accept all types. null will return all providers.
     * @return A list of provider names that secure the type
     */
    public List<String> getInstalledPALProviders(DataRequest.DataType type) {
        try {
            List<String> pals = sPDMS.getPALProviders(type.name());
            if(pals == null) {
                Log.w(TAG, "Received null PALs list from PDMS. Returning empty list.");
                pals = new ArrayList<>();
            }

            return pals;
        } catch (RemoteException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
