package com.zegocloud.zimkit.services.internal.interfaces;

import com.zegocloud.zimkit.services.callback.ClearUnreadCountCallback;
import com.zegocloud.zimkit.services.callback.DeleteConversationCallback;
import com.zegocloud.zimkit.services.callback.LoadMoreConversationCallback;
import im.zego.zim.enums.ZIMConversationType;
import com.zegocloud.zimkit.services.callback.GetConversationListCallback;

public interface IConversationService {

    void getConversationList(GetConversationListCallback callback);

    void deleteConversation(String conversationID, ZIMConversationType type, DeleteConversationCallback callback);

    void clearUnreadCount(String conversationID, ZIMConversationType type, ClearUnreadCountCallback callback);

    void loadMoreConversation(LoadMoreConversationCallback callback);

}
