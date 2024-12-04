package com.zegocloud.zimkit.components.message.model;

import androidx.databinding.Bindable;
import com.zegocloud.zimkit.services.utils.ZIMMessageUtil;
import im.zego.zim.entity.ZIMCombineMessage;
import im.zego.zim.entity.ZIMMessage;
import java.util.List;
import java.util.stream.Collectors;

public class CombineMessageModel extends ZIMKitMessageModel {

    private String title;
    private String summary;
    public List<ZIMKitMessageModel> messageList;
    private String combineID;

    @Override

    public void onProcessMessage(ZIMMessage message) {
        if (message instanceof ZIMCombineMessage) {
            ZIMCombineMessage combineMessage = (ZIMCombineMessage) message;
            title = combineMessage.title;
            combineID = combineMessage.getCombineID();
            summary = combineMessage.summary;
            messageList = combineMessage.messageList.stream().map(ZIMMessageUtil::parseZIMMessageToModel)
                .collect(Collectors.toList());
        }
    }

    @Bindable
    public String getTitle() {
        return title;
    }

    @Bindable
    public String getSummary() {
        return summary;
    }

    @Bindable
    public List<ZIMKitMessageModel> getMessageList() {
        return messageList;
    }
}
