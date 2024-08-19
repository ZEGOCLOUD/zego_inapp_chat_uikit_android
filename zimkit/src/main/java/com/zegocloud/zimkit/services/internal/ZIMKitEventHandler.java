package com.zegocloud.zimkit.services.internal;

import android.util.Log;
import com.zegocloud.uikit.plugin.adapter.ZegoPluginAdapter;
import com.zegocloud.uikit.plugin.adapter.plugins.call.ZegoCallPluginProtocol;
import com.zegocloud.zimkit.services.model.ZIMKitConversation;
import com.zegocloud.zimkit.services.model.ZIMKitMessage;
import com.zegocloud.zimkit.services.utils.MessageTransform;
import im.zego.zim.ZIM;
import im.zego.zim.callback.ZIMEventHandler;
import im.zego.zim.entity.ZIMConversationChangeInfo;
import im.zego.zim.entity.ZIMGroupMemberInfo;
import im.zego.zim.entity.ZIMGroupOperatedInfo;
import im.zego.zim.entity.ZIMMessage;
import im.zego.zim.entity.ZIMRevokeMessage;
import im.zego.zim.enums.ZIMConnectionEvent;
import im.zego.zim.enums.ZIMConnectionState;
import im.zego.zim.enums.ZIMConversationEvent;
import im.zego.zim.enums.ZIMConversationType;
import im.zego.zim.enums.ZIMGroupMemberEvent;
import im.zego.zim.enums.ZIMGroupMemberState;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;
import timber.log.Timber;

public class ZIMKitEventHandler extends ZIMEventHandler {

    private boolean kickedOutAccount = false;

    @Override
    public void onConnectionStateChanged(ZIM zim, ZIMConnectionState state, ZIMConnectionEvent event,
        JSONObject extendedData) {
        super.onConnectionStateChanged(zim, state, event, extendedData);
        Timber.d(
            "onConnectionStateChanged() called with: zim = [" + zim + "], state = [" + state + "], event = [" + event
                + "], extendedData = [" + extendedData + "]");
        if (state == ZIMConnectionState.DISCONNECTED && event == ZIMConnectionEvent.KICKED_OUT) {
            kickedOutAccount = true;
        }
        ZIMKitCore.getInstance().getZimkitNotifyList().notifyAllListener(zimKitDelegate -> {
            zimKitDelegate.onConnectionStateChange(state, event);
        });
    }

    @Override
    public void onConversationChanged(ZIM zim, ArrayList<ZIMConversationChangeInfo> conversationChangeInfoList) {
        super.onConversationChanged(zim, conversationChangeInfoList);
        handlerConversationChange(conversationChangeInfoList);
    }

    @Override
    public void onConversationTotalUnreadMessageCountUpdated(ZIM zim, int totalUnreadMessageCount) {
        super.onConversationTotalUnreadMessageCountUpdated(zim, totalUnreadMessageCount);
        ZIMKitCore.getInstance().setTotalUnreadMessageCount(totalUnreadMessageCount);
        ZIMKitCore.getInstance().getZimkitNotifyList().notifyAllListener(zimKitDelegate -> {
            zimKitDelegate.onTotalUnreadMessageCountChange(totalUnreadMessageCount);
        });
    }

    @Override
    public void onTokenWillExpire(ZIM zim, int second) {
        super.onTokenWillExpire(zim, second);
        ZIMKitCore.getInstance().getZimkitNotifyList()
            .notifyAllListener(zimKitDelegate -> zimKitDelegate.onTokenWillExpire(second));
    }

    @Override
    public void onReceivePeerMessage(ZIM zim, ArrayList<ZIMMessage> messageList, String fromUserID) {
        super.onReceivePeerMessage(zim, messageList, fromUserID);
        handleReceiveNewMessages(messageList);
    }

    @Override
    public void onReceiveGroupMessage(ZIM zim, ArrayList<ZIMMessage> messageList, String fromGroupID) {
        super.onReceiveGroupMessage(zim, messageList, fromGroupID);
        handleReceiveNewMessages(messageList);

    }

    @Override
    public void onGroupMemberInfoUpdated(ZIM zim, ArrayList<ZIMGroupMemberInfo> userList,
        ZIMGroupOperatedInfo operatedInfo, String groupID) {
        super.onGroupMemberInfoUpdated(zim, userList, operatedInfo, groupID);
    }

    @Override
    public void onGroupMemberStateChanged(ZIM zim, ZIMGroupMemberState state, ZIMGroupMemberEvent event,
        ArrayList<ZIMGroupMemberInfo> userList, ZIMGroupOperatedInfo operatedInfo, String groupID) {
        super.onGroupMemberStateChanged(zim, state, event, userList, operatedInfo, groupID);
        ZIMKitCore.getInstance().onGroupMemberStateChanged(state, event, userList, operatedInfo, groupID);
        ZIMKitCore.getInstance().getZimkitNotifyList().notifyAllListener(zimKitDelegate -> {
            zimKitDelegate.onGroupMemberStateChanged(state, event, userList, operatedInfo, groupID);
        });
    }

    @Override
    public void onMessageRevokeReceived(ZIM zim, ArrayList<ZIMRevokeMessage> messageList) {
        super.onMessageRevokeReceived(zim, messageList);
        ArrayList<ZIMMessage> messages = new ArrayList<>();

        for (ZIMRevokeMessage revokeMessage : messageList) {
            messages.add(revokeMessage);
        }
        ArrayList<ZIMKitMessage> kitMessages = MessageTransform.parseMessageList(messages);

        ZIMKitCore.getInstance().getZimkitNotifyList().notifyAllListener(zimKitDelegate -> {
            zimKitDelegate.onMessageRevokeReceived(kitMessages);
        });
    }

    private static final String TAG = "ZIMKitEventHandler";

    @Override
    public void onReceiveRoomMessage(ZIM zim, ArrayList<ZIMMessage> messageList, String fromRoomID) {
        super.onReceiveRoomMessage(zim, messageList, fromRoomID);
        handleReceiveNewMessages(messageList);
    }

    private void handlerConversationChange(List<ZIMConversationChangeInfo> infos) {
        if (infos.isEmpty()) {
            return;
        }
        for (ZIMConversationChangeInfo info : infos) {
            if (info.event == ZIMConversationEvent.ADDED) {
                ZIMKitConversation viewModel = new ZIMKitConversation(info.conversation);
                ZIMKitCore.getInstance().getConversations().add(viewModel);
            } else if (info.event == ZIMConversationEvent.UPDATED) {
                // Incremental Updates
                ZIMKitConversation oldModel = null;
                for (ZIMKitConversation model : ZIMKitCore.getInstance().getConversations()) {
                    if (model.getId().equals(info.conversation.conversationID)) {
                        oldModel = model;
                        break;
                    }
                }
                if (oldModel != null) {
                    ZIMKitCore.getInstance().getConversations().remove(oldModel);
                }
                ZIMKitCore.getInstance().getConversations().add(new ZIMKitConversation(info.conversation));
            } else if (info.event == ZIMConversationEvent.DISABLED) {
                ZIMKitConversation oldModel = null;
                for (ZIMKitConversation model : ZIMKitCore.getInstance().getConversations()) {
                    if (model.getId().equals(info.conversation.conversationID)) {
                        oldModel = model;
                        break;
                    }
                }
                if (oldModel != null) {
                    ZIMKitCore.getInstance().getConversations().remove(oldModel);
                    ZIMKitCore.getInstance().getConversations().add(new ZIMKitConversation(info.conversation));
                }
            }
        }
        ZIMKitCore.getInstance().getZimkitNotifyList().notifyAllListener(zimKitDelegate -> {
            zimKitDelegate.onConversationListChanged(new ArrayList<>(ZIMKitCore.getInstance().getConversations()));
        });
    }

    private void handleReceiveNewMessages(ArrayList<ZIMMessage> messageList) {

        if (messageList.isEmpty()) {
            return;
        }
        ZIMKitCore.getInstance().setGroupMemberInfo(messageList);

        String conversationID = messageList.get(0).getConversationID();
        ZIMConversationType type = messageList.get(0).getConversationType();
        ArrayList<ZIMKitMessage> kitMessages = MessageTransform.parseMessageList(messageList);

        ZIMKitCore.getInstance().getMessageList().addAll(kitMessages);

        ZIMKitCore.getInstance().getZimkitNotifyList().notifyAllListener(zimKitDelegate -> {
            zimKitDelegate.onMessageReceived(conversationID, type, kitMessages);
        });

    }

    public boolean isKickedOutAccount() {
        return kickedOutAccount;
    }

    public void setKickedOutAccount(boolean kickedOutAccount) {
        this.kickedOutAccount = kickedOutAccount;
    }
}
