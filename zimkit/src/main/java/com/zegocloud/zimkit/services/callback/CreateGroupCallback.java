package com.zegocloud.zimkit.services.callback;

import com.zegocloud.zimkit.services.model.ZIMKitGroupInfo;
import java.util.ArrayList;

import im.zego.zim.entity.ZIMError;
import im.zego.zim.entity.ZIMErrorUserInfo;

public interface CreateGroupCallback {

    void onCreateGroup(ZIMKitGroupInfo groupInfo, ArrayList<ZIMErrorUserInfo> inviteUserErrors, ZIMError error);

}
