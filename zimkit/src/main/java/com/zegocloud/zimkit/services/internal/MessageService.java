package com.zegocloud.zimkit.services.internal;

import android.media.MediaPlayer;
import android.text.TextUtils;
import com.zegocloud.uikit.plugin.signaling.ZegoSignalingPlugin;
import com.zegocloud.zimkit.R;
import com.zegocloud.zimkit.components.message.model.ZIMKitMessageModel;
import com.zegocloud.zimkit.components.message.utils.image.HEIFImageHelper;
import com.zegocloud.zimkit.services.ZIMKitConfig;
import com.zegocloud.zimkit.services.callback.DeleteMessageCallback;
import com.zegocloud.zimkit.services.callback.DownloadMediaFileCallback;
import com.zegocloud.zimkit.services.callback.GetMessageListCallback;
import com.zegocloud.zimkit.services.callback.LoadMoreMessageCallback;
import com.zegocloud.zimkit.services.callback.MessageSentCallback;
import com.zegocloud.zimkit.services.model.ZIMKitMessage;
import com.zegocloud.zimkit.services.model.ZIMKitUser;
import com.zegocloud.zimkit.services.utils.MessageTransform;
import com.zegocloud.zimkit.services.utils.ZIMMessageUtil;
import im.zego.zim.callback.ZIMMediaDownloadedCallback;
import im.zego.zim.callback.ZIMMediaMessageSentCallback;
import im.zego.zim.callback.ZIMMessageDeletedCallback;
import im.zego.zim.callback.ZIMMessageQueriedCallback;
import im.zego.zim.callback.ZIMMessageRevokedCallback;
import im.zego.zim.callback.ZIMMessageSentCallback;
import im.zego.zim.entity.ZIMAudioMessage;
import im.zego.zim.entity.ZIMError;
import im.zego.zim.entity.ZIMFileMessage;
import im.zego.zim.entity.ZIMImageMessage;
import im.zego.zim.entity.ZIMMediaMessage;
import im.zego.zim.entity.ZIMMessage;
import im.zego.zim.entity.ZIMMessageDeleteConfig;
import im.zego.zim.entity.ZIMMessageQueryConfig;
import im.zego.zim.entity.ZIMMessageRevokeConfig;
import im.zego.zim.entity.ZIMMessageSendConfig;
import im.zego.zim.entity.ZIMPushConfig;
import im.zego.zim.entity.ZIMTextMessage;
import im.zego.zim.entity.ZIMUserFullInfo;
import im.zego.zim.entity.ZIMVideoMessage;
import im.zego.zim.enums.ZIMConversationType;
import im.zego.zim.enums.ZIMErrorCode;
import im.zego.zim.enums.ZIMMediaFileType;
import im.zego.zim.enums.ZIMMessageType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import org.json.JSONException;
import org.json.JSONObject;

public class MessageService {

    public final static int QUERY_HISTORY_MESSAGE_COUNT = 30; //default  100
    private String conversationID;
    private ZIMConversationType conversationType;

    public void getMessageList(String conversationID, ZIMConversationType type, GetMessageListCallback callback) {
        this.conversationID = conversationID;
        this.conversationType = type;
        ZIMKitCore.getInstance().getMessageList().clear();
        ZIMKitCore.getInstance().getGroupUserInfoNameMap().clear();
        ZIMKitCore.getInstance().getGroupUserInfoAvatarMap().clear();
        ZIMKitUser userInfo = ZIMKitCore.getInstance().getLocalUser();
        if (userInfo != null) {
            ZIMKitCore.getInstance().getGroupUserInfoNameMap().put(userInfo.getId(), userInfo.getName());
            ZIMKitCore.getInstance().getGroupUserInfoAvatarMap().put(userInfo.getId(), userInfo.getAvatarUrl());
        }
        loadMoreMessage(conversationID, type, false, new LoadMoreMessageCallback() {
            @Override
            public void onLoadMoreMessage(ZIMError error) {
                if (callback != null) {
                    ArrayList<ZIMKitMessage> mMessageList = ZIMKitCore.getInstance().getMessageList();
                    boolean hasMore = mMessageList.size() >= QUERY_HISTORY_MESSAGE_COUNT;
                    callback.onGetMessageList(mMessageList, hasMore, error);
                }
            }
        });
    }

    public void loadMoreMessage(String conversationID, ZIMConversationType type, boolean isCallbackListChanged,
        LoadMoreMessageCallback callback) {
        this.conversationID = conversationID;
        this.conversationType = type;
        ArrayList<ZIMKitMessage> mMessageList = ZIMKitCore.getInstance().getMessageList();
        ZIMMessage message = mMessageList == null || mMessageList.size() <= 0 ? null : mMessageList.get(0).zim;

        ZIMMessageQueryConfig queryConfig = new ZIMMessageQueryConfig();
        queryConfig.count = QUERY_HISTORY_MESSAGE_COUNT;
        queryConfig.nextMessage = message;
        queryConfig.reverse = true;

        ZegoSignalingPlugin.getInstance()
            .queryHistoryMessage(conversationID, type, queryConfig, new ZIMMessageQueriedCallback() {
                @Override
                public void onMessageQueried(String conversationID, ZIMConversationType conversationType,
                    ArrayList<ZIMMessage> messageList, ZIMError errorInfo) {
                    ZIMKitCore.getInstance().setGroupMemberInfo(messageList);
                    ArrayList<ZIMKitMessage> zimkitMessageList = MessageTransform.parseMessageList(messageList);
                    if (errorInfo.code == ZIMErrorCode.SUCCESS) {
                        ArrayList<ZIMKitMessage> messageList1 = ZIMKitCore.getInstance().getMessageList();
                        for (ZIMKitMessage zimKitMessage : zimkitMessageList) {
                            Optional<ZIMKitMessage> any = messageList1.stream().filter(
                                    model -> Objects.equals(model.zim.getMessageID(), zimKitMessage.zim.getMessageID()))
                                .findAny();
                            if (!any.isPresent()) {
                                messageList1.addAll(0, zimkitMessageList);
                            }
                        }
                    }
                    if (callback != null) {
                        callback.onLoadMoreMessage(errorInfo);
                    }

                    if (isCallbackListChanged) {
                        ZIMKitCore.getInstance().getZimkitNotifyList().notifyAllListener(zimKitDelegate -> {
                            zimKitDelegate.onHistoryMessageLoaded(conversationID, conversationType, zimkitMessageList);
                        });
                    }

                }
            });
    }

    private String convertMessageData(ZIMMessage zimMessage) {
        String content = ZIMMessageUtil.simplifyZIMMessageContent(zimMessage);
        if (TextUtils.isEmpty(content)) {
            content = ZIMKitCore.getInstance().getApplication().getString(R.string.zimkit_message_unknown);
        }
        return content;
    }

    public void sendTextMessage(String text, String conversationID, String title, ZIMConversationType type,
        MessageSentCallback callback) {
        ZIMMessage message = new ZIMTextMessage(text);
        ZIMKitMessage kitMessage = ZIMMessageUtil.parseZIMMessageToKitMessage(message);
        AtomicBoolean canSendMessage = new AtomicBoolean(true);
        ZIMKitCore.getInstance().getZimkitNotifyList().notifyAllListener(zimKitDelegate -> {
            ZIMKitMessage preSending = zimKitDelegate.onMessagePreSending(kitMessage);
            if (preSending != null) {
                canSendMessage.set(preSending.canSendMessage);
            }
        });
        if (!canSendMessage.get()) {
            return;
        }

        ZIMKitUser localUser = ZIMKitCore.getInstance().getLocalUser();
        ZIMMessageSendConfig config = new ZIMMessageSendConfig();
        ZIMKitConfig zimKitConfig = ZIMKitCore.getInstance().getZimKitConfig();
        if (zimKitConfig != null) {
            config.pushConfig = new ZIMPushConfig();
            if (type == ZIMConversationType.PEER) {
                config.pushConfig.title = localUser.getName();
            } else if (type == ZIMConversationType.GROUP) {
                config.pushConfig.title = title;
            }
            config.pushConfig.content = convertMessageData(message);
            config.pushConfig.resourcesID = zimKitConfig.resourceID;
            JSONObject jsonObject = new JSONObject();
            try {
                if (type == ZIMConversationType.PEER) {
                    jsonObject.put("conversationID", localUser.getId());
                } else if (type == ZIMConversationType.GROUP) {
                    jsonObject.put("conversationID", conversationID);
                }
                jsonObject.put("conversationType", String.valueOf(type.value()));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            config.pushConfig.payload = jsonObject.toString();
        }

        sendMessage(message, conversationID, type, config, new ZIMMessageSentCallback() {
            @Override
            public void onMessageAttached(ZIMMessage message) {
                ZIMKitMessage zimKitMessage = ZIMMessageUtil.parseZIMMessageToKitMessage(message);
                ZIMKitCore.getInstance().getMessageList().add(zimKitMessage);
                ZIMKitCore.getInstance().getZimkitNotifyList().notifyAllListener(zimKitDelegate -> {
                    zimKitDelegate.onMessageSentStatusChanged(zimKitMessage);
                });
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
                    callback.onMessageSent(errorInfo);
                }

            }
        });
    }

    public void sendImageMessage(String imagePath, String conversationID, String title, ZIMConversationType type,
        MessageSentCallback callback) {
        String path = HEIFImageHelper.isHeif(imagePath) ? HEIFImageHelper.heifToJpg(imagePath) : imagePath;
        ZIMMediaMessage message = new ZIMImageMessage(path);
        sendMediaMessage(message, conversationID, title, type, callback);
    }

    public void sendAudioMessage(String audioPath, long duration, String conversationID, String title,
        ZIMConversationType type, MessageSentCallback callback) {
        if (duration == 0) {
            duration = getDuration(audioPath);
        }
        ZIMAudioMessage message = new ZIMAudioMessage(audioPath, duration);
        sendMediaMessage(message, conversationID, title, type, callback);
    }

    public void sendVideoMessage(String videoPath, long duration, String conversationID, String title,
        ZIMConversationType type, MessageSentCallback callback) {
        if (duration == 0) {
            duration = getDuration(videoPath);
        }
        ZIMVideoMessage message = new ZIMVideoMessage(videoPath, duration);
        sendMediaMessage(message, conversationID, title, type, callback);
    }

    public void sendFileMessage(String filePath, String conversationID, String title, ZIMConversationType type,
        MessageSentCallback callback) {
        ZIMFileMessage message = new ZIMFileMessage(filePath);
        sendMediaMessage(message, conversationID, title, type, callback);
    }

    private void sendMediaMessage(ZIMMediaMessage message, String conversationID, String title,
        ZIMConversationType type, MessageSentCallback callback) {

        ZIMKitMessage kitMessage = ZIMMessageUtil.parseZIMMessageToKitMessage(message);
        AtomicBoolean canSendMessage = new AtomicBoolean(true);
        ZIMKitCore.getInstance().getZimkitNotifyList().notifyAllListener(zimKitDelegate -> {
            ZIMKitMessage preSending = zimKitDelegate.onMessagePreSending(kitMessage);
            if (preSending != null) {
                canSendMessage.set(preSending.canSendMessage);
            }
        });
        if (!canSendMessage.get()) {
            return;
        }

        ZIMKitUser localUser = ZIMKitCore.getInstance().getLocalUser();
        ZIMMessageSendConfig config = new ZIMMessageSendConfig();
        ZIMKitConfig zimKitConfig = ZIMKitCore.getInstance().getZimKitConfig();
        if (zimKitConfig != null) {
            config.pushConfig = new ZIMPushConfig();
            if (type == ZIMConversationType.PEER) {
                config.pushConfig.title = localUser.getName();
            } else if (type == ZIMConversationType.GROUP) {
                config.pushConfig.title = title;
            }
            config.pushConfig.resourcesID = zimKitConfig.resourceID;
            config.pushConfig.content = convertMessageData(message);
            JSONObject jsonObject = new JSONObject();
            try {
                if (type == ZIMConversationType.PEER) {
                    jsonObject.put("conversationID", localUser.getId());
                } else if (type == ZIMConversationType.GROUP) {
                    jsonObject.put("conversationID", conversationID);
                }
                jsonObject.put("conversationType", String.valueOf(type.value()));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            config.pushConfig.payload = jsonObject.toString();
        }

        sendMediaMessage(message, conversationID, type, config, new ZIMMediaMessageSentCallback() {
            @Override
            public void onMessageAttached(ZIMMediaMessage message) {
                ZIMKitMessage zimKitMessage = ZIMMessageUtil.parseZIMMessageToKitMessage(message);
                ZIMKitCore.getInstance().getMessageList().add(zimKitMessage);
                ZIMKitCore.getInstance().getZimkitNotifyList().notifyAllListener(zimKitDelegate -> {
                    zimKitDelegate.onMessageSentStatusChanged(zimKitMessage);
                });
            }

            @Override
            public void onMediaUploadingProgress(ZIMMediaMessage message, long currentFileSize, long totalFileSize) {
                ZIMKitMessage zimKitMessage = getZIMKitMessage(message.getMessageID());
                if (zimKitMessage == null) {
                    zimKitMessage = ZIMMessageUtil.parseZIMMessageToKitMessage(message);
                }
                boolean isFinished = currentFileSize == totalFileSize;
                ZIMKitMessage finalZimKitMessage = zimKitMessage;
                ZIMKitCore.getInstance().getZimkitNotifyList().notifyAllListener(zimKitDelegate -> {
                    zimKitDelegate.onMediaMessageUploadingProgressUpdated(
                        MessageTransform.updateUploadProgress(finalZimKitMessage, currentFileSize, totalFileSize),
                        isFinished);
                });
            }

            @Override
            public void onMessageSent(ZIMMediaMessage message, ZIMError errorInfo) {
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
                    callback.onMessageSent(errorInfo);
                }

            }
        });
    }

    public void downloadMediaFile(ZIMKitMessage zimMessage, DownloadMediaFileCallback callback) {

        if (zimMessage == null) {
            ZIMError zimError = new ZIMError();
            zimError.code = ZIMErrorCode.FAILED;
            if (callback != null) {
                callback.onDownloadMediaFile(zimError);
            }
        }

        if (zimMessage.zim instanceof ZIMMediaMessage) {
            ZegoSignalingPlugin.getInstance()
                .downloadMediaFile((ZIMMediaMessage) zimMessage.zim, ZIMMediaFileType.ORIGINAL_FILE,
                    new ZIMMediaDownloadedCallback() {
                        @Override
                        public void onMediaDownloaded(ZIMMessage message, ZIMError errorInfo) {
                            boolean isFinished = errorInfo.code == ZIMErrorCode.SUCCESS;
                            ZIMKitCore.getInstance().getZimkitNotifyList().notifyAllListener(zimKitDelegate -> {
                                zimKitDelegate.onMediaMessageDownloadingProgressUpdated(
                                    ZIMMessageUtil.parseZIMMessageToKitMessage(message), isFinished);
                            });
                            if (callback != null) {
                                callback.onDownloadMediaFile(errorInfo);
                            }
                        }

                        @Override
                        public void onMediaDownloadingProgress(ZIMMessage message, long currentFileSize,
                            long totalFileSize) {
                            ZIMKitMessage zimKitMessage = getZIMKitMessage(message.getMessageID());
                            if (zimKitMessage == null) {
                                zimKitMessage = ZIMMessageUtil.parseZIMMessageToKitMessage(message);
                            }
                            ZIMKitMessage finalZimKitMessage = zimKitMessage;
                            ZIMKitCore.getInstance().getZimkitNotifyList().notifyAllListener(zimKitDelegate -> {
                                zimKitDelegate.onMediaMessageDownloadingProgressUpdated(
                                    MessageTransform.updateDownloadProgress(finalZimKitMessage, currentFileSize,
                                        totalFileSize), false);
                            });
                        }
                    });

        } else {
            ZIMError zimError = new ZIMError();
            zimError.code = ZIMErrorCode.FAILED;
            if (callback != null) {
                callback.onDownloadMediaFile(zimError);
            }
        }
    }

    public void deleteMessage(List<ZIMKitMessage> messages, DeleteMessageCallback callback) {
        if (messages == null || messages.isEmpty()) {
            ZIMError zimError = new ZIMError();
            zimError.code = ZIMErrorCode.FAILED;
            if (callback != null) {
                callback.onDeleteMessage(zimError);
            }
            return;
        }

        ArrayList<ZIMMessage> messageList = new ArrayList<>();
        for (ZIMKitMessage zimKitMessage : messages) {
            messageList.add(zimKitMessage.zim);
            ArrayList<ZIMKitMessage> mMessageList = ZIMKitCore.getInstance().getMessageList();
            Iterator<ZIMKitMessage> it = mMessageList.iterator();
            while (it.hasNext()) {
                ZIMKitMessage model = it.next();
                if (model.zim.getMessageID() == zimKitMessage.zim.getMessageID()) {
                    it.remove();
                }
            }
        }

        ArrayList<ZIMKitMessage> messageDeleted = new ArrayList<>();
        messageDeleted.addAll(messages);
        ZIMKitCore.getInstance().getZimkitNotifyList().notifyAllListener(zimKitDelegate -> {
            zimKitDelegate.onMessageDeleted(conversationID, conversationType, messageDeleted);
        });

        ZegoSignalingPlugin.getInstance()
            .deleteMessages(messageList, conversationID, conversationType, new ZIMMessageDeleteConfig(),
                new ZIMMessageDeletedCallback() {
                    @Override
                    public void onMessageDeleted(String conversationID, ZIMConversationType conversationType,
                        ZIMError errorInfo) {
                        if (callback != null) {
                            callback.onDeleteMessage(errorInfo);
                        }
                    }
                });
    }

    private ZIMKitMessage getZIMKitMessage(long messageID) {
        ZIMKitMessage zimKitMessage = null;
        ArrayList<ZIMKitMessage> mMessageList = ZIMKitCore.getInstance().getMessageList();
        for (int i = 0; i < mMessageList.size(); i++) {
            if (messageID == mMessageList.get(i).zim.getMessageID()) {
                zimKitMessage = mMessageList.get(i);
                return zimKitMessage;
            }
        }
        return null;
    }

    private long getDuration(String filePath) {
        MediaPlayer player = new MediaPlayer();
        try {
            player.setDataSource(filePath);
            player.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }
        long duration = player.getDuration();
        player.release();
        return (int) duration / 1000;
    }

    public String getConversationID() {
        return conversationID;
    }

    public ZIMConversationType getConversationType() {
        return conversationType;
    }

    public void withDrawMessage(ZIMKitMessageModel model, ZIMMessageRevokedCallback callback) {
        ZegoSignalingPlugin.getInstance().revokeMessage(model.getMessage(), new ZIMMessageRevokeConfig(), callback);
    }

    public void sendMessage(ZIMMessage message, String toConversationID, ZIMConversationType conversationType,
        ZIMMessageSendConfig config, ZIMMessageSentCallback callback) {
        ZegoSignalingPlugin.getInstance().sendMessage(message, toConversationID, conversationType, config, callback);
    }

    public void sendMediaMessage(ZIMMediaMessage message, String toConversationID, ZIMConversationType conversationType,
        ZIMMessageSendConfig config, ZIMMediaMessageSentCallback callback) {
        ZegoSignalingPlugin.getInstance().sendMediaMessage(message, toConversationID, conversationType, config, callback);
    }
}
