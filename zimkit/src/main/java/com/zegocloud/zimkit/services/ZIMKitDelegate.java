package com.zegocloud.zimkit.services;

import com.zegocloud.zimkit.services.model.ZIMKitConversation;
import com.zegocloud.zimkit.services.model.ZIMKitMessage;
import java.util.ArrayList;

import im.zego.zim.enums.ZIMConnectionEvent;
import im.zego.zim.enums.ZIMConnectionState;
import im.zego.zim.enums.ZIMConversationType;

public interface ZIMKitDelegate {

    default void onConnectionStateChange(ZIMConnectionState state, ZIMConnectionEvent event){}

    default void onTotalUnreadMessageCountChange(int totalCount){}

    default void onConversationListChanged(ArrayList<ZIMKitConversation> conversations){}

    default ZIMKitMessage onMessagePreSending(ZIMKitMessage message) {
        return null;
    }

    default void onMessageReceived(String conversationID, ZIMConversationType type, ArrayList<ZIMKitMessage> messages){}

    default void onHistoryMessageLoaded(String conversationID, ZIMConversationType type, ArrayList<ZIMKitMessage> messages){}

    default void onMessageDeleted(String conversationID, ZIMConversationType type, ArrayList<ZIMKitMessage> messages){}

    default void onMessageSentStatusChanged(ZIMKitMessage message){}

    default void onMediaMessageUploadingProgressUpdated(ZIMKitMessage message, boolean isFinished){}

    default void onMediaMessageDownloadingProgressUpdated(ZIMKitMessage message,boolean isFinished){}

}
