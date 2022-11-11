package im.zego.zimkitmessages.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;

import java.util.ArrayList;
import java.util.List;

import im.zego.zim.callback.ZIMMediaMessageSentCallback;
import im.zego.zim.callback.ZIMMessageDeletedCallback;
import im.zego.zim.callback.ZIMMessageQueriedCallback;
import im.zego.zim.callback.ZIMMessageSentCallback;
import im.zego.zim.entity.ZIMError;
import im.zego.zim.entity.ZIMMediaMessage;
import im.zego.zim.entity.ZIMMessage;
import im.zego.zim.entity.ZIMMessageDeleteConfig;
import im.zego.zim.entity.ZIMMessageQueryConfig;
import im.zego.zim.enums.ZIMConversationType;
import im.zego.zim.enums.ZIMErrorCode;
import im.zego.zimkitcommon.ZIMKitManager;
import im.zego.zimkitmessages.R;
import im.zego.zimkitmessages.ZIMKitMessageManager;
import im.zego.zimkitmessages.model.message.SystemMessageModel;
import im.zego.zimkitmessages.model.message.ZIMKitMessageModel;
import im.zego.zimkitmessages.utils.ChatMessageParser;

public abstract class ZIMKitMessageVM extends AndroidViewModel {
    protected String mtoId = "";// toGroupId、toUserId
    public ArrayList<ZIMKitMessageModel> mMessageList = new ArrayList<>();
    public final static int QUERY_HISTORY_MESSAGE_COUNT = 30; //default  100
    private OnReceiveMessageListener mReceiveMessageListener;

    public ZIMKitMessageVM(@NonNull Application application) {
        super(application);
    }

    protected void postList(List<ZIMKitMessageModel> newList, int state) {
        if (mReceiveMessageListener != null) {
            mReceiveMessageListener.onSuccess(new LoadData(state, newList));
        } else {
            ZIMKitMessageManager.share().sendMessage(new LoadData(state, newList).data);
        }
    }

    public void setId(String id) {
        this.mtoId = id;
    }

    abstract public void queryHistoryMessage();

    protected void queryHistoryMessageInner(@Nullable ZIMMessage message, ZIMConversationType type) {
        ZIMMessageQueryConfig queryConfig = new ZIMMessageQueryConfig();
        queryConfig.count = QUERY_HISTORY_MESSAGE_COUNT;
        queryConfig.nextMessage = message;
        queryConfig.reverse = true;
        ZIMKitManager.share().zim().queryHistoryMessage(mtoId, type, queryConfig, new ZIMMessageQueriedCallback() {
            @Override
            public void onMessageQueried(String conversationID, ZIMConversationType conversationType, ArrayList<ZIMMessage> messageList, ZIMError errorInfo) {
                if (errorInfo.code == ZIMErrorCode.SUCCESS) {
                    handlerHistoryMessageList(messageList, message == null ? LoadData.DATA_STATE_HISTORY_FIRST : LoadData.DATA_STATE_HISTORY_NEXT);
                } else {
                    if (mReceiveMessageListener != null) {
                        mReceiveMessageListener.onFail(errorInfo);
                    }
                }
            }
        });
    }

    public void clearUnreadCount(ZIMConversationType type) {
        ZIMKitManager.share().zim().clearConversationUnreadMessageCount(mtoId, type, null);
    }

    abstract protected void handlerHistoryMessageList(ArrayList<ZIMMessage> messageList, int state);

    public void loadNextPage() {
        loadNextPage(mMessageList == null || mMessageList.size() <= 0 ? null : mMessageList.get(0).getMessage());
    }

    abstract protected void loadNextPage(ZIMMessage message);

    abstract public void send(ZIMKitMessageModel model);

    abstract public void sendMediaMessage(List<ZIMKitMessageModel> messageModelList);

    abstract public void sendMediaMessage(ZIMKitMessageModel messageModel);

    /**
     * Send text message callback
     */
    protected final ZIMMessageSentCallback sentCallback = new ZIMMessageSentCallback() {
        @Override
        public void onMessageAttached(ZIMMessage message) {
        }

        @Override
        public void onMessageSent(ZIMMessage message, ZIMError errorInfo) {
            ArrayList<ZIMKitMessageModel> models = new ArrayList<>();
            ZIMKitMessageModel itemModel = ChatMessageParser.parseMessage(message);
            setNickNameAndAvatar(itemModel, ZIMKitManager.share().getUserInfo().getUserName(), ZIMKitManager.share().getUserInfo().getUserAvatarUrl());
            mMessageList.add(itemModel);
            models.add(itemModel);
            if (errorInfo.code == ZIMErrorCode.TARGET_DOES_NOT_EXIST) {
                SystemMessageModel errorItemMode = new SystemMessageModel();
                errorItemMode.setContent(getApplication().getString(R.string.message_user_not_exit_please_again, mtoId));
                mMessageList.add(errorItemMode);
                models.add(errorItemMode);
            }
            postList(models, LoadData.DATA_STATE_NEW_UPDATE);
            if (errorInfo.code != ZIMErrorCode.SUCCESS && errorInfo.code != ZIMErrorCode.TARGET_DOES_NOT_EXIST) {
                if (mReceiveMessageListener != null) {
                    mReceiveMessageListener.onFail(errorInfo);
                }
            }
        }
    };

    /**
     * Send rich media message callbacks
     */
    protected final ZIMMediaMessageSentCallback sentMediaCallback = new ZIMMediaMessageSentCallback() {

        @Override
        public void onMessageAttached(ZIMMediaMessage message) {
        }

        @Override
        public void onMessageSent(ZIMMediaMessage message, ZIMError errorInfo) {
            ArrayList<ZIMKitMessageModel> models = new ArrayList<>();
            ZIMKitMessageModel itemModel = ChatMessageParser.parseMessage(message);
            setNickNameAndAvatar(itemModel, ZIMKitManager.share().getUserInfo().getUserName(), ZIMKitManager.share().getUserInfo().getUserAvatarUrl());
            mMessageList.add(itemModel);
            models.add(itemModel);
            if (errorInfo.code == ZIMErrorCode.TARGET_DOES_NOT_EXIST) {
                SystemMessageModel errorItemMode = new SystemMessageModel();
                errorItemMode.setContent(getApplication().getString(R.string.message_user_not_exit_please_again, mtoId));
                mMessageList.add(errorItemMode);
                models.add(errorItemMode);
            }
            postList(models, LoadData.DATA_STATE_NEW_UPDATE);
            if (errorInfo.code != ZIMErrorCode.SUCCESS && errorInfo.code != ZIMErrorCode.TARGET_DOES_NOT_EXIST) {
                if (mReceiveMessageListener != null) {
                    mReceiveMessageListener.onFail(errorInfo);
                }
            }
        }

        @Override
        public void onMediaUploadingProgress(ZIMMediaMessage message, long currentFileSize, long totalFileSize) {

        }
    };

    /**
     * Delete Message
     * @param messageList
     * @param conversationType
     * @param callback
     */
    public void deleteMessage(List<ZIMMessage> messageList, ZIMConversationType conversationType, ZIMMessageDeletedCallback callback) {
        ZIMKitManager.share().zim().deleteMessages(messageList, mtoId, conversationType, new ZIMMessageDeleteConfig(), new ZIMMessageDeletedCallback() {
            @Override
            public void onMessageDeleted(String conversationID, ZIMConversationType conversationType, ZIMError errorInfo) {
                if (callback != null) {
                    callback.onMessageDeleted(conversationID, conversationType, errorInfo);
                }
            }
        });
    }


    public void setReceiveMessageListener(OnReceiveMessageListener listener) {
        mReceiveMessageListener = listener;
    }

    abstract protected void setNickNameAndAvatar(ZIMKitMessageModel model, String nickName, String avatar);

    public interface OnReceiveMessageListener {
        void onSuccess(LoadData data);

        void onFail(ZIMError error);
    }

    public static class LoadData {
        public final static int DATA_STATE_HISTORY_NEXT = 0;
        public final static int DATA_STATE_HISTORY_FIRST = 1;
        public final static int DATA_STATE_NEW = 2;
        public final static int DATA_STATE_NEW_UPDATE = 3;

        public int state;
        public List<ZIMKitMessageModel> data;

        public LoadData(int state, List<ZIMKitMessageModel> data) {
            this.state = state;
            this.data = data;
        }
    }
}
