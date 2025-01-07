package com.zegocloud.zimkit.services.internal;

import android.app.Application;
import android.text.TextUtils;
import com.zegocloud.uikit.plugin.adapter.plugins.signaling.RenewTokenCallback;
import com.zegocloud.uikit.plugin.signaling.ZegoSignalingPlugin;
import com.zegocloud.zimkit.R;
import com.zegocloud.zimkit.common.utils.ZIMKitActivityUtils;
import com.zegocloud.zimkit.common.utils.ZIMKitDateUtils;
import com.zegocloud.zimkit.common.utils.ZIMKitThreadHelper;
import com.zegocloud.zimkit.components.conversation.interfaces.ZIMKitConversationListListener;
import com.zegocloud.zimkit.components.forward.ZIMKitForwardType;
import com.zegocloud.zimkit.components.group.bean.ZIMKitGroupMemberInfo;
import com.zegocloud.zimkit.components.message.interfaces.ZIMKitMessagesListListener;
import com.zegocloud.zimkit.components.message.model.ZIMKitInputButtonModel;
import com.zegocloud.zimkit.components.message.model.ZIMKitMessageModel;
import com.zegocloud.zimkit.components.message.utils.notification.ZIMKitNotificationsManager;
import com.zegocloud.zimkit.services.ZIMKitConfig;
import com.zegocloud.zimkit.services.ZIMKitDelegate;
import com.zegocloud.zimkit.services.callback.ClearUnreadCountCallback;
import com.zegocloud.zimkit.services.callback.ConnectUserCallback;
import com.zegocloud.zimkit.services.callback.CreateGroupCallback;
import com.zegocloud.zimkit.services.callback.DeleteConversationCallback;
import com.zegocloud.zimkit.services.callback.DeleteMessageCallback;
import com.zegocloud.zimkit.services.callback.DownloadMediaFileCallback;
import com.zegocloud.zimkit.services.callback.GetConversationListCallback;
import com.zegocloud.zimkit.services.callback.GetMessageListCallback;
import com.zegocloud.zimkit.services.callback.InviteUsersToJoinGroupCallback;
import com.zegocloud.zimkit.services.callback.JoinGroupCallback;
import com.zegocloud.zimkit.services.callback.LeaveGroupCallback;
import com.zegocloud.zimkit.services.callback.LoadMoreConversationCallback;
import com.zegocloud.zimkit.services.callback.LoadMoreMessageCallback;
import com.zegocloud.zimkit.services.callback.MessageSentCallback;
import com.zegocloud.zimkit.services.callback.QueryGroupInfoCallback;
import com.zegocloud.zimkit.services.callback.QueryGroupMemberInfoCallback;
import com.zegocloud.zimkit.services.callback.QueryGroupMemberListCallback;
import com.zegocloud.zimkit.services.callback.QueryUserCallback;
import com.zegocloud.zimkit.services.callback.UserAvatarUrlUpdateCallback;
import com.zegocloud.zimkit.services.config.InputConfig;
import com.zegocloud.zimkit.services.config.ZIMKitInputButtonName;
import com.zegocloud.zimkit.services.internal.interfaces.IZIMKitCore;
import com.zegocloud.zimkit.services.model.ZIMKitConversation;
import com.zegocloud.zimkit.services.model.ZIMKitMessage;
import com.zegocloud.zimkit.services.model.ZIMKitUser;
import com.zegocloud.zimkit.services.utils.ZIMKitNotifyList;
import com.zegocloud.zimkit.services.utils.ZIMMessageUtil;
import im.zego.zim.ZIM;
import im.zego.zim.callback.ZIMCombineMessageDetailQueriedCallback;
import im.zego.zim.callback.ZIMConversationNotificationStatusSetCallback;
import im.zego.zim.callback.ZIMConversationPinnedStateUpdatedCallback;
import im.zego.zim.callback.ZIMMediaMessageSentCallback;
import im.zego.zim.callback.ZIMMessageReactionAddedCallback;
import im.zego.zim.callback.ZIMMessageReactionDeletedCallback;
import im.zego.zim.callback.ZIMMessageRevokedCallback;
import im.zego.zim.callback.ZIMMessageSentCallback;
import im.zego.zim.callback.ZIMMessageSentFullCallback;
import im.zego.zim.callback.ZIMUsersInfoQueriedCallback;
import im.zego.zim.entity.ZIMCombineMessage;
import im.zego.zim.entity.ZIMConversation;
import im.zego.zim.entity.ZIMError;
import im.zego.zim.entity.ZIMGroupMemberInfo;
import im.zego.zim.entity.ZIMGroupMemberQueryConfig;
import im.zego.zim.entity.ZIMGroupOperatedInfo;
import im.zego.zim.entity.ZIMMediaMessage;
import im.zego.zim.entity.ZIMMessage;
import im.zego.zim.entity.ZIMMessageSendConfig;
import im.zego.zim.entity.ZIMUserFullInfo;
import im.zego.zim.entity.ZIMUsersInfoQueryConfig;
import im.zego.zim.enums.ZIMConnectionState;
import im.zego.zim.enums.ZIMConversationNotificationStatus;
import im.zego.zim.enums.ZIMConversationType;
import im.zego.zim.enums.ZIMErrorCode;
import im.zego.zim.enums.ZIMGroupMemberEvent;
import im.zego.zim.enums.ZIMGroupMemberState;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public class ZIMKitCore implements IZIMKitCore {

    private static ZIMKitCore sInstance;

    private ZIMKitCore() {

    }

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
    private Map<String, String> mGroupUserInfoNameMap = new HashMap<>();
    private Map<String, String> mGroupUserInfoAvatarMap = new HashMap<>();
    private int totalUnreadMessageCount;
    private Map<String, List<ZIMKitGroupMemberInfo>> groupList = new HashMap<>();
    private ZIMKitConversationListListener conversationListListener;
    private ZIMKitMessagesListListener messagesListListener;
    private boolean isLoadConversationList = false;

    private InputConfig inputConfig;
    private ZIMKitConfig zimKitConfig;
    private List<ZIMKitMessageModel> forwardMessages = new ArrayList<>();
    private ZIMKitForwardType forwardType;

    public InputConfig getInputConfig() {
        return inputConfig;
    }

    public ZIMKitConfig getZimKitConfig() {
        return zimKitConfig;
    }

    private Map<ZIMKitInputButtonName, ZIMKitInputButtonModel> inputButtonMap = new HashMap<>();

    private final TreeSet<ZIMKitConversation> conversations = new TreeSet<>((model1, model2) -> {
        //Sort by orderKey
        long value = model2.getOrderKey() - model1.getOrderKey();
        if (value == 0) {
            return model1.getId().compareTo(model2.getId());
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

    public long appID;
    public String appSign;

    @Override
    public void initWith(Application application, Long appID, String appSign, ZIMKitConfig zimKitConfig) {
        this.application = application;
        this.totalUnreadMessageCount = 0;
        ZIMKitActivityUtils.init(application);
        ZIMKitDateUtils.setContext(application);
        ZegoSignalingPlugin.getInstance().init(application, appID, appSign);
        zim = ZIM.getInstance();
        ZegoSignalingPlugin.getInstance().registerZIMEventHandler(eventHandler);
        this.zimKitConfig = zimKitConfig;
        this.appID = appID;
        this.appSign = appSign;

        inputButtonMap.put(ZIMKitInputButtonName.AUDIO,
            new ZIMKitInputButtonModel(ZIMKitInputButtonName.AUDIO, application, R.string.zimkit_input_audio,
                R.drawable.zimkit_input_btn_audio, R.drawable.zimkit_input_btn_audio_selected,
                R.drawable.zimkit_input_btn_audio_expand));
        inputButtonMap.put(ZIMKitInputButtonName.EMOJI,
            new ZIMKitInputButtonModel(ZIMKitInputButtonName.EMOJI, application, R.string.zimkit_input_emoji,
                R.drawable.zimkit_input_btn_emoji, R.drawable.zimkit_input_btn_emoji_selected,
                R.drawable.zimkit_input_btn_emoji_expand));
        inputButtonMap.put(ZIMKitInputButtonName.PICTURE,
            new ZIMKitInputButtonModel(ZIMKitInputButtonName.PICTURE, application, R.string.zimkit_input_photo,
                R.drawable.zimkit_input_btn_pic, R.drawable.zimkit_input_btn_pic,
                R.drawable.zimkit_input_btn_pic_expand));
        inputButtonMap.put(ZIMKitInputButtonName.EXPAND,
            new ZIMKitInputButtonModel(ZIMKitInputButtonName.EXPAND, application, R.string.zimkit_input_emoji,
                R.drawable.zimkit_input_btn_expand, R.drawable.zimkit_input_btn_expand_selected,
                R.drawable.zimkit_input_btn_expand));
        inputButtonMap.put(ZIMKitInputButtonName.TAKE_PHOTO,
            new ZIMKitInputButtonModel(ZIMKitInputButtonName.TAKE_PHOTO, application, R.string.zimkit_take_photo,
                R.drawable.zimkit_input_btn_camera, R.drawable.zimkit_input_btn_camera,
                R.drawable.zimkit_input_btn_camera_expand));
        inputButtonMap.put(ZIMKitInputButtonName.VOICE_CALL,
            new ZIMKitInputButtonModel(ZIMKitInputButtonName.VOICE_CALL, application, R.string.zimkit_avcall,
                R.drawable.zimkit_input_btn_avcall, R.drawable.zimkit_input_btn_avcall,
                R.drawable.zimkit_input_btn_avcall_expand));
        inputButtonMap.put(ZIMKitInputButtonName.VIDEO_CALL,
            new ZIMKitInputButtonModel(ZIMKitInputButtonName.VIDEO_CALL, application, R.string.zimkit_avcall,
                R.drawable.zimkit_input_btn_avcall, R.drawable.zimkit_input_btn_avcall,
                R.drawable.zimkit_input_btn_avcall_expand));
        inputButtonMap.put(ZIMKitInputButtonName.FILE,
            new ZIMKitInputButtonModel(ZIMKitInputButtonName.FILE, application, R.string.zimkit_file,
                R.drawable.zimkit_input_btn_file, R.drawable.zimkit_input_btn_file,
                R.drawable.zimkit_input_btn_file_expand));
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
    public void connectUser(String userID, String userName, String avatarUrl, String token,
        ConnectUserCallback callback) {
        eventHandler.setKickedOutAccount(false);
        userService.connectUser(userID, userName, avatarUrl, token, callback);
    }

    private static final String TAG = "ZIMKitCore";

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
        groupList.clear();
    }

    public boolean isPluginConnected() {
        return ZegoSignalingPlugin.getInstance().getConnectionState() == ZIMConnectionState.CONNECTED;
    }

    public ZIMKitInputButtonModel getInputButtonModel(ZIMKitInputButtonName inputButtonName) {
        return inputButtonMap.get(inputButtonName);
    }

    @Override
    public void queryUserInfo(String userID, QueryUserCallback callback) {
        userService.queryUserInfo(userID, callback);
    }

    public void setConversationPinnedState(boolean isPinned, String conversationID,
        ZIMConversationType conversationType, ZIMConversationPinnedStateUpdatedCallback callback) {
        conversationService.updateConversationPinnedState(isPinned, conversationID, conversationType, callback);
    }

    public void setConversationNotificationStatus(ZIMConversationNotificationStatus status, String conversationID,
        ZIMConversationType conversationType, ZIMConversationNotificationStatusSetCallback callback) {
        conversationService.setConversationNotificationStatus(status, conversationID, conversationType, callback);
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
    public void deleteConversation(String conversationID, ZIMConversationType type,
        DeleteConversationCallback callback) {
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
            ZIMConversation conversation = lastModel.getZimConversation();
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
    public void createGroup(String groupName, String groupId, List<String> inviteUserIDs,
        CreateGroupCallback callback) {
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
        groupService.queryGroupMemberInfo(userID, groupID, new QueryGroupMemberInfoCallback() {
            @Override
            public void onQueryGroupMemberInfo(ZIMKitGroupMemberInfo member, ZIMError error) {
                if (error.code == ZIMErrorCode.SUCCESS) {
                    if (member != null) {
                        List<ZIMKitGroupMemberInfo> memberList = getOrNewGroupMemberList(groupID);
                        boolean contains = false;
                        for (ZIMKitGroupMemberInfo groupMember : memberList) {
                            if (groupMember.getId().equals(userID)) {
                                contains = true;
                                groupMember = member;
                                break;
                            }
                        }
                        if (!contains) {
                            memberList.add(member);
                        }
                    }
                }
                if (callback != null) {
                    callback.onQueryGroupMemberInfo(member, error);
                }
            }
        });
    }

    public List<ZIMKitGroupMemberInfo> getGroupMemberList(String groupID) {
        return groupList.get(groupID);
    }

    List<ZIMKitGroupMemberInfo> getOrNewGroupMemberList(String groupID) {
        List<ZIMKitGroupMemberInfo> list;
        if (groupList.containsKey(groupID)) {
            list = groupList.get(groupID);
        } else {
            list = new ArrayList<>();
            groupList.put(groupID, list);
        }
        return list;
    }

    public void queryGroupMemberList(String groupID, ZIMGroupMemberQueryConfig config,
        QueryGroupMemberListCallback callback) {
        groupService.queryGroupMemberList(groupID, config, new QueryGroupMemberListCallback() {
            @Override
            public void onGroupMemberListQueried(String groupID, ArrayList<ZIMKitGroupMemberInfo> userList,
                int nextFlag, ZIMError errorInfo) {
                if (errorInfo.code == ZIMErrorCode.SUCCESS) {
                    List<ZIMKitGroupMemberInfo> list = getOrNewGroupMemberList(groupID);
                    list.clear();
                    list.addAll(userList);
                }
                if (callback != null) {
                    callback.onGroupMemberListQueried(groupID, userList, nextFlag, errorInfo);
                }
            }
        });
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
    public void sendTextMessage(String text, String targetID, String targetName, ZIMConversationType targetType,
        MessageSentCallback callback) {
        messageService.sendTextMessage(text, targetID, targetName, targetType, callback);
    }

    @Override
    public void sendImageMessage(String imagePath, String conversationID, ZIMConversationType type,
        MessageSentCallback callback) {
        messageService.sendImageMessage(imagePath, conversationID, "", type, callback);
    }

    @Override
    public void sendGroupImageMessage(String imagePath, String conversationID, String title, ZIMConversationType type,
        MessageSentCallback callback) {
        messageService.sendImageMessage(imagePath, conversationID, title, type, callback);
    }

    @Override
    public void sendAudioMessage(String audioPath, long duration, String conversationID, ZIMConversationType type,
        MessageSentCallback callback) {
        messageService.sendAudioMessage(audioPath, duration, conversationID, "", type, callback);
    }

    @Override
    public void sendGroupAudioMessage(String audioPath, long duration, String conversationID, String title,
        ZIMConversationType type, MessageSentCallback callback) {
        messageService.sendAudioMessage(audioPath, duration, conversationID, title, type, callback);
    }

    @Override
    public void sendVideoMessage(String videoPath, long duration, String conversationID, ZIMConversationType type,
        MessageSentCallback callback) {
        messageService.sendVideoMessage(videoPath, duration, conversationID, "", type, callback);
    }

    @Override
    public void sendGroupVideoMessage(String videoPath, long duration, String conversationID, String title,
        ZIMConversationType type, MessageSentCallback callback) {
        messageService.sendVideoMessage(videoPath, duration, conversationID, title, type, callback);
    }

    @Override
    public void sendFileMessage(String filePath, String conversationID, ZIMConversationType type,
        MessageSentCallback callback) {
        messageService.sendFileMessage(filePath, conversationID, "", type, callback);
    }

    @Override
    public void sendGroupFileMessage(String filePath, String conversationID, String title, ZIMConversationType type,
        MessageSentCallback callback) {
        messageService.sendFileMessage(filePath, conversationID, title, type, callback);
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
        queryGroupMemberInfo(itemModel.getSenderUserID(), itemModel.getConversationID(),
            new QueryGroupMemberInfoCallback() {
                @Override
                public void onQueryGroupMemberInfo(ZIMKitGroupMemberInfo member, ZIMError error) {
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

    @Override
    public void renewToken(String token, RenewTokenCallback callback) {
        ZegoSignalingPlugin.getInstance().renewToken(token, callback);
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

    public ZIMKitConversation getZIMKitConversation(String conversationID) {
        ZIMKitConversation conversation = null;
        if (conversations != null && !conversations.isEmpty()) {
            Iterator<ZIMKitConversation> iterator = conversations.iterator();
            while (iterator.hasNext()) {
                ZIMKitConversation element = iterator.next();
                if (element.getId().equals(conversationID)) {
                    conversation = element;
                    break;
                }
            }
        }
        return conversation;
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

    public void onGroupMemberStateChanged(ZIMGroupMemberState state, ZIMGroupMemberEvent event,
        ArrayList<ZIMGroupMemberInfo> userList, ZIMGroupOperatedInfo operatedInfo, String groupID) {

        List<ZIMKitGroupMemberInfo> memberList = getOrNewGroupMemberList(groupID);
        if (state == ZIMGroupMemberState.ENTER) {
            if (event == ZIMGroupMemberEvent.JOINED || event == ZIMGroupMemberEvent.INVITED) {
                for (ZIMGroupMemberInfo info : userList) {
                    ZIMKitGroupMemberInfo member = GroupService.transGroupMember(info);
                    memberList.add(member);
                }
            }
        } else if (state == ZIMGroupMemberState.QUIT) {
            if (event == ZIMGroupMemberEvent.KICKED_OUT || event == ZIMGroupMemberEvent.LEFT) {
                for (ZIMGroupMemberInfo info : userList) {
                    final Iterator<ZIMKitGroupMemberInfo> iterator = memberList.iterator();
                    while (iterator.hasNext()) {
                        ZIMKitGroupMemberInfo next = iterator.next();
                        if (next.getId().equals(info.userID)) {
                            iterator.remove();
                        }
                    }
                }
            }
        }
    }

    public void withDrawMessage(ZIMKitMessageModel model, ZIMMessageRevokedCallback callback) {
        messageService.withDrawMessage(model, callback);
    }

    public void queryUserInfo(ArrayList<String> userIDs, ZIMUsersInfoQueryConfig config,
        ZIMUsersInfoQueriedCallback zimUsersInfoQueriedCallback) {
        ZegoSignalingPlugin.getInstance().queryUserInfo(userIDs, config, zimUsersInfoQueriedCallback);
    }

    public ZIMUserFullInfo getMemoryUserInfo(String userID) {
        return ZegoSignalingPlugin.getInstance().getMemoryUserInfo(userID);
    }

    public void replyMessage(ZIMMessage message, ZIMMessage repliedMessage, ZIMMessageSendConfig config,
        ZIMMessageSentFullCallback callback) {
        ZegoSignalingPlugin.getInstance().replyMessage(message, repliedMessage, config, callback);
    }

    public void addMessageReaction(String reactionType, ZIMMessage message, ZIMMessageReactionAddedCallback callback) {
        ZegoSignalingPlugin.getInstance().addMessageReaction(reactionType, message, callback);
    }

    public void deleteMessageReaction(String reactionType, ZIMMessage message,
        ZIMMessageReactionDeletedCallback callback) {
        ZegoSignalingPlugin.getInstance().deleteMessageReaction(reactionType, message, callback);
    }


    public void setForwardMessages(ZIMKitForwardType forwardType, List<ZIMKitMessageModel> zimKitMessageModels) {
        forwardMessages.clear();
        this.forwardType = forwardType;
        forwardMessages.addAll(zimKitMessageModels);
    }

    public void clearForwardMessages() {
        if (forwardMessages != null) {
            forwardMessages.clear();
        }
        forwardType = null;
    }

    public List<ZIMKitMessageModel> getForwardMessages() {
        return forwardMessages;
    }

    public ZIMKitForwardType getForwardType() {
        return forwardType;
    }

    public void sendMessage(ZIMMessage message, String toConversationID, ZIMConversationType conversationType,
        ZIMMessageSendConfig config, ZIMMessageSentCallback callback) {
        ZegoSignalingPlugin.getInstance()
            .sendMessage(message, toConversationID, conversationType, config, new ZIMMessageSentCallback() {
                @Override
                public void onMessageAttached(ZIMMessage message) {
                    ZIMKitMessage zimKitMessage = ZIMMessageUtil.parseZIMMessageToKitMessage(message);
                    ZIMKitCore.getInstance().getMessageList().add(zimKitMessage);
                    ZIMKitCore.getInstance().getZimkitNotifyList().notifyAllListener(zimKitDelegate -> {
                        zimKitDelegate.onMessageSentStatusChanged(zimKitMessage);
                    });
                    if (callback != null) {
                        callback.onMessageAttached(message);
                    }
                }

                @Override
                public void onMessageSent(ZIMMessage message, ZIMError errorInfo) {
                    ZIMKitMessage zimKitMessage = ZIMMessageUtil.parseZIMMessageToKitMessage(message);
                    ArrayList<ZIMKitMessage> mMessageList = ZIMKitCore.getInstance().getMessageList();
                    for (int i = 0; i < mMessageList.size(); i++) {
                        if (message.getMessageID() == mMessageList.get(i).zim.getMessageID()) {
                            mMessageList.set(i, zimKitMessage);
                            break;
                        }
                    }

                    ZIMKitCore.getInstance().getZimkitNotifyList().notifyAllListener(zimKitDelegate -> {
                        zimKitDelegate.onMessageSentStatusChanged(zimKitMessage);
                    });

                    if (callback != null) {
                        callback.onMessageSent(message, errorInfo);
                    }
                }
            });
    }

    public void sendMediaMessage(ZIMMediaMessage message, String toConversationID, ZIMConversationType conversationType,
        ZIMMessageSendConfig config, ZIMMediaMessageSentCallback callback) {
        ZegoSignalingPlugin.getInstance()
            .sendMediaMessage(message, toConversationID, conversationType, config, callback);
    }

    public void queryCombineMessageDetail(ZIMCombineMessage message, ZIMCombineMessageDetailQueriedCallback callback) {
        ZegoSignalingPlugin.getInstance().queryCombineMessageDetail(message, callback);
    }

    public boolean isSendMessageByServer() {
        ZIMKitConfig zimKitConfig = ZIMKitCore.getInstance().getZimKitConfig();
        if (zimKitConfig != null && zimKitConfig.advancedConfig != null) {
            if (zimKitConfig.advancedConfig.containsKey(ZIMKitAdvancedKey.send_message_by_server)) {
                String content = zimKitConfig.advancedConfig.get(ZIMKitAdvancedKey.send_message_by_server);
                return ("true".equalsIgnoreCase(content));
            }
        }
        return false;
    }

    public boolean isShowLoadingWhenSend() {
        ZIMKitConfig zimKitConfig = ZIMKitCore.getInstance().getZimKitConfig();
        if (zimKitConfig != null && zimKitConfig.advancedConfig != null) {
            if (zimKitConfig.advancedConfig.containsKey(ZIMKitAdvancedKey.showLoadingWhenSend)) {
                String content = zimKitConfig.advancedConfig.get(ZIMKitAdvancedKey.showLoadingWhenSend);
                return ("true".equalsIgnoreCase(content));
            }
        }
        return false;
    }
}
