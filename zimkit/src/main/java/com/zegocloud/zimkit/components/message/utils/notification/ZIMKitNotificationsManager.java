package com.zegocloud.zimkit.components.message.utils.notification;

import android.text.TextUtils;
import com.zegocloud.uikit.plugin.signaling.ZegoSignalingPlugin;
import com.zegocloud.zimkit.R;
import com.zegocloud.zimkit.common.utils.ZIMKitActivityUtils;
import com.zegocloud.zimkit.components.group.bean.ZIMKitGroupMemberInfo;
import com.zegocloud.zimkit.components.message.utils.SortMessageComparator;
import com.zegocloud.zimkit.services.ZIMKit;
import com.zegocloud.zimkit.services.ZIMKitDelegate;
import com.zegocloud.zimkit.services.callback.QueryGroupMemberInfoCallback;
import com.zegocloud.zimkit.services.internal.ZIMKitCore;
import com.zegocloud.zimkit.services.model.ZIMKitConversation;
import com.zegocloud.zimkit.services.model.ZIMKitMessage;
import com.zegocloud.zimkit.services.utils.MessageTransform;
import com.zegocloud.zimkit.services.utils.ZIMMessageUtil;
import im.zego.zim.callback.ZIMUsersInfoQueriedCallback;
import im.zego.zim.entity.ZIMConversation;
import im.zego.zim.entity.ZIMError;
import im.zego.zim.entity.ZIMErrorUserInfo;
import im.zego.zim.entity.ZIMMessage;
import im.zego.zim.entity.ZIMUserFullInfo;
import im.zego.zim.entity.ZIMUsersInfoQueryConfig;
import im.zego.zim.enums.ZIMConversationNotificationStatus;
import im.zego.zim.enums.ZIMConversationType;
import im.zego.zim.enums.ZIMErrorCode;
import im.zego.zim.enums.ZIMMessageDirection;
import im.zego.zim.enums.ZIMMessageType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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
        public void onMessageReceived(String conversationID, ZIMConversationType type,
            ArrayList<ZIMKitMessage> messages) {
            ZIMKitConversation conversation = ZIMKitCore.getInstance().getZIMKitConversation(conversationID);
            if (conversation != null) {
                ZIMConversation zimConversation = conversation.getZimConversation();
                if (zimConversation != null) {
                    if (zimConversation.notificationStatus == ZIMConversationNotificationStatus.DO_NOT_DISTURB) {
                        return;
                    }
                }
            }
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
        ZIMKitConversation conversation = ZIMKitCore.getInstance()
            .getZIMKitConversation(zimMessage.getConversationID());
        if (conversation == null) {
            return;
        }
        if (conversation.getType() == ZIMConversationType.PEER) {
            if (zimMessage.getType() == ZIMMessageType.REVOKE) {
                if (Objects.equals(zimMessage.getSenderUserID(), ZIMKitCore.getInstance().getLocalUser().getId())) {
                    String operatorName = ZIMKitCore.getInstance().getApplication().getString(R.string.zimkit_you);
                    noticeContent(zimMessage, operatorName + " ");
                } else {
                    ZegoSignalingPlugin.getInstance()
                        .queryUserInfo(Collections.singletonList(zimMessage.getSenderUserID()),
                            new ZIMUsersInfoQueryConfig(), new ZIMUsersInfoQueriedCallback() {
                                @Override
                                public void onUsersInfoQueried(ArrayList<ZIMUserFullInfo> userList,
                                    ArrayList<ZIMErrorUserInfo> errorUserList, ZIMError errorInfo) {
                                    if (errorInfo.code == ZIMErrorCode.SUCCESS) {
                                        String operatorName;
                                        ZIMUserFullInfo memoryUserInfo = ZIMKitCore.getInstance()
                                            .getMemoryUserInfo(zimMessage.getSenderUserID());
                                        if (memoryUserInfo == null) {
                                            operatorName = zimMessage.getSenderUserID();
                                        } else {
                                            operatorName = memoryUserInfo.baseInfo.userName;
                                        }
                                        noticeContent(zimMessage, operatorName + " ");
                                    }
                                }
                            });

                }
            } else {
                noticeContent(zimMessage, "");
            }
        } else {
            List<ZIMKitGroupMemberInfo> groupMemberList = ZIMKitCore.getInstance()
                .getGroupMemberList(conversation.getId());
            if (groupMemberList == null || groupMemberList.isEmpty()) {
                ZIMKitCore.getInstance().queryGroupMemberInfo(zimMessage.getSenderUserID(), conversation.getId(),
                    new QueryGroupMemberInfoCallback() {
                        @Override
                        public void onQueryGroupMemberInfo(ZIMKitGroupMemberInfo member, ZIMError error) {
                            String operatorName;
                            if (TextUtils.isEmpty(member.getNickName())) {
                                operatorName = member.getName();
                            } else {
                                operatorName = member.getNickName();
                            }
                            if (zimMessage.getType() == ZIMMessageType.TEXT
                                || zimMessage.getType() == ZIMMessageType.IMAGE
                                || zimMessage.getType() == ZIMMessageType.AUDIO
                                || zimMessage.getType() == ZIMMessageType.VIDEO
                                || zimMessage.getType() == ZIMMessageType.FILE
                                || zimMessage.getType() == ZIMMessageType.COMBINE) {
                                operatorName = operatorName + ":";
                            } else if (zimMessage.getType() == ZIMMessageType.TIPS
                                || zimMessage.getType() == ZIMMessageType.CUSTOM) {
                                operatorName = "";
                            } else if (zimMessage.getType() == ZIMMessageType.REVOKE) {
                            }
                            noticeContent(zimMessage, operatorName);
                        }
                    });
            } else {
                String operatorName = "";
                for (ZIMKitGroupMemberInfo groupMemberInfo : groupMemberList) {
                    if (zimMessage.getSenderUserID().equals(groupMemberInfo.getId())) {
                        if (TextUtils.isEmpty(groupMemberInfo.getNickName())) {
                            operatorName = groupMemberInfo.getName();
                        } else {
                            operatorName = groupMemberInfo.getNickName();
                        }
                        break;
                    }
                }
                if (TextUtils.isEmpty(operatorName)) {
                    operatorName = zimMessage.getSenderUserID();
                }
                if (zimMessage.getType() == ZIMMessageType.TEXT || zimMessage.getType() == ZIMMessageType.IMAGE
                    || zimMessage.getType() == ZIMMessageType.AUDIO || zimMessage.getType() == ZIMMessageType.VIDEO
                    || zimMessage.getType() == ZIMMessageType.FILE || zimMessage.getType() == ZIMMessageType.COMBINE) {
                    operatorName = operatorName + ":";
                } else if (zimMessage.getType() == ZIMMessageType.CUSTOM
                    || zimMessage.getType() == ZIMMessageType.TIPS) {
                    operatorName = operatorName + " ";
                } else if (zimMessage.getType() == ZIMMessageType.REVOKE) {
                }
                noticeContent(zimMessage, operatorName);
            }
        }
    }

    private void noticeContent(ZIMMessage zimMessage, String prefix) {
        String messageContent;
        String content = ZIMMessageUtil.simplifyZIMMessageContent(zimMessage);
        if (!TextUtils.isEmpty(content)) {
            messageContent = prefix + content;
        } else {
            messageContent = ZIMKitCore.getInstance().getApplication().getString(R.string.zimkit_message_unknown);
        }

        messageNotification(zimMessage.getConversationType().value(), zimMessage.getConversationID(), messageContent,
            zimMessage.getSenderUserID());
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
