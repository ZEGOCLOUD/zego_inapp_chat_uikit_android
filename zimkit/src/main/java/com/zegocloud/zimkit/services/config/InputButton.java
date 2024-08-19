package com.zegocloud.zimkit.services.config;

import android.graphics.drawable.Drawable;

public class InputButton {

    public String desc;
    public Drawable smallIcon;
    public Drawable smallIconSelected;
    public Drawable expandIcon;

    public InputButton(String desc, Drawable smallIcon, Drawable smallIconSelected, Drawable expandIcon) {
        this.desc = desc;
        this.smallIcon = smallIcon;
        this.smallIconSelected = smallIconSelected;
        this.expandIcon = expandIcon;
    }
}
