package im.zego.zimkitcommon.event;

import java.lang.ref.WeakReference;

public class ZIMKitEventCallBackEntity {

    private WeakReference<Object> mId;
    private WeakReference<IZIMKitEventCallBack> mCallBack;

    public void setId(Object id) {
        mId = new WeakReference<>(id);
    }

    public void setCallBack(IZIMKitEventCallBack callBack) {
        mCallBack = new WeakReference<>(callBack);
    }

    public Object getId() {
        if (mId == null) {
            return null;
        }
        return mId.get();
    }

    public IZIMKitEventCallBack getCallBack() {
        if (mCallBack == null) {
            return null;
        }
        return mCallBack.get();
    }

    public boolean isSelf(Object id) {
        if (id == null || mId == null || mId.get() == null) {
            return false;
        }
        return id == mId.get();
    }
}
