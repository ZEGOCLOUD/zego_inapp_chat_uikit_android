package im.zego.zimkitmessages.utils.notification;

import java.util.ArrayList;
import java.util.Collections;

import im.zego.zim.entity.ZIMMessage;
import im.zego.zim.entity.ZIMTextMessage;
import im.zego.zim.enums.ZIMMessageDirection;
import im.zego.zim.enums.ZIMMessageType;
import im.zego.zimkitcommon.ZIMKitConstant;
import im.zego.zimkitcommon.ZIMKitManager;
import im.zego.zimkitcommon.event.IZIMKitEventCallBack;
import im.zego.zimkitcommon.event.ZIMKitEventHandler;
import im.zego.zimkitcommon.utils.ZIMKitActivityUtils;
import im.zego.zimkitmessages.utils.SortMessageComparator;

public class ZIMKitNotificationsManager {

    private boolean isOpenNotification = false;
    private static ZIMKitNotificationsManager sInstance;

    public static ZIMKitNotificationsManager share() {
        if (sInstance == null) {
            synchronized (ZIMKitNotificationsManager.class) {
                if (sInstance == null) {
                    sInstance = new ZIMKitNotificationsManager();
                }
            }
        }
        return sInstance;
    }

    /**
     * Initialize this method when a message notification is needed
     */
    public void initNotifications() {
        isOpenNotification = true;
        ZIMKitEventHandler.share().addEventListener(ZIMKitConstant.EventConstant.KEY_RECEIVE_PEER_MESSAGE, this, eventCallBack);
        ZIMKitEventHandler.share().addEventListener(ZIMKitConstant.EventConstant.KEY_RECEIVE_GROUP_MESSAGE, this, eventCallBack);
    }

    private final IZIMKitEventCallBack eventCallBack = (key, event) -> {
        //Receiving peer messages
        if (key.equals(ZIMKitConstant.EventConstant.KEY_RECEIVE_PEER_MESSAGE) || key.equals(ZIMKitConstant.EventConstant.KEY_RECEIVE_GROUP_MESSAGE)) {
            ArrayList<ZIMMessage> messageList = (ArrayList<ZIMMessage>) event.get(ZIMKitConstant.EventConstant.PARAM_MESSAGE_LIST);
            if (messageList != null && !messageList.isEmpty()) {
                if(messageList.size() > 2){
                    Collections.sort(messageList,new SortMessageComparator());
                }
                handlerMessageList(messageList);
            }
        }
    };

    private void handlerMessageList(ArrayList<ZIMMessage> messageList) {
        //The app does not notify in the foreground
        boolean isForeground = !ZIMKitActivityUtils.isBackground();
        if (isForeground) {
            return;
        }
        for (ZIMMessage zimMessage : messageList) {
            if (zimMessage.getDirection() == ZIMMessageDirection.RECEIVE) {
                integratingMessageData(zimMessage);
            }
        }
    }

    private void integratingMessageData(ZIMMessage zimMessage) {
        String message = "";
        if (zimMessage.getType() == ZIMMessageType.TEXT) {
            message = ((ZIMTextMessage) zimMessage).message;
        } else if (zimMessage.getType() == ZIMMessageType.IMAGE) {
            message = ZIMKitManager.share().getApplication().getString(im.zego.zimkitcommon.R.string.common_message_photo);
        } else if (zimMessage.getType() == ZIMMessageType.VIDEO) {
            message = ZIMKitManager.share().getApplication().getString(im.zego.zimkitcommon.R.string.common_message_video);
        } else if (zimMessage.getType() == ZIMMessageType.AUDIO) {
            message = ZIMKitManager.share().getApplication().getString(im.zego.zimkitcommon.R.string.common_message_audio);
        } else if (zimMessage.getType() == ZIMMessageType.FILE) {
            message = ZIMKitManager.share().getApplication().getString(im.zego.zimkitcommon.R.string.common_message_file);
        } else {
            message = ZIMKitManager.share().getApplication().getString(im.zego.zimkitcommon.R.string.common_message_unknown);
        }
        messageNotification(zimMessage.getConversationType().value(), zimMessage.getConversationID(), message, zimMessage.getSenderUserID());
    }

    private void messageNotification(int conversationType, String conversationID, String message, String senderUserID) {
        NotificationsUtils.NotifyConfig notifyConfig = new NotificationsUtils.NotifyConfig();
        notifyConfig.conversationType = conversationType;
        notifyConfig.conversationID = conversationID;
        notifyConfig.conversationName = "";
        notifyConfig.message = message;
        notifyConfig.senderUserID = senderUserID;
        NotificationsUtils.showNotification(notifyConfig);
    }

    public boolean isOpenNotification() {
        return isOpenNotification;
    }

    public void onCleared(){
        isOpenNotification = false;
        ZIMKitEventHandler.share().removeEventListener(ZIMKitConstant.EventConstant.KEY_RECEIVE_PEER_MESSAGE, this);
        ZIMKitEventHandler.share().removeEventListener(ZIMKitConstant.EventConstant.KEY_RECEIVE_GROUP_MESSAGE, this);
    }

}
