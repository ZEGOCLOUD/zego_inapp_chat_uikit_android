package com.zegocloud.zimkit.services.model;

import im.zego.zim.enums.ZIMMessageRevokeStatus;
import im.zego.zim.enums.ZIMMessageType;
import im.zego.zim.enums.ZIMRevokeType;

public class RevokeMessageContent {

    private ZIMRevokeType revokeType;
    private long revokeTimestamp;
    private String operatedUserID;
    private ZIMMessageType originalMessageType;
    private String originalTextMessageContent;
    private String revokeExtendedData;
    private ZIMMessageRevokeStatus revokeStatus;
}
