package com.zegocloud.zimkit.components.message.model;

import android.text.TextUtils;

import androidx.databinding.Bindable;

import com.zegocloud.zimkit.components.message.utils.image.ImageSizeUtils;
import com.zegocloud.zimkit.components.message.utils.image.ImageSizeUtils.ImageSize;
import com.zegocloud.zimkit.services.model.MediaTransferProgress;
import im.zego.zim.entity.ZIMMessage;
import im.zego.zim.entity.ZIMVideoMessage;
import im.zego.zim.enums.ZIMMessageSentStatus;

public class VideoMessageModel extends ZIMKitMessageModel {

    private String fileDownloadUrl;
    private String fileLocalPath;
    //Video Length ms
    private long videoDuration;
    //Video first frame picture
    private String videoFirstFrameDownloadUrl;
    private String videoFirstFrameLocalPath;
    private String fileName;
    //The container width of the video display is obtained by calculating
    private int imgWidth;
    //The container height of the video display is obtained by calculating
    private int imgHeight;

    private MediaTransferProgress uploadProgress;
    private MediaTransferProgress downloadProgress;

    @Override
    public void onProcessMessage(ZIMMessage message) {
        if (message instanceof ZIMVideoMessage) {
            ZIMVideoMessage videoMessage = (ZIMVideoMessage) message;
            this.fileName = videoMessage.getFileName();
            this.videoDuration = videoMessage.getVideoDuration();
            this.fileDownloadUrl = videoMessage.getFileDownloadUrl();
            this.fileLocalPath = videoMessage.getFileLocalPath();

            boolean sentStatus = videoMessage.getSentStatus() == ZIMMessageSentStatus.FAILED || getSentStatus() == ZIMMessageSentStatus.SENDING;
            this.videoFirstFrameDownloadUrl = sentStatus ? videoMessage.getVideoFirstFrameLocalPath() : videoMessage.getVideoFirstFrameDownloadUrl();

            int imageWidth = videoMessage.getVideoFirstFrameWidth();
            int imageHeight = videoMessage.getVideoFirstFrameHeight();
            ImageSize imageSize = ImageSizeUtils.getImageConSize(imageWidth, imageHeight);
            this.imgWidth = imageSize.imgConWidth;
            this.imgHeight = imageSize.imgConHeight;
        }
    }

    @Bindable
    public long getVideoDuration() {
        return videoDuration;
    }

    public void setVideoDuration(long videoDuration) {
        this.videoDuration = videoDuration;
    }

    @Bindable
    public String getVideoFirstFrameDownloadUrl() {
        return !TextUtils.isEmpty(videoFirstFrameDownloadUrl) ? videoFirstFrameDownloadUrl : videoFirstFrameLocalPath;
    }

    public void setVideoFirstFrameDownloadUrl(String videoFirstFrameDownloadUrl) {
        this.videoFirstFrameDownloadUrl = videoFirstFrameDownloadUrl;
    }

    @Bindable
    public int getImgWidth() {
        return imgWidth;
    }

    public void setImgWidth(int imgWidth) {
        this.imgWidth = imgWidth;
    }

    @Bindable
    public int getImgHeight() {
        return imgHeight;
    }

    public void setImgHeight(int imgHeight) {
        this.imgHeight = imgHeight;
    }

    @Bindable
    public String getFileLocalPath() {
        return fileLocalPath;
    }

    public void setFileLocalPath(String fileLocalPath) {
        this.fileLocalPath = fileLocalPath;
    }

    @Bindable
    public String getFileDownloadUrl() {
        return fileDownloadUrl;
    }

    public void setFileDownloadUrl(String fileDownloadUrl) {
        this.fileDownloadUrl = fileDownloadUrl;
    }

    @Bindable
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Bindable
    public MediaTransferProgress getUploadProgress() {
        return uploadProgress;
    }

    public void setUploadProgress(MediaTransferProgress uploadProgress) {
        this.uploadProgress = uploadProgress;
    }

    @Bindable
    public MediaTransferProgress getDownloadProgress() {
        return downloadProgress;
    }

    public void setDownloadProgress(MediaTransferProgress downloadProgress) {
        this.downloadProgress = downloadProgress;
    }
}
