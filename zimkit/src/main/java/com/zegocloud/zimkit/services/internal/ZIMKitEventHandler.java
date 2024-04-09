package com.zegocloud.zimkit.services.internal;

import androidx.core.util.Consumer;
import com.zegocloud.zimkit.services.ZIMKitDelegate;
import com.zegocloud.zimkit.services.model.ZIMKitConversation;
import com.zegocloud.zimkit.services.model.ZIMKitMessage;
import com.zegocloud.zimkit.services.utils.MessageTransform;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import im.zego.zim.ZIM;
import im.zego.zim.callback.ZIMEventHandler;
import im.zego.zim.entity.ZIMConversationChangeInfo;
import im.zego.zim.entity.ZIMMessage;
import im.zego.zim.enums.ZIMConnectionEvent;
import im.zego.zim.enums.ZIMConnectionState;
import im.zego.zim.enums.ZIMConversationEvent;
import im.zego.zim.enums.ZIMConversationType;

public class ZIMKitEventHandler extends ZIMEventHandler {

    private boolean kickedOutAccount = false;

    @Override
    public void onConnectionStateChanged(ZIM zim, ZIMConnectionState state, ZIMConnectionEvent event, JSONObject extendedData) {
        super.onConnectionStateChanged(zim, state, event, extendedData);
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
        ZIMKitCore.getInstance().getZimkitNotifyList().notifyAllListener(
            zimKitDelegate -> zimKitDelegate.onTokenWillExpire(second));
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
                    ZIMKitCore.getInstance().getConversations().add(new ZIMKitConversation(info.conversation));
                }
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
