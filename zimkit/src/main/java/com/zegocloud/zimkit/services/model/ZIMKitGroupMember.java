package com.zegocloud.zimkit.services.model;

public class ZIMKitGroupMember {

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
}
