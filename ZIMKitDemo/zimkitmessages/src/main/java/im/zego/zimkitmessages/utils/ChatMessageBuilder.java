package im.zego.zimkitmessages.utils;

import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;

import java.io.File;

import im.zego.zim.entity.ZIMAudioMessage;
import im.zego.zim.entity.ZIMFileMessage;
import im.zego.zim.entity.ZIMImageMessage;
import im.zego.zim.entity.ZIMMessage;
import im.zego.zim.entity.ZIMTextMessage;
import im.zego.zim.entity.ZIMVideoMessage;
import im.zego.zimkitcommon.ZIMKitManager;
import im.zego.zimkitcommon.utils.ZIMKitToastUtils;
import im.zego.zimkitcommon.utils.ZIMKitFileUtils;
import im.zego.zimkitcommon.utils.ZLog;
import im.zego.zimkitmessages.R;
import im.zego.zimkitmessages.model.message.AudioMessageModel;
import im.zego.zimkitmessages.model.message.FileMessageModel;
import im.zego.zimkitmessages.model.message.ImageMessageModel;
import im.zego.zimkitmessages.model.message.TextMessageModel;
import im.zego.zimkitmessages.model.message.VideoMessageModel;
import im.zego.zimkitmessages.model.message.ZIMKitMessageModel;
import im.zego.zimkitmessages.utils.image.ImageSizeUtils;

public class ChatMessageBuilder {

    /**
     * Create a text message
     *
     * @param message Text Content
     * @return
     */
    public static ZIMKitMessageModel buildTextMessage(String message) {
        ZIMMessage messageText = new ZIMTextMessage(message);
        TextMessageModel textMessageModel = new TextMessageModel();
        textMessageModel.setCommonAttribute(messageText);
        textMessageModel.onProcessMessage(messageText);
        setNickNameAndAvatar(textMessageModel);
        return textMessageModel;
    }

    /**
     * Create picture messages
     *
     * @param imagePath Image path
     * @return
     */
    public static ZIMKitMessageModel buildImageMessage(String imagePath) {
        ZIMImageMessage message = new ZIMImageMessage(imagePath);
        ImageMessageModel messageModel = new ImageMessageModel();
        messageModel.setCommonAttribute(message);
        messageModel.onProcessMessage(message);
        messageModel.setFileLocalPath(imagePath);

        int[] size = ImageSizeUtils.getImageSize(imagePath);

        ImageSizeUtils.ImageSize imageSize = ImageSizeUtils.getImageConSize(size[0], size[1]);
        messageModel.setImgWidth(imageSize.imgConWidth);
        messageModel.setImgHeight(imageSize.imgConHeight);
        setNickNameAndAvatar(messageModel);
        return messageModel;
    }

    /**
     * Creating audio messages
     *
     * @param recordPath Audio Path
     * @param duration   Audio Duration
     * @return
     */
    public static ZIMKitMessageModel buildAudioMessage(String recordPath, int duration) {
        ZIMAudioMessage message = new ZIMAudioMessage(recordPath, duration / 1000);
        AudioMessageModel messageModel = new AudioMessageModel();
        messageModel.setCommonAttribute(message);
        messageModel.onProcessMessage(message);

        messageModel.setFileLocalPath(recordPath);
        messageModel.setAudioDuration(duration / 1000);
        setNickNameAndAvatar(messageModel);
        return messageModel;
    }

    /**
     * Create video messages
     *
     * @param mUri
     * @return
     */
    public static ZIMKitMessageModel buildVideoMessage(String mUri) {
        android.media.MediaMetadataRetriever mmr = new android.media.MediaMetadataRetriever();
        try {
            mmr.setDataSource(mUri);
            //Duration (milliseconds)
            String sDuration = mmr.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION);
            //
            Bitmap bitmap = mmr.getFrameAtTime(0, android.media.MediaMetadataRetriever.OPTION_NEXT_SYNC);//?????????

            if (bitmap == null) {
                return null;
            }

            String imgPath = ZIMKitFileUtils.saveBitmap("Image", bitmap);
            String videoPath = mUri;
            int imgWidth = bitmap.getWidth();
            int imgHeight = bitmap.getHeight();
            long duration = Long.valueOf(sDuration);
            ZIMKitMessageModel msg = buildVideoMessage(imgPath, videoPath, imgWidth, imgHeight, duration);

            return msg;
        } catch (Exception ex) {
        } finally {
            mmr.release();
        }

        return null;
    }

    /**
     * Create video messages
     *
     * @param imgPath
     * @param videoPath
     * @param width
     * @param height
     * @param duration
     * @return
     */
    public static ZIMKitMessageModel buildVideoMessage(String imgPath, String videoPath, int width, int height, long duration) {
        ZIMVideoMessage message = new ZIMVideoMessage(videoPath, (int) duration / 1000);
        VideoMessageModel messageModel = new VideoMessageModel();
        messageModel.setCommonAttribute(message);
        messageModel.onProcessMessage(message);
        messageModel.setVideoFirstFrameDownloadUrl(imgPath);
        messageModel.setFileLocalPath(videoPath);

        ImageSizeUtils.ImageSize imageSize = ImageSizeUtils.getImageConSize(width, height);
        messageModel.setImgWidth(imageSize.imgConWidth);
        messageModel.setImgHeight(imageSize.imgConHeight);
        setNickNameAndAvatar(messageModel);
        return messageModel;
    }

    /**
     * Create file message
     *
     * @param fileUri File path
     * @return
     */
    public static ZIMKitMessageModel buildFileMessage(Uri fileUri) {
        String filePath = ZIMKitFileUtils.getPathFromUri(fileUri);
        if (TextUtils.isEmpty(filePath)) {
            return null;
        }
        File file = new File(filePath);
        if (file.exists()) {
            if (file.length() == 0) {
                ZIMKitToastUtils.showToast(R.string.message_file_empty_error_tips);
                return null;
            }
            ZIMFileMessage message = new ZIMFileMessage(filePath);
            FileMessageModel messageModel = new FileMessageModel();
            messageModel.setCommonAttribute(message);
            messageModel.onProcessMessage(message);
            messageModel.setFileName(file.getName());
            messageModel.setFileLocalPath(filePath);
            messageModel.setFileSize(file.length());
            setNickNameAndAvatar(messageModel);
            return messageModel;
        }
        return null;
    }

    /**
     * Set user avatar and nickname
     *
     * @param model
     */
    public static void setNickNameAndAvatar(ZIMKitMessageModel model) {
        model.setNickName(ZIMKitManager.share().getUserInfo().getUserName());
        model.setAvatar(ZIMKitManager.share().getUserInfo().getUserAvatarUrl());
    }

}
