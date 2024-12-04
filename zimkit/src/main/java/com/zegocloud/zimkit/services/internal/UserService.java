package com.zegocloud.zimkit.services.internal;

import android.text.TextUtils;
import com.zegocloud.uikit.plugin.signaling.ZegoSignalingPlugin;
import com.zegocloud.zimkit.services.callback.ConnectUserCallback;
import com.zegocloud.zimkit.services.callback.QueryUserCallback;
import com.zegocloud.zimkit.services.callback.UserAvatarUrlUpdateCallback;
import com.zegocloud.zimkit.services.model.ZIMKitConversation;
import com.zegocloud.zimkit.services.model.ZIMKitUser;
import im.zego.zim.callback.ZIMUsersInfoQueriedCallback;
import im.zego.zim.entity.ZIMError;
import im.zego.zim.entity.ZIMErrorUserInfo;
import im.zego.zim.entity.ZIMUserFullInfo;
import im.zego.zim.entity.ZIMUserInfo;
import im.zego.zim.entity.ZIMUsersInfoQueryConfig;
import im.zego.zim.enums.ZIMErrorCode;
import java.util.ArrayList;

public class UserService {

    private static final String TAG = "UserService";

    private ZIMKitUser userInfo = new ZIMKitUser();

    /**
     * Connects user to the ZIMKit server. This method can only be used after calling the [init] method and before you
     * calling any other methods.
     *
     * @param token
     * @param callback callback for the results that whether the connection is successful.
     */
    public synchronized void connectUser(String userID, String userName, String avatarUrl, String token,
        ConnectUserCallback callback) {

        userInfo.setId(userID);
        userInfo.setName(userName);
        userInfo.setAvatarUrl(avatarUrl);

        ZIMUserInfo mZIMUserInfo = new ZIMUserInfo();
        mZIMUserInfo.userID = userID;
        mZIMUserInfo.userName = userName;
        ZegoSignalingPlugin.getInstance().connectUser(userID, userName,
            new com.zegocloud.uikit.plugin.adapter.plugins.signaling.ConnectUserCallback() {
                @Override
                public void onResult(int errorCode, String errorMessage) {
                    if (callback != null) {
                        ZIMError zimError = new ZIMError();
                        if (errorCode == ZIMErrorCode.USER_HAS_ALREADY_LOGGED.value()) {
                            zimError.code = ZIMErrorCode.SUCCESS;
                        } else {
                            zimError.code = ZIMErrorCode.getZIMErrorCode(errorCode);
                            zimError.message = errorMessage;
                        }
                        callback.onConnectUser(zimError);
                    }
                    if ((errorCode == ZIMErrorCode.SUCCESS.value()
                        || errorCode == ZIMErrorCode.USER_HAS_ALREADY_LOGGED.value()) && !TextUtils.isEmpty(
                        avatarUrl)) {
                        updateUserAvatarUrl(avatarUrl, (userAvatarUrl, errorInfo1) -> {
                            if (ZegoSignalingPlugin.getInstance().isOtherPushEnabled()
                                || ZegoSignalingPlugin.getInstance().isFCMPushEnabled()) {
                                ZegoSignalingPlugin.getInstance().registerPush();
                            }
                        });
                    }
                }
            });
    }

    /**
     * After a successful login, you can change the user avatar as needed.
     *
     * @param avatarUrl avatar URL.
     * @param callback  callback for the results that whether the user avatar is updated successfully.
     */
    public void updateUserAvatarUrl(String avatarUrl, UserAvatarUrlUpdateCallback callback) {
        if (TextUtils.isEmpty(avatarUrl)) {
            return;
        }
        ZegoSignalingPlugin.getInstance().updateUserAvatarUrl(avatarUrl, (userAvatarUrl, errorInfo) -> {
            if (errorInfo.code == ZIMErrorCode.SUCCESS) {
                updateUserAvatar(userAvatarUrl);
                if (callback != null) {
                    callback.onUserAvatarUrlUpdate(userAvatarUrl, errorInfo);
                }
            }
        });
    }

    /**
     * Disconnects current user from ZIMKit server.
     */
    public synchronized void disconnectUser() {
        ZegoSignalingPlugin.getInstance().disconnectUser();
    }

    public void queryUserInfo(String userID, QueryUserCallback callback) {
        ArrayList<String> userIDs = new ArrayList<>();
        userIDs.add(userID);
        ZIMUsersInfoQueryConfig config = new ZIMUsersInfoQueryConfig();
        config.isQueryFromServer = true;
        ZIMKitCore.getInstance().queryUsersInfo(userIDs, config, new ZIMUsersInfoQueriedCallback() {
            @Override
            public void onUsersInfoQueried(ArrayList<ZIMUserFullInfo> userList,
                ArrayList<ZIMErrorUserInfo> errorUserList, ZIMError errorInfo) {

                if (!userList.isEmpty()) {
                    ZIMUserFullInfo userFullInfo = userList.get(0);
                    ZIMKitUser userInfo = new ZIMKitUser();
                    userInfo.setId(userFullInfo.baseInfo.userID);
                    userInfo.setName(userFullInfo.baseInfo.userName);
                    userInfo.setAvatarUrl(userFullInfo.baseInfo.userAvatarUrl);
                    if (callback != null) {
                        callback.onQueryUser(userInfo, errorInfo);
                    }

                    ZIMKitConversation kitConversation = ZIMKitCore.getInstance()
                        .getZIMKitConversation(userFullInfo.baseInfo.userID);
                    if (kitConversation != null) {
                        kitConversation.setAvatarUrl(userFullInfo.baseInfo.userAvatarUrl);
                    }
                    ZIMKitCore.getInstance().getZimkitNotifyList().notifyAllListener(zimKitDelegate -> {
                        zimKitDelegate.onConversationListChanged(
                            new ArrayList<>(ZIMKitCore.getInstance().getConversations()));
                    });
                }
            }
        });
    }

    private void updateUserAvatar(String avatarUrl) {
        if (userInfo != null && !TextUtils.isEmpty(avatarUrl)) {
            userInfo.setAvatarUrl(avatarUrl);
        }
    }

    public ZIMKitUser getUserInfo() {
        return userInfo;
    }
}
