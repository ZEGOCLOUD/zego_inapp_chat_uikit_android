package com.zegocloud.zimkit.services.callback;

import im.zego.zim.entity.ZIMError;

public interface DeleteMessageCallback {
    void onDeleteMessage(ZIMError error);
}
