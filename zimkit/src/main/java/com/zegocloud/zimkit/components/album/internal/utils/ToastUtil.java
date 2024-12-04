package com.zegocloud.zimkit.components.album.internal.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.zegocloud.zimkit.R;

public class ToastUtil {

    private static final Handler toastHandler = new Handler(Looper.getMainLooper());

    public enum ToastMessageType {
        NORMAL
    }

    public static void showToast(Context context, String message) {
        showNormalToast(context, message);
    }

    public static void showNormalToast(Context context, String message) {
        showToast(context, ToastMessageType.NORMAL, message);
    }

    private static void showToast(Context context, ToastMessageType type, String message) {
        toastHandler.post(new Runnable() {
            @Override
            public void run() {
                if (type == ToastMessageType.NORMAL) {
                    showColorToast(context, ToastMessageType.NORMAL, message);
                }

            }
        });
    }

    public static void showColorToast(Context context, ToastMessageType type, String message) {
        View view = LayoutInflater.from(context).inflate(R.layout.zimkit_album_view_center_toast, null);
        TextView mTvToastMassage = (TextView) view.findViewById(R.id.tv_toast_message);
        mTvToastMassage.setText(message);

        Toast toast = new Toast(context);
        toast.setGravity(Gravity.CENTER | Gravity.FILL_HORIZONTAL, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(view);
        toast.setMargin(0, 0);
        toast.show();
    }

}
