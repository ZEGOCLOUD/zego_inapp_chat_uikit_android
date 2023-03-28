package com.zegocloud.zimkit.services.callback;

import java.util.ArrayList;

import im.zego.zim.entity.ZIMError;
import im.zego.zim.entity.ZIMErrorUserInfo;
import com.zegocloud.zimkit.services.model.ZIMKitGroupMember;

public interface InviteUsersToJoinGroupCallback {
    void onInviteUsersToJoinGroup(ArrayList<ZIMKitGroupMember> groupMembers, ArrayList<ZIMErrorUserInfo> inviteUserErrors, ZIMError error);
}
