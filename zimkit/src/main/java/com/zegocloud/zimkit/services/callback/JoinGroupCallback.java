package com.zegocloud.zimkit.services.callback;

import com.zegocloud.zimkit.services.model.ZIMKitGroupInfo;
import im.zego.zim.entity.ZIMError;

public interface JoinGroupCallback {

    void onJoinGroup(ZIMKitGroupInfo groupInfo, ZIMError error);

}
