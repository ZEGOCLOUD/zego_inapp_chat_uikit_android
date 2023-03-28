package com.zegocloud.zimkit.components.message.model;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

public class ZIMKitEmojiItemModel extends BaseObservable {

    private String mEmojiContent;

    public ZIMKitEmojiItemModel(String emoji) {
        this.mEmojiContent = emoji;
    }

    @Bindable
    public String getEmojiContent() {
        return mEmojiContent;
    }

    public void setEmojiContent(String mEmojiContent) {
        this.mEmojiContent = mEmojiContent;
    }
}
