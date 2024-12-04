package com.zegocloud.zimkit.services;

import com.zegocloud.zimkit.services.model.ZIMKitConversation;
import com.zegocloud.zimkit.services.model.ZIMKitErrorToast;
import com.zegocloud.zimkit.services.model.ZIMKitMessage;
import im.zego.zim.entity.ZIMGroupMemberInfo;
import im.zego.zim.entity.ZIMGroupOperatedInfo;
import im.zego.zim.entity.ZIMMessageReaction;
import im.zego.zim.enums.ZIMConnectionEvent;
import im.zego.zim.enums.ZIMConnectionState;
import im.zego.zim.enums.ZIMConversationType;
import im.zego.zim.enums.ZIMGroupMemberEvent;
import im.zego.zim.enums.ZIMGroupMemberState;
import java.util.ArrayList;

public interface ZIMKitDelegate {

    default void onConnectionStateChange(ZIMConnectionState state, ZIMConnectionEvent event) {
    }

    default void onTotalUnreadMessageCountChange(int totalCount) {
    }

    default void onConversationListChanged(ArrayList<ZIMKitConversation> conversations) {
    }

    default ZIMKitMessage onMessagePreSending(ZIMKitMessage message) {
        return null;
    }

    default void onMessageReceived(String conversationID, ZIMConversationType type, ArrayList<ZIMKitMessage> messages) {
    }

    default void onHistoryMessageLoaded(String conversationID, ZIMConversationType type,
        ArrayList<ZIMKitMessage> messages) {
    }

    default void onMessageDeleted(String conversationID, ZIMConversationType type, ArrayList<ZIMKitMessage> messages) {
    }

    default void onMessageSentStatusChanged(ZIMKitMessage message) {
    }

    default void onMediaMessageUploadingProgressUpdated(ZIMKitMessage message, boolean isFinished) {
    }

    default void onMediaMessageDownloadingProgressUpdated(ZIMKitMessage message, boolean isFinished) {
    }

    default ZIMKitErrorToast onErrorToastCallback(int errorCode, ZIMKitErrorToast defaultToast) {
        return null;
    }

    default void onTokenWillExpire(int second) {
    }

    default void onGroupMemberStateChanged(ZIMGroupMemberState state, ZIMGroupMemberEvent event,
        ArrayList<ZIMGroupMemberInfo> userList, ZIMGroupOperatedInfo operatedInfo, String groupID) {
    }

    default void onMessageRevokeReceived(String conversationID, ZIMConversationType type,
        ArrayList<ZIMKitMessage> messageList) {
    }

    default void onMessageRepliedInfoChanged(String conversationID, ZIMConversationType type,
        ArrayList<ZIMKitMessage> kitMessages) {
    }

    default void onMessageReactionsChanged(ArrayList<ZIMMessageReaction> reactions) {

    }
}
