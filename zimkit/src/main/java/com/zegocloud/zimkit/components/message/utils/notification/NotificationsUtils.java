package com.zegocloud.zimkit.components.message.utils.notification;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.zegocloud.zimkit.common.ZIMKitConstant;
import im.zego.zim.enums.ZIMConversationType;
import com.zegocloud.zimkit.R;
import com.zegocloud.zimkit.components.message.ui.ZIMKitMessageActivity;
import com.zegocloud.zimkit.services.internal.ZIMKitCore;

/**
 * Notification bar message notification
 */
public class NotificationsUtils {

    public static final String chatChannelId = "im.zego.zimkitmessages";

    public static boolean isNotificationEnabled(Context context) {
        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        return manager.areNotificationsEnabled();
    }

    @TargetApi(Build.VERSION_CODES.O)
    public static NotificationChannel createNotificationChannel(NotificationManager manager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelName = "chat";
            NotificationChannel channel;
            channel = new NotificationChannel(chatChannelId, channelName, NotificationManager.IMPORTANCE_HIGH);
            channel.canBypassDnd();//Is it possible to bypass Do Not Disturb mode
            channel.canShowBadge();//Is it possible to display icon corner markers
            channel.enableLights(false);//Whether to display the notification flashing light
            channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_SECRET);
            channel.getAudioAttributes();//Get to set ringtone settings
            channel.enableVibration(false);
            channel.setVibrationPattern(new long[]{0});//Set vibration mode

            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
            return channel;
        }
        return null;
    }

    public static class NotifyConfig {
        public int conversationType;
        public String conversationID;
        public String conversationName;
        public String message;
        public String senderUserID;
    }

    public static void showNotification(NotifyConfig notifyConfig) {
        NotificationCompat.Builder builder;
        NotificationManager notificationManager = (NotificationManager) ZIMKitCore.getInstance().getApplication().getSystemService(Context
                .NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = NotificationsUtils.createNotificationChannel(notificationManager);
            builder = new NotificationCompat.Builder(ZIMKitCore.getInstance().getApplication(), channel.getId());
        } else {
            builder = new NotificationCompat.Builder(ZIMKitCore.getInstance().getApplication());
        }

        builder.setSmallIcon(R.mipmap.zimkit_icon_notify)
//                .setContentTitle(notifyConfig.conversationName)
                .setContentText(notifyConfig.message)
                .setAutoCancel(true);
        Notification notification = builder.build();

        Intent notificationIntent = new Intent(ZIMKitCore.getInstance().getApplication(), ZIMKitMessageActivity.class);

        Bundle data = new Bundle();
        if (notifyConfig.conversationType == ZIMConversationType.GROUP.value()) {
            data.putString(ZIMKitConstant.MessagePageConstant.KEY_TYPE, ZIMKitConstant.MessagePageConstant.TYPE_GROUP_MESSAGE);
        } else if (notifyConfig.conversationType == ZIMConversationType.PEER.value()) {
            data.putString(ZIMKitConstant.MessagePageConstant.KEY_TYPE, ZIMKitConstant.MessagePageConstant.TYPE_SINGLE_MESSAGE);
        }
        data.putString(ZIMKitConstant.MessagePageConstant.KEY_ID, notifyConfig.conversationID);
        data.putString(ZIMKitConstant.MessagePageConstant.KEY_TITLE, notifyConfig.conversationName);
        data.putBoolean(ZIMKitConstant.MessagePageConstant.KEY_PUSH, true);
        notificationIntent.putExtra(ZIMKitConstant.RouterConstant.KEY_BUNDLE, data);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent contentIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            contentIntent = PendingIntent.getActivity(ZIMKitCore.getInstance().getApplication(), notifyConfig.senderUserID.hashCode(), notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        } else {
            contentIntent = PendingIntent.getActivity(ZIMKitCore.getInstance().getApplication(), notifyConfig.senderUserID.hashCode(), notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        notification.contentIntent = contentIntent;
        notification.flags = Notification.FLAG_AUTO_CANCEL;

        int notifyId = notifyConfig.senderUserID.hashCode();
        notificationManager.notify(notifyId, notification);
    }

}
