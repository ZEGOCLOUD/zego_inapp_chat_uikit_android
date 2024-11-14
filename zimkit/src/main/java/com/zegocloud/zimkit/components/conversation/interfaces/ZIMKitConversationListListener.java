package com.zegocloud.zimkit.components.conversation.interfaces;

import com.zegocloud.zimkit.services.model.ZIMKitConversation;
import com.zegocloud.zimkit.components.conversation.model.DefaultAction;
import com.zegocloud.zimkit.components.conversation.ui.ZIMKitConversationFragment;
import im.zego.zim.entity.ZIMConversation;

public interface ZIMKitConversationListListener {

    default void onConversationListClick(ZIMKitConversationFragment conversationFragment,
        ZIMKitConversation conversation, DefaultAction defaultAction) {
    }

    default void onConversationDeleted(ZIMConversation conversation, int position) {

    }

    default boolean shouldHideSwipePinnedItem(ZIMConversation conversation, int position) {
        return true;
    }

    default boolean shouldHideSwipeDeleteItem(ZIMConversation conversation, int position) {
        return true;
    }
}
