package com.zegocloud.zimkit.common.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.zegocloud.zimkit.services.internal.ZIMKitCore;

public class ZIMKitScreenUtils {

    private static final String TAG = ZIMKitScreenUtils.class.getSimpleName();

    public static int getScreenHeight(Context context) {
        DisplayMetrics metric = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metric);
        return metric.heightPixels;
    }

    public static int getScreenWidth(Context context) {
        DisplayMetrics metric = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metric);
        return metric.widthPixels;
    }

    public static int getPxByDp(float dp) {
        float scale = ZIMKitCore.getInstance().getApplication().getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public static int dip2px(float dpValue) {
        final float scale = ZIMKitCore.getInstance().getApplication().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int px2dip(float pxValue) {
        final float scale = ZIMKitCore.getInstance().getApplication().getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

}
