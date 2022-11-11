package im.zego.zimkitcommon.utils;

import android.content.Context;
import android.content.SharedPreferences;

import im.zego.zimkitcommon.ZIMKitManager;

public class ZIMKitSPUtils {

    public static final String KEY_AUDIO_PLAY_MODE = "audioPlayMode";

    private static SharedPreferences sp = ZIMKitManager.share().getApplication().getSharedPreferences("zimkit", Context.MODE_PRIVATE);

    public static void putBoolean(String key, Boolean value) {
        sp.edit().putBoolean(key, value).apply();
    }

    public static boolean getBoolean(String key, Boolean value) {
        return sp.getBoolean(key, value);
    }

    public static void putString(String key, String value) {
        sp.edit().putString(key, value).apply();
    }

    public static String getString(String key, String defValue) {
        return sp.getString(key, defValue);
    }

    public static void putInt(String key, int value) {
        sp.edit().putInt(key, value).apply();
    }

    public static int getInt(String key, int defValue) {
        return sp.getInt(key, defValue);
    }

    public static long getLong(String key, long defValue) {
        return sp.getLong(key, defValue);
    }

    public static void putLong(String key, long value) {
        sp.edit().putLong(key, value).apply();
    }

    public static void remove(String key) {
        sp.edit().remove(key).apply();
    }

}
