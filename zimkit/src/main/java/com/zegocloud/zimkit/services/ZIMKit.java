package com.zegocloud.zimkit.services;

import android.app.Application;

import com.zegocloud.zimkit.services.callback.ClearUnreadCountCallback;
import com.zegocloud.zimkit.services.callback.ConnectUserCallback;
import com.zegocloud.zimkit.services.callback.DeleteConversationCallback;
import com.zegocloud.zimkit.services.callback.DeleteMessageCallback;
import com.zegocloud.zimkit.services.callback.DownloadMediaFileCallback;
import com.zegocloud.zimkit.services.callback.GetMessageListCallback;
import com.zegocloud.zimkit.services.callback.InviteUsersToJoinGroupCallback;
import com.zegocloud.zimkit.services.callback.LeaveGroupCallback;
import com.zegocloud.zimkit.services.callback.LoadMoreConversationCallback;
import com.zegocloud.zimkit.services.callback.LoadMoreMessageCallback;
import com.zegocloud.zimkit.services.callback.MessageSentCallback;
import com.zegocloud.zimkit.services.callback.QueryGroupInfoCallback;
import com.zegocloud.zimkit.services.callback.UserAvatarUrlUpdateCallback;
import com.zegocloud.zimkit.services.model.ZIMKitMessage;
import com.zegocloud.zimkit.services.model.ZIMKitUser;
import java.util.List;

import im.zego.zim.enums.ZIMConversationType;
import com.zegocloud.zimkit.components.conversation.interfaces.ZIMKitConversationListListener;
import com.zegocloud.zimkit.components.message.interfaces.ZIMKitMessagesListListener;
import com.zegocloud.zimkit.services.callback.CreateGroupCallback;
import com.zegocloud.zimkit.services.callback.GetConversationListCallback;
import com.zegocloud.zimkit.services.callback.JoinGroupCallback;
import com.zegocloud.zimkit.services.callback.QueryGroupMemberInfoCallback;
import com.zegocloud.zimkit.services.callback.QueryUserCallback;
import com.zegocloud.zimkit.services.internal.ZIMKitCore;
import com.zegocloud.zimkit.services.internal.interfaces.IZIMKitCore;

public class ZIMKit {

    private static IZIMKitCore zimKitCore;

    public static void initWith(Application application, Long appID, String appSign) {
        zimKitCore = ZIMKitCore.getInstance();
        zimKitCore.initWith(application, appID, appSign);
    }

    public static void initNotifications() {
        zimKitCore.initNotifications();
    }

    public static void unInitNotifications() {
        zimKitCore.unInitNotifications();
    }

    public static void registerZIMKitDelegate(ZIMKitDelegate delegate) {
        zimKitCore.registerZIMKitDelegate(delegate);
    }

    public static void unRegisterZIMKitDelegate(ZIMKitDelegate delegate) {
        zimKitCore.unRegisterZIMKitDelegate(delegate);
    }

    public static void getConversationList(GetConversationListCallback callback) {
        zimKitCore.getConversationList(callback);
    }

    public static void deleteConversation(String conversationID, ZIMConversationType type, DeleteConversationCallback callback) {
        zimKitCore.deleteConversation(conversationID, type, callback);
    }

    public static void clearUnreadCount(String conversationID, ZIMConversationType type, ClearUnreadCountCallback callback) {
        zimKitCore.clearUnreadCount(conversationID, type, callback);
    }

    public static void loadMoreConversation(LoadMoreConversationCallback callback) {
        zimKitCore.loadMoreConversation(callback);
    }

    public static void createGroup(String groupName, List<String> inviteUserIDs, CreateGroupCallback callback) {
        zimKitCore.createGroup(groupName, inviteUserIDs, callback);
    }

    public static void createGroup(String groupName, String groupId, List<String> inviteUserIDs, CreateGroupCallback callback) {
        zimKitCore.createGroup(groupName, groupId, inviteUserIDs, callback);
    }

    public static void joinGroup(String groupID, JoinGroupCallback callback) {
        zimKitCore.joinGroup(groupID, callback);
    }

    public static void leaveGroup(String groupID, LeaveGroupCallback callback) {
        zimKitCore.leaveGroup(groupID, callback);
    }

    public static void inviteUsersToJoinGroup(List<String> userIDs, String groupID, InviteUsersToJoinGroupCallback callback) {
        zimKitCore.inviteUsersToJoinGroup(userIDs, groupID, callback);
    }

    public static void queryGroupInfo(String groupID, QueryGroupInfoCallback callback) {
        zimKitCore.queryGroupInfo(groupID, callback);
    }

    public static void queryGroupMemberInfo(String userID, String groupID, QueryGroupMemberInfoCallback callback) {
        zimKitCore.queryGroupMemberInfo(userID, groupID, callback);
    }

    public static void getMessageList(String conversationID, ZIMConversationType type, GetMessageListCallback callback) {
        zimKitCore.getMessageList(conversationID, type, callback);
    }

    public static void loadMoreMessage(String conversationID, ZIMConversationType type, LoadMoreMessageCallback callback) {
        zimKitCore.loadMoreMessage(conversationID, type, callback);
    }

    public static void sendTextMessage(String text, String conversationID, ZIMConversationType type, MessageSentCallback callback) {
        zimKitCore.sendTextMessage(text, conversationID, type, callback);
    }

    public static void sendImageMessage(String imagePath, String conversationID, ZIMConversationType type, MessageSentCallback callback) {
        zimKitCore.sendImageMessage(imagePath, conversationID, type, callback);
    }

    public static void sendAudioMessage(String audioPath, long duration, String conversationID, ZIMConversationType type, MessageSentCallback callback) {
        zimKitCore.sendAudioMessage(audioPath, duration, conversationID, type, callback);
    }

    public static void sendVideoMessage(String videoPath, long duration, String conversationID, ZIMConversationType type, MessageSentCallback callback) {
        zimKitCore.sendVideoMessage(videoPath, duration, conversationID, type, callback);
    }

    public static void sendFileMessage(String filePath, String conversationID, ZIMConversationType type, MessageSentCallback callback) {
        zimKitCore.sendFileMessage(filePath, conversationID, type, callback);
    }

    public static void downloadMediaFile(ZIMKitMessage message, DownloadMediaFileCallback callback) {
        zimKitCore.downloadMediaFile(message, callback);
    }

    public static void deleteMessage(List<ZIMKitMessage> messages, DeleteMessageCallback callback) {
        zimKitCore.deleteMessage(messages, callback);
    }

    public static ZIMKitUser getLocalUser() {
        return zimKitCore.getLocalUser();
    }

    public static void connectUser(String userID, String userName, String avatarUrl, ConnectUserCallback callback) {
        zimKitCore.connectUser(userID, userName, avatarUrl, callback);
    }

    public static void disconnectUser() {
        zimKitCore.disconnectUser();
    }

    public static void queryUserInfo(String userID, QueryUserCallback callback) {
        zimKitCore.queryUserInfo(userID, callback);
    }

    public static void updateUserAvatarUrl(String avatarUrl, UserAvatarUrlUpdateCallback callback) {
        zimKitCore.updateUserAvatarUrl(avatarUrl, callback);
    }

    public static void registerConversationListListener(ZIMKitConversationListListener listener) {
        zimKitCore.registerConversationListListener(listener);
    }

    public static void unRegisterConversationListListener() {
        zimKitCore.unRegisterConversationListListener();
    }

    public static void registerMessageListListener(ZIMKitMessagesListListener listener) {
        zimKitCore.registerMessageListListener(listener);
    }

    public static void unRegisterMessageListListener() {
        zimKitCore.unRegisterMessageListListener();
    }

}
