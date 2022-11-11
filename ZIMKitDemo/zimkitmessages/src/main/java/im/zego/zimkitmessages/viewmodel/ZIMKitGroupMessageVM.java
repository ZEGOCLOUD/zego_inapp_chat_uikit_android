package im.zego.zimkitmessages.viewmodel;

import android.app.Application;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import im.zego.zim.entity.ZIMAudioMessage;
import im.zego.zim.entity.ZIMFileMessage;
import im.zego.zim.entity.ZIMGroupMemberInfo;
import im.zego.zim.entity.ZIMGroupMemberQueryConfig;
import im.zego.zim.entity.ZIMImageMessage;
import im.zego.zim.entity.ZIMMessage;
import im.zego.zim.entity.ZIMMessageSendConfig;
import im.zego.zim.entity.ZIMVideoMessage;
import im.zego.zim.enums.ZIMConversationType;
import im.zego.zim.enums.ZIMErrorCode;
import im.zego.zim.enums.ZIMGroupMemberState;
import im.zego.zimkitcommon.ZIMKitConstant;
import im.zego.zimkitcommon.ZIMKitManager;
import im.zego.zimkitcommon.event.IZIMKitEventCallBack;
import im.zego.zimkitcommon.event.ZIMKitEventHandler;
import im.zego.zimkitcommon.model.UserInfo;
import im.zego.zimkitmessages.model.message.AudioMessageModel;
import im.zego.zimkitmessages.model.message.FileMessageModel;
import im.zego.zimkitmessages.model.message.ImageMessageModel;
import im.zego.zimkitmessages.model.message.TextMessageModel;
import im.zego.zimkitmessages.model.message.VideoMessageModel;
import im.zego.zimkitmessages.model.message.ZIMKitMessageModel;
import im.zego.zimkitmessages.utils.ChatMessageParser;
import im.zego.zimkitmessages.utils.SortMessageComparator;

public class ZIMKitGroupMessageVM extends ZIMKitMessageVM {
    private final Map<String, String> mGroupUserInfoNameMap = new HashMap<>();
    private final Map<String, String> mGroupUserInfoAvatarMap = new HashMap<>();

    private final IZIMKitEventCallBack eventCallBack = (key, event) -> {
        if (key.equals(ZIMKitConstant.EventConstant.KEY_RECEIVE_GROUP_MESSAGE)) {
            String fromGroupId = (String) event.get(ZIMKitConstant.EventConstant.PARAM_FROM_GROUP_ID);
            if (!mtoId.equals(fromGroupId)) {
                return;
            }
            ArrayList<ZIMMessage> messageList = (ArrayList<ZIMMessage>) event.get(ZIMKitConstant.EventConstant.PARAM_MESSAGE_LIST);
            if (messageList != null && !messageList.isEmpty()) {
                clearUnreadCount(ZIMConversationType.GROUP);
                if (messageList.size() > 1) {
                    Collections.sort(messageList, new SortMessageComparator());
                }
                handlerNewMessageList(messageList);
            }
        } else if (key.equals(ZIMKitConstant.EventConstant.KEY_GROUP_MEMBER_STATE_CHANGED)) {
            String groupId = (String) event.get(ZIMKitConstant.EventConstant.PARAM_GROUP_ID);
            ArrayList<ZIMGroupMemberInfo> userList = (ArrayList<ZIMGroupMemberInfo>) event.get(ZIMKitConstant.EventConstant.PARAM_USER_LIST);
            if (!mtoId.equals(groupId) || userList == null) {
                return;
            }
            ZIMGroupMemberState memberState = (ZIMGroupMemberState) event.get(ZIMKitConstant.EventConstant.PARAM_STATE);
            if (memberState == ZIMGroupMemberState.ENTER) {
                for (ZIMGroupMemberInfo info : userList) {
                    mGroupUserInfoNameMap.put(info.userID, info.userName);
                    mGroupUserInfoAvatarMap.put(info.userID, info.memberAvatarUrl);
                }
            } else if (memberState == ZIMGroupMemberState.QUIT) {
                for (ZIMGroupMemberInfo info : userList) {
                    mGroupUserInfoNameMap.remove(info.userID);
                    mGroupUserInfoAvatarMap.remove(info.userID);
                }
            }
        }
    };

    public ZIMKitGroupMessageVM(@NonNull Application application) {
        super(application);
        ZIMKitEventHandler.share().addEventListener(ZIMKitConstant.EventConstant.KEY_RECEIVE_GROUP_MESSAGE, this, eventCallBack);
        ZIMKitEventHandler.share().addEventListener(ZIMKitConstant.EventConstant.KEY_GROUP_MEMBER_STATE_CHANGED, this, eventCallBack);
        UserInfo userInfo = ZIMKitManager.share().getUserInfo();
        if (userInfo != null) {
            mGroupUserInfoNameMap.put(userInfo.getUserID(), userInfo.getUserName());
            mGroupUserInfoAvatarMap.put(userInfo.getUserID(), userInfo.getUserAvatarUrl());
        }
    }

    @Override
    public void setId(String id) {
        super.setId(id);
        queryGroupMemberList(0);
    }

    @Override
    public void queryHistoryMessage() {
        queryHistoryMessageInner(null, ZIMConversationType.GROUP);
    }

    private void queryGroupMemberList(int nextFlag) {
        ZIMGroupMemberQueryConfig queryConfig = new ZIMGroupMemberQueryConfig();
        queryConfig.count = 100;
        queryConfig.nextFlag = nextFlag;
        ZIMKitManager.share().zim().queryGroupMemberList(mtoId, queryConfig, (groupID, userList, nextFlag1, errorInfo) -> {
            if (errorInfo.code == ZIMErrorCode.SUCCESS) {
                for (ZIMGroupMemberInfo info : userList) {
                    mGroupUserInfoNameMap.put(info.userID, info.userName);
                    mGroupUserInfoAvatarMap.put(info.userID, info.memberAvatarUrl);
                }
                if (userList.size() == 100) {
                    queryGroupMemberList(nextFlag1);
                } else {
                    checkCanPostGroupHistoryList();
                }
            }
        });
    }

    private void checkCanPostGroupHistoryList() {
        if (!mMessageList.isEmpty()) {
            for (ZIMKitMessageModel itemModel : mMessageList) {
                if (itemModel.getMessage() != null) {
                    String nickName = mGroupUserInfoNameMap.get(itemModel.getMessage().getSenderUserID());
                    String avatar = mGroupUserInfoAvatarMap.get(itemModel.getMessage().getSenderUserID());
                    if (!TextUtils.isEmpty(nickName)) {
                        setNickNameAndAvatar(itemModel, nickName, avatar);
                    } else {
                        setNickNameAndAvatar(itemModel, itemModel.getMessage().getSenderUserID(), avatar);
                    }
                }
            }
            postList(mMessageList, LoadData.DATA_STATE_HISTORY_FIRST);
        }
    }

    @Override
    protected void handlerHistoryMessageList(ArrayList<ZIMMessage> messageList, int state) {
        ArrayList<ZIMKitMessageModel> models = new ArrayList<>();
        for (ZIMMessage zimMessage : messageList) {
            ZIMKitMessageModel itemModel = ChatMessageParser.parseMessage(zimMessage);
            String nickName = mGroupUserInfoNameMap.get(zimMessage.getSenderUserID());
            String avatar = mGroupUserInfoAvatarMap.get(zimMessage.getSenderUserID());
            if (!TextUtils.isEmpty(nickName)) {
                setNickNameAndAvatar(itemModel, nickName, avatar);
            } else {
                setNickNameAndAvatar(itemModel, zimMessage.getSenderUserID(), avatar);
            }
            models.add(itemModel);
        }
        if (state == LoadData.DATA_STATE_HISTORY_NEXT) {
            mMessageList.addAll(0, models);
        } else {
            mMessageList.addAll(models);
        }
        postList(models, state);
    }

    @Override
    public void loadNextPage(ZIMMessage message) {
        queryHistoryMessageInner(message, ZIMConversationType.GROUP);
    }

    @Override
    protected void setNickNameAndAvatar(ZIMKitMessageModel model, String nickName, String avatar) {
        model.setNickName(nickName);
        model.setAvatar(avatar);
    }

    private void handlerNewMessageList(ArrayList<ZIMMessage> messageList) {
        ArrayList<ZIMKitMessageModel> models = new ArrayList<>();
        for (ZIMMessage message : messageList) {
            ZIMKitMessageModel itemModel = ChatMessageParser.parseMessage(message);
            String nickName = mGroupUserInfoNameMap.get(message.getSenderUserID());
            String avatar = mGroupUserInfoAvatarMap.get(message.getSenderUserID());
            if (!TextUtils.isEmpty(nickName)) {
                setNickNameAndAvatar(itemModel, nickName, avatar);
            } else {
                setNickNameAndAvatar(itemModel, message.getSenderUserID(), avatar);
            }
            models.add(itemModel);
        }
        postList(models, LoadData.DATA_STATE_NEW);
    }

    @Override
    public void send(ZIMKitMessageModel model) {
        if (model instanceof TextMessageModel) {
            TextMessageModel textMessageModel = (TextMessageModel) model;
            ZIMKitManager.share().zim().sendMessage(textMessageModel.getMessage(), mtoId, ZIMConversationType.GROUP, new ZIMMessageSendConfig(), sentCallback);
        }
    }

    /**
     * Send rich media messages
     *
     * @param messageModelList
     */
    @Override
    public void sendMediaMessage(List<ZIMKitMessageModel> messageModelList) {
        for (ZIMKitMessageModel model : messageModelList) {
            sendMediaMessage(model);
        }
    }

    @Override
    public void sendMediaMessage(ZIMKitMessageModel messageModel) {
        if (messageModel instanceof ImageMessageModel) {
            ImageMessageModel imageMessageModel = (ImageMessageModel) messageModel;
            ZIMKitManager.share().zim().sendMediaMessage((ZIMImageMessage) imageMessageModel.getMessage(), mtoId, ZIMConversationType.GROUP, new ZIMMessageSendConfig(), sentMediaCallback);
        } else if (messageModel instanceof VideoMessageModel) {
            VideoMessageModel videoMessageModel = (VideoMessageModel) messageModel;
            ZIMKitManager.share().zim().sendMediaMessage((ZIMVideoMessage) videoMessageModel.getMessage(), mtoId, ZIMConversationType.GROUP, new ZIMMessageSendConfig(), sentMediaCallback);
        } else if (messageModel instanceof AudioMessageModel) {
            AudioMessageModel audioMessageModel = (AudioMessageModel) messageModel;
            ZIMKitManager.share().zim().sendMediaMessage((ZIMAudioMessage) audioMessageModel.getMessage(), mtoId, ZIMConversationType.GROUP, new ZIMMessageSendConfig(), sentMediaCallback);
        } else if (messageModel instanceof FileMessageModel) {
            FileMessageModel fileMessageModel = (FileMessageModel) messageModel;
            ZIMKitManager.share().zim().sendMediaMessage((ZIMFileMessage) fileMessageModel.getMessage(), mtoId, ZIMConversationType.GROUP, new ZIMMessageSendConfig(), sentMediaCallback);
        }
    }

    @Override
    protected void onCleared() {
        ZIMKitEventHandler.share().removeEventListener(ZIMKitConstant.EventConstant.KEY_RECEIVE_GROUP_MESSAGE, this);
        ZIMKitEventHandler.share().removeEventListener(ZIMKitConstant.EventConstant.KEY_GROUP_MEMBER_STATE_CHANGED, this);
    }
}
