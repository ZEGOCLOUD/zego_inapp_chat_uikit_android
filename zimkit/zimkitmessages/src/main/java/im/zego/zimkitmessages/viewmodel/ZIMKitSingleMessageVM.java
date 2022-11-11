package im.zego.zimkitmessages.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import im.zego.zim.entity.ZIMAudioMessage;
import im.zego.zim.entity.ZIMFileMessage;
import im.zego.zim.entity.ZIMImageMessage;
import im.zego.zim.entity.ZIMMessage;
import im.zego.zim.entity.ZIMMessageSendConfig;
import im.zego.zim.entity.ZIMVideoMessage;
import im.zego.zim.enums.ZIMConversationType;
import im.zego.zim.enums.ZIMMessageDirection;
import im.zego.zimkitcommon.ZIMKitConstant;
import im.zego.zimkitcommon.ZIMKitManager;
import im.zego.zimkitcommon.event.IZIMKitEventCallBack;
import im.zego.zimkitcommon.event.ZIMKitEventHandler;
import im.zego.zimkitmessages.model.message.AudioMessageModel;
import im.zego.zimkitmessages.model.message.FileMessageModel;
import im.zego.zimkitmessages.model.message.ImageMessageModel;
import im.zego.zimkitmessages.model.message.TextMessageModel;
import im.zego.zimkitmessages.model.message.VideoMessageModel;
import im.zego.zimkitmessages.model.message.ZIMKitMessageModel;
import im.zego.zimkitmessages.utils.ChatMessageParser;
import im.zego.zimkitmessages.utils.SortMessageComparator;

public class ZIMKitSingleMessageVM extends ZIMKitMessageVM {

    private String mSingleOtherSideUserName;
    private String mSingleOtherSideUserAvatar;

    private final IZIMKitEventCallBack eventCallBack = (key, event) -> {
        //Receiving messages
        if (key.equals(ZIMKitConstant.EventConstant.KEY_RECEIVE_PEER_MESSAGE)) {
            ArrayList<ZIMMessage> messageList = (ArrayList<ZIMMessage>) event.get(ZIMKitConstant.EventConstant.PARAM_MESSAGE_LIST);
            String fromUserId = (String) event.get(ZIMKitConstant.EventConstant.PARAM_FROM_USER_ID);
            if (fromUserId != null && fromUserId.equals(mtoId)) {
                if (messageList != null && !messageList.isEmpty()) {
                    clearUnreadCount(ZIMConversationType.PEER);
                    if (messageList.size() > 1) {
                        Collections.sort(messageList, new SortMessageComparator());
                    }
                    handlerNewMessageList(messageList);
                }
            }
        }
    };

    public ZIMKitSingleMessageVM(@NonNull Application application) {
        super(application);
        ZIMKitEventHandler.share().addEventListener(ZIMKitConstant.EventConstant.KEY_RECEIVE_PEER_MESSAGE, this, eventCallBack);
    }

    public void setSingleOtherSideUserName(String userName) {
        this.mSingleOtherSideUserName = userName;
    }

    public void setSingleOtherSideUserAvatar(String userAvatar) {
        this.mSingleOtherSideUserAvatar = userAvatar;
    }

    @Override
    public void queryHistoryMessage() {
        queryHistoryMessageInner(null, ZIMConversationType.PEER);
    }

    @Override
    protected void handlerHistoryMessageList(ArrayList<ZIMMessage> messageList, int state) {
        ArrayList<ZIMKitMessageModel> models = new ArrayList<>();
        for (ZIMMessage zimMessage : messageList) {
            ZIMKitMessageModel itemModel = ChatMessageParser.parseMessage(zimMessage);
            if (zimMessage.getDirection() == ZIMMessageDirection.RECEIVE) {
                setNickNameAndAvatar(itemModel, mSingleOtherSideUserName, mSingleOtherSideUserAvatar);
            } else {
                setNickNameAndAvatar(itemModel, ZIMKitManager.share().getUserInfo().getUserName(), ZIMKitManager.share().getUserInfo().getUserAvatarUrl());
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

    private void handlerNewMessageList(ArrayList<ZIMMessage> messageList) {
        ArrayList<ZIMKitMessageModel> models = new ArrayList<>();
        for (ZIMMessage zimMessage : messageList) {
            ZIMKitMessageModel itemModel = ChatMessageParser.parseMessage(zimMessage);
            if (zimMessage.getDirection() == ZIMMessageDirection.RECEIVE) {
                setNickNameAndAvatar(itemModel, mSingleOtherSideUserName, mSingleOtherSideUserAvatar);
            }
            models.add(itemModel);
        }
        postList(models, LoadData.DATA_STATE_NEW);
    }

    @Override
    public void send(ZIMKitMessageModel model) {
        if (model instanceof TextMessageModel) {
            TextMessageModel textMessageModel = (TextMessageModel) model;
            ZIMKitManager.share().zim().sendMessage(textMessageModel.getMessage(), mtoId, ZIMConversationType.PEER, new ZIMMessageSendConfig(), sentCallback);
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
            ZIMKitManager.share().zim().sendMediaMessage((ZIMImageMessage) imageMessageModel.getMessage(), mtoId, ZIMConversationType.PEER, new ZIMMessageSendConfig(), sentMediaCallback);
        } else if (messageModel instanceof VideoMessageModel) {
            VideoMessageModel videoMessageModel = (VideoMessageModel) messageModel;
            ZIMKitManager.share().zim().sendMediaMessage((ZIMVideoMessage) videoMessageModel.getMessage(), mtoId, ZIMConversationType.PEER, new ZIMMessageSendConfig(), sentMediaCallback);
        } else if (messageModel instanceof AudioMessageModel) {
            AudioMessageModel audioMessageModel = (AudioMessageModel) messageModel;
            ZIMKitManager.share().zim().sendMediaMessage((ZIMAudioMessage) audioMessageModel.getMessage(), mtoId, ZIMConversationType.PEER, new ZIMMessageSendConfig(), sentMediaCallback);
        } else if (messageModel instanceof FileMessageModel) {
            FileMessageModel fileMessageModel = (FileMessageModel) messageModel;
            ZIMKitManager.share().zim().sendMediaMessage((ZIMFileMessage) fileMessageModel.getMessage(), mtoId, ZIMConversationType.PEER, new ZIMMessageSendConfig(), sentMediaCallback);
        }
    }

    @Override
    protected void onCleared() {
        ZIMKitEventHandler.share().removeEventListener(ZIMKitConstant.EventConstant.KEY_RECEIVE_PEER_MESSAGE, this);
        super.onCleared();
    }
}
