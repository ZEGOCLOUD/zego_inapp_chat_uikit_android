package im.zego.zimkitcommon.interfaces;

import im.zego.zim.entity.ZIMError;
import im.zego.zimkitcommon.model.ZIMKitGroupInfo;

public interface ZIMKitJoinGroupCallback {

    /**
     * Callback for the results that whether the group chat is joined successfully.
     *
     * @param groupInfo  group chat info.
     * @param errorInfo error information.
     */
    void onJoinGroup(ZIMKitGroupInfo groupInfo, ZIMError errorInfo);

}
