package com.raidzero.superbattroid;

import android.util.Log;

import com.raidzero.superbattroid.BuildConfig;

/**
 * Created by posborn on 6/28/14.
 */
public class LogUtility {
    //private static final boolean debug = false;
    //private static final boolean debug = true;

    private static final boolean debug = BuildConfig.DEBUG;

    public static void Log(String tag, String msg) {
        if (debug) {
            Log.d(tag, msg);
        }
    }

    public static void Log(String tag, String msg, Exception e) {
        if (debug) {
            Log.d(tag, msg);
            e.printStackTrace();
        }
    }
}
