package im.zego.zimkitcommon.interfaces;

import im.zego.zim.entity.ZIMError;

public interface ZIMKitUserAvatarUrlUpdatedCallback {
    void onUserAvatarUrlUpdated(String userAvatarUrl, ZIMError errorInfo);
}
