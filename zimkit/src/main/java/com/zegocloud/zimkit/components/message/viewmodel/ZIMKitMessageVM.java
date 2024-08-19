package com.zegocloud.zimkit.components.message.viewmodel;

import android.app.Application;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import com.zegocloud.zimkit.R;
import com.zegocloud.zimkit.components.message.ZIMKitMessageManager;
import com.zegocloud.zimkit.components.message.model.ImageMessageModel;
import com.zegocloud.zimkit.components.message.model.RevokeMessageModel;
import com.zegocloud.zimkit.components.message.model.SystemMessageModel;
import com.zegocloud.zimkit.components.message.model.VideoMessageModel;
import com.zegocloud.zimkit.components.message.model.ZIMKitMessageModel;
import com.zegocloud.zimkit.components.message.utils.ChatMessageParser;
import com.zegocloud.zimkit.components.message.utils.SortZIMKitMessageComparator;
import com.zegocloud.zimkit.components.message.utils.image.ImageSizeUtils;
import com.zegocloud.zimkit.components.message.utils.image.ImageSizeUtils.ImageSize;
import com.zegocloud.zimkit.services.ZIMKit;
import com.zegocloud.zimkit.services.ZIMKitDelegate;
import com.zegocloud.zimkit.services.callback.DeleteMessageCallback;
import com.zegocloud.zimkit.services.callback.GetMessageListCallback;
import com.zegocloud.zimkit.services.callback.LoadMoreMessageCallback;
import com.zegocloud.zimkit.services.internal.ZIMKitCore;
import com.zegocloud.zimkit.services.model.ZIMKitMessage;
import com.zegocloud.zimkit.services.utils.MessageTransform;
import im.zego.zim.callback.ZIMMessageDeletedCallback;
import im.zego.zim.callback.ZIMMessageRevokedCallback;
import im.zego.zim.entity.ZIMError;
import im.zego.zim.entity.ZIMMessage;
import im.zego.zim.enums.ZIMConversationType;
import im.zego.zim.enums.ZIMErrorCode;
import im.zego.zim.enums.ZIMMessageSentStatus;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class ZIMKitMessageVM extends AndroidViewModel {

    protected String mtoId = "";// toGroupId、toUserId
    public ArrayList<ZIMKitMessageModel> mMessageList = new ArrayList<>();
    public final static int QUERY_HISTORY_MESSAGE_COUNT = 30; //default  100
    private OnReceiveMessageListener mReceiveMessageListener;
    private MessageRevokeListener messageRevokeListener;

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

        private static final String TAG = "ZIMKitMessageVM";

        @Override
        public void onMessageRevokeReceived(ArrayList<ZIMKitMessage> messageList) {
            if (messageRevokeListener != null) {
                messageRevokeListener.onMessageRevokeReceived(messageList);
            }
        }

        @Override
        public void onMessageSentStatusChanged(ZIMKitMessage message) {
            ArrayList<ZIMKitMessageModel> models = new ArrayList<>();
            ZIMKitMessageModel itemModel = ChatMessageParser.parseMessage(message.zim);
            if (itemModel instanceof RevokeMessageModel) {
                return;
            }
            setNickNameAndAvatar(itemModel, ZIMKit.getLocalUser().getName(), ZIMKit.getLocalUser().getAvatarUrl());
            boolean isSending = itemModel.getSentStatus() == ZIMMessageSentStatus.SENDING;
            if (!isSending) {
                mMessageList.add(itemModel);
            }
            if (isSending) {
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

    abstract public void sendTextMessage(ZIMKitMessageModel model);

    abstract public void sendMediaMessage(List<ZIMKitMessageModel> messageModelList);

    abstract public void sendMediaMessage(ZIMKitMessageModel messageModel);

    /**
     * Delete Message
     *
     * @param messageList
     * @param conversationType
     * @param callback
     */
    public void deleteMessage(ArrayList<ZIMMessage> messageList, ZIMConversationType conversationType,
        ZIMMessageDeletedCallback callback) {
        ZIMKit.deleteMessage(MessageTransform.parseMessageList(messageList), new DeleteMessageCallback() {
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

    public void setMessageRevokeListener(MessageRevokeListener messageRevokeListener) {
        this.messageRevokeListener = messageRevokeListener;
    }

    abstract protected void setNickNameAndAvatar(ZIMKitMessageModel model, String nickName, String avatar);

    public void withDrawMessage(ZIMKitMessageModel model, ZIMMessageRevokedCallback callback) {
        ZIMKitCore.getInstance().withDrawMessage(model, callback);
    }

    public interface MessageRevokeListener {

        void onMessageRevokeReceived(ArrayList<ZIMKitMessage> messageList);
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
