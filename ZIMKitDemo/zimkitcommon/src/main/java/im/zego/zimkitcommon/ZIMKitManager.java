package im.zego.zimkitcommon;

import android.app.Application;
import android.text.TextUtils;

import java.util.List;
import java.util.Map;

import im.zego.zim.ZIM;
import im.zego.zim.entity.ZIMAppConfig;
import im.zego.zim.entity.ZIMGroupFullInfo;
import im.zego.zim.entity.ZIMGroupInfo;
import im.zego.zim.entity.ZIMUserInfo;
import im.zego.zim.enums.ZIMConnectionEvent;
import im.zego.zim.enums.ZIMConnectionState;
import im.zego.zim.enums.ZIMErrorCode;
import im.zego.zimkitcommon.event.IZIMKitEventCallBack;
import im.zego.zimkitcommon.event.ZIMKitEventHandler;
import im.zego.zimkitcommon.interfaces.ZIMKitConnectUserCallback;
import im.zego.zimkitcommon.interfaces.ZIMKitCreateGroupCallback;
import im.zego.zimkitcommon.interfaces.ZIMKitEventListener;
import im.zego.zimkitcommon.interfaces.ZIMKitJoinGroupCallback;
import im.zego.zimkitcommon.interfaces.ZIMKitUserAvatarUrlUpdatedCallback;
import im.zego.zimkitcommon.model.UserInfo;
import im.zego.zimkitcommon.model.ZIMKitGroupInfo;
import im.zego.zimkitcommon.utils.ZIMKitActivityUtils;
import im.zego.zimkitcommon.utils.ZIMKitDateUtils;
import im.zego.zimkitcommon.utils.ZLog;

public class ZIMKitManager {
    private static final String TAG = "ZIMManager";
    private ZIM mZim;
    private static ZIMKitManager sInstance;
    private Application mApplication;
    private UserInfo mUserInfo;
    private boolean kickedOutAccount = false;

    private ZIMKitManager() {
    }

    /**
     * Create a ZIMKitManager instance.
     *
     * @return
     */
    public static ZIMKitManager share() {
        if (sInstance == null) {
            synchronized (ZIMKitManager.class) {
                if (sInstance == null) {
                    sInstance = new ZIMKitManager();
                }
            }
        }
        return sInstance;
    }

    public void setContext(Application application) {
        this.mApplication = application;
        //init router
        ZIMKitRouter.initRouter(mApplication);
        //init activity
        ZIMKitActivityUtils.init(mApplication);
        ZIMKitDateUtils.setContext(application);
    }

    public Application getApplication() {
        return mApplication;
    }

    public ZIM zim() {
        if (mZim == null) {
            throw new IllegalArgumentException(mApplication.getString(R.string.common_create_zim_fail_log));
        }
        return mZim;
    }

    /**
     * You will need to initialize the ZIMKit SDK before calling methods.
     *
     * @param appID   appID. To get this, go to ZEGOCLOUD Admin Console (https://console.zegocloud.com/).
     * @param appSign appSign. To get this, go to ZEGOCLOUD Admin Console (https://console.zegocloud.com/).
     */
    public void init(Long appID, String appSign) {
        if (mZim == null) {
            ZIMAppConfig config = new ZIMAppConfig();
            config.appID = appID;
            config.appSign = appSign;
            mZim = ZIM.getInstance() == null ? ZIM.create(config, mApplication) : ZIM.getInstance();
            mZim.setEventHandler(ZIMKitEventHandler.share());
            ZIMKitEventHandler.share().addEventListener(ZIMKitConstant.EventConstant.KEY_CONNECTION_STATE_CHANGED,
                    this, mKitEventCallBack);
            ZIMKitEventHandler.share().addEventListener(ZIMKitConstant.EventConstant.KEY_CONVERSATION_TOTAL_UNREAD_MESSAGE_COUNT_UPDATED, this, mKitEventCallBack);
        }
    }

    private final IZIMKitEventCallBack mKitEventCallBack = new IZIMKitEventCallBack() {
        @Override
        public void onCall(String key, Map<String, Object> event) {
            if (key.equals(ZIMKitConstant.EventConstant.KEY_CONNECTION_STATE_CHANGED)) {
                ZIMConnectionEvent connectionEvent = (ZIMConnectionEvent) event.get(ZIMKitConstant.EventConstant.PARAM_EVENT);
                ZIMConnectionState connectionState = (ZIMConnectionState) event.get(ZIMKitConstant.EventConstant.PARAM_STATE);
                if (connectionState == ZIMConnectionState.DISCONNECTED && connectionEvent == ZIMConnectionEvent.KICKED_OUT) {
                    kickedOutAccount = true;
                }
                if (mZIMKitEventListener != null) {
                    mZIMKitEventListener.onConnectionStateChange(connectionEvent, connectionState);
                }
            } else if (key.equals(ZIMKitConstant.EventConstant.KEY_CONVERSATION_TOTAL_UNREAD_MESSAGE_COUNT_UPDATED)) {
                if (mZIMKitEventListener != null) {
                    int totalCount = (int) event.get(ZIMKitConstant.EventConstant.PARAM_TOTAL_UNREAD_MESSAGE_COUNT);
                    mZIMKitEventListener.onTotalUnreadMessageCountChange(totalCount);
                }
            }
        }
    };

    /**
     * Connects user to the ZIMKit server. This method can only be used after
     * calling the [init] method and before you calling any other methods.
     *
     * @param userInfo user info
     * @param callback callback for the results that whether the connection is successful.
     */
    public synchronized void connectUser(UserInfo userInfo, ZIMKitConnectUserCallback callback) {
        if (mZim == null && mApplication != null) {
            ZLog.e(TAG, mApplication.getString(R.string.common_login_room_fail_zim_not_create_log));
            return;
        }
        this.kickedOutAccount = false;
        this.mUserInfo = userInfo;
        ZIMUserInfo mZIMUserInfo = new ZIMUserInfo();
        mZIMUserInfo.userID = userInfo.getUserID();
        mZIMUserInfo.userName = userInfo.getUserName();
        mZim.login(mZIMUserInfo, "", errorInfo -> {
            if (callback != null) {
                callback.onUserConnect(errorInfo);
            }
            if (errorInfo.code == ZIMErrorCode.SUCCESS) {
                updateUserAvatarUrl(userInfo.getUserAvatarUrl(), (userAvatarUrl, errorInfo1) -> {
                });
            }
        });
    }

    /**
     * Obtain user information
     *
     * @return
     */
    public UserInfo getUserInfo() {
        return mUserInfo;
    }

    private void updateUserAvatar(String avatar) {
        if (mUserInfo != null && !TextUtils.isEmpty(avatar)) {
            mUserInfo.setUserAvatarUrl(avatar);
        }
    }

    /**
     * After a successful login, you can change the user avatar as needed.
     *
     * @param avatar   avatar URL.
     * @param callback callback for the results that whether the user avatar is updated successfully.
     */
    public void updateUserAvatarUrl(String avatar, ZIMKitUserAvatarUrlUpdatedCallback callback) {
        if (TextUtils.isEmpty(avatar)) {
            return;
        }
        mZim.updateUserAvatarUrl(avatar, (userAvatarUrl, errorInfo) -> {
            if (errorInfo.code == ZIMErrorCode.SUCCESS) {
                updateUserAvatar(userAvatarUrl);
                if (callback != null) {
                    callback.onUserAvatarUrlUpdated(userAvatarUrl, errorInfo);
                }
            }
        });
    }

    /**
     * You can choose multiple users besides yourself to start a group chat.
     *
     * @param groupName group name.
     * @param userIDList user ID list
     */

    /**
     * You can choose multiple users besides yourself to start a group chat.
     *
     * @param groupName   group name.
     * @param userIDList  user ID list
     * @param callback callback for the results that whether the group chat is created successfully.
     */
    public void createGroup(String groupName, List<String> userIDList, ZIMKitCreateGroupCallback callback) {
        if (mZim == null && mApplication != null) {
            ZLog.e(TAG, mApplication.getString(R.string.common_login_room_fail_zim_not_create_log));
            return;
        }
        if (userIDList == null || userIDList.isEmpty()) {
            return;
        }
        ZIMGroupInfo zimGroupInfo = new ZIMGroupInfo();
        zimGroupInfo.groupName = groupName;
        mZim.createGroup(zimGroupInfo, userIDList, (groupInfo, userList, errorUserList, errorInfo) -> {
            if (callback != null) {
                callback.onCreateGroup(getZIMKitGroupInfo(groupInfo), errorUserList, errorInfo);
            }
        });
    }

    /**
     * @param groupId  group ID
     * @param callback callback for the results that whether the group chat is joined successfully.
     */
    public void joinGroup(String groupId, ZIMKitJoinGroupCallback callback) {
        if (mZim == null && mApplication != null) {
            ZLog.e(TAG, mApplication.getString(R.string.common_login_room_fail_zim_not_create_log));
            return;
        }
        mZim.joinGroup(groupId, (groupInfo, errorInfo) -> {
            if (callback != null) {
                callback.onJoinGroup(getZIMKitGroupInfo(groupInfo), errorInfo);
            }
        });
    }

    private ZIMKitGroupInfo getZIMKitGroupInfo(ZIMGroupFullInfo groupInfo) {
        ZIMKitGroupInfo zimKitGroupInfo = new ZIMKitGroupInfo();
        zimKitGroupInfo.setGroupID(groupInfo.baseInfo.groupID);
        zimKitGroupInfo.setGroupName(groupInfo.baseInfo.groupName);
        zimKitGroupInfo.setGroupAvatarUrl(groupInfo.baseInfo.groupAvatarUrl);
        return zimKitGroupInfo;
    }

    /**
     * Disconnects current user from ZIMKit server.
     */
    public synchronized void disconnectUser() {
        if (mZim != null) {
            mZim.logout();
        }
    }

    public void destroy() {
        if (mZim != null) {
            ZIMKitEventHandler.share().removeEventListener(ZIMKitConstant.EventConstant.KEY_CONNECTION_STATE_CHANGED, this);
            mZim.destroy();
            mZim = null;
        }
        mApplication = null;
        mUserInfo = null;
    }

    public boolean isKickedOutAccount() {
        return kickedOutAccount;
    }

    private ZIMKitEventListener mZIMKitEventListener;

    /**
     * Listen for related event callbacks.
     *
     * @param mZIMKitEventListener
     */
    public void addZIMKitEventListener(ZIMKitEventListener mZIMKitEventListener) {
        this.mZIMKitEventListener = mZIMKitEventListener;
    }

}
