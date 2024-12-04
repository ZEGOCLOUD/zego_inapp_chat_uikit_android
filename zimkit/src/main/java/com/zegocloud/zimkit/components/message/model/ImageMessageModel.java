package com.zegocloud.zimkit.components.message.model;

import androidx.databinding.Bindable;
import com.zegocloud.zimkit.components.message.utils.image.ImageSizeUtils;
import com.zegocloud.zimkit.components.message.utils.image.ImageSizeUtils.ImageSize;
import com.zegocloud.zimkit.services.model.MediaTransferProgress;
import im.zego.zim.entity.ZIMImageMessage;
import im.zego.zim.entity.ZIMMessage;

public class ImageMessageModel extends ZIMKitMessageModel {

    private String fileLocalPath;
    private String fileDownloadUrl;
    private String thumbnailDownloadUrl;
    private String largeImageDownloadUrl;
    private String fileName;
    //The container width of the image display is obtained by calculating
    private int imgWidth;
    //The height of the container displayed in the picture is obtained by calculating
    private int imgHeight;

    private MediaTransferProgress uploadProgress;
    private MediaTransferProgress downloadProgress;

    @Override
    public void onProcessMessage(ZIMMessage message) {
        if (message instanceof ZIMImageMessage) {
            ZIMImageMessage imageMessage = (ZIMImageMessage) message;
            this.fileName = imageMessage.getFileName();
            this.fileLocalPath = imageMessage.getFileLocalPath();
            this.fileDownloadUrl = imageMessage.getFileDownloadUrl();
            this.largeImageDownloadUrl = imageMessage.getLargeImageDownloadUrl();

            this.thumbnailDownloadUrl = imageMessage.getThumbnailDownloadUrl();

            int imageWidth = imageMessage.getThumbnailWidth();
            int imageHeight = imageMessage.getThumbnailHeight();
            ImageSize imageSize = ImageSizeUtils.getImageConSize(imageWidth, imageHeight);
            this.imgWidth = imageSize.imgConWidth;
            this.imgHeight = imageSize.imgConHeight;
        }
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
    public String getThumbnailDownloadUrl() {
        return thumbnailDownloadUrl;
    }

    public void setThumbnailDownloadUrl(String thumbnailDownloadUrl) {
        this.thumbnailDownloadUrl = thumbnailDownloadUrl;
    }

    @Bindable
    public String getLargeImageDownloadUrl() {
        return largeImageDownloadUrl;
    }

    public void setLargeImageDownloadUrl(String largeImageDownloadUrl) {
        this.largeImageDownloadUrl = largeImageDownloadUrl;
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
