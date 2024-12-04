package com.zegocloud.zimkit.components.message.widget.input;

import com.zegocloud.zimkit.components.message.model.ZIMKitInputButtonModel;
import com.zegocloud.zimkit.components.message.model.ZIMKitMessageModel;

public interface InputCallback {

    void onSendTextMessage(String text,ZIMKitMessageModel replyMessage);

    void onSendAudioMessage(String path, int duration, ZIMKitMessageModel repliedMessage);

    void onClickSmallItem(int position, ZIMKitInputButtonModel itemModel, ZIMKitMessageModel repliedMessage);

    void onClickExtraItem(int position, ZIMKitInputButtonModel itemModel, ZIMKitMessageModel repliedMessage);

    void onClickExpandButton(CharSequence inputMsg, int selectionStart, int selectionEnd,
        ZIMKitMessageModel repliedMessage);

    void onRequestScrollToBottom();

}
