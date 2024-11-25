package com.zegocloud.zimkit.components.message.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import com.zegocloud.zimkit.components.message.model.AudioMessageModel;
import com.zegocloud.zimkit.components.message.model.FileMessageModel;
import com.zegocloud.zimkit.components.message.model.ImageMessageModel;
import com.zegocloud.zimkit.components.message.model.TextMessageModel;
import com.zegocloud.zimkit.components.message.model.VideoMessageModel;
import com.zegocloud.zimkit.components.message.model.ZIMKitMessageModel;
import com.zegocloud.zimkit.components.message.utils.ChatMessageParser;
import com.zegocloud.zimkit.services.ZIMKit;
import com.zegocloud.zimkit.services.callback.MessageSentCallback;
import com.zegocloud.zimkit.services.model.ZIMKitMessage;
import im.zego.zim.entity.ZIMError;
import im.zego.zim.entity.ZIMMessage;
import im.zego.zim.enums.ZIMConversationType;
import im.zego.zim.enums.ZIMMessageDirection;
import java.util.ArrayList;
import java.util.List;

public class ZIMKitSingleMessageVM extends ZIMKitMessageVM {

    private String mSingleOtherSideUserName;
    private String mSingleOtherSideUserAvatar;

    public ZIMKitSingleMessageVM(@NonNull Application application) {
        super(application);
    }

    public void setSingleOtherSideUserName(String userName) {
        this.mSingleOtherSideUserName = userName;
    }

    public void setSingleOtherSideUserAvatar(String userAvatar) {
        this.mSingleOtherSideUserAvatar = userAvatar;
    }

    public void updateHistoryMessage(String userName, String userAvatar) {
        if (!mMessageList.isEmpty()) {
            for (ZIMKitMessageModel itemModel : mMessageList) {
                if (itemModel.getMessage() != null) {
                    if (itemModel.getMessage().getDirection() == ZIMMessageDirection.RECEIVE) {
                        setNickNameAndAvatar(itemModel, userName, userAvatar);
                    } else {
                        setNickNameAndAvatar(itemModel, ZIMKit.getLocalUser().getName(), ZIMKit.getLocalUser().getAvatarUrl());
                    }
                }
            }
            postList(mMessageList, LoadData.DATA_STATE_UPDATE_AVATAR);
        }
    }

    @Override
    public void queryHistoryMessage() {
        queryHistoryMessageInner(null, ZIMConversationType.PEER);
    }

    @Override
    protected void handlerHistoryMessageList(ArrayList<ZIMKitMessage> messages, int state) {
        ArrayList<ZIMKitMessageModel> models = new ArrayList<>();
        for (ZIMKitMessage zimMessage : messages) {
            ZIMKitMessageModel itemModel = ChatMessageParser.parseMessage(zimMessage.zim);
            if (zimMessage.zim.getDirection() == ZIMMessageDirection.RECEIVE) {
                setNickNameAndAvatar(itemModel, mSingleOtherSideUserName, mSingleOtherSideUserAvatar);
            } else {
                setNickNameAndAvatar(itemModel, ZIMKit.getLocalUser().getName(), ZIMKit.getLocalUser().getAvatarUrl());
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
        queryHistoryMessageInner(message, ZIMConversationType.PEER);
    }

    @Override
    protected void setNickNameAndAvatar(ZIMKitMessageModel model, String nickName, String avatar) {
        model.setAvatar(avatar);
    }

    @Override
    protected void handlerNewMessageList(ArrayList<ZIMKitMessage> messageList) {
        ArrayList<ZIMKitMessageModel> models = new ArrayList<>();
        for (ZIMKitMessage zimMessage : messageList) {
            ZIMKitMessageModel itemModel = ChatMessageParser.parseMessage(zimMessage.zim);
            if (zimMessage.zim.getDirection() == ZIMMessageDirection.RECEIVE) {
                setNickNameAndAvatar(itemModel, mSingleOtherSideUserName, mSingleOtherSideUserAvatar);
            }else {
                setNickNameAndAvatar(itemModel, ZIMKit.getLocalUser().getName(), ZIMKit.getLocalUser().getAvatarUrl());
            }
            models.add(itemModel);
        }
        postList(models, LoadData.DATA_STATE_NEW);
    }

    @Override
    public void sendTextMessage(ZIMKitMessageModel model, MessageSentCallback callback) {
        if (model instanceof TextMessageModel) {
            TextMessageModel textMessageModel = (TextMessageModel) model;
            ZIMKit.sendTextMessage(textMessageModel.getContent(), mtoId, ZIMConversationType.PEER, new MessageSentCallback() {
                @Override
                public void onMessageSent(ZIMError error) {
                    targetDoesNotExist(error);
                    if (callback != null) {
                        callback.onMessageSent(error);
                    }
                }
            });
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
            ZIMKit.sendImageMessage(imageMessageModel.getFileLocalPath(), mtoId, ZIMConversationType.PEER, error -> targetDoesNotExist(error));
        } else if (messageModel instanceof VideoMessageModel) {
            VideoMessageModel videoMessageModel = (VideoMessageModel) messageModel;
            ZIMKit.sendVideoMessage(videoMessageModel.getFileLocalPath(), videoMessageModel.getVideoDuration(), mtoId, ZIMConversationType.PEER, error -> targetDoesNotExist(error));
        } else if (messageModel instanceof AudioMessageModel) {
            AudioMessageModel audioMessageModel = (AudioMessageModel) messageModel;
            ZIMKit.sendAudioMessage(audioMessageModel.getFileLocalPath(), audioMessageModel.getAudioDuration(), mtoId, ZIMConversationType.PEER, error -> targetDoesNotExist(error));
        } else if (messageModel instanceof FileMessageModel) {
            FileMessageModel fileMessageModel = (FileMessageModel) messageModel;
            ZIMKit.sendFileMessage(fileMessageModel.getFileLocalPath(), mtoId, ZIMConversationType.PEER, error -> targetDoesNotExist(error));
        }
    }

}
