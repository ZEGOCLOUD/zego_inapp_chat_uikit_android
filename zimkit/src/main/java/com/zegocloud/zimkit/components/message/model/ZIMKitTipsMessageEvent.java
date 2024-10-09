package com.zegocloud.zimkit.components.message.model;

public enum ZIMKitTipsMessageEvent {
    GROUP_CREATED(1), GROUP_DISMISSED(2), GROUP_JOINED(3), GROUP_INVITED(4), GROUP_LEFT(5), GROUP_KICKED_OUT(
        6), GROUP_INFO_CHANGED(7), GROUP_MEMBER_INFO_CHANGED(8);

    private int value;

    private ZIMKitTipsMessageEvent(int value) {
        this.value = value;
    }

    public int value() {
        return this.value;
    }

    public static ZIMKitTipsMessageEvent getZIMKitTipsMessageEvent(int value) {
        try {
            if (GROUP_CREATED.value == value) {
                return GROUP_CREATED;
            }

            if (GROUP_DISMISSED.value == value) {
                return GROUP_DISMISSED;
            }

            if (GROUP_JOINED.value == value) {
                return GROUP_JOINED;
            }

            if (GROUP_INVITED.value == value) {
                return GROUP_INVITED;
            }

            if (GROUP_LEFT.value == value) {
                return GROUP_LEFT;
            }

            if (GROUP_KICKED_OUT.value == value) {
                return GROUP_KICKED_OUT;
            }

            if (GROUP_INFO_CHANGED.value == value) {
                return GROUP_INFO_CHANGED;
            }

            if (GROUP_MEMBER_INFO_CHANGED.value == value) {
                return GROUP_MEMBER_INFO_CHANGED;
            }
        } catch (Exception var2) {
            throw new RuntimeException("The enumeration cannot be found");
        }

        return GROUP_CREATED;
    }
}
