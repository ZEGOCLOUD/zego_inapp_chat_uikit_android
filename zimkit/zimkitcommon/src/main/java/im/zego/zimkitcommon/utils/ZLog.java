package im.zego.zimkitcommon.utils;

import android.util.Log;

import androidx.annotation.Nullable;

public class ZLog {
    private final static String TAG = "ZLog";

    public static void e(String info) {
        // todo TAG Automatically get the caller's class name
        e(TAG, info);
    }

    public static void e(String TAG, String info) {
        Log.e(TAG, info);
    }

    public static void i(String info) {
        // todo TAG Automatically get the caller's class name
        i(TAG, info);
    }

    public static void i(String TAG, String info) {
        Log.i(TAG, info);
    }

    public static void d(String info) {
        // todo TAG Automatically get the caller's class name
        d(TAG, info);
    }

    public static void d(String TAG, String info) {
        Log.d(TAG, info);
    }

    public static void w(String info) {
        // todo TAG Automatically get the caller's class name
        w(TAG, info);
    }

    public static void w(String TAG, String info) {
        Log.w(TAG, info);
    }
}
