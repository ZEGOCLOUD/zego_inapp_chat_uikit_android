package com.zegocloud.zimkit.services.utils;

import android.graphics.Bitmap;
import android.text.TextUtils;
import com.zegocloud.zimkit.R;
import com.zegocloud.zimkit.common.utils.ZIMKitFileUtils;
import com.zegocloud.zimkit.components.message.model.AudioMessageModel;
import com.zegocloud.zimkit.components.message.model.CombineMessageModel;
import com.zegocloud.zimkit.components.message.model.CustomMessageModel;
import com.zegocloud.zimkit.components.message.model.FileMessageModel;
import com.zegocloud.zimkit.components.message.model.ImageMessageModel;
import com.zegocloud.zimkit.components.message.model.RevokeMessageModel;
import com.zegocloud.zimkit.components.message.model.SystemMessageModel;
import com.zegocloud.zimkit.components.message.model.TextMessageModel;
import com.zegocloud.zimkit.components.message.model.TipsMessageModel;
import com.zegocloud.zimkit.components.message.model.VideoMessageModel;
import com.zegocloud.zimkit.components.message.model.ZIMKitMessageModel;
import com.zegocloud.zimkit.components.message.utils.image.ImageSizeUtils;
import com.zegocloud.zimkit.services.internal.ZIMKitCore;
import com.zegocloud.zimkit.services.model.ZIMKitMessage;
import im.zego.zim.entity.ZIMAudioMessage;
import im.zego.zim.entity.ZIMCombineMessage;
import im.zego.zim.entity.ZIMCustomMessage;
import im.zego.zim.entity.ZIMFileMessage;
import im.zego.zim.entity.ZIMImageMessage;
import im.zego.zim.entity.ZIMMessage;
import im.zego.zim.entity.ZIMMessageRepliedInfo;
import im.zego.zim.entity.ZIMRevokeMessage;
import im.zego.zim.entity.ZIMSystemMessage;
import im.zego.zim.entity.ZIMTextMessage;
import im.zego.zim.entity.ZIMTextMessageLiteInfo;
import im.zego.zim.entity.ZIMTipsMessage;
import im.zego.zim.entity.ZIMVideoMessage;
import im.zego.zim.enums.ZIMMessageRepliedInfoState;
import im.zego.zim.enums.ZIMMessageSentStatus;
import im.zego.zim.enums.ZIMMessageType;
import java.io.IOException;

public class ZIMMessageUtil {

    // in some case ,we need to change media message to text,like:
    // in conversation list,show latest message,
    // when reply message in input,
    // notification,
    // forward.etc.
    public static String simplifyZIMMessageContent(ZIMMessage message) {
        String content = null;
        if (message.getType() == ZIMMessageType.TEXT) {
            ZIMTextMessage textMessage = (ZIMTextMessage) message;
            content = textMessage.message.strip();
        } else if (message.getType() == ZIMMessageType.IMAGE) {
            content = ZIMKitCore.getInstance().getApplication().getString(R.string.zimkit_message_photo);
        } else if (message.getType() == ZIMMessageType.VIDEO) {
            content = ZIMKitCore.getInstance().getApplication().getString(R.string.zimkit_message_video);
        } else if (message.getType() == ZIMMessageType.AUDIO) {
            content = ZIMKitCore.getInstance().getApplication().getString(R.string.zimkit_message_audio);
        } else if (message.getType() == ZIMMessageType.FILE) {
            content = ZIMKitCore.getInstance().getApplication().getString(R.string.zimkit_message_file);
        } else if (message.getType() == ZIMMessageType.COMBINE) {
            content = ZIMKitCore.getInstance().getApplication().getString(R.string.zimkit_chat_records);
        } else if (message.getType() == ZIMMessageType.REVOKE) {
            content = ZIMKitCore.getInstance().getApplication().getString(R.string.zimkit_message_revoke);
        } else if (message.getType() == ZIMMessageType.TIPS) {
            // already contains operator name
            TipsMessageModel tipsMessageModel = (TipsMessageModel) ZIMMessageUtil.parseZIMMessageToModel(message);
            content = tipsMessageModel.getContent().toString();
        } else if (message.getType() == ZIMMessageType.CUSTOM) {
            // already contains operator name
            CustomMessageModel customMessageModel = (CustomMessageModel) ZIMMessageUtil.parseZIMMessageToModel(message);
            content = customMessageModel.getContent();
        }
        return content;
    }

    public static String simplifyZIMMessageRepliedContent(ZIMMessage zimMessage) {
        ZIMMessageRepliedInfo repliedInfo = zimMessage.getRepliedInfo();
        String content = "";
        if (repliedInfo.state == ZIMMessageRepliedInfoState.DELETED) {
            content = ZIMKitCore.getInstance().getApplication().getString(R.string.zimkit_message_reply_delete);
        } else if (repliedInfo.state == ZIMMessageRepliedInfoState.NORMAL) {
            if (repliedInfo.messageInfo.type == ZIMMessageType.TEXT) {
                ZIMTextMessageLiteInfo textMessage = (ZIMTextMessageLiteInfo) repliedInfo.messageInfo;
                content = textMessage.message.strip();
            } else if (repliedInfo.messageInfo.type == ZIMMessageType.IMAGE) {
                content = ZIMKitCore.getInstance().getApplication().getString(R.string.zimkit_message_photo);
            } else if (repliedInfo.messageInfo.type == ZIMMessageType.VIDEO) {
                content = ZIMKitCore.getInstance().getApplication().getString(R.string.zimkit_message_video);
            } else if (repliedInfo.messageInfo.type == ZIMMessageType.AUDIO) {
                content = ZIMKitCore.getInstance().getApplication().getString(R.string.zimkit_message_audio);
            } else if (repliedInfo.messageInfo.type == ZIMMessageType.FILE) {
                content = ZIMKitCore.getInstance().getApplication().getString(R.string.zimkit_message_file);
            } else if (repliedInfo.messageInfo.type == ZIMMessageType.REVOKE) {
                content = ZIMKitCore.getInstance().getApplication().getString(R.string.zimkit_message_reply_revoke);
            } else if (repliedInfo.messageInfo.type == ZIMMessageType.COMBINE) {
                content = ZIMKitCore.getInstance().getApplication().getString(R.string.zimkit_chat_records);
            }
        }

        return content;
    }

    /**
     * Convert to the corresponding message type
     *
     * @param zimMessage
     * @return
     */
    public static ZIMKitMessageModel parseZIMMessageToModel(ZIMMessage zimMessage) {
        if (zimMessage == null) {
            return null;
        }
        ZIMKitMessageModel message = null;
        ZIMMessageType msgType = zimMessage.getType();
        switch (msgType) {
            case TEXT:
                message = new TextMessageModel();
                break;
            case IMAGE:
                message = new ImageMessageModel();
                break;
            case VIDEO:
                message = new VideoMessageModel();
                break;
            case AUDIO:
                message = new AudioMessageModel();
                break;
            case FILE:
                message = new FileMessageModel();
                break;
            case SYSTEM:
                message = new SystemMessageModel();
                break;
            case REVOKE:
                message = new RevokeMessageModel();
                break;
            case TIPS:
                message = new TipsMessageModel();
                break;
            case COMBINE:
                message = new CombineMessageModel();
                break;
            case CUSTOM:
                message = new CustomMessageModel();
                break;
            default:
                message = new TextMessageModel();
                ((TextMessageModel) message).setContent(
                    ZIMKitCore.getInstance().getApplication().getString(R.string.zimkit_message_unknown));
                break;
        }

        if (message != null) {
            message.setCommonAttribute(zimMessage);
            message.onProcessMessage(zimMessage);
        }
        return message;
    }

    public static ZIMKitMessage parseZIMMessageToKitMessage(ZIMMessage zimMessage) {
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
            case COMBINE:
                ZIMCombineMessage combineMessage = (ZIMCombineMessage) zimMessage;
                zimKitMessage.combineMessageContent.message = combineMessage;
                break;
            case CUSTOM:
                ZIMCustomMessage customMessage = (ZIMCustomMessage) zimMessage;
                zimKitMessage.customMessageContent.customMessage = customMessage;
                break;
            default:
                zimKitMessage.textContent.content = ZIMKitCore.getInstance().getApplication()
                    .getString(R.string.zimkit_message_unknown);
                break;
        }

        return zimKitMessage;
    }

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
}
