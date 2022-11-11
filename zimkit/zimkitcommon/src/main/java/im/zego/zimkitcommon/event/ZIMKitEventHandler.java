package im.zego.zimkitcommon.event;

import android.text.TextUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import im.zego.zim.ZIM;
import im.zego.zim.callback.ZIMEventHandler;
import im.zego.zim.entity.ZIMConversationChangeInfo;
import im.zego.zim.entity.ZIMGroupMemberInfo;
import im.zego.zim.entity.ZIMGroupOperatedInfo;
import im.zego.zim.entity.ZIMMessage;
import im.zego.zim.enums.ZIMConnectionEvent;
import im.zego.zim.enums.ZIMConnectionState;
import im.zego.zim.enums.ZIMGroupMemberEvent;
import im.zego.zim.enums.ZIMGroupMemberState;
import im.zego.zimkitcommon.ZIMKitConstant;
import im.zego.zimkitcommon.ZIMKitManager;
import im.zego.zimkitcommon.utils.ZLog;

public final class ZIMKitEventHandler extends ZIMEventHandler {
    private final static String TAG = "ZIMKitEventHandler";
    private static ZIMKitEventHandler sInstance;
    private final Map<String, ArrayList<ZIMKitEventCallBackEntity>> mCallBackMap = new ConcurrentHashMap<>();

    public static ZIMKitEventHandler share() {
        if (sInstance == null) {
            synchronized (ZIMKitManager.class) {
                if (sInstance == null) {
                    sInstance = new ZIMKitEventHandler();
                }
            }
        }
        return sInstance;
    }

    public void addEventListener(String key, Object id, IZIMKitEventCallBack callBack) {
        if (TextUtils.isEmpty(key) || id == null || callBack == null) {
            ZLog.e(TAG, String.format("add event error: key:%s,id:%s,callback:%s", key, id, callBack));
            return;
        }
        ZLog.i(TAG, String.format("key:%s,id:%s,callback:%s", key, id, callBack));

        ZIMKitEventCallBackEntity callBackEntity = new ZIMKitEventCallBackEntity();
        callBackEntity.setCallBack(callBack);
        callBackEntity.setId(id);
        ArrayList<ZIMKitEventCallBackEntity> entityList = mCallBackMap.get(key);
        if (entityList == null) {
            entityList = new ArrayList<>();
            mCallBackMap.put(key, entityList);
        }
        entityList.add(callBackEntity);
    }

    public void removeEventListener(String key, Object id) {
        ArrayList<ZIMKitEventCallBackEntity> eventCallBackEntities = mCallBackMap.get(key);
        if (eventCallBackEntities != null) {
            Iterator<ZIMKitEventCallBackEntity> iterator = eventCallBackEntities.iterator();
            while (iterator.hasNext()) {
                if (iterator.next().isSelf(id)) {
                    iterator.remove();
                }
            }
        }
    }

    private void notifyEvent(String key, Map<String, Object> param) {
        ArrayList<ZIMKitEventCallBackEntity> callBackEntityList = mCallBackMap.get(key);
        if (callBackEntityList != null) {
            for (ZIMKitEventCallBackEntity callBackEntity : callBackEntityList) {
                if (callBackEntity.getCallBack() != null && callBackEntity.getId() != null) {
                    callBackEntity.getCallBack().onCall(key, param);
                }
                // todo need delete entity from list if id or callback is null ？
            }
        }
    }

    @Override
    public void onReceiveGroupMessage(ZIM zim, ArrayList<ZIMMessage> messageList, String fromGroupID) {
        super.onReceiveGroupMessage(zim, messageList, fromGroupID);
        Map<String, Object> param = new HashMap<>();
        param.put(ZIMKitConstant.EventConstant.PARAM_MESSAGE_LIST, messageList);
        param.put(ZIMKitConstant.EventConstant.PARAM_FROM_GROUP_ID, fromGroupID);
        notifyEvent(ZIMKitConstant.EventConstant.KEY_RECEIVE_GROUP_MESSAGE, param);
    }

    @Override
    public void onConnectionStateChanged(ZIM zim, ZIMConnectionState state, ZIMConnectionEvent event, JSONObject extendedData) {
        super.onConnectionStateChanged(zim, state, event, extendedData);
        Map<String, Object> param = new HashMap<>();
        param.put(ZIMKitConstant.EventConstant.PARAM_STATE, state);
        param.put(ZIMKitConstant.EventConstant.PARAM_EVENT, event);
        param.put(ZIMKitConstant.EventConstant.PARAM_EXTENDED_DATA, extendedData);
        notifyEvent(ZIMKitConstant.EventConstant.KEY_CONNECTION_STATE_CHANGED, param);
    }

    @Override
    public void onConversationChanged(ZIM zim, ArrayList<ZIMConversationChangeInfo> conversationChangeInfoList) {
        super.onConversationChanged(zim, conversationChangeInfoList);
        Map<String, Object> param = new HashMap<>();
        param.put(ZIMKitConstant.EventConstant.PARAM_CONVERSATION_CHANGE_INFO_LIST, conversationChangeInfoList);
        notifyEvent(ZIMKitConstant.EventConstant.KEY_CONVERSATION_CHANGED, param);
    }

    @Override
    public void onConversationTotalUnreadMessageCountUpdated(ZIM zim, int totalUnreadMessageCount) {
        super.onConversationTotalUnreadMessageCountUpdated(zim, totalUnreadMessageCount);
        Map<String, Object> param = new HashMap<>();
        param.put(ZIMKitConstant.EventConstant.PARAM_TOTAL_UNREAD_MESSAGE_COUNT, totalUnreadMessageCount);
        notifyEvent(ZIMKitConstant.EventConstant.KEY_CONVERSATION_TOTAL_UNREAD_MESSAGE_COUNT_UPDATED, param);
    }

    @Override
    public void onReceivePeerMessage(ZIM zim, ArrayList<ZIMMessage> messageList, String fromUserID) {
        super.onReceivePeerMessage(zim, messageList, fromUserID);
        Map<String, Object> param = new HashMap<>();
        param.put(ZIMKitConstant.EventConstant.PARAM_MESSAGE_LIST, messageList);
        param.put(ZIMKitConstant.EventConstant.PARAM_FROM_USER_ID, fromUserID);
        notifyEvent(ZIMKitConstant.EventConstant.KEY_RECEIVE_PEER_MESSAGE, param);
    }

    @Override
    public void onGroupMemberStateChanged(ZIM zim, ZIMGroupMemberState state, ZIMGroupMemberEvent event, ArrayList<ZIMGroupMemberInfo> userList, ZIMGroupOperatedInfo operatedInfo, String groupID) {
        super.onGroupMemberStateChanged(zim, state, event, userList, operatedInfo, groupID);
        Map<String, Object> param = new HashMap<>();
        param.put(ZIMKitConstant.EventConstant.PARAM_STATE, state);
        param.put(ZIMKitConstant.EventConstant.PARAM_EVENT, event);
        param.put(ZIMKitConstant.EventConstant.PARAM_USER_LIST, userList);
        param.put(ZIMKitConstant.EventConstant.PARAM_GROUP_OPERATED_INFO, operatedInfo);
        param.put(ZIMKitConstant.EventConstant.PARAM_GROUP_ID, groupID);
        notifyEvent(ZIMKitConstant.EventConstant.KEY_GROUP_MEMBER_STATE_CHANGED, param);
    }
}
