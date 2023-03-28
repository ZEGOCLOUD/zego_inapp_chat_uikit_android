package com.zegocloud.zimkit.services.callback;

import java.util.ArrayList;

import im.zego.zim.entity.ZIMError;
import com.zegocloud.zimkit.services.model.ZIMKitMessage;

public interface GetMessageListCallback {
    void onGetMessageList(ArrayList<ZIMKitMessage> messages, boolean hasMoreHistoryMessage, ZIMError error);
}
