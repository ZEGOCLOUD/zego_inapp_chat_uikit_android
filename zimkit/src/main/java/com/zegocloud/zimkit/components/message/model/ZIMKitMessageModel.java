package com.zegocloud.zimkit.components.message.model;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import com.zegocloud.zimkit.BR;
import im.zego.zim.entity.ZIMMessage;
import im.zego.zim.entity.ZIMMessageReaction;
import im.zego.zim.enums.ZIMMessageDirection;
import im.zego.zim.enums.ZIMMessageSentStatus;
import java.util.ArrayList;

public abstract class ZIMKitMessageModel extends BaseObservable {

    private String mAvatar;
    private String mNickName;
    private ZIMMessage mMessage;
    private ZIMMessageSentStatus sentStatus = ZIMMessageSentStatus.SENDING;
    //Whether messages are sent or received
    private ZIMMessageDirection direction = ZIMMessageDirection.SEND;
    private String extra;
    private boolean isCheck = false;
    private ArrayList<ZIMMessageReaction> reactions;

    public void setCommonAttribute(ZIMMessage message) {
        if (message == null) {
            return;
        }
        this.mMessage = message;
        this.direction = message.getDirection() == null ? ZIMMessageDirection.SEND : message.getDirection();
        this.sentStatus = message.getSentStatus() == null ? ZIMMessageSentStatus.SENDING : message.getSentStatus();
        setReactions(message.getReactions());
    }

    /**
     * ZIMMessage Parsed as ZIMKitMessageModel
     *
     * @param message
     */
    public abstract void onProcessMessage(ZIMMessage message);

    @Bindable
    public String getAvatar() {
        return mAvatar;
    }

    public void setAvatar(String mAvatar) {
        this.mAvatar = mAvatar;
    }

    @Bindable
    public String getNickName() {
        return mNickName == null ? "" : mNickName;
    }

    public void setNickName(String nickName) {
        this.mNickName = nickName;
    }

    public void setSentStatus(ZIMMessageSentStatus sentStatus) {
        this.sentStatus = sentStatus;
        notifyPropertyChanged(BR.sentStatus);
    }

    @Bindable
    public ZIMMessageSentStatus getSentStatus() {
        return sentStatus;
    }

    public ZIMMessage getMessage() {
        return mMessage;
    }

    @Bindable
    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public ZIMMessageDirection getDirection() {
        return direction;
    }

    public void setDirection(ZIMMessageDirection direction) {
        this.direction = direction;
    }

    public int getType() {
        if (mMessage == null) {
            return 999;
        } else {
            return mMessage.getType().value();
        }
    }

    @Bindable
    public boolean isCheck() {
        return isCheck;
    }

    public void setCheck(boolean check) {
        this.isCheck = check;
        notifyPropertyChanged(BR.check);
    }

    @Bindable
    public ArrayList<ZIMMessageReaction> getReactions() {
        return reactions;
    }

    public void setReactions(ArrayList<ZIMMessageReaction> reactions) {
        this.reactions = reactions;
        notifyPropertyChanged(BR.reactions);
    }
}
