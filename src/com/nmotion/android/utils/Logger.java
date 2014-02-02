package com.nmotion.android.utils;

import android.util.Log;


public class Logger {

    public static void verbose(String message) {
        if (Config.DEBUG_MODE) {
            Log.v(Config.TAG, message);
        }
    }

    public static void info(String message) {
        if (Config.DEBUG_MODE) {
            Log.i(Config.TAG, message);
        }
    }

    public static void debug(String message) {
        if (Config.DEBUG_MODE) {
            Log.d(Config.TAG, message);
        }
    }

    public static void warning(String message) {
        if (Config.DEBUG_MODE) {
            Log.w(Config.TAG, message);
        }
    }
}
