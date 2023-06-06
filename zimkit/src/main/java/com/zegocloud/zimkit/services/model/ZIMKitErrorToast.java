package com.zegocloud.zimkit.services.model;

public class ZIMKitErrorToast {
    public String message;
    public Boolean isShow = true;

    public ZIMKitErrorToast(String defaultMessage) {
        this.message = defaultMessage;
    }
}
