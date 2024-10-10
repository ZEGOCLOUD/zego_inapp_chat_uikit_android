package com.zegocloud.zimkit.common.utils;

import android.Manifest;
import android.Manifest.permission;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import com.permissionx.guolindev.PermissionX;
import com.permissionx.guolindev.callback.RequestCallback;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PermissionHelper {

    private static final String TAG = "PermissionHelper";

    public static void onWriteSDCardPermissionGranted(FragmentActivity activity, RequestCallback requestCallback) {
        List<String> requestList = new ArrayList<>();
        // android 13 ,use media store api no need permission to write to public
        //      pictures dir.
        if (Build.VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) {
            //            requestList.add(Manifest.permission.READ_MEDIA_IMAGES);
            //            requestList.add(Manifest.permission.READ_MEDIA_AUDIO);
            //            requestList.add(Manifest.permission.READ_MEDIA_VIDEO);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // android 10 - android 12 ,use media store api no need permission to write to public
            // pictures dir.
            //            requestList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            //            requestList.add(permission.WRITE_EXTERNAL_STORAGE);
        } else {
            // below android 10,use media store api or File api both need WRITE_EXTERNAL_STORAGE permission
            //  to write to public pictures dir.
            requestList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            requestList.add(permission.WRITE_EXTERNAL_STORAGE);
        }
        requestPermissionsIfNeed(activity, requestList, requestCallback);
    }

    public static void requestReadSDCardPermissionIfNeed(FragmentActivity activity, RequestCallback callback) {
        List<String> permissions = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES);
            permissions.add(Manifest.permission.READ_MEDIA_AUDIO);
            permissions.add(Manifest.permission.READ_MEDIA_VIDEO);
        } else {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        requestPermissionsIfNeed(activity, permissions, callback);
    }

    public static void requestCameraPermissionIfNeed(FragmentActivity activity, RequestCallback callback) {
        List<String> permissions = Collections.singletonList(permission.CAMERA);
        requestPermissionsIfNeed(activity, permissions, callback);
    }

    public static void requestRecordAudioPermissionIfNeed(FragmentActivity activity, RequestCallback callback) {
        List<String> permissions = Collections.singletonList(permission.RECORD_AUDIO);
        requestPermissionsIfNeed(activity, permissions, callback);
    }

    public static void requestPermissionsIfNeed(FragmentActivity activity, List<String> permissions,
        RequestCallback callback) {
        List<String> granted = new ArrayList<>();
        List<String> notGranted = new ArrayList<>();
        for (String permission : permissions) {
            int checkedSelfPermission = ContextCompat.checkSelfPermission(activity, permission);
            if (checkedSelfPermission != PackageManager.PERMISSION_GRANTED) {
                notGranted.add(permission);
            } else {
                granted.add(permission);
            }
        }
        if (notGranted.isEmpty()) {
            if (callback != null) {
                callback.onResult(true, granted, notGranted);
            }
            return;
        }
        PermissionX.init(activity).permissions(permissions).request(callback);
    }
}





