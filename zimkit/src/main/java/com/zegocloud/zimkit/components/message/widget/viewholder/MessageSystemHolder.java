package com.zegocloud.zimkit.components.message.widget.viewholder;

import androidx.databinding.ViewDataBinding;

import com.zegocloud.zimkit.components.message.model.ZIMKitMessageModel;

public class MessageSystemHolder extends MessageViewHolder {

    public MessageSystemHolder(ViewDataBinding binding) {
        super(binding);
    }

    @Override
    public void bind(int id, int position, ZIMKitMessageModel model) {
        super.bind(id, position, model);
    }
}
