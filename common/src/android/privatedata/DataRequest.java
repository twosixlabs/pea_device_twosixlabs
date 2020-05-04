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

import android.annotation.Nullable;
import android.content.Context;
import android.os.Bundle;
import android.os.ResultReceiver;

/**
 * @author      Irwin Reyes <irwin.reyes@twosixlabs.com>
 */
public class DataRequest {
    private Context mContext;
    private DataType mDataType;
    private Bundle mDataTypeExtras;
    private String mPalProvider;
    private Bundle mPalExtras;
    private Purpose mPurpose;
    private ResultReceiver mReceiver;

    /**
     *  @param context          The context from where this request originated.
     *  @param dataType         The type of private data to request and send to the PAL
     *  @param dataTypeExtras   Additional parameters for obtaining the private data.
     *  @param palProvider      String identifier for the PAL to process the requested
     *                          private data.
     *  @param palExtras        Additional parameters for the target PAL. Nullable.
     *  @param purpose          The purpose for this request. Specify using the
     *                          DataRequest.Purpose class.
     *  @param receiver         Callback to receive and handle the processed private data.
     */
    public DataRequest(Context context,
                            DataType dataType,
                            @Nullable Bundle dataTypeExtras,
                            String palProvider,
                            @Nullable Bundle palExtras,
                            Purpose purpose,
                            ResultReceiver receiver) {
        mContext = context;
        mDataType = dataType;
        mDataTypeExtras = dataTypeExtras;
        mPalProvider = palProvider;
        mPalExtras = palExtras;
        mPurpose = purpose;
        mReceiver = receiver;
    }

    public Context getContext() {
        return mContext;
    }

    public DataType getDataType() {
        return mDataType;
    }

    public String getPermission() {
        return DataRequest.dataTypeToPermission(mDataType);
    }

    public Bundle getDataTypeExtras() {
        return mDataTypeExtras;
    }

    public String getPalProvider() {
        return mPalProvider;
    }

    public Bundle getPalExtras() {
        return mPalExtras;
    }

    public Purpose getPurpose() {
        return mPurpose;
    }

    public ResultReceiver getReceiver() {
        return mReceiver;
    }

    // NOTE(irwin): There could be a better way to represent data type selection.
    //              Might be ideal to use the same representation here and in the
    //              PAL so we can check if they match.
    public enum DataType {
        ANY,            // NOTE(irwin): Always synchronous; no data will be retrieved in the PDMS
        EMPTY,          // no data will be retrieved in the PDMS

        //ACCOUNTS,     // TODO Implement ACCOUNTS type in the future, perhaps
        CALENDAR,       // NOTE(irwin): Always synchronous
        CALL_LOGS,      // NOTE(irwin): Always synchronous
        CONTACTS,       // NOTE(irwin): Always synchronous
        LOCATION,       // NOTE(irwin): getLastLocation() is synchronous, but updates are asynchronous
        PHONE_STATE,    // NOTE(irwin): Always synchronous
        SMS,            // NOTE(irwin): Always synchronous
    }

    public static String dataTypeToPermission(DataType dataType) {
        switch(dataType) {
            // TODO Implement ACCOUNTS type in the future, perhaps
            //case ACCOUNTS:
            //    return android.Manifest.permission.GET_ACCOUNTS;

            case CALENDAR:
                return android.Manifest.permission.READ_CALENDAR;

            case CALL_LOGS:
                return android.Manifest.permission.READ_CALL_LOG;

            case CONTACTS:
                return android.Manifest.permission.READ_CONTACTS;

            case LOCATION:
                return android.Manifest.permission.ACCESS_FINE_LOCATION;

            case PHONE_STATE:
                return android.Manifest.permission.READ_PHONE_STATE;

            case SMS:
                return android.Manifest.permission.READ_SMS;

            default:
                return null;
        }
    }

    /**
     * Purpose strings as defined in PrivacyStreams
     * https://github.com/PrivacyStreams/PrivacyStreams
     */
    public static class Purpose {
        private String purposeString;
        private Purpose(String purposeString) {
            this.purposeString = purposeString;
        }

        private static final String PURPOSE_ADS = "Advertisement";
        private static final String PURPOSE_ANALYTICS = "Analytics";
        private static final String PURPOSE_HEALTH = "Health";
        private static final String PURPOSE_SOCIAL = "Social";
        private static final String PURPOSE_UTILITY = "Utility";
        private static final String PURPOSE_RESEARCH = "Research";
        private static final String PURPOSE_GAME = "Game";
        private static final String PURPOSE_FEATURE = "Feature";

        private static final String PURPOSE_LIB_INTERNAL = "LibInternal";
        private static final String PURPOSE_TEST = "Test";

        /**
         * Advertising purpose.
         * @param description a short description of the purpose.
         * @return the Purpose instance
         */
        public static Purpose ADS(String description) {
            return new Purpose(PURPOSE_ADS + ": " + description);
        }

        /**
         * The purpose for analytics.
         * @param description a short description of the purpose.
         * @return the Purpose instance
         */
        public static Purpose ANALYTICS(String description) {
            return new Purpose(PURPOSE_ANALYTICS + ": " + description);
        }

        /**
         * The purpose for app's features.
         * @param description a short description of the purpose.
         * @return the Purpose instance
         */
        public static Purpose FEATURE(String description) {
            return new Purpose(PURPOSE_FEATURE + ": " + description);
        }

        /**
         * Utility purpose.
         * @param description a short description of the purpose.
         * @return the Purpose instance
         */
        public static Purpose UTILITY(String description) {
            return new Purpose(PURPOSE_UTILITY + ": " + description);
        }

        /**
         * The purpose for health monitoring.
         * @param description a short description of the purpose.
         * @return the Purpose instance
         */
        public static Purpose HEALTH(String description) {
            return new Purpose(PURPOSE_HEALTH + ": " + description);
        }

        /**
         * The purpose for social interaction (analyzing social relationship, recommending friends, etc.).
         * @param description a short description of the purpose.
         * @return the Purpose instance
         */
        public static Purpose SOCIAL(String description) {
            return new Purpose(PURPOSE_SOCIAL + ": " + description);
        }

        /**
         * Research purpose.
         * @param description a short description of the purpose.
         * @return the Purpose instance
         */
        public static Purpose RESEARCH(String description) {
            return new Purpose(PURPOSE_RESEARCH + ": " + description);
        }

        /**
         * The purpose for game.
         * @param description a short description of the purpose.
         * @return the Purpose instance
         */
        public static Purpose GAME(String description) {
            return new Purpose(PURPOSE_GAME + ": " + description);
        }

        /**
         * Testing purpose.
         * App should not use this purpose in released app.
         *
         * @param description a short description of the purpose.
         * @return the Purpose instance
         */
        public static Purpose TEST(String description) {
            return new Purpose(PURPOSE_TEST + ": " + description);
        }

        /**
         * For internal library use.
         * App should not use this purpose anyway.
         *
         * @param description a short description of the purpose.
         * @return the Purpose instance
         */
        public static Purpose LIB_INTERNAL(String description) {
            return new Purpose(PURPOSE_LIB_INTERNAL + ": " + description);
        }

        public String toString() {
            return this.purposeString;
        }
    }

    /**
     * Helper class to generate paramter Bundle for location requests
     */
    public static class LocationParamsBuilder {

        public static final String UPDATE_MODE = "update_mode";
        public static final String UPDATE_TIMEOUT_MILLIS = "update_timeout_millis";
        public static final String PROVIDER = "provider";

        public static final String MODE_LAST_LOCATION = "last_location";
        public static final String MODE_SINGLE_UPDATE = "single_update";
        //public static final String MODE_CONTINUOUS_UPDATE = "continuous_update";
        public static final String MODE_UNSET = "unset";

        private String mMode = MODE_UNSET;
        private String mProvider = null;
        private int mTimeout = -1;

        public LocationParamsBuilder setUpdateMode(String mode) {
            mMode = mode;
            return this;
        }

        public LocationParamsBuilder setTimeoutMillis(int timeout) {
            mTimeout = timeout;
            return this;
        }

        public LocationParamsBuilder setProvider(String provider) {
            mProvider = provider;
            return this;
        }

        public Bundle build() {
            Bundle params = new Bundle();
            params.putString(UPDATE_MODE, mMode);
            params.putInt(UPDATE_TIMEOUT_MILLIS, mTimeout);
            params.putString(PROVIDER, mProvider);
            return params;
        }
    }

    /**
     * Helper class to generate parameter Bundle for message requests
     */
    public static class MessageParamsBuilder extends TimeParamsBuilder {
    }

    /**
     * Helepr class to generate parameter Bundle for calendar requests
     */
    public static class CalendarParamsBuilder extends TimeParamsBuilder {
    }

    /**
     *
     */

    public static class CallParamsBuilder extends TimeParamsBuilder {
    }

    public static class TimeParamsBuilder {

        public static final String START_UTC_MILLIS = "start_utc_millis";
        public static final String END_UTC_MILLIS = "end_utc_millis";

        private long mStartMillis = -1l;
        private long mEndMillis = -1l;

        public TimeParamsBuilder setStartUtcMillis(long startMillis) {
            mStartMillis = startMillis;
            return this;
        }

        public TimeParamsBuilder setEndUtcMillis(long endMillis) {
            mEndMillis = endMillis;
            return this;
        }

        public Bundle build() {
            Bundle params = new Bundle();
            params.putLong(START_UTC_MILLIS, mStartMillis);
            params.putLong(END_UTC_MILLIS, mEndMillis);
            return params;
        }

    }
}
