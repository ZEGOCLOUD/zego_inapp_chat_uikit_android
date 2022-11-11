package im.zego.zimkitcommon.utils;

import androidx.fragment.app.FragmentActivity;

import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.RequestCallback;

public class PermissionHelper {
    private static void onPermissionGranted(FragmentActivity activity, String permission, GrantResult grantResult) {
        PermissionX.init(activity).permissions(new String[]{permission}).request((RequestCallback) (
                (allGranted, grantedList, deniedList) -> {
                    if (allGranted) {
                        grantResult.onGrantResult(true);
                    } else {
                        grantResult.onGrantResult(false);
                    }

                }));
    }

    private static void onPermissionGranted(FragmentActivity activity, String[] permissions, GrantResult grantResult) {
        PermissionX.init(activity).permissions(permissions).request((RequestCallback) (
                (allGranted, grantedList, deniedList) -> {
                    if (allGranted) {
                        grantResult.onGrantResult(true);
                    } else {
                        grantResult.onGrantResult(false);
                    }

                }));
    }

    public static void onAudioPermissionGranted(FragmentActivity activity, GrantResult grantResult) {
        onPermissionGranted(activity, "android.permission.RECORD_AUDIO", grantResult);
    }

    public static void onMicrophonePermissionGranted(FragmentActivity activity, GrantResult grantResult) {
        onPermissionGranted(activity, new String[]{"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.RECORD_AUDIO"}, grantResult);
    }

    public static void onCameraPermissionGranted(FragmentActivity activity, GrantResult grantResult) {
        onPermissionGranted(activity, "android.permission.CAMERA", grantResult);
    }

    public static void onCameraAndAudioPermissionGranted(FragmentActivity activity, GrantResult grantResult) {
        onPermissionGranted(activity, new String[]{"android.permission.CAMERA", "android.permission.RECORD_AUDIO"}, grantResult);
    }

    public static void onReadSDCardPermissionGranted(FragmentActivity activity, GrantResult grantResult) {
        onPermissionGranted(activity, "android.permission.READ_EXTERNAL_STORAGE", grantResult);
    }

    public static void onWriteSDCardPermissionGranted(FragmentActivity activity, GrantResult
            grantResult) {
        onPermissionGranted(activity, new String[]{"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE"}, grantResult);
    }

    public interface GrantResult {
        void onGrantResult(boolean allGranted);
    }
}





