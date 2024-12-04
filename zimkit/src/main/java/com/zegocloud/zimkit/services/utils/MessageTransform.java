package com.zegocloud.zimkit.services.utils;

import com.zegocloud.zimkit.services.model.MediaTransferProgress;
import com.zegocloud.zimkit.services.model.ZIMKitMessage;
import im.zego.zim.entity.ZIMAudioMessage;
import im.zego.zim.entity.ZIMCustomMessage;
import im.zego.zim.entity.ZIMFileMessage;
import im.zego.zim.entity.ZIMImageMessage;
import im.zego.zim.entity.ZIMMessage;
import im.zego.zim.entity.ZIMVideoMessage;
import java.util.ArrayList;

public class MessageTransform {

    private static final String TAG = "MessageTransform";

    public static ZIMKitMessage updateUploadProgress(ZIMKitMessage zimKitMessage, long currentFileSize,
        long totalFileSize) {
        if (zimKitMessage == null) {
            return null;
        }

        ZIMMessage zimMessage = zimKitMessage.zim;

        switch (zimKitMessage.type) {
            case IMAGE:
                if (zimMessage instanceof ZIMImageMessage) {
                    MediaTransferProgress progress = new MediaTransferProgress(currentFileSize, totalFileSize);
                    zimKitMessage.imageContent.uploadProgress = progress;
                }
                break;
            case VIDEO:
                if (zimMessage instanceof ZIMVideoMessage) {
                    MediaTransferProgress progress = new MediaTransferProgress(currentFileSize, totalFileSize);
                    zimKitMessage.videoContent.uploadProgress = progress;
                }
                break;
            case AUDIO:
                if (zimMessage instanceof ZIMAudioMessage) {
                    MediaTransferProgress progress = new MediaTransferProgress(currentFileSize, totalFileSize);
                    zimKitMessage.audioContent.uploadProgress = progress;
                }
                break;
            case FILE:
                if (zimMessage instanceof ZIMFileMessage) {
                    MediaTransferProgress progress = new MediaTransferProgress(currentFileSize, totalFileSize);
                    zimKitMessage.fileContent.uploadProgress = progress;
                }
                break;
        }

        return zimKitMessage;

    }

    public static ZIMKitMessage updateDownloadProgress(ZIMKitMessage zimKitMessage, long currentFileSize,
        long totalFileSize) {
        if (zimKitMessage == null) {
            return null;
        }

        ZIMMessage zimMessage = zimKitMessage.zim;

        switch (zimKitMessage.type) {
            case IMAGE:
                if (zimMessage instanceof ZIMImageMessage) {
                    MediaTransferProgress progress = new MediaTransferProgress(currentFileSize, totalFileSize);
                    zimKitMessage.imageContent.downloadProgress = progress;
                }
                break;
            case VIDEO:
                if (zimMessage instanceof ZIMVideoMessage) {
                    MediaTransferProgress progress = new MediaTransferProgress(currentFileSize, totalFileSize);
                    zimKitMessage.videoContent.downloadProgress = progress;
                }
                break;
            case AUDIO:
                if (zimMessage instanceof ZIMAudioMessage) {
                    MediaTransferProgress progress = new MediaTransferProgress(currentFileSize, totalFileSize);
                    zimKitMessage.audioContent.downloadProgress = progress;
                }
                break;
            case FILE:
                if (zimMessage instanceof ZIMFileMessage) {
                    MediaTransferProgress progress = new MediaTransferProgress(currentFileSize, totalFileSize);
                    zimKitMessage.fileContent.downloadProgress = progress;
                }
                break;
        }

        return zimKitMessage;

    }

    public static ArrayList<ZIMKitMessage> parseMessageList(ArrayList<ZIMMessage> zimMessageList) {
        if (zimMessageList == null) {
            return null;
        }
        ArrayList<ZIMKitMessage> messageList = new ArrayList<>();
        for (int i = 0; i < zimMessageList.size(); i++) {
            ZIMMessage timMessage = zimMessageList.get(i);
            ZIMKitMessage message = ZIMMessageUtil.parseZIMMessageToKitMessage(timMessage);
            if (message != null) {
                messageList.add(message);
            }
        }
        return messageList;
    }

    public static ArrayList<ZIMMessage> transformMessageListToZIM(ArrayList<ZIMKitMessage> messages) {
        if (messages == null) {
            return null;
        }
        ArrayList<ZIMMessage> messageList = new ArrayList<>();
        for (ZIMKitMessage zimKitMessage : messages) {
            messageList.add(zimKitMessage.zim);
        }
        return messageList;
    }

}
