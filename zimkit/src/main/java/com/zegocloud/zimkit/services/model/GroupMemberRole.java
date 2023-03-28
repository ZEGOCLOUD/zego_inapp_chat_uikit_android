package com.zegocloud.zimkit.services.model;

public enum GroupMemberRole {

    OWNER(1),MEMBER(3);

    private int value;

    private GroupMemberRole(int value) {
        this.value = value;
    }

    public int value() {
        return this.value;
    }
}
