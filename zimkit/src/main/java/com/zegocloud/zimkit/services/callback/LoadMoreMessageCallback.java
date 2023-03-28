package com.zegocloud.zimkit.services.callback;

import im.zego.zim.entity.ZIMError;

public interface LoadMoreMessageCallback {
    void onLoadMoreMessage(ZIMError error);
}
