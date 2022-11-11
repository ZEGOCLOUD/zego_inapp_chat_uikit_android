package im.zego.zimkitcommon.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;

import im.zego.zimkitcommon.R;
import im.zego.zimkitcommon.databinding.CommonLayoutToastBinding;

/**
 * Customize toast
 */
public class ZIMKitCustomToastUtil {

    private static final Handler toastHandler = new Handler(Looper.getMainLooper());

    public static void showToast(Context context, String message) {
        showToast(context, message, R.mipmap.common_ic_toast_tip);
    }

    public static void showToast(Context context, String message, int iconTip) {
        initView(context, message, iconTip);
    }

    private static void initView(Context context, String message, int iconTip) {
        toastHandler.post(new Runnable() {
            @Override
            public void run() {
                toastMessage(context, message, iconTip);
            }
        });
    }

    private static void toastMessage(Context context, String message, int iconTip) {

        CommonLayoutToastBinding mBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.common_layout_toast, null, false);
        mBinding.ivTip.setBackgroundResource(iconTip);
        mBinding.tvTip.setText(message);

        Toast toast = new Toast(context);
        toast.setGravity(Gravity.CENTER | Gravity.FILL_HORIZONTAL, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(mBinding.getRoot());
        toast.setMargin(0, 0);
        toast.show();

    }

}
