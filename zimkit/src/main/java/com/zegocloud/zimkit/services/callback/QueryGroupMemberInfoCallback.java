package com.zegocloud.zimkit.services.callback;

import com.zegocloud.zimkit.services.model.ZIMKitGroupMember;
import im.zego.zim.entity.ZIMError;

public interface QueryGroupMemberInfoCallback {
    void onQueryGroupMemberInfo(ZIMKitGroupMember member, ZIMError error);
}
