package im.zego.zimkitmessages.widget.interfaces;

import android.view.View;

import im.zego.zimkitmessages.model.message.ZIMKitMessageModel;

public interface OnItemClickListener {

    void onMessageLongClick(View view, int position, ZIMKitMessageModel messageInfo);

}
