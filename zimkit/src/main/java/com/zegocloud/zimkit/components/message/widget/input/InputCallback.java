package com.zegocloud.zimkit.components.message.widget.input;

import com.zegocloud.zimkit.components.message.model.ZIMKitInputButtonModel;
import com.zegocloud.zimkit.components.message.model.ZIMKitMessageModel;

public interface InputCallback {

    void onSendTextMessage(ZIMKitMessageModel model);

    void onSendAudioMessage(ZIMKitMessageModel model);

    void onClickSmallItem(int position, ZIMKitInputButtonModel itemModel);

    void onClickExpandItem(int position, ZIMKitInputButtonModel itemModel);
}
