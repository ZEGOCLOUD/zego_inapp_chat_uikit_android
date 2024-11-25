package com.zegocloud.zimkit.components.message.widget.viewholder;

import android.view.View;
import androidx.databinding.ViewDataBinding;
import com.zegocloud.zimkit.components.message.model.TextMessageModel;
import com.zegocloud.zimkit.components.message.model.ZIMKitMessageModel;
import com.zegocloud.zimkit.databinding.ZimkitItemMessageReceiveTextBinding;
import com.zegocloud.zimkit.databinding.ZimkitItemMessageSendTextBinding;
import im.zego.zim.enums.ZIMMessageDirection;
import java.util.Objects;

public class TextMessageHolder extends MessageViewHolder {

    private ZimkitItemMessageSendTextBinding sendTextBinding;
    private ZimkitItemMessageReceiveTextBinding receiveTextBinding;

    public TextMessageHolder(ViewDataBinding binding) {
        super(binding);
        if (binding instanceof ZimkitItemMessageSendTextBinding) {
            sendTextBinding = (ZimkitItemMessageSendTextBinding) binding;
        } else if (binding instanceof ZimkitItemMessageReceiveTextBinding) {
            receiveTextBinding = (ZimkitItemMessageReceiveTextBinding) binding;
        }
    }

    @Override
    public void bind(int id, int position, ZIMKitMessageModel model) {
        super.bind(id, position, model);
        if (model instanceof TextMessageModel) {
            if (Objects.equals(model.getMessage().localExtendedData, "loading")) {
                if (receiveTextBinding != null) {
                    receiveTextBinding.contentLoading.setVisibility(View.VISIBLE);
                    receiveTextBinding.tvMessage.setVisibility(View.GONE);
                }
            } else {
                if (receiveTextBinding != null) {
                    receiveTextBinding.contentLoading.setVisibility(View.GONE);
                    receiveTextBinding.tvMessage.setVisibility(View.VISIBLE);
                }

                TextMessageModel textMessageModel = (TextMessageModel) model;
                boolean isSend = model.getDirection() == ZIMMessageDirection.SEND;
                mMutiSelectCheckBox = isSend ? sendTextBinding.selectCheckbox : receiveTextBinding.selectCheckbox;
                msgContent = isSend ? sendTextBinding.tvMessage : receiveTextBinding.tvMessage;

                if (isSend) {
                    sendTextBinding.tvMessage.setOnLongClickListener(v -> {
                        initLongClickListener(v, position, model);
                        return true;
                    });
                } else {
                    receiveTextBinding.tvMessage.setOnLongClickListener(v -> {
                        initLongClickListener(v, position, model);
                        return true;
                    });
                }
            }
        }
    }
}
