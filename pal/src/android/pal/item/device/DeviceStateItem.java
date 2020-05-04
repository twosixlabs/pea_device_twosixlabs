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

import android.os.Parcel;
import android.os.Parcelable;
import android.pal.item.Item;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;

/**
 * A DeviceStateItem represents device information.
 */
public class DeviceStateItem extends Item {
    /**
     *  The device's phone number; null if unavailable
     */
    public static final String PHONE_NUMBER = "phone_number";
    private static final String DEFAULT_PHONE_NUMBER = null;

    /**
     *  The name of the cell carrier; null if unavailable
     */
    public static final String CELL_CARRIER= "cell_carrier";
    private static final String DEFAULT_CELL_CARRIER = null;

    /**
     * The ID number of the cell carrier; TelephonyManager.UNKNOWN_CARRIER_ID if unavailable
     */
    public static final String CELL_CARRIER_ID = "cell_carrier_id";
    private static final int DEFAULT_CELL_CARRIER_ID = TelephonyManager.UNKNOWN_CARRIER_ID;

    /**
     * The overall signal stregth of the cellular connection; SignalStrength.INVALID if unavailable.
     */
    public static final String CELL_SIGNAL_STRENGTH = "cell_signal_strength";
    private static final int DEFAULT_CELL_SIGNAL_STRENGTH = SignalStrength.INVALID;

    /**
     * The serial number of the SIM card; null if unavailable
     */
    public static final String SIM_ID = "sim_id";
    private static final String DEFAULT_SIM_ID = null;

    /**
     * The IMEI of the device; null if unavailable
     */
    public static final String IMEI = "imei";
    private static final String DEFAULT_IMEI = null;

    /**
     * The MEID of the device; null if unavailable
     */
    public static final String MEID = "meid";
    private static final String DEFAULT_MEID = null;

    public DeviceStateItem() {
        this.setFieldValue(PHONE_NUMBER, DEFAULT_PHONE_NUMBER);
        this.setFieldValue(CELL_CARRIER, DEFAULT_CELL_CARRIER);
        this.setFieldValue(CELL_CARRIER_ID, DEFAULT_CELL_CARRIER_ID);
        this.setFieldValue(CELL_SIGNAL_STRENGTH, DEFAULT_CELL_SIGNAL_STRENGTH);
        this.setFieldValue(SIM_ID, DEFAULT_SIM_ID);
        this.setFieldValue(IMEI, DEFAULT_IMEI);
        this.setFieldValue(MEID, DEFAULT_MEID);
    }

    public static final Parcelable.Creator<DeviceStateItem> CREATOR = new Parcelable.Creator<DeviceStateItem>() {
        public DeviceStateItem createFromParcel(Parcel in) {
            return new DeviceStateItem(in);
        }

        public DeviceStateItem[] newArray(int size) {
            return new DeviceStateItem[size];
        }
    };

    private DeviceStateItem (Parcel in) {
        super(in);
    }

}
