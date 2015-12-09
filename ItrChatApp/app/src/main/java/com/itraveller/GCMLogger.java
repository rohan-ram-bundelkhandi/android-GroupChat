package com.itraveller;

/**
 * Created by rohan bundelkhandi on 11/29/2015.
 */
import android.util.Log;

/**
 * Custom logger.
 *
 * @deprecated GCM library has been moved to Google Play Services
 * (com.google.android.gms.gcm), and this version is no longer supported.
 */
@Deprecated
class GCMLogger {

    private final String mTag;
    // can't use class name on TAG since size is limited to 23 chars
    private final String mLogPrefix;

    GCMLogger(String tag, String logPrefix) {
        mTag = tag;
        mLogPrefix = logPrefix;
    }

    /**
     * Logs a message on logcat.
     *
     * @param priority logging priority
     * @param template message's template
     * @param args list of arguments
     */
    protected void log(int priority, String template, Object... args) {
        if (Log.isLoggable(mTag, priority)) {
            String message = String.format(template, args);
            Log.println(priority, mTag, mLogPrefix + message);
        }
    }
}


