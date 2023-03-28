package com.zegocloud.zimkit.services.callback;

import com.zegocloud.zimkit.services.model.ZIMKitConversation;
import java.util.ArrayList;

import im.zego.zim.entity.ZIMError;

public interface GetConversationListCallback {
    void onGetConversationList(ArrayList<ZIMKitConversation> conversations, ZIMError error);
}
