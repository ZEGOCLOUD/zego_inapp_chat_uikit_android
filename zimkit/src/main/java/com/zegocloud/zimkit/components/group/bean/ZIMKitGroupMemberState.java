package com.zegocloud.zimkit.components.group.bean;

public enum ZIMKitGroupMemberState {
    UNKNOWN(-1),
    QUIT(0),
    ENTER(1);

    private int value;

    private ZIMKitGroupMemberState(int value) {
        this.value = value;
    }

    public int value() {
        return this.value;
    }

    public static ZIMKitGroupMemberState getZIMGroupMemberState(int value) {
        try {
            if (QUIT.value == value) {
                return QUIT;
            }

            if (ENTER.value == value) {
                return ENTER;
            }
        } catch (Exception var2) {
            throw new RuntimeException("The enumeration cannot be found");
        }

        return UNKNOWN;
    }
}
