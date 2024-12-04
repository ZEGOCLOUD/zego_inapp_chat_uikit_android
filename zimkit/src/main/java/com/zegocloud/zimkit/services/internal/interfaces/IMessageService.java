package com.zegocloud.zimkit.services.internal.interfaces;

import com.zegocloud.zimkit.services.callback.DeleteMessageCallback;
import com.zegocloud.zimkit.services.callback.DownloadMediaFileCallback;
import com.zegocloud.zimkit.services.callback.GetMessageListCallback;
import com.zegocloud.zimkit.services.callback.LoadMoreMessageCallback;
import com.zegocloud.zimkit.services.callback.MessageSentCallback;
import com.zegocloud.zimkit.services.model.ZIMKitMessage;
import java.util.List;

import im.zego.zim.enums.ZIMConversationType;

public interface IMessageService {

    void getMessageList(String conversationID, ZIMConversationType type, GetMessageListCallback callback);

    void loadMoreMessage(String conversationID, ZIMConversationType type, LoadMoreMessageCallback callback);

    void sendTextMessage(String text, String targetID, String targetName, ZIMConversationType targetType,
        MessageSentCallback callback);

    void sendImageMessage(String imagePath, String conversationID, ZIMConversationType type,
        MessageSentCallback callback);

    void sendGroupImageMessage(String imagePath, String conversationID, String title, ZIMConversationType type,
        MessageSentCallback callback);

    void sendAudioMessage(String audioPath, long duration, String conversationID, ZIMConversationType type,
        MessageSentCallback callback);

    void sendGroupAudioMessage(String audioPath, long duration, String conversationID, String title,
        ZIMConversationType type, MessageSentCallback callback);

    void sendVideoMessage(String videoPath, long duration, String conversationID, ZIMConversationType type,
        MessageSentCallback callback);

    void sendGroupVideoMessage(String videoPath, long duration, String conversationID, String title,
        ZIMConversationType type, MessageSentCallback callback);

    void sendFileMessage(String filePath, String conversationID, ZIMConversationType type,
        MessageSentCallback callback);

    void sendGroupFileMessage(String filePath, String conversationID, String title, ZIMConversationType type,
        MessageSentCallback callback);

    void downloadMediaFile(ZIMKitMessage message, DownloadMediaFileCallback callback);

    void deleteMessage(List<ZIMKitMessage> messages, DeleteMessageCallback callback);

}
