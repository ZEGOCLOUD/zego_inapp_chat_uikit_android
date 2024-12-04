package com.zegocloud.zimkit.components.message.viewmodel;

import android.app.Application;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import com.zegocloud.zimkit.R;
import com.zegocloud.zimkit.components.message.ZIMKitMessageManager;
import com.zegocloud.zimkit.components.message.model.AudioMessageModel;
import com.zegocloud.zimkit.components.message.model.FileMessageModel;
import com.zegocloud.zimkit.components.message.model.ImageMessageModel;
import com.zegocloud.zimkit.components.message.model.SystemMessageModel;
import com.zegocloud.zimkit.components.message.model.VideoMessageModel;
import com.zegocloud.zimkit.components.message.model.ZIMKitMessageModel;
import com.zegocloud.zimkit.components.message.utils.SortZIMKitMessageComparator;
import com.zegocloud.zimkit.components.message.utils.image.ImageSizeUtils;
import com.zegocloud.zimkit.components.message.utils.image.ImageSizeUtils.ImageSize;
import com.zegocloud.zimkit.services.ZIMKit;
import com.zegocloud.zimkit.services.ZIMKitDelegate;
import com.zegocloud.zimkit.services.callback.DeleteMessageCallback;
import com.zegocloud.zimkit.services.callback.GetMessageListCallback;
import com.zegocloud.zimkit.services.callback.LoadMoreMessageCallback;
import com.zegocloud.zimkit.services.callback.MessageSentCallback;
import com.zegocloud.zimkit.services.internal.ZIMKitCore;
import com.zegocloud.zimkit.services.model.ZIMKitMessage;
import com.zegocloud.zimkit.services.utils.ZIMMessageUtil;
import im.zego.zim.callback.ZIMMessageDeletedCallback;
import im.zego.zim.callback.ZIMMessageRevokedCallback;
import im.zego.zim.callback.ZIMMessageSentFullCallback;
import im.zego.zim.entity.ZIMError;
import im.zego.zim.entity.ZIMMessage;
import im.zego.zim.entity.ZIMMessageReaction;
import im.zego.zim.entity.ZIMMessageSendConfig;
import im.zego.zim.enums.ZIMConversationType;
import im.zego.zim.enums.ZIMErrorCode;
import im.zego.zim.enums.ZIMMessageSentStatus;
import im.zego.zim.enums.ZIMMessageType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class ZIMKitMessageVM extends AndroidViewModel {

    protected String mtoId = "";// toGroupId、toUserId
    public ArrayList<ZIMKitMessageModel> mMessageList = new ArrayList<>();
    public final static int QUERY_HISTORY_MESSAGE_COUNT = 30; //default  100
    private OnReceiveMessageListener mReceiveMessageListener;

    public ZIMKitMessageVM(@NonNull Application application) {
        super(application);
        ZIMKit.registerZIMKitDelegate(eventCallBack);
    }

    private final ZIMKitDelegate eventCallBack = new ZIMKitDelegate() {

        @Override
        public void onHistoryMessageLoaded(String conversationID, ZIMConversationType type,
            ArrayList<ZIMKitMessage> messages) {
            if (!mtoId.equals(conversationID)) {
                return;
            }

            handlerHistoryMessageList(messages, LoadData.DATA_STATE_HISTORY_NEXT);

        }

        @Override
        public void onMessageReceived(String conversationID, ZIMConversationType type,
            ArrayList<ZIMKitMessage> messages) {
            if (!mtoId.equals(conversationID)) {
                return;
            }
            if (messages != null && !messages.isEmpty()) {
                clearUnreadCount(type);
                if (messages.size() > 1) {
                    Collections.sort(messages, new SortZIMKitMessageComparator());
                }
                handlerNewMessageList(messages);
            }
        }

        @Override
        public void onMessageRevokeReceived(String conversationID, ZIMConversationType type,
            ArrayList<ZIMKitMessage> messageList) {
            List<ZIMKitMessageModel> collect = messageList.stream()
                .map(zimKitMessage -> ZIMMessageUtil.parseZIMMessageToModel(zimKitMessage.zim))
                .collect(Collectors.toList());
            postList(collect, LoadData.DATA_STATE_NEW_UPDATE);
        }

        @Override
        public void onMediaMessageUploadingProgressUpdated(ZIMKitMessage message, boolean isFinished) {
            if (!message.zim.getConversationID().equals(mtoId)) {
                return;
            }
            ZIMKitMessageModel messageModel = ZIMMessageUtil.parseZIMMessageToModel(message.zim);
            if (message.type == ZIMMessageType.IMAGE) {
                ((ImageMessageModel) messageModel).setUploadProgress(message.imageContent.uploadProgress);
            } else if (message.type == ZIMMessageType.AUDIO) {
                ((AudioMessageModel) messageModel).setUploadProgress(message.audioContent.uploadProgress);
            } else if (message.type == ZIMMessageType.FILE) {
                ((FileMessageModel) messageModel).setUploadProgress(message.fileContent.uploadProgress);
            } else if (message.type == ZIMMessageType.VIDEO) {
                ((VideoMessageModel) messageModel).setUploadProgress(message.videoContent.uploadProgress);
            }
            // when uploading,no with and height in model.so make it
            if (messageModel.getMessage().getType() == ZIMMessageType.IMAGE) {
                ImageMessageModel imageMessageModel = (ImageMessageModel) messageModel;
                ImageSize imageSize = ImageSizeUtils.getImageConSize(message.imageContent.thumbnailWidth,
                    message.imageContent.thumbnailHeight);
                imageMessageModel.setImgWidth(imageSize.imgConWidth);
                imageMessageModel.setImgHeight(imageSize.imgConHeight);
            } else if (messageModel.getMessage().getType() == ZIMMessageType.VIDEO) {
                VideoMessageModel videoMessageModel = (VideoMessageModel) messageModel;
                ImageSizeUtils.ImageSize imageSize = ImageSizeUtils.getImageConSize(
                    message.videoContent.videoFirstFrameWidth, message.videoContent.videoFirstFrameHeight);
                videoMessageModel.setImgWidth(imageSize.imgConWidth);
                videoMessageModel.setImgHeight(imageSize.imgConHeight);
                if (TextUtils.isEmpty(((VideoMessageModel) messageModel).getVideoFirstFrameDownloadUrl())) {
                    videoMessageModel.setVideoFirstFrameDownloadUrl(message.videoContent.firstFrameLocalPath);
                }
            }
            postList(Collections.singletonList(messageModel), LoadData.DATA_STATE_NEW_UPDATE);
        }

        @Override
        public void onMediaMessageDownloadingProgressUpdated(ZIMKitMessage message, boolean isFinished) {
            if (!message.zim.getConversationID().equals(mtoId)) {
                return;
            }
            ZIMKitMessageModel messageModel = ZIMMessageUtil.parseZIMMessageToModel(message.zim);
            if (message.type == ZIMMessageType.IMAGE) {
                ((ImageMessageModel) messageModel).setDownloadProgress(message.imageContent.downloadProgress);
            } else if (message.type == ZIMMessageType.AUDIO) {
                ((AudioMessageModel) messageModel).setDownloadProgress(message.audioContent.downloadProgress);
            } else if (message.type == ZIMMessageType.FILE) {
                ((FileMessageModel) messageModel).setDownloadProgress(message.fileContent.downloadProgress);
            } else if (message.type == ZIMMessageType.VIDEO) {
                ((VideoMessageModel) messageModel).setDownloadProgress(message.videoContent.downloadProgress);
            }
            // when uploading,no with and height in model.so make it
            if (messageModel.getMessage().getType() == ZIMMessageType.IMAGE) {
                ImageMessageModel imageMessageModel = (ImageMessageModel) messageModel;
                ImageSize imageSize = ImageSizeUtils.getImageConSize(message.imageContent.thumbnailWidth,
                    message.imageContent.thumbnailHeight);
                imageMessageModel.setImgWidth(imageSize.imgConWidth);
                imageMessageModel.setImgHeight(imageSize.imgConHeight);
            } else if (messageModel.getMessage().getType() == ZIMMessageType.VIDEO) {
                VideoMessageModel videoMessageModel = (VideoMessageModel) messageModel;
                ImageSizeUtils.ImageSize imageSize = ImageSizeUtils.getImageConSize(
                    message.videoContent.videoFirstFrameWidth, message.videoContent.videoFirstFrameHeight);
                videoMessageModel.setImgWidth(imageSize.imgConWidth);
                videoMessageModel.setImgHeight(imageSize.imgConHeight);
                if (TextUtils.isEmpty(((VideoMessageModel) messageModel).getVideoFirstFrameDownloadUrl())) {
                    videoMessageModel.setVideoFirstFrameDownloadUrl(message.videoContent.firstFrameLocalPath);
                }
            }
            postList(Collections.singletonList(messageModel), LoadData.DATA_STATE_NEW_UPDATE);
        }

        @Override
        public void onMessageSentStatusChanged(ZIMKitMessage message) {
            if (!message.zim.getConversationID().equals(mtoId)) {
                return;
            }
            ArrayList<ZIMKitMessageModel> models = new ArrayList<>();
            ZIMKitMessageModel itemModel = ZIMMessageUtil.parseZIMMessageToModel(message.zim);
            setNickNameAndAvatar(itemModel, ZIMKit.getLocalUser().getName(), ZIMKit.getLocalUser().getAvatarUrl());
            boolean isSending = itemModel.getSentStatus() == ZIMMessageSentStatus.SENDING;
            if (!isSending) {
                mMessageList.add(itemModel);
            }
            if (isSending) {
                // when onmessage attached,no with and height in model.so make it
                if (itemModel instanceof ImageMessageModel) {
                    ImageMessageModel imageMessageModel = (ImageMessageModel) itemModel;
                    ImageSize imageSize = ImageSizeUtils.getImageConSize(message.imageContent.thumbnailWidth,
                        message.imageContent.thumbnailHeight);
                    imageMessageModel.setImgWidth(imageSize.imgConWidth);
                    imageMessageModel.setImgHeight(imageSize.imgConHeight);
                } else if (itemModel instanceof VideoMessageModel) {
                    VideoMessageModel videoMessageModel = (VideoMessageModel) itemModel;
                    ImageSizeUtils.ImageSize imageSize = ImageSizeUtils.getImageConSize(
                        message.videoContent.videoFirstFrameWidth, message.videoContent.videoFirstFrameHeight);
                    videoMessageModel.setImgWidth(imageSize.imgConWidth);
                    videoMessageModel.setImgHeight(imageSize.imgConHeight);
                    if (TextUtils.isEmpty(((VideoMessageModel) itemModel).getVideoFirstFrameDownloadUrl())) {
                        videoMessageModel.setVideoFirstFrameDownloadUrl(message.videoContent.firstFrameLocalPath);
                    }
                }
            }

            models.add(itemModel);
            postList(models, isSending ? LoadData.DATA_STATE_NEW : LoadData.DATA_STATE_NEW_UPDATE);
        }

        @Override
        public void onMessageRepliedInfoChanged(String conversationID, ZIMConversationType type,
            ArrayList<ZIMKitMessage> kitMessages) {
            List<ZIMKitMessageModel> collect = kitMessages.stream()
                .map(zimKitMessage -> ZIMMessageUtil.parseZIMMessageToModel(zimKitMessage.zim))
                .collect(Collectors.toList());
            postList(collect, LoadData.DATA_STATE_NEW_UPDATE);
        }

        @Override
        public void onMessageReactionsChanged(ArrayList<ZIMMessageReaction> reactions) {
            ArrayList<ZIMKitMessage> messageList = ZIMKitCore.getInstance().getMessageList();

            // key: userID ,value : List<ZIMMessageReaction>
            Map<Long, List<ZIMMessageReaction>> reactionMap = new HashMap<>();
            for (ZIMMessageReaction reaction : reactions) {
                long messageID = reaction.messageID;
                List<ZIMMessageReaction> reactionList;
                if (reactionMap.containsKey(messageID)) {
                    reactionList = reactionMap.get(messageID);
                } else {
                    reactionList = new ArrayList<>();
                    reactionMap.put(messageID, reactionList);
                }
                reactionList.add(reaction);
            }

            List<ZIMKitMessageModel> collect = messageList.stream()
                .filter(zimKitMessage -> reactionMap.containsKey(zimKitMessage.info.messageID)).map(zimKitMessage -> {
                    // merge two ZIMMessageReaction list
                    ArrayList<ZIMMessageReaction> zimReactions = zimKitMessage.zim.getReactions();
                    List<ZIMMessageReaction> newReactions = reactionMap.get(zimKitMessage.info.messageID);
                    //  key:emoji , value: ZIMMessageReaction
                    Map<String, ZIMMessageReaction> newReactionMap = new HashMap<>();
                    for (ZIMMessageReaction newReaction : newReactions) {
                        newReactionMap.put(newReaction.reactionType, newReaction);
                    }
                    Iterator<ZIMMessageReaction> iterator = zimReactions.iterator();
                    while (iterator.hasNext()) {
                        ZIMMessageReaction messageReaction = iterator.next();
                        if (newReactionMap.containsKey(messageReaction.reactionType)) {
                            ZIMMessageReaction newReaction = newReactionMap.get(messageReaction.reactionType);
                            messageReaction.userList = newReaction.userList;
                            newReactions.remove(newReaction);
                            if (messageReaction.userList.isEmpty()) {
                                iterator.remove();
                            }
                        }
                    }

                    if (!newReactions.isEmpty()) {
                        zimReactions.addAll(newReactions);
                    }
                    return ZIMMessageUtil.parseZIMMessageToModel(zimKitMessage.zim);
                }).collect(Collectors.toList());

            postList(collect, LoadData.DATA_STATE_NEW_UPDATE);
        }
    };

    protected void postList(List<ZIMKitMessageModel> newList, int state) {
        if (mReceiveMessageListener != null) {
            mReceiveMessageListener.onSuccess(new LoadData(state, newList));
        } else {
            ZIMKitMessageManager.share().sendMessage(new LoadData(state, newList).data);
        }
    }

    protected void targetDoesNotExist(ZIMError errorInfo) {

        if (errorInfo.code == ZIMErrorCode.TARGET_DOES_NOT_EXIST) {
            ArrayList<ZIMKitMessageModel> models = new ArrayList<>();
            SystemMessageModel errorItemMode = new SystemMessageModel();
            errorItemMode.setContent(getApplication().getString(R.string.zimkit_user_not_exit_please_again, mtoId));
            mMessageList.add(errorItemMode);
            models.add(errorItemMode);
            postList(models, LoadData.DATA_STATE_NEW_UPDATE);
        }

        if (errorInfo.code != ZIMErrorCode.SUCCESS && errorInfo.code != ZIMErrorCode.TARGET_DOES_NOT_EXIST) {
            if (mReceiveMessageListener != null) {
                mReceiveMessageListener.onFail(errorInfo);
            }
        }
    }

    public void setId(String id) {
        this.mtoId = id;
    }

    abstract public void queryHistoryMessage();

    protected void queryHistoryMessageInner(@Nullable ZIMMessage message, ZIMConversationType type) {
        if (message == null) {
            ZIMKit.getMessageList(mtoId, type, new GetMessageListCallback() {
                @Override
                public void onGetMessageList(ArrayList<ZIMKitMessage> messages, boolean hasMoreHistoryMessage,
                    ZIMError error) {
                    if (error.code == ZIMErrorCode.SUCCESS) {
                        handlerHistoryMessageList(messages, LoadData.DATA_STATE_HISTORY_FIRST);
                    } else {
                        if (mReceiveMessageListener != null) {
                            mReceiveMessageListener.onFail(error);
                        }
                    }
                }
            });
        } else {
            ZIMKit.loadMoreMessage(mtoId, type, new LoadMoreMessageCallback() {
                @Override
                public void onLoadMoreMessage(ZIMError error) {
                    if (error.code != ZIMErrorCode.SUCCESS) {
                        if (mReceiveMessageListener != null) {
                            mReceiveMessageListener.onFail(error);
                        }
                    }
                }
            });
        }

    }

    public void clearUnreadCount(ZIMConversationType type) {
        ZIMKit.clearUnreadCount(mtoId, type, null);
    }

    abstract protected void handlerHistoryMessageList(ArrayList<ZIMKitMessage> messages, int state);

    abstract protected void handlerNewMessageList(ArrayList<ZIMKitMessage> zimMessages);

    public void loadNextPage() {
        loadNextPage(mMessageList == null || mMessageList.size() <= 0 ? null : mMessageList.get(0).getMessage());
    }

    abstract protected void loadNextPage(ZIMMessage message);

    abstract public void sendMediaMessage(List<ZIMKitMessageModel> messageModelList);

    abstract public void sendMediaMessage(ZIMKitMessageModel messageModel);

    public void sendTextMessage(String text, String targetID, String targetName, ZIMConversationType targetType,
        MessageSentCallback callback) {
        ZIMKit.sendTextMessage(text, targetID, targetName, targetType, new MessageSentCallback() {
            @Override
            public void onMessageSent(ZIMError error) {
                targetDoesNotExist(error);
                if (callback != null) {
                    callback.onMessageSent(error);
                }
            }
        });
    }

    public void replyToMessage(ZIMMessage zimMessage, ZIMKitMessageModel repliedMessage,
        ZIMMessageSentFullCallback callback) {
        if (zimMessage == null) {
            return;
        }
        ZIMKitCore.getInstance()
            .replyMessage(zimMessage, repliedMessage.getMessage(), new ZIMMessageSendConfig(), callback);
    }

    public void insertNewMessage(ZIMKitMessage zimMessage) {
        handlerNewMessageList(new ArrayList<>(Collections.singletonList(zimMessage)));
    }

    /**
     * Delete Message
     *
     * @param messageList
     * @param conversationType
     * @param callback
     */
    public void deleteMessage(ArrayList<ZIMKitMessageModel> messageList, ZIMConversationType conversationType,
        ZIMMessageDeletedCallback callback) {
        List<ZIMKitMessage> collect = messageList.stream()
            .map(kitMessageModel -> ZIMMessageUtil.parseZIMMessageToKitMessage(kitMessageModel.getMessage()))
            .collect(Collectors.toList());
        ZIMKit.deleteMessage(collect, new DeleteMessageCallback() {
            @Override
            public void onDeleteMessage(ZIMError error) {
                if (callback != null) {
                    callback.onMessageDeleted(mtoId, conversationType, error);
                }
            }
        });
    }

    public void setReceiveMessageListener(OnReceiveMessageListener listener) {
        mReceiveMessageListener = listener;
    }

    abstract protected void setNickNameAndAvatar(ZIMKitMessageModel model, String nickName, String avatar);

    public void withDrawMessage(ZIMKitMessageModel model, ZIMMessageRevokedCallback callback) {
        ZIMKitCore.getInstance().withDrawMessage(model, callback);
    }

    public interface OnReceiveMessageListener {

        void onSuccess(LoadData data);

        void onFail(ZIMError error);
    }

    public static class LoadData {

        public final static int DATA_STATE_HISTORY_NEXT = 0;
        public final static int DATA_STATE_HISTORY_FIRST = 1;
        public final static int DATA_STATE_NEW = 2;
        public final static int DATA_STATE_NEW_UPDATE = 3;
        public final static int DATA_STATE_UPDATE_AVATAR = 4;

        public int state;
        public List<ZIMKitMessageModel> data;

        public LoadData(int state, List<ZIMKitMessageModel> data) {
            this.state = state;
            this.data = data;
        }
    }

    @Override
    protected void onCleared() {
        ZIMKit.unRegisterZIMKitDelegate(eventCallBack);
        super.onCleared();
    }
}
