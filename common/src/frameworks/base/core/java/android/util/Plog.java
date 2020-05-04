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

package android.util;

public final class Plog {

    /** @hide */ public static final int LOG_ID_PERMISSION = 8;

    private Plog() {
    }

    /**
     * Send a {@link Log#VERBOSE} log message.
     * @param tag Used to identify the source of a log message. It usually identifies
     *        the class or activity where the log occurs.
     * @param msg The message you would like logged.
     */
    public static int v(String tag, String msg) {
        return Log.println_native(LOG_ID_PERMISSION, Log.VERBOSE, tag, msg);
    }

    /**
     * Send a {@link Log#VERBOSE} log message and log the exception.
     * @param tag Used to identify the source of a log message. It usually identifies
     *        the class or activity where the log occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    public static int v(String tag, String msg, Throwable tr) {
        return Log.println_native(LOG_ID_PERMISSION, Log.VERBOSE, tag,
                msg + '\n' + Log.getStackTraceString(tr));
    }

    /**
     * Send a {@link Log#DEBUG} log message.
     * @param tag Used to identify the source of a log message. It usually identifies
     *        the class or activity where the log occurs.
     * @param msg The message you would like logged.
     */
    public static int d(String tag, String msg) {
        return Log.println_native(LOG_ID_PERMISSION, Log.DEBUG, tag, msg);
    }

    /**
     * Send a {@link Log#DEBUG} log message and log the exception.
     * @param tag Used to identify the source of a log message. It usually identifies
     *        the class or activity where the log occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    public static int d(String tag, String msg, Throwable tr) {
        return Log.println_native(LOG_ID_PERMISSION, Log.DEBUG, tag,
                msg + '\n' + Log.getStackTraceString(tr));
    }

    /**
     * Send a {@link Log#INFO} log message.
     * @param tag Used to identify the source of a log message. It usually identifies
     *        the class or activity where the log occurs.
     * @param msg The message you would like logged.
     */
    public static int i(String tag, String msg) {
        return Log.println_native(LOG_ID_PERMISSION, Log.INFO, tag, msg);
    }

    /**
     * Send a {@link Log#INFO} log message and log the exception
     * @param tag Used to identify the source of a log message. It usually identifies
     *        the class or activity where the log occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    public static int i(String tag, String msg, Throwable tr) {
        return Log.println_native(LOG_ID_PERMISSION, Log.INFO, tag,
                msg + '\n' + Log.getStackTraceString(tr));
    }

    /**
     * Send a {@link Log#WARN} log message
     * @param tag Used to identify the source of a log message. It usually identifies
     *        the class or activity where the log occurs.
     * @param msg The message you would like logged.
     */
    public static int w(String tag, String msg) {
        return Log.println_native(LOG_ID_PERMISSION, Log.WARN, tag, msg);
    }

    /**
     * Send a {@link Log#WARN} log message and log the exception.
     * @param tag Used to identify the source of a log message. It usually identifies
     *        the class or activity where the log occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    public static int w(String tag, String msg, Throwable tr) {
        return Log.println_native(LOG_ID_PERMISSION, Log.WARN, tag,
                msg + '\n' + Log.getStackTraceString(tr));
    }

    /**
     * Send a {@link Log#WARN} log message and log the exception.
     * @param tag Used to identify the source of a log message. It usually identifies
     *        the class or activity where the log occurs.
     * @param tr An exception to log
     */
    public static int w(String tag, Throwable tr) {
        return Log.println_native(LOG_ID_PERMISSION, Log.WARN, tag, Log.getStackTraceString(tr));
    }

    /**
     * Send a {@link Log#ERROR} log message.
     * @param tag Used to identify the source of a log message. It usually identifies
     *        the class or activity where the log occurs.
     * @param msg The message you would like logged.
     */
    public static int e(String tag, String msg) {
        return Log.println_native(LOG_ID_PERMISSION, Log.ERROR, tag, msg);
    }

    /**
     * Send a {@link Log#ERROR} log message and log the exception.
     * @param tag Used to identify the source of a log message. It usually identifies
     *        the class or activity where the log occurs.
     * @param msg The message you would like logged.
     * @param tr An exception to log
     */
    public static int e(String tag, String msg, Throwable tr) {
        return Log.println_native(LOG_ID_PERMISSION, Log.ERROR, tag,
                msg + '\n' + Log.getStackTraceString(tr));
    }

    /**
     * Like {@link android.util.Log#wtf(String, String)}, but will never cause the caller to crash, and
     * will always be handled asynchronously.  Primarily for use by coding running within
     * the system process.
     */
    public static int wtf(String tag, String msg) {
        return Log.wtf(LOG_ID_PERMISSION, tag, msg, null, false, true);
    }

    /**
     * Like {@link #wtf(String, String)}, but does not output anything to the log.
     */
    public static void wtfQuiet(String tag, String msg) {
        Log.wtfQuiet(LOG_ID_PERMISSION, tag, msg, true);
    }

    /**
     * Like {@link android.util.Log#wtfStack(String, String)}, but will never cause the caller to crash, and
     * will always be handled asynchronously.  Primarily for use by coding running within
     * the system process.
     * @hide
     */
    public static int wtfStack(String tag, String msg) {
        return Log.wtf(LOG_ID_PERMISSION, tag, msg, null, true, true);
    }

    /**
     * Like {@link android.util.Log#wtf(String, Throwable)}, but will never cause the caller to crash,
     * and will always be handled asynchronously.  Primarily for use by coding running within
     * the system process.
     */
    public static int wtf(String tag, Throwable tr) {
        return Log.wtf(LOG_ID_PERMISSION, tag, tr.getMessage(), tr, false, true);
    }

    /**
     * Like {@link android.util.Log#wtf(String, String, Throwable)}, but will never cause the caller to crash,
     * and will always be handled asynchronously.  Primarily for use by coding running within
     * the system process.
     */
    public static int wtf(String tag, String msg, Throwable tr) {
        return Log.wtf(LOG_ID_PERMISSION, tag, msg, tr, false, true);
    }

    /**
     * Low-level logging call.
     * @param priority The priority/type of this log message
     * @param tag Used to identify the source of a log message.  It usually identifies
     *        the class or activity where the log call occurs.
     * @param msg The message you would like logged.
     * @return The number of bytes written.
     */
    public static int println(int priority, String tag, String msg) {
        return Log.println_native(LOG_ID_PERMISSION, priority, tag, msg);
    }
}
