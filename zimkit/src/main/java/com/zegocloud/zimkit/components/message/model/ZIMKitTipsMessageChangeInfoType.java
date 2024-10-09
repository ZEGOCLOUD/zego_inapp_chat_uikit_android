package com.zegocloud.zimkit.components.message.model;

public enum ZIMKitTipsMessageChangeInfoType {
    UNKNOWN(0), GROUP_DATA_CHANGED(1), GROUP_NOTICE_CHANGED(2), GROUP_NAME_CHANGED(3), GROUP_AVATAR_URL_CHANGED(
        4), GROUP_MUTE_CHANGED(5), GROUP_OWNER_TRANSFERRED(10), GROUP_MEMBER_ROLE_CHANGED(
        11), GROUP_MEMBER_MUTE_CHANGED(12);

    private int value;

    private ZIMKitTipsMessageChangeInfoType(int value) {
        this.value = value;
    }

    public int value() {
        return this.value;
    }

    public static ZIMKitTipsMessageChangeInfoType getZIMKitTipsMessageChangeInfoType(int value) {
        try {
            if (GROUP_DATA_CHANGED.value == value) {
                return GROUP_DATA_CHANGED;
            }

            if (GROUP_NOTICE_CHANGED.value == value) {
                return GROUP_NOTICE_CHANGED;
            }

            if (GROUP_NAME_CHANGED.value == value) {
                return GROUP_NAME_CHANGED;
            }

            if (GROUP_AVATAR_URL_CHANGED.value == value) {
                return GROUP_AVATAR_URL_CHANGED;
            }

            if (GROUP_MUTE_CHANGED.value == value) {
                return GROUP_MUTE_CHANGED;
            }

            if (GROUP_OWNER_TRANSFERRED.value == value) {
                return GROUP_OWNER_TRANSFERRED;
            }

            if (GROUP_MEMBER_ROLE_CHANGED.value == value) {
                return GROUP_MEMBER_ROLE_CHANGED;
            }

            if (GROUP_MEMBER_MUTE_CHANGED.value == value) {
                return GROUP_MEMBER_MUTE_CHANGED;
            }
        } catch (Exception var2) {
            throw new RuntimeException("The enumeration cannot be found");
        }

        return UNKNOWN;
    }
}
