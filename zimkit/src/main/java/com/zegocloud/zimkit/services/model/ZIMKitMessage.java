package com.zegocloud.zimkit.services.model;

import im.zego.zim.entity.ZIMMessage;
import im.zego.zim.enums.ZIMMessageType;

public class ZIMKitMessage {

    public ZIMMessage zim;
    public ZIMMessageType type = ZIMMessageType.UNKNOWN;
    public boolean canSendMessage = true;

    public MessageBaseInfo info = new MessageBaseInfo();
    public TextMessageContent textContent = new TextMessageContent();
    public SystemMessageContent systemContent = new SystemMessageContent();
    public ImageMessageContent imageContent = new ImageMessageContent();
    public AudioMessageContent audioContent = new AudioMessageContent();
    public VideoMessageContent videoContent = new VideoMessageContent();
    public FileMessageContent fileContent = new FileMessageContent();

}
