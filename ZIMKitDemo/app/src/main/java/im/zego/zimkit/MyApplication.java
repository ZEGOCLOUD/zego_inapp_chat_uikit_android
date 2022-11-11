package im.zego.zimkit;

import android.app.Application;

import im.zego.zimkit.keycenter.KeyCenter;
import im.zego.zimkitcommon.ZIMKitManager;
import im.zego.zimkitmessages.utils.notification.ZIMKitNotificationsManager;

public class MyApplication extends Application {
    public static MyApplication sInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        ZIMKitManager.share().setContext(this);
        // Initialization ZIM
        ZIMKitManager.share().init(KeyCenter.APP_ID, KeyCenter.APP_SIGN);
        //Initialization Message Notification
        ZIMKitNotificationsManager.share().initNotifications();
    }
}
