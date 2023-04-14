package com.zegocloud.zimkit.common.utils;

import android.Manifest;
import android.Manifest.permission;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.RequestCallback;
import java.util.ArrayList;
import java.util.List;

public class PermissionHelper {

    private static void onPermissionGranted(FragmentActivity activity, String permission, GrantResult grantResult) {
        PermissionX.init(activity).permissions(new String[]{permission})
            .request((RequestCallback) ((allGranted, grantedList, deniedList) -> {
                if (allGranted) {
                    grantResult.onGrantResult(true);
                } else {
                    grantResult.onGrantResult(false);
                }

            }));
    }

    private static void onPermissionGranted(FragmentActivity activity, String[] permissions, GrantResult grantResult) {
        PermissionX.init(activity).permissions(permissions)
            .request((RequestCallback) ((allGranted, grantedList, deniedList) -> {
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
        List<String> requestList = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestList.add(Manifest.permission.READ_MEDIA_IMAGES);
            requestList.add(Manifest.permission.READ_MEDIA_AUDIO);
            requestList.add(Manifest.permission.READ_MEDIA_VIDEO);
            requestList.add(permission.RECORD_AUDIO);
        } else {
            requestList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            requestList.add(permission.WRITE_EXTERNAL_STORAGE);
            requestList.add(permission.RECORD_AUDIO);
        }
        PermissionX.init(activity).permissions(requestList)
            .request((RequestCallback) ((allGranted, grantedList, deniedList) -> {
                if (allGranted) {
                    grantResult.onGrantResult(true);
                } else {
                    grantResult.onGrantResult(false);
                }

            }));
    }

    public static void onCameraPermissionGranted(FragmentActivity activity, GrantResult grantResult) {
        onPermissionGranted(activity, "android.permission.CAMERA", grantResult);
    }

    public static void onCameraAndAudioPermissionGranted(FragmentActivity activity, GrantResult grantResult) {
        onPermissionGranted(activity, new String[]{"android.permission.CAMERA", "android.permission.RECORD_AUDIO"},
            grantResult);
    }

    public static void onReadSDCardPermissionGranted(FragmentActivity activity, GrantResult grantResult) {
        onPermissionGranted(activity, "android.permission.READ_EXTERNAL_STORAGE", grantResult);
    }

    private static final String TAG = "PermissionHelper";

    public static void onWriteSDCardPermissionGranted(FragmentActivity activity, GrantResult grantResult) {
        List<String> requestList = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestList.add(Manifest.permission.READ_MEDIA_IMAGES);
            requestList.add(Manifest.permission.READ_MEDIA_AUDIO);
            requestList.add(Manifest.permission.READ_MEDIA_VIDEO);
        } else {
            requestList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            requestList.add(permission.WRITE_EXTERNAL_STORAGE);
        }
        boolean allPermissionGranted = true;
        for (String permission : requestList) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                allPermissionGranted = false;
            }
        }
        if (allPermissionGranted) {
            grantResult.onGrantResult(false);
            return;
        }

        PermissionX.init(activity).permissions(requestList)
            .request((RequestCallback) ((allGranted, grantedList, deniedList) -> {
                if (allGranted) {
                    grantResult.onGrantResult(true);
                } else {
                    grantResult.onGrantResult(false);
                }

            }));
    }

    public interface GrantResult {

        void onGrantResult(boolean allGranted);
    }
}





