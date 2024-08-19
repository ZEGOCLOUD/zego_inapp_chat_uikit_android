package com.zegocloud.zimkit.components.message.model;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.databinding.BaseObservable;
import com.zegocloud.zimkit.services.config.ZIMKitInputButtonName;

public class ZIMKitInputButtonModel extends BaseObservable {

    private ZIMKitInputButtonName buttonName;
    private String desc;
    private Drawable smallIcon;
    private Drawable smallIconSelected;
    private Drawable expandIcon;

    public ZIMKitInputButtonModel(ZIMKitInputButtonName buttonName, String desc, Drawable smallIcon,
        Drawable smallIconSelected, Drawable expandIcon) {
        this.buttonName = buttonName;
        this.desc = desc;
        this.smallIcon = smallIcon;
        this.smallIconSelected = smallIconSelected;
        this.expandIcon = expandIcon;
    }

    public ZIMKitInputButtonModel(ZIMKitInputButtonName buttonName, Context context, @StringRes int desc,
        @DrawableRes int smallIcon,
        @DrawableRes int smallIconSelected, @DrawableRes int expandIcon) {
        this.buttonName = buttonName;
        this.desc = context.getString(desc);
        this.smallIcon = ContextCompat.getDrawable(context, smallIcon);
        this.smallIconSelected = ContextCompat.getDrawable(context, smallIconSelected);
        this.expandIcon = ContextCompat.getDrawable(context, expandIcon);
    }

    public ZIMKitInputButtonName getButtonName() {
        return buttonName;
    }

    public String getDesc() {
        return desc;
    }

    public Drawable getSmallIcon() {
        return smallIcon;
    }

    public Drawable getSmallIconSelected() {
        return smallIconSelected;
    }

    public Drawable getExpandIcon() {
        return expandIcon;
    }
}
