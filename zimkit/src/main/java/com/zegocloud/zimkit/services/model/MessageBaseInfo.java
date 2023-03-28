package com.zegocloud.zimkit.services.model;

import im.zego.zim.enums.ZIMConversationType;
import im.zego.zim.enums.ZIMMessageDirection;
import im.zego.zim.enums.ZIMMessageSentStatus;

public class MessageBaseInfo {

    public long messageID;
    public long localMessageID;
    public String senderUserID;
    public String conversationID;
    public ZIMConversationType conversationType = ZIMConversationType.PEER;
    public ZIMMessageDirection direction = ZIMMessageDirection.SEND;
    public ZIMMessageSentStatus sentStatus = ZIMMessageSentStatus.SENDING;
    public long timestamp;
    public long conversationSeq;
    public long orderKey;
    public boolean isUserInserted;

    public String senderUserName;
    public String senderUserAvatarUrl;

}
