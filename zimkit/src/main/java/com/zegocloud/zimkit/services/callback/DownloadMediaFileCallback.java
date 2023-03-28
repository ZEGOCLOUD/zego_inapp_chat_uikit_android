package com.zegocloud.zimkit.services.callback;

import im.zego.zim.entity.ZIMError;

public interface DownloadMediaFileCallback {
    void onDownloadMediaFile(ZIMError error);
}
