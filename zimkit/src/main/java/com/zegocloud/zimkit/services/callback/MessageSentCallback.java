package com.zegocloud.zimkit.services.callback;

import im.zego.zim.entity.ZIMError;

public interface MessageSentCallback {
    void onMessageSent(ZIMError error);
}
