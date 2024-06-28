package com.zegocloud.zimkit.components.message.viewmodel;

import android.app.Application;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.zegocloud.zimkit.common.utils.ZIMKitBackgroundTasks;
import com.zegocloud.zimkit.common.utils.ZIMKitThreadHelper;
import com.zegocloud.zimkit.services.ZIMKit;
import com.zegocloud.zimkit.services.internal.ZIMKitCore;
import com.zegocloud.zimkit.services.model.ZIMKitGroupMember;
import com.zegocloud.zimkit.services.model.ZIMKitMessage;
import com.zegocloud.zimkit.services.model.ZIMKitUser;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import im.zego.zim.entity.ZIMError;
import im.zego.zim.entity.ZIMMessage;
import im.zego.zim.enums.ZIMConversationType;
import im.zego.zim.enums.ZIMErrorCode;
import com.zegocloud.zimkit.components.message.model.AudioMessageModel;
import com.zegocloud.zimkit.components.message.model.FileMessageModel;
import com.zegocloud.zimkit.components.message.model.ImageMessageModel;
import com.zegocloud.zimkit.components.message.model.TextMessageModel;
import com.zegocloud.zimkit.components.message.model.VideoMessageModel;
import com.zegocloud.zimkit.components.message.model.ZIMKitMessageModel;
import com.zegocloud.zimkit.components.message.utils.ChatMessageParser;
import com.zegocloud.zimkit.services.callback.QueryGroupMemberInfoCallback;

public class ZIMKitGroupMessageVM extends ZIMKitMessageVM {

    private final Map<String, String> mGroupUserInfoNameMap = new HashMap<>();
    private final Map<String, String> mGroupUserInfoAvatarMap = new HashMap<>();
    private String title;

    public ZIMKitGroupMessageVM(@NonNull Application application) {
        super(application);
        ZIMKitUser userInfo = ZIMKit.getLocalUser();
        if (userInfo != null) {
            mGroupUserInfoNameMap.put(userInfo.getId(), userInfo.getName());
            mGroupUserInfoAvatarMap.put(userInfo.getId(), userInfo.getAvatarUrl());
        }
    }

    @Override
    public void setId(String id) {
        super.setId(id);
    }

    @Override
    public void queryHistoryMessage() {
        queryHistoryMessageInner(null, ZIMConversationType.GROUP);
    }

    private void setGroupMemberInfo(ZIMKitMessageModel itemModel) {
        ZIMKitThreadHelper.INST.execute(new Runnable() {
            @Override
            public void run() {
                String nickName = mGroupUserInfoNameMap.get(itemModel.getMessage().getSenderUserID());
                String avatar = mGroupUserInfoAvatarMap.get(itemModel.getMessage().getSenderUserID());
                if (!TextUtils.isEmpty(nickName)) {
                    setNickNameAndAvatar(itemModel, nickName, avatar);
                } else {
                    setNickNameAndAvatar(itemModel, itemModel.getMessage().getSenderUserID(), avatar);
                    queryGroupMemberInfo(itemModel);
                }
            }
        });
    }

    private void queryGroupMemberInfo(ZIMKitMessageModel itemModel) {
        ZIMKit.queryGroupMemberInfo(itemModel.getMessage().getSenderUserID(), mtoId, new QueryGroupMemberInfoCallback() {
            @Override
            public void onQueryGroupMemberInfo(ZIMKitGroupMember member, ZIMError error) {
                if (error.code == ZIMErrorCode.SUCCESS) {
                    mGroupUserInfoNameMap.put(member.getId(), member.getName());
                    mGroupUserInfoAvatarMap.put(member.getId(), member.getAvatarUrl());
                    setNickNameAndAvatar(itemModel, member.getName(), member.getAvatarUrl());
                    ZIMKitBackgroundTasks.getInstance().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ArrayList<ZIMKitMessageModel> models = new ArrayList<>();
                            models.add(itemModel);
                            postList(models, LoadData.DATA_STATE_UPDATE_AVATAR);
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void handlerHistoryMessageList(ArrayList<ZIMKitMessage> messageList, int state) {
        ArrayList<ZIMKitMessageModel> models = new ArrayList<>();
        for (ZIMKitMessage zimMessage : messageList) {
            ZIMKitMessageModel itemModel = ChatMessageParser.parseMessage(zimMessage.zim);
            String nickName = zimMessage.info.senderUserName;
            String avatar = zimMessage.info.senderUserAvatarUrl;
            if (!TextUtils.isEmpty(nickName)) {
                setNickNameAndAvatar(itemModel, nickName, avatar);
            } else {
                String nickNameLocal = mGroupUserInfoNameMap.get(itemModel.getMessage().getSenderUserID());
                String avatarLocal = mGroupUserInfoAvatarMap.get(itemModel.getMessage().getSenderUserID());
                if (!TextUtils.isEmpty(nickNameLocal)) {
                    setNickNameAndAvatar(itemModel, nickNameLocal, avatarLocal);
                } else {
                    setNickNameAndAvatar(itemModel, zimMessage.info.senderUserID, avatar);
                    setGroupMemberInfo(itemModel);
                }
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

    @Override
    protected void handlerNewMessageList(ArrayList<ZIMKitMessage> messageList) {
        ArrayList<ZIMKitMessageModel> models = new ArrayList<>();
        for (ZIMKitMessage message : messageList) {
            ZIMKitMessageModel itemModel = ChatMessageParser.parseMessage(message.zim);
            String nickName = message.info.senderUserName;
            String avatar = message.info.senderUserAvatarUrl;
            if (!TextUtils.isEmpty(nickName)) {
                setNickNameAndAvatar(itemModel, nickName, avatar);
            } else {
                setNickNameAndAvatar(itemModel, message.info.senderUserID, avatar);
                setGroupMemberInfo(itemModel);
            }
            models.add(itemModel);
        }
        postList(models, LoadData.DATA_STATE_NEW);
    }

    @Override
    public void send(ZIMKitMessageModel model) {
        if (model instanceof TextMessageModel) {
            TextMessageModel textMessageModel = (TextMessageModel) model;
            ZIMKit.sendGroupTextMessage(textMessageModel.getContent(), mtoId, title, ZIMConversationType.GROUP,
                    error -> targetDoesNotExist(error));
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
            ZIMKit.sendGroupImageMessage(imageMessageModel.getFileLocalPath(), mtoId,title, ZIMConversationType.GROUP, error -> targetDoesNotExist(error));
        } else if (messageModel instanceof VideoMessageModel) {
            VideoMessageModel videoMessageModel = (VideoMessageModel) messageModel;
            ZIMKit.sendGroupVideoMessage(videoMessageModel.getFileLocalPath(), videoMessageModel.getVideoDuration(), mtoId, title,ZIMConversationType.GROUP, error -> targetDoesNotExist(error));
        } else if (messageModel instanceof AudioMessageModel) {
            AudioMessageModel audioMessageModel = (AudioMessageModel) messageModel;
            ZIMKit.sendGroupAudioMessage(audioMessageModel.getFileLocalPath(), audioMessageModel.getAudioDuration(), mtoId, title,ZIMConversationType.GROUP, error -> targetDoesNotExist(error));
        } else if (messageModel instanceof FileMessageModel) {
            FileMessageModel fileMessageModel = (FileMessageModel) messageModel;
            ZIMKit.sendGroupFileMessage(fileMessageModel.getFileLocalPath(), mtoId,title, ZIMConversationType.GROUP, error -> targetDoesNotExist(error));
        }
    }

    public void setGroupTitle(String title) {
        this.title = title;
    }
}
