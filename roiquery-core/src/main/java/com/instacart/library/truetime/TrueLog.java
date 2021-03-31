package com.instacart.library.truetime;

import android.util.Log;

import com.roiquery.analytics.utils.LogUtils;

class TrueLog {

    private static boolean LOGGING_ENABLED = false;

    static void v(String tag, String msg) {
        if (LOGGING_ENABLED) {
            LogUtils.v(tag, msg);
        }
    }

    static void d(String tag, String msg) {
        if (LOGGING_ENABLED) {
            LogUtils.d(tag, msg);
        }
    }

    static void i(String tag, String msg) {
        if (LOGGING_ENABLED) {
            LogUtils.i(tag, msg);
        }
    }

    static void w(String tag, String msg) {
        if (LOGGING_ENABLED) {
            LogUtils.w(tag, msg);
        }
    }

    static void w(String tag, String msg, Throwable t) {
        if (LOGGING_ENABLED) {
            LogUtils.w(tag, msg, t);
        }
    }

    static void e(String tag, String msg) {
        if (LOGGING_ENABLED) {
            LogUtils.e(tag, msg);
        }
    }

    static void e(String tag, String msg, Throwable t) {
        if (LOGGING_ENABLED) {
            LogUtils.e(tag, msg, t);
        }
    }

    static void wtf(String tag, String msg) {
        if (LOGGING_ENABLED) {
            Log.wtf(tag, msg);
        }
    }

    static void wtf(String tag, String msg, Throwable tr) {
        if (LOGGING_ENABLED) {
            Log.wtf(tag, msg, tr);
        }
    }

    static void setLoggingEnabled(boolean isLoggingEnabled) {
        LOGGING_ENABLED = isLoggingEnabled;
    }
}
