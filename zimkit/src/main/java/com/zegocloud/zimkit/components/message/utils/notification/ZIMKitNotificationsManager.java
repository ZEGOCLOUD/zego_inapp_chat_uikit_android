package com.zegocloud.zimkit.components.message.utils.notification;

import com.zegocloud.zimkit.common.utils.ZIMKitActivityUtils;
import com.zegocloud.zimkit.services.ZIMKit;
import com.zegocloud.zimkit.services.ZIMKitDelegate;
import com.zegocloud.zimkit.services.model.ZIMKitMessage;
import com.zegocloud.zimkit.services.utils.MessageTransform;
import java.util.ArrayList;
import java.util.Collections;

import im.zego.zim.entity.ZIMMessage;
import im.zego.zim.entity.ZIMTextMessage;
import im.zego.zim.enums.ZIMConversationType;
import im.zego.zim.enums.ZIMMessageDirection;
import im.zego.zim.enums.ZIMMessageType;
import com.zegocloud.zimkit.R;
import com.zegocloud.zimkit.components.message.utils.SortMessageComparator;
import com.zegocloud.zimkit.services.internal.ZIMKitCore;

public class ZIMKitNotificationsManager {

    private boolean isOpenNotification = false;
    private static ZIMKitNotificationsManager sInstance;

    public static ZIMKitNotificationsManager share() {
        if (sInstance == null) {
            synchronized (ZIMKitNotificationsManager.class) {
                if (sInstance == null) {
                    sInstance = new ZIMKitNotificationsManager();
                }
            }
        }
        return sInstance;
    }

    /**
     * Initialize this method when a message notification is needed
     */
    public void initNotifications() {
        isOpenNotification = true;
        ZIMKit.registerZIMKitDelegate(eventCallBack);
    }

    private final ZIMKitDelegate eventCallBack = new ZIMKitDelegate() {
        @Override
        public void onMessageReceived(String conversationID, ZIMConversationType type, ArrayList<ZIMKitMessage> messages) {
            if (messages != null && !messages.isEmpty()) {
                ArrayList<ZIMMessage> zimMessages = MessageTransform.transformMessageListToZIM(messages);
                if (messages.size() > 2) {
                    Collections.sort(zimMessages, new SortMessageComparator());
                }
                handlerMessageList(zimMessages);
            }
        }
    };

    private void handlerMessageList(ArrayList<ZIMMessage> messageList) {
        //The app does not notify in the foreground
        boolean isForeground = !ZIMKitActivityUtils.isBackground();
        if (isForeground) {
            return;
        }
        for (ZIMMessage zimMessage : messageList) {
            if (zimMessage.getDirection() == ZIMMessageDirection.RECEIVE) {
                integratingMessageData(zimMessage);
            }
        }
    }

    private void integratingMessageData(ZIMMessage zimMessage) {
        String message = "";
        if (zimMessage.getType() == ZIMMessageType.TEXT) {
            message = ((ZIMTextMessage) zimMessage).message;
        } else if (zimMessage.getType() == ZIMMessageType.IMAGE) {
            message = ZIMKitCore.getInstance().getApplication().getString(R.string.zimkit_message_photo);
        } else if (zimMessage.getType() == ZIMMessageType.VIDEO) {
            message = ZIMKitCore.getInstance().getApplication().getString(R.string.zimkit_message_video);
        } else if (zimMessage.getType() == ZIMMessageType.AUDIO) {
            message = ZIMKitCore.getInstance().getApplication().getString(R.string.zimkit_message_audio);
        } else if (zimMessage.getType() == ZIMMessageType.FILE) {
            message = ZIMKitCore.getInstance().getApplication().getString(R.string.zimkit_message_file);
        } else {
            message = ZIMKitCore.getInstance().getApplication().getString(R.string.zimkit_message_unknown);
        }
        messageNotification(zimMessage.getConversationType().value(), zimMessage.getConversationID(), message, zimMessage.getSenderUserID());
    }

    private void messageNotification(int conversationType, String conversationID, String message, String senderUserID) {
        NotificationsUtils.NotifyConfig notifyConfig = new NotificationsUtils.NotifyConfig();
        notifyConfig.conversationType = conversationType;
        notifyConfig.conversationID = conversationID;
        notifyConfig.conversationName = "";
        notifyConfig.message = message;
        notifyConfig.senderUserID = senderUserID;
        NotificationsUtils.showNotification(notifyConfig);
    }

    public boolean isOpenNotification() {
        return isOpenNotification;
    }

    public void onNotificationCleared() {
        isOpenNotification = false;
        ZIMKit.unRegisterZIMKitDelegate(eventCallBack);
    }

}
