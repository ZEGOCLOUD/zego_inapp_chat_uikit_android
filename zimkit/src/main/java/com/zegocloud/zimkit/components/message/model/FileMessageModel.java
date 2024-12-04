package com.zegocloud.zimkit.components.message.model;

import androidx.databinding.Bindable;

import com.zegocloud.zimkit.common.utils.ZIMKitFileUtils;
import com.zegocloud.zimkit.components.message.utils.FileIconUtils;
import com.zegocloud.zimkit.BR;
import com.zegocloud.zimkit.services.model.MediaTransferProgress;
import im.zego.zim.entity.ZIMFileMessage;
import im.zego.zim.entity.ZIMMessage;

public class FileMessageModel extends ZIMKitMessageModel {

    private String fileLocalPath;
    private String fileUID;
    private String fileDownloadUrl;
    private String fileName;
    private long fileSize;
    //Files over 10M need to be flagged and downloaded manually
    private boolean sizeLimit = false;

    private MediaTransferProgress uploadProgress;
    private MediaTransferProgress downloadProgress;

    @Override
    public void onProcessMessage(ZIMMessage message) {
        if (message instanceof ZIMFileMessage) {
            ZIMFileMessage fileMessage = (ZIMFileMessage) message;
            this.fileLocalPath = fileMessage.getFileLocalPath();
            this.fileUID = fileMessage.getFileUID();
            this.fileDownloadUrl = fileMessage.getFileDownloadUrl();
            this.fileName = fileMessage.getFileName();
            this.fileSize = fileMessage.getFileSize();
            this.sizeLimit = fileMessage.getFileSize() >= 10485760;
        }
    }

    @Bindable
    public String getFileLocalPath() {
        return fileLocalPath;
    }

    public void setFileLocalPath(String fileLocalPath) {
        this.fileLocalPath = fileLocalPath;
        notifyPropertyChanged(BR.fileLocalPath);
    }

    @Bindable
    public String getFileUID() {
        return fileUID;
    }

    public void setFileUID(String fileUID) {
        this.fileUID = fileUID;
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
    public String getFileSize() {
        return ZIMKitFileUtils.formatFileSize(fileSize);
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    @Bindable
    public int getFileIcon() {
        return FileIconUtils.queryFileIcon(fileName);
    }

    @Bindable
    public boolean isSizeLimit() {
        return sizeLimit;
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
