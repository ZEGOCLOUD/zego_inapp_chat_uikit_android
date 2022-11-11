package im.zego.zimkitmessages;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import im.zego.zim.enums.ZIMConnectionState;
import im.zego.zimkitcommon.ZIMKitConstant;
import im.zego.zimkitcommon.ZIMKitManager;
import im.zego.zimkitcommon.event.IZIMKitEventCallBack;
import im.zego.zimkitcommon.event.ZIMKitEventHandler;
import im.zego.zimkitmessages.interfaces.NetworkConnectionListener;
import im.zego.zimkitmessages.model.message.ZIMKitMessageModel;

public class ZIMKitMessageManager {

    private volatile static ZIMKitMessageManager mInstance;

    public static ZIMKitMessageManager share() {
        if (mInstance == null) {
            synchronized (ZIMKitManager.class) {
                if (mInstance == null) {
                    mInstance = new ZIMKitMessageManager();
                }
            }
        }
        return mInstance;
    }

    private final List<WeakReference<NetworkConnectionListener>> connectListenerList = new ArrayList<>();

    private List<Long> limitFile = new ArrayList<>();

    public void initNetworkConnection() {
        ZIMKitEventHandler.share().addEventListener(ZIMKitConstant.EventConstant.KEY_CONNECTION_STATE_CHANGED,
                this, new IZIMKitEventCallBack() {
                    @Override
                    public void onCall(String key, Map<String, Object> event) {
                        ZIMConnectionState connectionState = (ZIMConnectionState) event.get(ZIMKitConstant.EventConstant.PARAM_STATE);
                        if (connectionState == ZIMConnectionState.CONNECTED) {
                            //CONNECTED
                            for (WeakReference<NetworkConnectionListener> listenerWeakReference : connectListenerList) {
                                NetworkConnectionListener listener = listenerWeakReference.get();
                                if (listener != null) {
                                    listener.onConnected();
                                }
                            }
                        }
                    }
                });
    }

    public void registerNetworkListener(NetworkConnectionListener listener) {
        if (listener == null) {
            return;
        }

        for (WeakReference<NetworkConnectionListener> weakReference : connectListenerList) {
            NetworkConnectionListener networkConnectionListener = weakReference.get();
            if (networkConnectionListener == listener) {
                return;
            }
        }

        WeakReference<NetworkConnectionListener> weakReference = new WeakReference<>(listener);
        connectListenerList.add(weakReference);
    }

    public void unRegisterNetworkListener(NetworkConnectionListener listener) {
        if (listener == null || connectListenerList.size() == 0) {
            return;
        }
        Iterator<WeakReference<NetworkConnectionListener>> it = connectListenerList.iterator();
        while (it.hasNext()) {
            WeakReference<NetworkConnectionListener> weakReference = it.next();
            NetworkConnectionListener networkConnectionListener = weakReference.get();
            if (networkConnectionListener == listener) {
                it.remove();
            }
        }
    }

    public void clearNetworkListener() {
        connectListenerList.clear();
    }

    public void removeNetworkConnection() {
        ZIMKitEventHandler.share().removeEventListener(ZIMKitConstant.EventConstant.KEY_CONNECTION_STATE_CHANGED, this);
    }

    public void addLimitFile(long messageID) {
        limitFile.add(messageID);
    }

    public void removeLimitFile(long messageID) {
        if (messageID == 0 || limitFile.size() == 0) {
            return;
        }
        Iterator<Long> it = limitFile.iterator();
        while (it.hasNext()) {
            long id = it.next().longValue();
            if (id == messageID) {
                it.remove();
            }
        }
    }

    public boolean isDownloading(long messageID) {
        if (messageID == 0 || limitFile.size() == 0) {
            return false;
        }
        Iterator<Long> it = limitFile.iterator();
        while (it.hasNext()) {
            long id = it.next().longValue();
            if (id == messageID) {
                return true;
            }
        }
        return false;
    }

    //Connection Status Listening
    private IMessageListener mStateListener;

    public void setMessageStateListener(IMessageListener mStateListener) {
        this.mStateListener = mStateListener;
    }

    public interface IMessageListener {
        void onSendState(List<ZIMKitMessageModel> data);
    }

    public void sendMessage(List<ZIMKitMessageModel> data) {
        if (mStateListener != null) {
            mStateListener.onSendState(data);
        }
    }

}
