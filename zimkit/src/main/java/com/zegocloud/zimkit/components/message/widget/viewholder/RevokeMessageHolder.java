package com.zegocloud.zimkit.components.message.widget.viewholder;

import androidx.databinding.ViewDataBinding;
import com.zegocloud.zimkit.components.message.model.RevokeMessageModel;
import com.zegocloud.zimkit.components.message.model.TipsMessageModel;
import com.zegocloud.zimkit.components.message.model.ZIMKitMessageModel;
import com.zegocloud.zimkit.databinding.ZimkitItemMessageRevokeBinding;

public class RevokeMessageHolder extends MessageViewHolder {

    private ZimkitItemMessageRevokeBinding binding;

    public RevokeMessageHolder(ViewDataBinding binding) {
        super(binding);
        if (binding instanceof ZimkitItemMessageRevokeBinding) {
            this.binding = (ZimkitItemMessageRevokeBinding) binding;
        }
    }

    @Override
    public void bind(int id, int position, ZIMKitMessageModel model) {
        super.bind(id, position, model);
    }
}
