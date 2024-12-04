package com.zegocloud.zimkit.components.message.model;

import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
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
    private CharSequence mContent;

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

            ForegroundColorSpan blueSpan = new ForegroundColorSpan(Color.parseColor("#3478FC"));
            ForegroundColorSpan graySpan = new ForegroundColorSpan(Color.parseColor("#b8b8b8"));
            if (event == ZIMKitTipsMessageEvent.GROUP_CREATED) {
                String targetNames = namesOpt.orElse("");
                String string = ZIMKitCore.getInstance().getApplication()
                    .getString(R.string.zimkit_tips_group_create, operatedUser.getName(), targetNames);
                int indexOfOperator = string.indexOf(operatedUser.getName());
                SpannableString spannableString = new SpannableString(string);
                spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#3478FC")), indexOfOperator,
                    indexOfOperator + operatedUser.getName().length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                if (targetNames.isEmpty()) {
                    spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#b8b8b8")),
                        indexOfOperator + operatedUser.getName().length(), string.length() ,
                        Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                } else {
                    int indexOfTargets = string.indexOf(targetNames);
                    spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#b8b8b8")),
                        indexOfOperator + operatedUser.getName().length(), indexOfTargets,
                        Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#3478FC")), indexOfTargets,
                        indexOfTargets + targetNames.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#b8b8b8")),
                        indexOfTargets + targetNames.length(), string.length() , Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                }

                mContent = spannableString;
            } else if (event == ZIMKitTipsMessageEvent.GROUP_INVITED) {
                String targetNames = namesOpt.orElse("");
                String string = ZIMKitCore.getInstance().getApplication()
                    .getString(R.string.zimkit_tips_group_invite, operatedUser.getName(), namesOpt.get());
                int indexOfOperator = string.indexOf(operatedUser.getName());
                SpannableString spannableString = new SpannableString(string);
                spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#3478FC")), indexOfOperator,
                    indexOfOperator + operatedUser.getName().length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                if (targetNames.isEmpty()) {
                    spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#b8b8b8")),
                        indexOfOperator + operatedUser.getName().length(), string.length() - 1,
                        Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                } else {
                    int indexOfTargets = string.indexOf(targetNames);
                    spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#b8b8b8")),
                        indexOfOperator + operatedUser.getName().length(), indexOfTargets,
                        Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#3478FC")), indexOfTargets,
                        indexOfTargets + targetNames.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#b8b8b8")),
                        indexOfTargets + targetNames.length(), string.length() , Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                }
                mContent = spannableString;
            }
        }

    }

    @Bindable
    public CharSequence getContent() {
        return mContent;
    }

    public void setContent(CharSequence content) {
        this.mContent = content;
    }
}
