package com.zegocloud.zimkit.common.utils;

import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.zegocloud.zimkit.services.internal.ZIMKitCore;
import com.zegocloud.zimkit.services.model.ZIMKitErrorToast;

import java.util.concurrent.atomic.AtomicReference;

public class ZIMKitToastUtils {

    private static Toast toast;

    public static void showToast(String message) {
        toastShortMessage(message);
    }

    public static void showToast(int messageId) {
        toastShortMessage(ZIMKitCore.getInstance().getApplication().getString(messageId));
    }

    public static void showErrorMessageIfNeeded(int errorCode, String defaultMessage) {
        AtomicReference<ZIMKitErrorToast> errorToast = new AtomicReference<>(new ZIMKitErrorToast(defaultMessage));
        ZIMKitCore.getInstance().getZimkitNotifyList().notifyAllListener( delegate -> {
            ZIMKitErrorToast toast = delegate.onErrorToastCallback(errorCode, new ZIMKitErrorToast(defaultMessage));
            if (toast != null) {
                errorToast.set(toast);
            }
        });
        if (errorToast.get().isShow) {
            toastShortMessage(errorToast.get().message);
        }
    }

    public static void toastLongMessage(final String message) {
        toastMessage(message, true);
    }

    public static void toastShortMessage(final String message) {
        toastMessage(message, false);
    }

    private static void toastMessage(final String message, boolean isLong) {
        ZIMKitBackgroundTasks.getInstance().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (toast != null) {
                    toast.cancel();
                    toast = null;
                }
                toast = Toast.makeText(ZIMKitCore.getInstance().getApplication(), message,
                        isLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);
                // Solve the problem of inconsistent toast text alignment for each phone system
                View view = toast.getView();
                if (view != null) {
                    TextView textView = view.findViewById(android.R.id.message);
                    if (textView != null) {
                        textView.setGravity(Gravity.CENTER);
                    }
                }
                toast.show();
            }
        });
    }

}
