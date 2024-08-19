package com.zegocloud.zimkit.services.callback;

import com.zegocloud.zimkit.components.group.bean.ZIMKitGroupMemberInfo;
import im.zego.zim.entity.ZIMError;
import java.util.ArrayList;

public interface QueryGroupMemberListCallback {

    void onGroupMemberListQueried(String groupID, ArrayList<ZIMKitGroupMemberInfo> userList, int nextFlag,
        ZIMError errorInfo);
}