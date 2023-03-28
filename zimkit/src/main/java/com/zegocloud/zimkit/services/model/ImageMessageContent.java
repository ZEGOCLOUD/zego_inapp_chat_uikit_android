package com.zegocloud.zimkit.services.model;

public class ImageMessageContent {

    public String fileLocalPath;
    public String fileDownloadUrl;
    public String fileUID;
    public String fileName;
    public long fileSize;

    public String thumbnailDownloadUrl;
    public String thumbnailLocalPath;
    public String largeImageDownloadUrl;
    public String largeImageLocalPath;

    public int originalImageWidth = 0;
    public int originalImageHeight = 0;
    public int largeImageWidth = 0;
    public int largeImageHeight = 0;
    public int thumbnailWidth = 0;
    public int thumbnailHeight = 0;

    public MediaTransferProgress uploadProgress;
    public MediaTransferProgress downloadProgress;

}
