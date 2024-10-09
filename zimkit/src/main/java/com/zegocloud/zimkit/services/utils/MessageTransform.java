package com.zegocloud.zimkit.services.utils;

import android.graphics.Bitmap;
import android.text.TextUtils;
import com.zegocloud.zimkit.R;
import com.zegocloud.zimkit.common.utils.ZIMKitFileUtils;
import com.zegocloud.zimkit.components.message.utils.image.ImageSizeUtils;
import com.zegocloud.zimkit.services.internal.ZIMKitCore;
import com.zegocloud.zimkit.services.model.MediaTransferProgress;
import com.zegocloud.zimkit.services.model.ZIMKitMessage;
import im.zego.zim.entity.ZIMAudioMessage;
import im.zego.zim.entity.ZIMFileMessage;
import im.zego.zim.entity.ZIMImageMessage;
import im.zego.zim.entity.ZIMMessage;
import im.zego.zim.entity.ZIMRevokeMessage;
import im.zego.zim.entity.ZIMSystemMessage;
import im.zego.zim.entity.ZIMTextMessage;
import im.zego.zim.entity.ZIMTipsMessage;
import im.zego.zim.entity.ZIMVideoMessage;
import im.zego.zim.enums.ZIMMessageSentStatus;
import im.zego.zim.enums.ZIMMessageType;
import java.io.IOException;
import java.util.ArrayList;

public class MessageTransform {

    public static ZIMKitMessage parseMessage(ZIMMessage zimMessage) {
        if (zimMessage == null) {
            return null;
        }
        ZIMMessageType msgType = zimMessage.getType();
        ZIMKitMessage zimKitMessage = new ZIMKitMessage();
        zimKitMessage.zim = zimMessage;
        zimKitMessage.type = msgType;
        zimKitMessage.info.messageID = zimMessage.getMessageID();
        zimKitMessage.info.localMessageID = zimMessage.getLocalMessageID();
        zimKitMessage.info.conversationID = zimMessage.getConversationID();
        zimKitMessage.info.conversationSeq = zimMessage.getConversationSeq();
        zimKitMessage.info.conversationType = zimMessage.getConversationType();
        zimKitMessage.info.direction = zimMessage.getDirection();
        zimKitMessage.info.isUserInserted = zimMessage.isUserInserted();
        zimKitMessage.info.orderKey = zimMessage.getOrderKey();
        zimKitMessage.info.senderUserID = zimMessage.getSenderUserID();
        zimKitMessage.info.sentStatus = zimMessage.getSentStatus();
        zimKitMessage.info.timestamp = zimMessage.getTimestamp();

        String nickName = ZIMKitCore.getInstance().getGroupUserInfoNameMap().get(zimMessage.getSenderUserID());
        String avatar = ZIMKitCore.getInstance().getGroupUserInfoAvatarMap().get(zimMessage.getSenderUserID());
        if (!TextUtils.isEmpty(nickName)) {
            zimKitMessage.info.senderUserName = nickName;
        }
        if (!TextUtils.isEmpty(avatar)) {
            zimKitMessage.info.senderUserAvatarUrl = avatar;
        }

        switch (msgType) {
            case TEXT:
                if (zimMessage instanceof ZIMTextMessage) {
                    if (!TextUtils.isEmpty(((ZIMTextMessage) zimMessage).message)) {
                        zimKitMessage.textContent.content = ((ZIMTextMessage) zimMessage).message;
                    }
                }
                break;
            case IMAGE:
                if (zimMessage instanceof ZIMImageMessage) {
                    ZIMImageMessage imageMessage = (ZIMImageMessage) zimMessage;
                    zimKitMessage.imageContent.fileName = imageMessage.getFileName();
                    zimKitMessage.imageContent.fileSize = imageMessage.getFileSize();
                    zimKitMessage.imageContent.fileUID = imageMessage.getFileUID();
                    zimKitMessage.imageContent.fileDownloadUrl = imageMessage.getFileDownloadUrl();
                    zimKitMessage.imageContent.fileLocalPath = imageMessage.getFileLocalPath();
                    zimKitMessage.imageContent.thumbnailDownloadUrl = imageMessage.getThumbnailDownloadUrl();
                    zimKitMessage.imageContent.thumbnailLocalPath = imageMessage.getThumbnailLocalPath();
                    zimKitMessage.imageContent.largeImageDownloadUrl = imageMessage.getLargeImageDownloadUrl();
                    zimKitMessage.imageContent.largeImageLocalPath = imageMessage.getLargeImageLocalPath();
                    zimKitMessage.imageContent.originalImageWidth = imageMessage.getOriginalImageWidth();
                    zimKitMessage.imageContent.originalImageHeight = imageMessage.getOriginalImageHeight();
                    zimKitMessage.imageContent.largeImageWidth = imageMessage.getLargeImageWidth();
                    zimKitMessage.imageContent.largeImageHeight = imageMessage.getLargeImageHeight();

                    zimKitMessage.imageContent.thumbnailWidth = imageMessage.getThumbnailWidth();
                    zimKitMessage.imageContent.thumbnailHeight = imageMessage.getThumbnailHeight();
                    if (imageMessage.getSentStatus() == ZIMMessageSentStatus.SENDING) {
                        int[] size = ImageSizeUtils.getImageSize(imageMessage.getFileLocalPath());
                        zimKitMessage.imageContent.thumbnailWidth = size[0];
                        zimKitMessage.imageContent.thumbnailHeight = size[1];
                    }

                }
                break;
            case VIDEO:
                if (zimMessage instanceof ZIMVideoMessage) {
                    ZIMVideoMessage videoMessage = (ZIMVideoMessage) zimMessage;
                    zimKitMessage.videoContent.fileName = videoMessage.getFileName();
                    zimKitMessage.videoContent.fileSize = videoMessage.getFileSize();
                    zimKitMessage.videoContent.fileUID = videoMessage.getFileUID();
                    zimKitMessage.videoContent.fileDownloadUrl = videoMessage.getFileDownloadUrl();
                    zimKitMessage.videoContent.fileLocalPath = videoMessage.getFileLocalPath();
                    zimKitMessage.videoContent.duration = videoMessage.getVideoDuration();
                    zimKitMessage.videoContent.firstFrameDownloadUrl = videoMessage.getVideoFirstFrameDownloadUrl();
                    zimKitMessage.videoContent.firstFrameLocalPath = videoMessage.getVideoFirstFrameLocalPath();
                    zimKitMessage.videoContent.videoFirstFrameWidth = videoMessage.getVideoFirstFrameWidth();
                    zimKitMessage.videoContent.videoFirstFrameHeight = videoMessage.getVideoFirstFrameHeight();

                    if (videoMessage.getSentStatus() == ZIMMessageSentStatus.SENDING) {
                        setVideoMessage(zimKitMessage, videoMessage);
                    }
                }
                break;
            case AUDIO:
                if (zimMessage instanceof ZIMAudioMessage) {
                    ZIMAudioMessage audioMessage = (ZIMAudioMessage) zimMessage;
                    zimKitMessage.audioContent.fileName = audioMessage.getFileName();
                    zimKitMessage.audioContent.fileSize = audioMessage.getFileSize();
                    zimKitMessage.audioContent.fileUID = audioMessage.getFileUID();
                    zimKitMessage.audioContent.fileDownloadUrl = audioMessage.getFileDownloadUrl();
                    zimKitMessage.audioContent.fileLocalPath = audioMessage.getFileLocalPath();
                    zimKitMessage.audioContent.duration = audioMessage.getAudioDuration();
                }
                break;
            case FILE:
                if (zimMessage instanceof ZIMFileMessage) {
                    ZIMFileMessage fileMessage = (ZIMFileMessage) zimMessage;
                    zimKitMessage.fileContent.fileName = fileMessage.getFileName();
                    zimKitMessage.fileContent.fileSize = fileMessage.getFileSize();
                    zimKitMessage.fileContent.fileUID = fileMessage.getFileUID();
                    zimKitMessage.fileContent.fileDownloadUrl = fileMessage.getFileDownloadUrl();
                    zimKitMessage.fileContent.fileLocalPath = fileMessage.getFileLocalPath();
                }
                break;
            case SYSTEM:
                if (zimMessage instanceof ZIMSystemMessage) {
                    if (!TextUtils.isEmpty(((ZIMSystemMessage) zimMessage).message)) {
                        zimKitMessage.systemContent.content = ((ZIMSystemMessage) zimMessage).message;
                    }
                }
                break;
            case REVOKE:
                if (zimMessage instanceof ZIMRevokeMessage) {
                    ZIMRevokeMessage revokeMessage = (ZIMRevokeMessage) zimMessage;
                    zimKitMessage.revokeContent.revokeMessage = revokeMessage;
                }
                break;
            case TIPS:
                ZIMTipsMessage tipsMessage = (ZIMTipsMessage) zimMessage;
                zimKitMessage.tipsMessageContent.tipsMessage = tipsMessage;
                break;
            default:
                zimKitMessage.textContent.content = ZIMKitCore.getInstance().getApplication()
                    .getString(R.string.zimkit_message_unknown);
                break;
        }

        return zimKitMessage;
    }

    private static final String TAG = "MessageTransform";

    private static void setVideoMessage(ZIMKitMessage zimKitMessage, ZIMVideoMessage videoMessage) {
        android.media.MediaMetadataRetriever mmr = new android.media.MediaMetadataRetriever();
        try {
            mmr.setDataSource(videoMessage.getFileLocalPath());
            Bitmap bitmap = mmr.getFrameAtTime(0, android.media.MediaMetadataRetriever.OPTION_NEXT_SYNC);

            if (bitmap == null) {
                return;
            }

            String imgPath = ZIMKitFileUtils.saveBitmap("Image", bitmap);
            int imgWidth = bitmap.getWidth();
            int imgHeight = bitmap.getHeight();

            zimKitMessage.videoContent.videoFirstFrameWidth = imgWidth;
            zimKitMessage.videoContent.videoFirstFrameHeight = imgHeight;
            if (TextUtils.isEmpty(videoMessage.getVideoFirstFrameLocalPath())) {
                zimKitMessage.videoContent.firstFrameLocalPath = imgPath;
            }

        } catch (Exception ex) {
        } finally {
            try {
                mmr.release();
            } catch (IOException e) {
            }
        }
    }

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
            ZIMKitMessage message = parseMessage(timMessage);
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
