package com.zegocloud.zimkit.services.callback;

import com.zegocloud.zimkit.services.model.ZIMKitUser;
import im.zego.zim.entity.ZIMError;

public interface QueryUserCallback {
    void onQueryUser(ZIMKitUser userInfo, ZIMError error);
}
