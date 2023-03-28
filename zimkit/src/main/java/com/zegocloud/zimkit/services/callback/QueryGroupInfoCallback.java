package com.zegocloud.zimkit.services.callback;

import im.zego.zim.entity.ZIMError;
import com.zegocloud.zimkit.services.model.ZIMKitGroupInfo;

public interface QueryGroupInfoCallback {
    void onQueryGroupInfo(ZIMKitGroupInfo info, ZIMError error);
}
