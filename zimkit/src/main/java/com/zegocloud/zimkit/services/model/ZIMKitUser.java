package com.zegocloud.zimkit.services.model;

public class ZIMKitUser {

    /**
     * id: 1 to 32 characters, can only contain digits, letters, and the following special characters:
     * '~', '!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '_', '+', '=', '-', '`', ';', '’', ',', '.', '<', '>', '/', '\'.
     */
    private String id;
    /**
     * User name: 1 - 64 characters.
     */
    private String name;
    /**
     * User avatar URL.
     */
    private String avatarUrl;

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
}
