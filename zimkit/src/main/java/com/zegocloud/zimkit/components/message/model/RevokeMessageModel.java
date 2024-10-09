package com.zegocloud.zimkit.components.message.model;

import androidx.databinding.Bindable;
import com.zegocloud.zimkit.R;
import com.zegocloud.zimkit.services.ZIMKit;
import com.zegocloud.zimkit.services.internal.ZIMKitCore;
import im.zego.zim.callback.ZIMUsersInfoQueriedCallback;
import im.zego.zim.entity.ZIMError;
import im.zego.zim.entity.ZIMErrorUserInfo;
import im.zego.zim.entity.ZIMMessage;
import im.zego.zim.entity.ZIMRevokeMessage;
import im.zego.zim.entity.ZIMUserFullInfo;
import im.zego.zim.entity.ZIMUsersInfoQueryConfig;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class RevokeMessageModel extends ZIMKitMessageModel {

    private String mContent;

    @Override
    public void onProcessMessage(ZIMMessage message) {
        if (message instanceof ZIMRevokeMessage) {
            ZIMRevokeMessage revokeMessage = (ZIMRevokeMessage) message;
            if (Objects.equals(revokeMessage.getSenderUserID(), ZIMKit.getLocalUser().getId())) {
                String you = ZIMKitCore.getInstance().getApplication().getString(R.string.zimkit_you);
                mContent =
                    you + " " + ZIMKitCore.getInstance().getApplication().getString(R.string.zimkit_message_revoke);
            } else {
                ArrayList<String> stringList = new ArrayList<>(
                    Collections.singletonList(revokeMessage.getSenderUserID()));
                ZIMKitCore.getInstance()
                    .queryUsersInfo(stringList, new ZIMUsersInfoQueryConfig(), new ZIMUsersInfoQueriedCallback() {
                        @Override
                        public void onUsersInfoQueried(ArrayList<ZIMUserFullInfo> userList,
                            ArrayList<ZIMErrorUserInfo> errorUserList, ZIMError errorInfo) {
                            if (!userList.isEmpty()) {
                                mContent =
                                    userList.get(0).baseInfo.userName + " " + ZIMKitCore.getInstance().getApplication()
                                        .getString(R.string.zimkit_message_revoke);
                            } else {
                                mContent =
                                    revokeMessage.getSenderUserID() + " " + ZIMKitCore.getInstance().getApplication()
                                        .getString(R.string.zimkit_message_revoke);
                            }
                        }
                    });
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
