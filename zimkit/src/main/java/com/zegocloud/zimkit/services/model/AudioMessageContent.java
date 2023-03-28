package com.zegocloud.zimkit.services.model;

public class AudioMessageContent {

    public String fileLocalPath;
    public String fileDownloadUrl;
    public String fileUID;
    public String fileName;
    public long fileSize;

    public long duration;
    public MediaTransferProgress uploadProgress;
    public MediaTransferProgress downloadProgress;

}
