package im.zego.zimkitcommon;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import im.zego.zimkitcommon.enums.ZIMKitConversationType;
import im.zego.zimkitcommon.utils.ZLog;

/**
 * Implement Activity / fragment routing jumps
 */
public class ZIMKitRouter {
    private static final String TAG = ZIMKitRouter.class.getSimpleName();
    private static final Map<String, String> routerMap = new HashMap<>();

    private ZIMKitRouter() {
    }

    public static void initRouter(Context context) {
        ActivityInfo[] activityInfos = null;
        List<String> activityNames = new ArrayList<>();
        PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES);
            activityInfos = packageInfo.activities;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (activityInfos != null) {
            for (ActivityInfo activityInfo : activityInfos) {
                activityNames.add(activityInfo.name);
            }
        }
        for (String activityName : activityNames) {
            if (activityName.contains("im.zego.zimkit")) {
                String[] splitStr = activityName.split("\\.");
                routerMap.put(splitStr[splitStr.length - 1], activityName);
            }
        }
    }

    /**
     * to activity or fragment page based on path
     *
     * @param context the context
     * @param path    the path
     */
    public static void to(Context context, String path, Bundle bundle) {
        if (context == null) {
            ZLog.d(TAG, "StartActivity failed, context is null.Please init");
            return;
        }
        String activityName = routerMap.get(path);
        if (activityName == null) {
            throw new IllegalArgumentException(String.format("the %s not found activity,please check", path));
        }
        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(context, activityName));
            intent.putExtra(ZIMKitConstant.RouterConstant.KEY_BUNDLE, bundle);
            if (!(context instanceof Activity)) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            ActivityCompat.startActivity(context, intent, bundle);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }


    /**
     * Jump to the chat page via session
     * No avatar required as parameter
     *
     * @param context
     * @param conversationId
     * @param type           Session type single chat or group chat
     */
    public static void toMessageActivity(Context context, String conversationId, ZIMKitConversationType type) {
        Bundle data = new Bundle();
        if (type == ZIMKitConversationType.ZIMKitConversationTypeGroup) {
            data.putString(ZIMKitConstant.MessagePageConstant.KEY_TYPE, ZIMKitConstant.MessagePageConstant.TYPE_GROUP_MESSAGE);
        } else if (type == ZIMKitConversationType.ZIMKitConversationTypePeer) {
            data.putString(ZIMKitConstant.MessagePageConstant.KEY_TYPE, ZIMKitConstant.MessagePageConstant.TYPE_SINGLE_MESSAGE);
        }
        data.putString(ZIMKitConstant.MessagePageConstant.KEY_ID, conversationId);
        data.putBoolean(ZIMKitConstant.MessagePageConstant.KEY_PUSH, false);
        to(context, ZIMKitConstant.RouterConstant.ROUTER_MESSAGE, data);
    }

    /**
     * Jump to the chat page via session
     *
     * @param context
     * @param conversationId
     * @param name           Session Title
     * @param avatar
     * @param type           Session type single chat or group chat
     */
    public static void toMessageActivity(Context context, String conversationId, String name, String avatar, ZIMKitConversationType type) {
        Bundle data = new Bundle();
        if (type == ZIMKitConversationType.ZIMKitConversationTypeGroup) {
            data.putString(ZIMKitConstant.MessagePageConstant.KEY_TYPE, ZIMKitConstant.MessagePageConstant.TYPE_GROUP_MESSAGE);
        } else if (type == ZIMKitConversationType.ZIMKitConversationTypePeer) {
            data.putString(ZIMKitConstant.MessagePageConstant.KEY_TYPE, ZIMKitConstant.MessagePageConstant.TYPE_SINGLE_MESSAGE);
            data.putString(ZIMKitConstant.MessagePageConstant.KEY_AVATAR, avatar);
        }
        data.putString(ZIMKitConstant.MessagePageConstant.KEY_ID, conversationId);
        data.putString(ZIMKitConstant.MessagePageConstant.KEY_TITLE, name);
        data.putBoolean(ZIMKitConstant.MessagePageConstant.KEY_PUSH, false);
        to(context, ZIMKitConstant.RouterConstant.ROUTER_MESSAGE, data);
    }

    /**
     * to activity or fragment page based on path And finish context activity
     *
     * @param context the context
     * @param path    the path
     */
    public static void toAndFinish(Context context, String path, Bundle bundle) {
        to(context, path, bundle);
        if (context instanceof Activity) {
            ((Activity) context).finish();
        }
    }

    /**
     * @param path
     * @return
     */
    public static String getActivityName(String path) {
        String activityName = routerMap.get(path);
        if (activityName == null) {
            throw new IllegalArgumentException(String.format("the %s not found activity,please check", path));
        }
        return activityName;
    }

}