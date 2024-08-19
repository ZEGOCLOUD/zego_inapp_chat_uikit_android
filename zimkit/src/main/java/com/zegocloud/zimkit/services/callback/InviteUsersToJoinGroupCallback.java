package com.zegocloud.zimkit.services.callback;

import java.util.ArrayList;

import im.zego.zim.entity.ZIMError;
import im.zego.zim.entity.ZIMErrorUserInfo;
import com.zegocloud.zimkit.components.group.bean.ZIMKitGroupMemberInfo;

public interface InviteUsersToJoinGroupCallback {
    void onInviteUsersToJoinGroup(ArrayList<ZIMKitGroupMemberInfo> groupMembers, ArrayList<ZIMErrorUserInfo> inviteUserErrors, ZIMError error);
}
