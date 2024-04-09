package com.zegocloud.zimkit.services.internal.interfaces;

import com.zegocloud.zimkit.services.callback.ConnectUserCallback;
import com.zegocloud.zimkit.services.callback.UserAvatarUrlUpdateCallback;
import com.zegocloud.zimkit.services.model.ZIMKitUser;
import com.zegocloud.zimkit.services.callback.QueryUserCallback;

public interface IUserService {

    ZIMKitUser getLocalUser();

    void connectUser(String userID, String userName, String avatarUrl, String token, ConnectUserCallback callback);

    void disconnectUser();

    void queryUserInfo(String userID, QueryUserCallback callback);

    void updateUserAvatarUrl(String avatarUrl, UserAvatarUrlUpdateCallback callback);

}
