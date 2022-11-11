package im.zego.zimkitmessages.widget.message.viewholder;

import androidx.databinding.ViewDataBinding;

import im.zego.zim.enums.ZIMMessageDirection;
import im.zego.zimkitmessages.databinding.MessageItemReceiveTextBinding;
import im.zego.zimkitmessages.databinding.MessageItemSendTextBinding;
import im.zego.zimkitmessages.model.message.TextMessageModel;
import im.zego.zimkitmessages.model.message.ZIMKitMessageModel;

public class TextMessageHolder extends MessageViewHolder {

    private MessageItemSendTextBinding sendTextBinding;
    private MessageItemReceiveTextBinding receiveTextBinding;

    public TextMessageHolder(ViewDataBinding binding) {
        super(binding);
        if (binding instanceof MessageItemSendTextBinding) {
            sendTextBinding = (MessageItemSendTextBinding) binding;
        } else if (binding instanceof MessageItemReceiveTextBinding) {
            receiveTextBinding = (MessageItemReceiveTextBinding) binding;
        }
    }

    @Override
    public void bind(int id, int position, ZIMKitMessageModel model) {
        super.bind(id, position, model);
        if (model instanceof TextMessageModel) {
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
