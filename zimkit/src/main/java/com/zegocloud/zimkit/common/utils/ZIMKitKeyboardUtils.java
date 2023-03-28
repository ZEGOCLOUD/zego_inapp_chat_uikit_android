package com.zegocloud.zimkit.common.utils;

import android.app.Activity;
import android.content.Context;
import android.os.IBinder;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class ZIMKitKeyboardUtils {

    /**
     * Close soft keyboard
     *
     * @param context
     */
    public static void closeSoftKeyboard(Context context) {
        if (context == null || !(context instanceof Activity) || ((Activity) context).getCurrentFocus() == null) {
            return;
        }
        try {
            View view = ((Activity) context).getCurrentFocus();
            InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            view.clearFocus();
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class ActivityDispatchTouchEvent {
        /**
         * For global monitoring, close the soft keyboard other than by clicking editText
         *
         * @param ev
         * @param activity
         */
        public void dispatchTouchEventCloseInput(MotionEvent ev, Activity activity) {
            if (ev.getAction() == MotionEvent.ACTION_DOWN) {
                View v = activity.getCurrentFocus();
                if (isShouldHideInput(v, ev)) {
                    hideSoftInput(v.getWindowToken(), activity);
                }
            }
        }
    }

    /**
     * To determine whether to hide the keyboard based on the coordinates of the
     * EditText compared to the coordinates of the user's click,
     * because there is no need to hide the EditText when the user clicks on it.
     *
     * @param v
     * @param event
     * @return
     */
    private static boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] l = {0, 0};
            v.getLocationInWindow(l);
            int left = l[0], top = l[1], bottom = top + v.getHeight(), right = left
                    + v.getWidth();
            if (event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom) {
                // Click on the event of EditText and ignore it.
                return false;
            } else {
                return true;
            }
        }
        // Ignore if the focus is not EditText, this happens when the view has just been drawn,
        // the first focus is not on EditView, and the user selects the other focus with the trackball
        return false;
    }

    /**
     * One of the many ways to hide the software disk
     *
     * @param token
     */
    public static void hideSoftInput(IBinder token, Context context) {
        if (token != null) {
            InputMethodManager im = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            im.hideSoftInputFromWindow(token,
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

}
