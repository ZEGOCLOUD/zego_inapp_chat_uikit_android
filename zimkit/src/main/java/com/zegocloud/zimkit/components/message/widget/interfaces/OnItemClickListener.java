package com.zegocloud.zimkit.components.message.widget.interfaces;

import android.view.View;

import com.zegocloud.zimkit.components.message.model.ZIMKitMessageModel;

public interface OnItemClickListener {

    void onMessageLongClick(View view, int position, ZIMKitMessageModel messageInfo);

}
