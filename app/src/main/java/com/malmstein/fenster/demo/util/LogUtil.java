package com.malmstein.fenster.demo.util;

import android.util.Log;

/**
 * Created by dumingwei on 2017/8/22.
 */
public class LogUtil {

    private static int VERBOSE = 1;
    private static int DEBUG = 2;
    private static int INFO = 3;
    private static int WARN = 4;
    private static int ERROR = 5;

    private static int ASSERT = 6;

    private static int level = ASSERT;

    public static void d(String TAG, String message) {
        if (level > DEBUG) {
            Log.d(TAG, message);
        }
    }

    public static void e(String TAG, String message) {
        if (level > ERROR) {
            Log.e(TAG, message);
        }
    }
}
