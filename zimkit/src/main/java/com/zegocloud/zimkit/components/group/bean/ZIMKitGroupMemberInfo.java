package com.zegocloud.zimkit.components.group.bean;

import com.zegocloud.zimkit.services.model.GroupMemberRole;
import im.zego.zim.entity.ZIMGroupMemberInfo;

public class ZIMKitGroupMemberInfo {

    private String id;
    private String name;
    private String avatarUrl;
    private String nickName;
    private GroupMemberRole role;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public GroupMemberRole getRole() {
        return role;
    }

    public void setRole(GroupMemberRole role) {
        this.role = role;
    }

    public static ZIMKitGroupMemberInfo transGroupMember(ZIMGroupMemberInfo info) {
        ZIMKitGroupMemberInfo member = new ZIMKitGroupMemberInfo();
        if (info == null) {
            return member;
        }
        member.setId(info.userID);
        member.setName(info.userName);
        member.setNickName(info.memberNickname);
        member.setAvatarUrl(info.userAvatarUrl);
        member.setRole(info.memberRole == 1 ? GroupMemberRole.OWNER : GroupMemberRole.MEMBER);
        return member;
    }
}
