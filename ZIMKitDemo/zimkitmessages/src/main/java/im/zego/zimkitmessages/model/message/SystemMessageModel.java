package im.zego.zimkitmessages.model.message;

import android.text.TextUtils;

import androidx.databinding.Bindable;

import im.zego.zim.entity.ZIMMessage;
import im.zego.zim.entity.ZIMTextMessage;

public class SystemMessageModel extends ZIMKitMessageModel {

    private String mContent;

    @Override
    public void onProcessMessage(ZIMMessage message) {
        if (message instanceof ZIMTextMessage) {
            if (!TextUtils.isEmpty(((ZIMTextMessage) message).message)) {
                this.mContent = ((ZIMTextMessage) message).message;
            }
        }
    }

    @Bindable
    public String getContent() {
        return mContent;
    }

    public void setContent(String content) {
        this.mContent = content;
    }
}
