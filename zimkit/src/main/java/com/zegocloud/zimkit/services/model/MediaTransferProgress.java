package com.zegocloud.zimkit.services.model;

public class MediaTransferProgress {

    public long currentSize;
    public long totalSize;

    public MediaTransferProgress(long currentSize,long totalSize){
        this.currentSize = currentSize;
        this.totalSize = totalSize;
    }

}
