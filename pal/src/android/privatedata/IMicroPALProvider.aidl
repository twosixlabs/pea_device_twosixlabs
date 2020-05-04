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
 */

package android.privatedata;

import android.os.Bundle;
import android.privatedata.ItemWrapper;

interface IMicroPALProvider {
    Bundle processData(in ItemWrapper privateData, in Bundle palExtras);
    String getId();
    String getSupportedType();
    String getDescription();
}
