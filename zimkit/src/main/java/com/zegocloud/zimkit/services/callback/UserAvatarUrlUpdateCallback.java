package com.zegocloud.zimkit.services.callback;

import im.zego.zim.entity.ZIMError;

public interface UserAvatarUrlUpdateCallback {
    void onUserAvatarUrlUpdate(String userAvatarUrl, ZIMError error);
}
