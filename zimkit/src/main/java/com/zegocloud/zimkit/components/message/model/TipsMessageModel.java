package com.zegocloud.zimkit.components.message.model;

import androidx.databinding.Bindable;
import com.zegocloud.zimkit.R;
import com.zegocloud.zimkit.services.internal.ZIMKitCore;
import com.zegocloud.zimkit.services.model.ZIMKitUser;
import im.zego.zim.entity.ZIMMessage;
import im.zego.zim.entity.ZIMTipsMessage;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

public class TipsMessageModel extends ZIMKitMessageModel {

    public ZIMKitTipsMessageEvent event;
    public ZIMKitUser operatedUser;
    public ArrayList<ZIMKitUser> targetUserList;
    public ZIMKitTipsMessageChangeInfo changeInfo;
    private String mContent;

    @Override
    public void onProcessMessage(ZIMMessage message) {
        if (message instanceof ZIMTipsMessage) {
            ZIMTipsMessage tipsMessage = (ZIMTipsMessage) message;
            event = ZIMKitTipsMessageEvent.getZIMKitTipsMessageEvent(tipsMessage.event.value());
            if (tipsMessage.changeInfo != null) {
                ZIMKitTipsMessageChangeInfoType changeInfoType = ZIMKitTipsMessageChangeInfoType.getZIMKitTipsMessageChangeInfoType(
                    tipsMessage.changeInfo.type.value());
                changeInfo = new ZIMKitTipsMessageChangeInfo();
                changeInfo.type = changeInfoType;
            }
            operatedUser = new ZIMKitUser();
            operatedUser.setId(tipsMessage.operatedUser.userID);
            operatedUser.setName(tipsMessage.operatedUser.userName);
            operatedUser.setAvatarUrl(tipsMessage.operatedUser.userAvatarUrl);
            targetUserList = tipsMessage.targetUserList.stream().map(zimUserInfo -> {
                ZIMKitUser zimKitUser = new ZIMKitUser();
                zimKitUser.setId(zimUserInfo.userID);
                zimKitUser.setName(zimUserInfo.userName);
                zimKitUser.setAvatarUrl(zimUserInfo.userAvatarUrl);
                return zimKitUser;
            }).collect(Collectors.toCollection(ArrayList::new));

            Optional<String> namesOpt = targetUserList.stream().map(ZIMKitUser::getName)
                .reduce((s, s2) -> s + "," + s2);

            if (event == ZIMKitTipsMessageEvent.GROUP_CREATED) {
                mContent = ZIMKitCore.getInstance().getApplication()
                    .getString(R.string.zimkit_tips_group_create, operatedUser.getName(), namesOpt.get());
            } else if (event == ZIMKitTipsMessageEvent.GROUP_INVITED) {
                mContent = ZIMKitCore.getInstance().getApplication()
                    .getString(R.string.zimkit_tips_group_invite, operatedUser.getName(), namesOpt.get());
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
