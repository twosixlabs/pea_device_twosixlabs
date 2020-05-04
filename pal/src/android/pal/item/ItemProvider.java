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

package android.pal.item;

import android.content.Context;
import android.os.Bundle;
import android.privatedata.ItemWrapper;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

/**
 *  Base class for calls to the platform to request sensitive information.
 */
public abstract class ItemProvider<T extends Item> {
    private static final String TAG = ItemProvider.class.getSimpleName();
    private static final Bundle EMPTY_BUNDLE = new Bundle();

    protected Context mContext;

    protected enum ParamStatus {
        GOOD,
        ERROR_NULL_EXPECTED,
        ERROR_NON_NULL_EXPECTED,
        ERROR_MISSING_KEYS,
        ERROR_UNEXPECTED_PARAM_VALUE,
    };

    public ItemProvider(Context context) {
        mContext = context;
    }

    /**
     *  Get private data from the platform.
     *  @param params Paramaters used in acquiring private data.
     *  @return Item-wrapped private data.
     */
    public final ItemWrapper<T> getPrivateData(Bundle params) throws IllegalArgumentException {
        ParamStatus checkResult = checkParams(params);
        Log.i(TAG, "Got param check result " + checkResult.name());

        if(checkResult == ParamStatus.GOOD) {
            return acquirePrivateData(params);
        }

        throw new IllegalArgumentException(checkResult.name());
    }

    /**
     *  Get a Bundle containing the expected parameters to request private
     *  data from the platform.
     *  @return A Bundle strucutered for the expected parameters.
     */
    public abstract Bundle getParams();

    protected abstract ItemWrapper<T> acquirePrivateData(Bundle params);

    protected ParamStatus checkParams(Bundle params) {
        Log.i(TAG, "Param checK: Starting basic ItemProvider param check");

        Bundle expected = getParams();
        boolean paramsExpected = expected != null && !expected.isEmpty();

        // Simple case: this Item provider doesn't expect any parameters, so any input is good
        if(!paramsExpected) {
            if(params != null) {
                printBundleDiff(params, EMPTY_BUNDLE);
            }

            Log.i(TAG, "Param check: No params required, GOOD");
            return ParamStatus.GOOD;
        }

        // More complicated case: this provider does expect parameters
        else {
            boolean paramsGiven = params != null && !params.isEmpty();
            if(paramsGiven) {
                printBundleDiff(params, expected);

                // If parameters are given, make sure all the expected keys are present
                Set<String> expectedKeys = expected.keySet();
                Set<String> givenKeys = params.keySet();
                boolean allPresent = givenKeys.containsAll(expectedKeys);
                if(allPresent) {
                    Log.i(TAG, "Param check: All required param keys present, GOOD");
                    return ParamStatus.GOOD;
                } else {
                    Log.w(TAG, "Param check: Missing required param keys, FAIL");
                    return ParamStatus.ERROR_MISSING_KEYS;
                }


            } else {
                // If no parameters are given, then fail
                printBundleDiff(EMPTY_BUNDLE, expected);
                Log.w(TAG, "Param check: Params required but non provided, FAIL");
                return ParamStatus.ERROR_MISSING_KEYS;
            }
        }
    }

    protected final void printBundleDiff(Bundle received, Bundle expected) {
        Set<String> receivedKeys = new HashSet<>(received.keySet());
        Set<String> expectedKeys = new HashSet<>(expected.keySet());

        expectedKeys.removeAll(receivedKeys);
        for(String key : expectedKeys) {
            Log.e(TAG, "Missing expected key " + key);
        }

        expectedKeys = new HashSet<>(expected.keySet());
        receivedKeys.removeAll(expectedKeys);
        for(String key : receivedKeys) {
            Log.w(TAG, "Received unnecessary key " + key);
        }
    }

    protected final Context getContext() {
        return mContext;
    }
}
