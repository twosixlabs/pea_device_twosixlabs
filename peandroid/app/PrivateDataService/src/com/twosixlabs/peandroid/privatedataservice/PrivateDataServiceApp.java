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

package com.twosixlabs.peandroid.privatedataservice;

import android.app.Application;
import android.os.ServiceManager;
import android.util.Log;
//import android.privatedata.IPrivateDataManager;
import android.privatedata.IPrivateDataManagerService;

public class PrivateDataServiceApp extends Application {
  private static final String TAG = "PrivateDataServiceApp";
  //private static final String REMOTE_SERVICE_NAME = IPrivateDataManager.class.getName();
  private static final String REMOTE_SERVICE_NAME = IPrivateDataManagerService.class.getName();
  //private IPrivateDataServiceImpl serviceImpl;
  private PrivateDataManagerService serviceImpl;

  public void onCreate() {
    super.onCreate();
    //this.serviceImpl = new IPrivateDataServiceImpl(this);
    this.serviceImpl = new PrivateDataManagerService(this);
    ServiceManager.addService(REMOTE_SERVICE_NAME, this.serviceImpl);
    Log.d(TAG, "Registered [" + serviceImpl.getClass().getName() + "] as [" + REMOTE_SERVICE_NAME + "]");
  }

  public void onTerminate() {
    super.onTerminate();
    Log.d(TAG, "Terminated");
  }
}
