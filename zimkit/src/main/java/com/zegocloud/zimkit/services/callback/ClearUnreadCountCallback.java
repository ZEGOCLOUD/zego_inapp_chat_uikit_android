package com.zegocloud.zimkit.services.callback;

import im.zego.zim.entity.ZIMError;

public interface ClearUnreadCountCallback {
    void onClearUnreadCount(ZIMError error);
}
