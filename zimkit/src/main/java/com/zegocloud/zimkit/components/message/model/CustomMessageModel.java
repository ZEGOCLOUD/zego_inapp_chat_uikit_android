package com.zegocloud.zimkit.components.message.model;

import androidx.databinding.Bindable;
import im.zego.zim.entity.ZIMCustomMessage;
import im.zego.zim.entity.ZIMMessage;
import im.zego.zim.enums.ZIMMessageType;

public class CustomMessageModel extends ZIMKitMessageModel {

    private String mContent;

    @Override
    public void onProcessMessage(ZIMMessage message) {
        if (message.getType() == ZIMMessageType.CUSTOM) {
            ZIMCustomMessage customMessage = (ZIMCustomMessage) message;
            if (customMessage.subType == 0) {
                mContent = customMessage.message;
            }
        }
    }

    @Bindable
    public String getContent() {
        return mContent;
    }

    public void setContent(String content) {
        this.mContent = content;
    }
}
