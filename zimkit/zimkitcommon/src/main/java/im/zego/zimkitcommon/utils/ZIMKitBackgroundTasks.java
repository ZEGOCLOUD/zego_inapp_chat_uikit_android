package im.zego.zimkitcommon.utils;

import android.os.Handler;
import android.os.Looper;

public class ZIMKitBackgroundTasks {

    private static final ZIMKitBackgroundTasks instance = new ZIMKitBackgroundTasks();
    private final Handler handler = new Handler(Looper.getMainLooper());

    public static ZIMKitBackgroundTasks getInstance() {
        return instance;
    }

    private ZIMKitBackgroundTasks() {
    }

    public void runOnUiThread(Runnable runnable) {
        handler.post(runnable);
    }

    public boolean postDelayed(Runnable r, long delayMillis) {
        return handler.postDelayed(r, delayMillis);
    }

}
