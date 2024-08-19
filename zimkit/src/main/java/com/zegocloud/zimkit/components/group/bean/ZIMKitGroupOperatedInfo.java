package com.zegocloud.zimkit.components.group.bean;

public class ZIMKitGroupOperatedInfo {

    public String userID;
    public String userName;
    public String memberNickname;
    public int memberRole;

    public ZIMKitGroupOperatedInfo() {
    }

    public String toString() {
        return "ZIMGroupOperatedInfo{operatedUserInfo=" + ", userID='" + this.userID + '\'' + ", userName='"
            + this.userName + '\'' + ", memberNickname='" + this.memberNickname + '\'' + ", memberRole="
            + this.memberRole + '}';
    }
}