package com.zegocloud.zimkit.components.message.model;

import android.text.TextUtils;

import androidx.databinding.Bindable;
import androidx.lifecycle.MutableLiveData;

import com.zegocloud.zimkit.BR;
import com.zegocloud.zimkit.services.model.MediaTransferProgress;
import im.zego.zim.entity.ZIMAudioMessage;
import im.zego.zim.entity.ZIMMessage;

public class AudioMessageModel extends ZIMKitMessageModel {

    //Whether the download of the audio file is complete
    public MutableLiveData<Boolean> isDownloadComplete = new MutableLiveData<>(false);
    //Whether the audio is playing
    public MutableLiveData<Boolean> isPlaying = new MutableLiveData<>(false);

    private long audioDuration;
    private String fileLocalPath;
    private String fileDownloadUrl;

    private MediaTransferProgress uploadProgress;
    private MediaTransferProgress downloadProgress;

    @Override
    public void onProcessMessage(ZIMMessage message) {
        if (message instanceof ZIMAudioMessage) {
            ZIMAudioMessage audioMessage = (ZIMAudioMessage) message;
            this.audioDuration = audioMessage.getAudioDuration();
            this.fileLocalPath = audioMessage.getFileLocalPath();
            this.fileDownloadUrl = audioMessage.getFileDownloadUrl();
            this.isDownloadComplete.setValue(!TextUtils.isEmpty(fileLocalPath));
        }
    }

    @Bindable
    public String getShowDuration() {
        return audioDuration + "''";
    }

    @Bindable
    public long getAudioDuration() {
        return audioDuration;
    }

    public void setAudioDuration(long audioDuration) {
        this.audioDuration = audioDuration;
    }

    @Bindable
    public String getFileLocalPath() {
        return fileLocalPath;
    }

    public void setFileLocalPath(String fileLocalPath) {
        if (!TextUtils.isEmpty(fileLocalPath)) {
            this.fileLocalPath = fileLocalPath;
            isDownloadComplete.setValue(true);
            notifyPropertyChanged(BR.fileLocalPath);
        }
    }

    @Bindable
    public String getFileDownloadUrl() {
        return fileDownloadUrl;
    }

    public void setFileDownloadUrl(String fileDownloadUrl) {
        this.fileDownloadUrl = fileDownloadUrl;
    }

    public void setPlaying(boolean playing) {
        isPlaying.setValue(playing);
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
