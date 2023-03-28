package com.zegocloud.zimkit.components.message.interfaces;

import com.zegocloud.zimkit.components.message.model.ZIMKitHeaderBar;
import com.zegocloud.zimkit.components.message.ui.ZIMKitMessageFragment;

public interface ZIMKitMessagesListListener {

    ZIMKitHeaderBar getMessageListHeaderBar(ZIMKitMessageFragment fragment);

}
