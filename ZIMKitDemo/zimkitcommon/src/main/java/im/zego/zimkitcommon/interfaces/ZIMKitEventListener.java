package im.zego.zimkitcommon.interfaces;

import im.zego.zim.enums.ZIMConnectionEvent;
import im.zego.zim.enums.ZIMConnectionState;

public interface ZIMKitEventListener {

    /**
     * Callback for updates on the connection status changes.
     * The event callback when the connection state changes.
     * @param connectionEvent the event happened. The event that causes the connection status to change.
     * @param connectionState  the current connection status.
     */
    void onConnectionStateChange(ZIMConnectionEvent connectionEvent, ZIMConnectionState connectionState);

    /**
     * Total number of unread messages.
     * @param totalCount Total number of unread messages.
     */
    default void onTotalUnreadMessageCountChange(int totalCount){}
}
