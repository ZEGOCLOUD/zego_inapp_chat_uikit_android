package com.zegocloud.zimkit.components.conversation.interfaces;

import com.zegocloud.zimkit.services.model.ZIMKitConversation;
import com.zegocloud.zimkit.components.conversation.model.DefaultAction;
import com.zegocloud.zimkit.components.conversation.ui.ZIMKitConversationFragment;

public interface ZIMKitConversationListListener {

    default void onConversationListClick(ZIMKitConversationFragment conversationFragment,
        ZIMKitConversation conversation, DefaultAction defaultAction) {}

}
