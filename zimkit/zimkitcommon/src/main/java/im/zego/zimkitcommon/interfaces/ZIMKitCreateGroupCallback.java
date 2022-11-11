package im.zego.zimkitcommon.interfaces;

import java.util.ArrayList;

import im.zego.zim.entity.ZIMError;
import im.zego.zim.entity.ZIMErrorUserInfo;
import im.zego.zimkitcommon.model.ZIMKitGroupInfo;

public interface ZIMKitCreateGroupCallback {
    /**
     * Callback for the results that whether the group chat is created successfully.
     * @param groupInfo group chat info.
     * @param errorUserList user error list, indicating that a user failed to join the group chat for some reason (e.g., the user does not exist), if the list is empty, indicating that all users have joined the group chat.
     * @param errorInfo error information, which indicates whether the current method is called successfully.
     */
    void onCreateGroup(ZIMKitGroupInfo groupInfo, ArrayList<ZIMErrorUserInfo> errorUserList, ZIMError errorInfo);
}
