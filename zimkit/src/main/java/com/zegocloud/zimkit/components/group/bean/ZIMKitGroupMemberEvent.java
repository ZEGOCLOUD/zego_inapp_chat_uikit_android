package com.zegocloud.zimkit.components.group.bean;

public enum ZIMKitGroupMemberEvent {
    UNKNOWN(-1),
    JOINED(1),
    LEFT(2),
    KICKED_OUT(4),
    INVITED(5);

    private int value;

    private ZIMKitGroupMemberEvent(int value) {
        this.value = value;
    }

    public int value() {
        return this.value;
    }

    public static ZIMKitGroupMemberEvent getZIMGroupMemberEvent(int value) {
        try {
            if (JOINED.value == value) {
                return JOINED;
            }

            if (INVITED.value == value) {
                return INVITED;
            }

            if (LEFT.value == value) {
                return LEFT;
            }

            if (KICKED_OUT.value == value) {
                return KICKED_OUT;
            }
        } catch (Exception var2) {
            throw new RuntimeException("The enumeration cannot be found");
        }

        return UNKNOWN;
    }
}
