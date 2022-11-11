package im.zego.zimkitcommon.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import im.zego.zimkitcommon.ZIMKitManager;

public class ZIMKitActivityUtils {

    private static List<Activity> activityList = new ArrayList<>();
    private static Activity currentActivity;

    public static void recreateAll() {
        Iterator<Activity> iterator = activityList.iterator();
        //The activity at the top of the stack is destroyed first, then started, and the rest goes to rebuild
        while (iterator.hasNext()) {
            Activity activity = iterator.next();
            activity.recreate();
            iterator.remove();
        }
    }

    private static Application context;

    public static void init(Application application) {
        if (application == null) {
            return;
        }
        context = application;
        application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                removeDestroyActivity(activity, savedInstanceState);
                if (activityList != null) {
                    activityList.add(activity);
                }
            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {
                currentActivity = activity;
            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                if (activityList != null) {
                    activityList.remove(activity);
                }
                currentActivity = currentActivity == activity ? null : currentActivity;
            }
        });
    }

    /**
     * Get the current Activity
     *
     * @return
     */
    public static Activity getCurrentActivity() {
        return isActivityAlive(currentActivity) ? currentActivity : null;
    }

    /**
     * Whether Activity is alive or not
     *
     * @param activity
     * @return
     */
    public static boolean isActivityAlive(final Activity activity) {
        return activity != null && !activity.isFinishing()
                && (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1 || !activity.isDestroyed());
    }

    /**
     * Remove the system from destroying the rebuilt activity due to lack of memory
     *
     * @param activity
     * @param savedInstanceState
     */
    private static void removeDestroyActivity(Activity activity, Bundle savedInstanceState) {
        if (activity != null && savedInstanceState != null) {//系统恢复创建Activity
            Iterator<Activity> iterator = activityList.iterator();
            while (iterator.hasNext()) {
                Activity exitActivity = iterator.next();
                if (exitActivity == null || activity.getComponentName().getClassName().equals(exitActivity.getComponentName().getClassName())) {
                    iterator.remove();
                }
            }
        }
    }

    /**
     * Whether the process is running in the background
     *
     * @return
     */
    public static boolean isBackstage() {
        return activityList.isEmpty();
    }

    /**
     * Destroy all activities
     */
    public static void finishAll() {
        Iterator<Activity> activityIterator = activityList.iterator();
        while (activityIterator.hasNext()) {
            Activity activity = activityIterator.next();
            if (activity != null) {
                activity.finish();
            }
            activityIterator.remove();
        }
    }

    public static boolean isExitActivity(Class<?> mClass) {
        return isExitActivity(mClass.getName());
    }

    /**
     * Keep only the specified Activity
     *
     * @param mClass
     */
    public static void onlyExitActivity(Class<? extends Activity> mClass) {
        Iterator<Activity> activityIterator = activityList.iterator();
        String activityName = mClass.getName();
        while (activityIterator.hasNext()) {
            Activity activity = activityIterator.next();
            ComponentName componentName = activity.getComponentName();
            if (componentName != null) {
                if (!componentName.getClassName().equals(activityName)) {
                    activity.finish();
                }
            }
        }
    }

    /**
     * Message notifications are retained on the login page, the session page and the current message page
     */
    public static void finishActivityForMessage() {
        if (activityList.size() <= 3) {
            return;
        }
        for (int i = 0; i < activityList.size(); i++) {
            Activity activity = activityList.get(i);
            ComponentName componentName = activity.getComponentName();
            if (i != 0 && i != 1 && i != activityList.size() - 1) {
                activity.finish();
            }
        }
    }

    /**
     * Keep only the specified Activity
     *
     * @param activityName
     */
    public static void onlyExitActivity(String activityName) {
        Iterator<Activity> activityIterator = activityList.iterator();
        while (activityIterator.hasNext()) {
            Activity activity = activityIterator.next();
            ComponentName componentName = activity.getComponentName();
            if (componentName != null) {
                if (!componentName.getClassName().equals(activityName)) {
                    activity.finish();
                }
            }
        }
    }

    /**
     * finish the specified activity
     *
     * @param mClass
     */
    public static void onlyFinishActivity(Class<? extends Activity> mClass) {
        Iterator<Activity> activityIterator = activityList.iterator();
        String activityName = mClass.getName();
        while (activityIterator.hasNext()) {
            Activity activity = activityIterator.next();
            ComponentName componentName = activity.getComponentName();
            if (componentName != null) {
                if (componentName.getClassName().equals(activityName)) {
                    activity.finish();
                }
            }
        }
    }

    /**
     * Whether an Activity exists or not
     *
     * @param activityName ( The name of the complete package path)
     * @return
     */
    public static boolean isExitActivity(String activityName) {
        boolean isExit = false;
        for (Activity activity : activityList) {
            ComponentName componentName = activity.getComponentName();
            if (componentName != null) {
                if (componentName.getClassName().equals(activityName)) {
                    isExit = true;
                    break;
                }
            }
        }
        return isExit;
    }

    /**
     * Exit the specified page, and the page above it,
     * If the specified page does not exist no action is taken
     *
     * @param firstPushEnterActivity
     */
    public static void back2Activity(Activity firstPushEnterActivity) {
        boolean isExistActivity = false;
        List<Activity> tmpList = new ArrayList<>();
        Iterator<Activity> activityIterator = activityList.iterator();
        while (activityIterator.hasNext()) {
            Activity activity = activityIterator.next();
            if (activity != null) {
                if (activity == firstPushEnterActivity) {
                    isExistActivity = true;
                }
                if (isExistActivity) {
                    tmpList.add(activity);
                }
            }
        }
        if (isExistActivity) {
            for (Activity activity : tmpList) {
                if (isActivityAlive(activity)) {
                    activity.finish();
                }
            }
        }
        tmpList.clear();
    }

    /**
     * Whether at the front desk
     *
     * @return
     */
    public static boolean isBackground() {
        Context context = ZIMKitManager.share().getApplication();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(context.getPackageName())) {
                if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    return false;
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Clear all message notifications
     */
    public static void clearAllNotifications() {
        NotificationManager notificationManager = (NotificationManager) ZIMKitManager.share().getApplication().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

}
