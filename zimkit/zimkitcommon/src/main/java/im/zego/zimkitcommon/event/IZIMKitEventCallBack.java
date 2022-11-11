package im.zego.zimkitcommon.event;

import java.util.Map;

public interface IZIMKitEventCallBack {
    void onCall(String key,Map<String, Object> event);
}
