package com.zegocloud.zimkit.common.utils;

import android.os.SystemClock;

public class ZIMKitCheckDoubleClick {
    private static long lastClickTime = 0;

    public static boolean isFastDoubleClick() {
        long time = getBoostTimeMillis();
        long timeD = time - lastClickTime;
        if (0 < timeD && timeD < 800) {
            return true;
        }
        lastClickTime = time;
        return false;
    }

    private static long lastClickTime2 = 0;

    public static boolean isFastDoubleClick(long l) {
        long time = getBoostTimeMillis();
        long timeD = time - lastClickTime2;
        if (0 < timeD && timeD < l) {
            return true;
        }
        lastClickTime2 = time;
        return false;
    }

    public static long getBoostTimeMillis() {
        return SystemClock.elapsedRealtime();
    }
}
