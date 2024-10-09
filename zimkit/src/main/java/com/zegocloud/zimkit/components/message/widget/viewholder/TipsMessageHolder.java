package com.zegocloud.zimkit.components.message.widget.viewholder;

import androidx.databinding.ViewDataBinding;
import com.zegocloud.zimkit.components.message.model.TipsMessageModel;
import com.zegocloud.zimkit.components.message.model.ZIMKitMessageModel;
import com.zegocloud.zimkit.databinding.ZimkitItemMessageTipsBinding;

public class TipsMessageHolder extends MessageViewHolder {

    private ZimkitItemMessageTipsBinding binding;

    public TipsMessageHolder(ViewDataBinding binding) {
        super(binding);
        if (binding instanceof ZimkitItemMessageTipsBinding) {
            this.binding = (ZimkitItemMessageTipsBinding) binding;
        }
    }

    @Override
    public void bind(int id, int position, ZIMKitMessageModel model) {
        super.bind(id, position, model);
        if (model instanceof TipsMessageModel) {
            TipsMessageModel tipsMessageModel = (TipsMessageModel) model;
            msgContent = binding.tvMessage;
        }
    }
}
