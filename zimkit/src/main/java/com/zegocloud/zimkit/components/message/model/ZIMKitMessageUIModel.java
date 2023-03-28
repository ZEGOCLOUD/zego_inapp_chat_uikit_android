package com.zegocloud.zimkit.components.message.model;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DimenRes;
import androidx.core.content.ContextCompat;

import com.zegocloud.zimkit.R;
import com.zegocloud.zimkit.services.internal.ZIMKitCore;

public class ZIMKitMessageUIModel {
    public static
    float nameFontSP = ZIMKitCore.getInstance().getApplication().getResources().getDimension(R.dimen.message_other_side_nick_name_size);
    public @ColorInt
    static int nameColor = ContextCompat.getColor(ZIMKitCore.getInstance().getApplication(), R.color.color_666666);

    public static void setNameColor(@ColorRes int color) {
        nameColor = ContextCompat.getColor(ZIMKitCore.getInstance().getApplication(), color);
    }

    public static void setNameFontSP(@DimenRes int size) {
        nameFontSP = ZIMKitCore.getInstance().getApplication().getResources().getDimension(size);
    }

}
