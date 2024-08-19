package com.zegocloud.zimkit.services.model;

import android.text.TextUtils;

import com.zegocloud.zimkit.services.utils.MessageTransform;
import im.zego.zim.entity.ZIMConversation;
import im.zego.zim.enums.ZIMConversationNotificationStatus;
import im.zego.zim.enums.ZIMConversationType;

public class ZIMKitConversation {

    private String id;
    private String name;
    private String avatarUrl;
    private ZIMConversationType type = ZIMConversationType.PEER;
    private ZIMConversationNotificationStatus notificationStatus = ZIMConversationNotificationStatus.NOTIFY;
    private int unreadMessageCount;
    private ZIMKitMessage lastMessage;
    private long orderKey;
    private ZIMConversation zimConversation;

    public ZIMKitConversation(ZIMConversation conversation) {
        if (conversation == null) {
            return;
        }
        this.zimConversation = conversation;
        this.setId(conversation.conversationID);
        this.setName(conversation);
        this.setAvatarUrl(conversation.conversationAvatarUrl);
        this.setType(conversation.type);
        this.setNotificationStatus(conversation.notificationStatus);
        this.setUnreadMessageCount(conversation.unreadMessageCount);
        this.setLastMessage(MessageTransform.parseMessage(conversation.lastMessage));
        this.setOrderKey(conversation.orderKey);
    }

    public void setName(ZIMConversation conversation) {
        this.name = TextUtils.isEmpty(conversation.conversationName)
                ? conversation.conversationID
                : conversation.conversationName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public ZIMConversationType getType() {
        return type;
    }

    public void setType(ZIMConversationType type) {
        this.type = type;
    }

    public ZIMConversationNotificationStatus getNotificationStatus() {
        return notificationStatus;
    }

    public void setNotificationStatus(ZIMConversationNotificationStatus notificationStatus) {
        this.notificationStatus = notificationStatus;
    }

    public int getUnreadMessageCount() {
        return unreadMessageCount;
    }

    public void setUnreadMessageCount(int unreadMessageCount) {
        this.unreadMessageCount = unreadMessageCount;
    }

    public ZIMKitMessage getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(ZIMKitMessage lastMessage) {
        this.lastMessage = lastMessage;
    }

    public long getOrderKey() {
        return orderKey;
    }

    public void setOrderKey(long orderKey) {
        this.orderKey = orderKey;
    }

    public ZIMConversation getZimConversation() {
        return zimConversation;
    }

    public void setZimConversation(ZIMConversation zimConversation) {
        this.zimConversation = zimConversation;
    }
}
