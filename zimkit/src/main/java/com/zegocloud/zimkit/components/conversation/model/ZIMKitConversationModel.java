package com.zegocloud.zimkit.components.conversation.model;

import android.text.TextUtils;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.zegocloud.zimkit.common.utils.ZIMKitDateUtils;
import com.zegocloud.zimkit.BR;
import java.util.Objects;

import im.zego.zim.entity.ZIMConversation;
import im.zego.zim.entity.ZIMMessage;
import im.zego.zim.entity.ZIMTextMessage;
import im.zego.zim.enums.ZIMConversationEvent;
import im.zego.zim.enums.ZIMConversationType;
import im.zego.zim.enums.ZIMMessageSentStatus;
import im.zego.zim.enums.ZIMMessageType;
import com.zegocloud.zimkit.R;
import com.zegocloud.zimkit.services.internal.ZIMKitCore;

public class ZIMKitConversationModel extends BaseObservable {

    private String mName;
    private String mTime;
    private Integer mUnReadCount;
    private String mAvatar;
    private final ZIMConversation mConversation;
    private String mLastMsgContent;
    private ZIMConversationEvent mConversationEvent;

    public ZIMKitConversationModel(ZIMConversation conversation, ZIMConversationEvent conversationEvent) {
        this.setLastMsgContent(conversation);
        this.setAvatar(conversation.conversationAvatarUrl);
        this.setName(conversation);
        this.setTime(conversation.lastMessage);
        this.setUnReadCount(conversation.unreadMessageCount);
        this.mConversationEvent = conversationEvent;
        this.mConversation = conversation;
    }

    private void setName(ZIMConversation conversation) {
        this.mName = TextUtils.isEmpty(conversation.conversationName)
                ? conversation.conversationID
                : conversation.conversationName;
        notifyPropertyChanged(BR.name);
    }

    private void setTime(ZIMMessage message) {
        if (message == null || message.getTimestamp() == 0) {
            return;
        }
        long time = message.getTimestamp();
        this.mTime = ZIMKitDateUtils.getMessageDate(time, false);
        notifyPropertyChanged(BR.time);
    }

    private void setUnReadCount(Integer mUnReadCount) {
        this.mUnReadCount = mUnReadCount;
        notifyPropertyChanged(BR.unReadCount);
    }

    private void setAvatar(String mAvatar) {
        this.mAvatar = mAvatar;
        notifyPropertyChanged(BR.lastMsgContent);
    }

    private void setLastMsgContent(ZIMConversation conversation) {
        ZIMMessage message = conversation.lastMessage;
        if (message == null) {
            return;
        }
        if (message.getType() == ZIMMessageType.TEXT) {
            ZIMTextMessage textMessage = (ZIMTextMessage) message;
            mLastMsgContent = textMessage.message;
        } else if (message.getType() == ZIMMessageType.IMAGE) {
            mLastMsgContent = ZIMKitCore.getInstance().getApplication().getString(R.string.zimkit_message_photo);
        } else if (message.getType() == ZIMMessageType.VIDEO) {
            mLastMsgContent = ZIMKitCore.getInstance().getApplication().getString(R.string.zimkit_message_video);
        } else if (message.getType() == ZIMMessageType.AUDIO) {
            mLastMsgContent = ZIMKitCore.getInstance().getApplication().getString(R.string.zimkit_message_audio);
        } else if (message.getType() == ZIMMessageType.FILE) {
            mLastMsgContent = ZIMKitCore.getInstance().getApplication().getString(R.string.zimkit_message_file);
        } else {
            mLastMsgContent = ZIMKitCore.getInstance().getApplication().getString(R.string.zimkit_message_unknown);
        }
        notifyPropertyChanged(BR.lastMsgContent);
    }

    @Bindable
    public String getLastMsgContent() {
        return mLastMsgContent;
    }

    @Bindable
    public String getName() {
        return mName;
    }

    @Bindable
    public String getTime() {
        return mTime;
    }

    @Bindable
    public Integer getUnReadCount() {
        return mUnReadCount;
    }

    @Bindable
    public String getAvatar() {
        return mAvatar;
    }

    public ZIMConversation getConversation() {
        return mConversation;
    }

    public String getConversationID() {
        return mConversation.conversationID;
    }

    public ZIMConversationType getType() {
        return mConversation.type;
    }

    public ZIMMessageSentStatus getSendState() {
        if (mConversation.lastMessage == null) {
            return ZIMMessageSentStatus.UNKNOWN;
        } else {
            return mConversation.lastMessage.getSentStatus();
        }
    }

    public ZIMConversationEvent getConversationEvent() {
        return mConversationEvent;
    }

    public boolean isShowMessageFailTip() {
        if (getConversation() == null || getConversation().lastMessage == null) {
            return false;
        }
        boolean sentStatus = getConversation().lastMessage.getSentStatus() == ZIMMessageSentStatus.FAILED;
        boolean conversationEvent = getConversationEvent().value() == ZIMConversationEvent.DISABLED.value() || getConversationEvent().value() == ZIMConversationEvent.UNKNOWN.value();
        return sentStatus || conversationEvent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ZIMKitConversationModel model = (ZIMKitConversationModel) o;
        return Objects.equals(model.getConversation().conversationID, mConversation.conversationID)
                && Objects.equals(model.getConversation().orderKey, mConversation.orderKey)
                && Objects.equals(model.getConversation().type.value(), mConversation.type.value());
    }

    @Override
    public int hashCode() {
        return Objects.hash(mConversation.conversationID, mConversation.orderKey, mConversation.type.value());
    }

}
