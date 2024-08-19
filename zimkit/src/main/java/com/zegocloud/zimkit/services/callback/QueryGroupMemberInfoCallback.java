package com.zegocloud.zimkit.services.callback;

import com.zegocloud.zimkit.components.group.bean.ZIMKitGroupMemberInfo;
import im.zego.zim.entity.ZIMError;

public interface QueryGroupMemberInfoCallback {
    void onQueryGroupMemberInfo(ZIMKitGroupMemberInfo member, ZIMError error);
}
