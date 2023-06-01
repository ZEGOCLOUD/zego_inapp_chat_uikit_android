package com.zegocloud.zimkit.services.internal;

import android.app.Application;
import android.text.TextUtils;

import com.zegocloud.zimkit.common.utils.ZIMKitActivityUtils;
import com.zegocloud.zimkit.common.utils.ZIMKitDateUtils;
import com.zegocloud.zimkit.common.utils.ZIMKitThreadHelper;
import com.zegocloud.zimkit.components.conversation.interfaces.ZIMKitConversationListListener;
import com.zegocloud.zimkit.components.message.interfaces.ZIMKitMessagesListListener;
import com.zegocloud.zimkit.components.message.utils.notification.ZIMKitNotificationsManager;
import com.zegocloud.zimkit.services.ZIMKitDelegate;
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
import com.zegocloud.zimkit.services.config.InputConfig;
import com.zegocloud.zimkit.services.internal.interfaces.IZIMKitCore;
import com.zegocloud.zimkit.services.model.ZIMKitConversation;
import com.zegocloud.zimkit.services.model.ZIMKitGroupMember;
import com.zegocloud.zimkit.services.model.ZIMKitMessage;
import com.zegocloud.zimkit.services.model.ZIMKitUser;
import com.zegocloud.zimkit.services.utils.ZIMKitNotifyList;
import com.zegocloud.uikit.plugin.signaling.ZegoSignalingPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import im.zego.zim.ZIM;
import im.zego.zim.entity.ZIMConversation;
import im.zego.zim.entity.ZIMError;
import im.zego.zim.entity.ZIMMessage;
import im.zego.zim.enums.ZIMConversationType;
import im.zego.zim.enums.ZIMErrorCode;
import com.zegocloud.zimkit.R;
import com.zegocloud.zimkit.services.callback.CreateGroupCallback;
import com.zegocloud.zimkit.services.callback.GetConversationListCallback;
import com.zegocloud.zimkit.services.callback.JoinGroupCallback;
import com.zegocloud.zimkit.services.callback.QueryGroupMemberInfoCallback;
import com.zegocloud.zimkit.services.callback.QueryUserCallback;

public class ZIMKitCore implements IZIMKitCore {

    private static ZIMKitCore sInstance;

    private ZIMKitCore() {}

    public static ZIMKitCore getInstance() {
        synchronized (ZIMKitCore.class) {
            if (sInstance == null) {
                sInstance = new ZIMKitCore();
            }
            return sInstance;
        }
    }

    private Application application;
    private ZIM zim;
    private UserService userService = new UserService();
    private ConversationService conversationService = new ConversationService();
    private GroupService groupService = new GroupService();
    private MessageService messageService = new MessageService();
    private ZIMKitEventHandler eventHandler = new ZIMKitEventHandler();
    private ArrayList<ZIMKitMessage> mMessageList = new ArrayList<>();
    private ZIMKitNotifyList<ZIMKitDelegate> zimkitNotifyList = new ZIMKitNotifyList<>();
    private  Map<String, String> mGroupUserInfoNameMap = new HashMap<>();
    private  Map<String, String> mGroupUserInfoAvatarMap = new HashMap<>();
    private int totalUnreadMessageCount;

    private ZIMKitConversationListListener conversationListListener;
    private ZIMKitMessagesListListener messagesListListener;

    private boolean isLoadConversationList = false;

    private InputConfig inputConfig;

    public InputConfig getInputConfig() {
        return inputConfig;
    }

    private final TreeSet<ZIMKitConversation> conversations = new TreeSet<>((model1, model2) -> {
        //Sort by orderKey
        long value = model2.getOrderKey() - model1.getOrderKey();
        if (value == 0) {
            return 0;
        }
        return value > 0 ? 1 : -1;
    });

    public Application getApplication() {
        return application;
    }

    public ZIM zim() {
        if (zim == null) {
            throw new IllegalArgumentException(application.getString(R.string.zimkit_create_zim_fail_log));
        }
        return zim;
    }

    @Override
    public void initWith(Application application, Long appID, String appSign) {
        this.application = application;
        this.totalUnreadMessageCount = 0;
        ZIMKitActivityUtils.init(application);
        ZIMKitDateUtils.setContext(application);
        ZegoSignalingPlugin.getInstance().init(application, appID, appSign);
        zim = ZIM.getInstance();
        ZegoSignalingPlugin.getInstance().registerZIMEventHandler(eventHandler);
    }

    @Override
    public void initNotifications() {
        ZIMKitNotificationsManager.share().initNotifications();
    }

    @Override
    public void unInitNotifications() {
        ZIMKitNotificationsManager.share().onNotificationCleared();
    }

    @Override
    public void registerZIMKitDelegate(ZIMKitDelegate delegate) {
        zimkitNotifyList.addListener(delegate, false);
    }

    @Override
    public void unRegisterZIMKitDelegate(ZIMKitDelegate delegate) {
        zimkitNotifyList.removeListener(delegate, false);
    }

    @Override
    public ZIMKitUser getLocalUser() {
        return userService.getUserInfo();
    }

    @Override
    public void connectUser(String userID, String userName, String avatarUrl, ConnectUserCallback callback) {
        eventHandler.setKickedOutAccount(false);
        userService.connectUser(userID, userName, avatarUrl, callback);
    }

    @Override
    public void disconnectUser() {
        conversationListListener = null;
        messagesListListener = null;
        isLoadConversationList = false;
        totalUnreadMessageCount = 0;
        conversations.clear();
        mMessageList.clear();
        mGroupUserInfoNameMap.clear();
        mGroupUserInfoAvatarMap.clear();
        zimkitNotifyList.clear();
        userService.disconnectUser();
    }

    @Override
    public void queryUserInfo(String userID, QueryUserCallback callback) {
        userService.queryUserInfo(userID, callback);
    }

    @Override
    public void updateUserAvatarUrl(String avatarUrl, UserAvatarUrlUpdateCallback callback) {
        userService.updateUserAvatarUrl(avatarUrl, callback);
    }

    @Override
    public void getConversationList(GetConversationListCallback callback) {
        conversationService.getConversationList(callback);
    }

    @Override
    public void deleteConversation(String conversationID, ZIMConversationType type, DeleteConversationCallback callback) {
        conversationService.deleteConversation(conversationID, type, callback);
    }

    @Override
    public void clearUnreadCount(String conversationID, ZIMConversationType type, ClearUnreadCountCallback callback) {
        conversationService.clearUnreadCount(conversationID, type, callback);
    }

    @Override
    public void loadMoreConversation(LoadMoreConversationCallback callback) {
        ZIMKitConversation lastModel = conversations.last();
        if (lastModel != null) {
            ZIMConversation conversation = lastModel.getZim();
            conversationService.loadMoreConversation(true, conversation, callback);
        } else {
            if (callback != null) {
                ZIMError zimError = new ZIMError();
                zimError.code = ZIMErrorCode.FAILED;
                zimError.message = "not next page";
                callback.onLoadMoreConversation(zimError);
            }
        }
    }

    @Override
    public void createGroup(String groupName, List<String> inviteUserIDs, CreateGroupCallback callback) {
        groupService.createGroup(groupName, inviteUserIDs, callback);
    }

    @Override
    public void createGroup(String groupName, String groupId, List<String> inviteUserIDs, CreateGroupCallback callback) {
        groupService.createGroup(groupName, groupId, inviteUserIDs, callback);
    }

    @Override
    public void joinGroup(String groupID, JoinGroupCallback callback) {
        groupService.joinGroup(groupID, callback);
    }

    @Override
    public void leaveGroup(String groupID, LeaveGroupCallback callback) {
        groupService.leaveGroup(groupID, callback);
    }

    @Override
    public void inviteUsersToJoinGroup(List<String> userIDs, String groupID, InviteUsersToJoinGroupCallback callback) {
        groupService.inviteUsersToJoinGroup(userIDs, groupID, callback);
    }

    @Override
    public void queryGroupInfo(String groupID, QueryGroupInfoCallback callback) {
        groupService.queryGroupInfo(groupID, callback);
    }

    @Override
    public void queryGroupMemberInfo(String userID, String groupID, QueryGroupMemberInfoCallback callback) {
        groupService.queryGroupMemberInfo(userID, groupID, callback);
    }

    @Override
    public void getMessageList(String conversationID, ZIMConversationType type, GetMessageListCallback callback) {
        messageService.getMessageList(conversationID, type, callback);
    }

    @Override
    public void loadMoreMessage(String conversationID, ZIMConversationType type, LoadMoreMessageCallback callback) {
        messageService.loadMoreMessage(conversationID, type, true, callback);
    }

    @Override
    public void sendTextMessage(String text, String conversationID, ZIMConversationType type, MessageSentCallback callback) {
        messageService.sendTextMessage(text, conversationID, type, callback);
    }

    @Override
    public void sendImageMessage(String imagePath, String conversationID, ZIMConversationType type, MessageSentCallback callback) {
        messageService.sendImageMessage(imagePath, conversationID, type, callback);
    }

    @Override
    public void sendAudioMessage(String audioPath, long duration, String conversationID, ZIMConversationType type, MessageSentCallback callback) {
        messageService.sendAudioMessage(audioPath, duration, conversationID, type, callback);
    }

    @Override
    public void sendVideoMessage(String videoPath, long duration, String conversationID, ZIMConversationType type, MessageSentCallback callback) {
        messageService.sendVideoMessage(videoPath, duration, conversationID, type, callback);
    }

    @Override
    public void sendFileMessage(String filePath, String conversationID, ZIMConversationType type, MessageSentCallback callback) {
        messageService.sendFileMessage(filePath, conversationID, type, callback);
    }

    @Override
    public void downloadMediaFile(ZIMKitMessage message, DownloadMediaFileCallback callback) {
        messageService.downloadMediaFile(message, callback);
    }

    @Override
    public void deleteMessage(List<ZIMKitMessage> messages, DeleteMessageCallback callback) {
        messageService.deleteMessage(messages, callback);
    }

    public void setGroupMemberInfo(ArrayList<ZIMMessage> zimMessages) {
        if (messageService.getConversationType() != ZIMConversationType.GROUP) {
            return;
        }
        ZIMKitThreadHelper.INST.execute(new Runnable() {
            @Override
            public void run() {
                for (ZIMMessage itemModel : zimMessages) {
                    String nickName = mGroupUserInfoNameMap.get(itemModel.getSenderUserID());
                    if (!TextUtils.isEmpty(nickName)) {
                        queryGroupMemberInfo(itemModel);
                    }
                }
            }
        });
    }

    private void queryGroupMemberInfo(ZIMMessage itemModel) {
        queryGroupMemberInfo(itemModel.getSenderUserID(), itemModel.getConversationID(), new QueryGroupMemberInfoCallback() {
            @Override
            public void onQueryGroupMemberInfo(ZIMKitGroupMember member, ZIMError error) {
                if (error.code == ZIMErrorCode.SUCCESS) {
                    mGroupUserInfoNameMap.put(member.getId(), member.getName());
                    mGroupUserInfoAvatarMap.put(member.getId(), member.getAvatarUrl());
                }
            }
        });
    }

    public boolean isKickedOutAccount() {
        return eventHandler.isKickedOutAccount();
    }

    @Override
    public void registerConversationListListener(ZIMKitConversationListListener listener) {
        this.conversationListListener = listener;
    }

    @Override
    public void unRegisterConversationListListener() {
        this.conversationListListener = null;
    }

    @Override
    public void registerMessageListListener(ZIMKitMessagesListListener listener) {
        this.messagesListListener = listener;
    }

    @Override
    public void unRegisterMessageListListener() {
        this.messagesListListener = null;
    }

    public ZIMKitConversationListListener getConversationListListener() {
        return conversationListListener;
    }

    public ZIMKitMessagesListListener getMessageListListener() {
        return messagesListListener;
    }

    public int getTotalUnreadMessageCount() {
        return totalUnreadMessageCount;
    }

    public void setTotalUnreadMessageCount(int totalUnreadMessageCount) {
        this.totalUnreadMessageCount = totalUnreadMessageCount;
    }

    public boolean isLoadConversationList() {
        return isLoadConversationList;
    }

    public void setLoadConversationList(boolean loadConversationList) {
        this.isLoadConversationList = loadConversationList;
    }

    public TreeSet<ZIMKitConversation> getConversations() {
        return conversations;
    }

    public ArrayList<ZIMKitMessage> getMessageList() {
        return mMessageList;
    }

    public ZIMKitNotifyList<ZIMKitDelegate> getZimkitNotifyList() {
        return zimkitNotifyList;
    }

    public Map<String, String> getGroupUserInfoNameMap() {
        return mGroupUserInfoNameMap;
    }

    public Map<String, String> getGroupUserInfoAvatarMap() {
        return mGroupUserInfoAvatarMap;
    }

    @Override
    public void setInputConfig(InputConfig config) {
        inputConfig = config;
    }
}
