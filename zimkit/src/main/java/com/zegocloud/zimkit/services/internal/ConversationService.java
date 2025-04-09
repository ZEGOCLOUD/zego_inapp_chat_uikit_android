package com.zegocloud.zimkit.services.internal;

import com.zegocloud.uikit.plugin.signaling.ZegoSignalingPlugin;
import com.zegocloud.zimkit.services.ZIMKitConfig;
import com.zegocloud.zimkit.services.callback.ClearUnreadCountCallback;
import com.zegocloud.zimkit.services.callback.DeleteConversationCallback;
import com.zegocloud.zimkit.services.callback.GetConversationListCallback;
import com.zegocloud.zimkit.services.callback.LoadMoreConversationCallback;
import com.zegocloud.zimkit.services.model.ZIMKitConversation;
import com.zegocloud.zimkit.services.utils.ZIMMessageUtil;
import im.zego.zim.callback.ZIMConversationNotificationStatusSetCallback;
import im.zego.zim.callback.ZIMConversationPinnedStateUpdatedCallback;
import im.zego.zim.callback.ZIMConversationUnreadMessageCountClearedCallback;
import im.zego.zim.entity.ZIMConversation;
import im.zego.zim.entity.ZIMConversationDeleteConfig;
import im.zego.zim.entity.ZIMConversationQueryConfig;
import im.zego.zim.entity.ZIMError;
import im.zego.zim.enums.ZIMConversationNotificationStatus;
import im.zego.zim.enums.ZIMConversationType;
import im.zego.zim.enums.ZIMErrorCode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import timber.log.Timber;

public class ConversationService {

    private static final int MAX_PAGE_COUNT = 100;

    public void getConversationList(GetConversationListCallback callback) {
        if (ZIMKitCore.getInstance().getConversations().size() > 0 && ZIMKitCore.getInstance().isLoadConversationList()) {
            if (callback != null) {
                ZIMError zimError = new ZIMError();
                zimError.code = ZIMErrorCode.SUCCESS;
                callback.onGetConversationList(new ArrayList<>(ZIMKitCore.getInstance().getConversations()), zimError);
            }
        } else {
            ZIMKitCore.getInstance().getConversations().clear();
            ZIMKitCore.getInstance().setLoadConversationList(true);
            loadMoreConversation(false, null, new LoadMoreConversationCallback() {
                @Override
                public void onLoadMoreConversation(ZIMError error) {
                    callback.onGetConversationList(new ArrayList<>(ZIMKitCore.getInstance().getConversations()), error);
                }
            });
        }
    }

    public void deleteConversation(String conversationID, ZIMConversationType type,
        DeleteConversationCallback callback) {
        ZegoSignalingPlugin.getInstance().deleteConversation(conversationID, type, new ZIMConversationDeleteConfig(),
            (conversationID1, conversationType, errorInfo) -> {
                if (errorInfo.code == ZIMErrorCode.SUCCESS) {
                    Iterator<ZIMKitConversation> iterator = ZIMKitCore.getInstance().getConversations().iterator();
                    while (iterator.hasNext()) {
                        ZIMKitConversation model = iterator.next();
                        if (model.getId().equals(conversationID) && conversationType == model.getType()) {
                            iterator.remove();
                            break;
                        }
                    }
                }
                if (callback != null) {
                    callback.onDeleteConversation(errorInfo);
                }


                ArrayList<ZIMKitConversation> conversations = new ArrayList<>(ZIMKitCore.getInstance().getConversations()) ;

                ZIMKitCore.getInstance().getZimkitNotifyList().notifyAllListener(zimKitDelegate -> {
                    zimKitDelegate.onConversationListChanged(conversations);
                });


            });
    }

    public void clearUnreadCount(String conversationID, ZIMConversationType type, ClearUnreadCountCallback callback) {
        ZegoSignalingPlugin.getInstance().clearConversationUnreadMessageCount(conversationID, type,
            new ZIMConversationUnreadMessageCountClearedCallback() {
                @Override
                public void onConversationUnreadMessageCountCleared(String conversationID,
                    ZIMConversationType conversationType, ZIMError errorInfo) {
                    if (callback != null) {
                        callback.onClearUnreadCount(errorInfo);
                    }
                }
            });
    }

    public void loadMoreConversation(boolean isCallbackListChanged, ZIMConversation conversation,
        LoadMoreConversationCallback callback) {

        ZIMConversationQueryConfig config = new ZIMConversationQueryConfig();
        config.count = MAX_PAGE_COUNT;
        config.nextConversation = conversation;
        Timber.d("queryConversationList() called with: isCallbackListChanged = [" + isCallbackListChanged
            + "], conversation = [" + conversation + "], callback = [" + callback + "]");
        ZegoSignalingPlugin.getInstance().queryConversationList(config, (conversationList, errorInfo) -> {

            if (errorInfo.code == ZIMErrorCode.SUCCESS) {
                handleConversationListLoad(conversationList);
            }

            if (callback != null) {
                callback.onLoadMoreConversation(errorInfo);
            }

            if (isCallbackListChanged) {

                ArrayList<ZIMKitConversation> conversations = new ArrayList<>(ZIMKitCore.getInstance().getConversations()) ;

                ZIMKitCore.getInstance().getZimkitNotifyList().notifyAllListener(zimKitDelegate -> {
                    zimKitDelegate.onConversationListChanged(conversations);
                });

            }

        });

    }

    private void handleConversationListLoad(ArrayList<ZIMConversation> newConversationList) {
        if (newConversationList.isEmpty()) {
            return;
        }
        ArrayList<ZIMKitConversation> newViewModels = new ArrayList<>();
        for (ZIMConversation zimConversation : newConversationList) {
            newViewModels.add(new ZIMKitConversation(zimConversation));
        }
        ZIMKitCore.getInstance().getConversations().addAll(newViewModels);
    }

    public void updateConversationPinnedState(boolean isPinned, String conversationID,
        ZIMConversationType conversationType, ZIMConversationPinnedStateUpdatedCallback callback) {
        ZegoSignalingPlugin.getInstance()
            .updateConversationPinnedState(isPinned, conversationID, conversationType, callback);
    }

    public void setConversationNotificationStatus(ZIMConversationNotificationStatus status, String conversationID,
        ZIMConversationType conversationType, ZIMConversationNotificationStatusSetCallback callback) {
        ZegoSignalingPlugin.getInstance().setConversationNotificationStatus(status, conversationID, conversationType, callback);
    }
}
