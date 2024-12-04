package com.zegocloud.zimkit.components.message.widget.interfaces;


import com.zegocloud.zimkit.components.message.model.ZIMKitMessageModel;

public interface OnPopActionClickListener {

    void onActionMultiSelectClick(ZIMKitMessageModel messageModel);

    void onActionReplyMessageClick(ZIMKitMessageModel repliedMessage);

    void onActionForwardMessageClick(ZIMKitMessageModel model);
}
