package com.zegocloud.zimkit.services.model;

public class VideoMessageContent {

    public String fileLocalPath;
    public String fileDownloadUrl;
    public String fileUID;
    public String fileName;
    public long fileSize;

    public long duration;
    public String firstFrameDownloadUrl;
    public String firstFrameLocalPath;

    public int videoFirstFrameWidth;
    public int videoFirstFrameHeight;

    public MediaTransferProgress uploadProgress;
    public MediaTransferProgress downloadProgress;

}
