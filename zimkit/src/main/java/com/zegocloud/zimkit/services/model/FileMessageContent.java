package com.zegocloud.zimkit.services.model;

public class FileMessageContent {

    public String fileLocalPath;
    public String fileDownloadUrl;
    public String fileUID;
    public String fileName;
    public long fileSize;

    public MediaTransferProgress uploadProgress;
    public MediaTransferProgress downloadProgress;

}
