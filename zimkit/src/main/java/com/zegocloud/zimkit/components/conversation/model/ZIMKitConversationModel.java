package com.zegocloud.zimkit.components.conversation.model;

import android.text.TextUtils;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import com.zegocloud.zimkit.BR;
import com.zegocloud.zimkit.R;
import com.zegocloud.zimkit.common.utils.ZIMKitDateUtils;
import com.zegocloud.zimkit.components.group.bean.ZIMKitGroupMemberInfo;
import com.zegocloud.zimkit.components.message.model.CustomMessageModel;
import com.zegocloud.zimkit.components.message.model.TipsMessageModel;
import com.zegocloud.zimkit.components.message.utils.ChatMessageParser;
import com.zegocloud.zimkit.services.ZIMKit;
import com.zegocloud.zimkit.services.callback.QueryGroupMemberInfoCallback;
import com.zegocloud.zimkit.services.internal.ZIMKitCore;
import im.zego.zim.entity.ZIMConversation;
import im.zego.zim.entity.ZIMError;
import im.zego.zim.entity.ZIMMessage;
import im.zego.zim.entity.ZIMTextMessage;
import im.zego.zim.enums.ZIMConversationEvent;
import im.zego.zim.enums.ZIMConversationNotificationStatus;
import im.zego.zim.enums.ZIMConversationType;
import im.zego.zim.enums.ZIMMessageSentStatus;
import im.zego.zim.enums.ZIMMessageType;
import java.util.List;
import java.util.Objects;

public class ZIMKitConversationModel extends BaseObservable {

    private String mName;
    private String mTime;
    private Integer mUnReadCount;
    private String mAvatar;
    private final ZIMConversation mConversation;
    private String mLastMsgContent;
    private ZIMConversationEvent mConversationEvent;
    private boolean doNotDisturb;
    private boolean pinned;

    public ZIMKitConversationModel(ZIMConversation conversation, ZIMConversationEvent conversationEvent) {
        this.mConversation = conversation;
        this.setLastMsgContent(conversation);
        this.setAvatar(conversation.conversationAvatarUrl);
        this.setName(conversation);
        this.setTime(conversation.lastMessage);
        this.setUnReadCount(conversation.unreadMessageCount);
        this.setDoNotDisturb(conversation.notificationStatus == ZIMConversationNotificationStatus.DO_NOT_DISTURB);
        this.setPinned(conversation.isPinned);
        this.mConversationEvent = conversationEvent;
    }

    private void setName(ZIMConversation conversation) {
        this.mName = TextUtils.isEmpty(conversation.conversationName) ? conversation.conversationID
            : conversation.conversationName;
        notifyPropertyChanged(BR.name);
    }

    public void setDoNotDisturb(boolean doNotDisturb) {
        this.doNotDisturb = doNotDisturb;
        notifyPropertyChanged(BR.doNotDisturb);
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

    private void setPinned(boolean isPinned) {
        this.pinned = isPinned;
        notifyPropertyChanged(BR.pinned);
    }

    private void setAvatar(String mAvatar) {
        this.mAvatar = mAvatar;
        notifyPropertyChanged(BR.lastMsgContent);
    }

    private void setLastMsgContent(ZIMConversation conversation) {
        ZIMMessage lastMessage = conversation.lastMessage;
        if (lastMessage == null) {
            return;
        }
        if (conversation.type == ZIMConversationType.PEER) {
            if (lastMessage.getType() == ZIMMessageType.REVOKE) {
                String prefix = ZIMKitCore.getInstance().getApplication().getString(R.string.zimkit_you);
                updateLastMessage(lastMessage, prefix + " ");
            } else {
                updateLastMessage(lastMessage, "");
            }
        } else {
            if (Objects.equals(lastMessage.getSenderUserID(), ZIMKit.getLocalUser().getId())) {
                String prefix = ZIMKitCore.getInstance().getApplication().getString(R.string.zimkit_you);
                if (lastMessage.getType() == ZIMMessageType.TEXT || lastMessage.getType() == ZIMMessageType.IMAGE
                    || lastMessage.getType() == ZIMMessageType.AUDIO || lastMessage.getType() == ZIMMessageType.VIDEO
                    || lastMessage.getType() == ZIMMessageType.FILE) {
                    prefix = prefix + ":";
                } else if (lastMessage.getType() == ZIMMessageType.REVOKE
                    || lastMessage.getType() == ZIMMessageType.TIPS || lastMessage.getType() == ZIMMessageType.CUSTOM) {
                    prefix = prefix + " ";
                }
                updateLastMessage(lastMessage, prefix);
            } else {
                List<ZIMKitGroupMemberInfo> groupMemberList = ZIMKitCore.getInstance()
                    .getGroupMemberList(conversation.conversationID);
                if (groupMemberList == null || groupMemberList.isEmpty()) {
                    ZIMKitCore.getInstance()
                        .queryGroupMemberInfo(lastMessage.getSenderUserID(), conversation.conversationID,
                            new QueryGroupMemberInfoCallback() {
                                @Override
                                public void onQueryGroupMemberInfo(ZIMKitGroupMemberInfo member, ZIMError error) {
                                    String prefix;
                                    if (TextUtils.isEmpty(member.getNickName())) {
                                        prefix = member.getName();
                                    } else {
                                        prefix = member.getNickName();
                                    }
                                    if (lastMessage.getType() == ZIMMessageType.TEXT
                                        || lastMessage.getType() == ZIMMessageType.IMAGE
                                        || lastMessage.getType() == ZIMMessageType.AUDIO
                                        || lastMessage.getType() == ZIMMessageType.VIDEO
                                        || lastMessage.getType() == ZIMMessageType.FILE) {
                                        prefix = prefix + ":";
                                    } else if (lastMessage.getType() == ZIMMessageType.REVOKE
                                        || lastMessage.getType() == ZIMMessageType.TIPS
                                        || lastMessage.getType() == ZIMMessageType.CUSTOM) {
                                        prefix = prefix + " ";
                                    }
                                    updateLastMessage(lastMessage, prefix);
                                }
                            });
                } else {
                    String prefix = "";
                    for (ZIMKitGroupMemberInfo groupMemberInfo : groupMemberList) {
                        if (lastMessage.getSenderUserID().equals(groupMemberInfo.getId())) {
                            if (TextUtils.isEmpty(groupMemberInfo.getNickName())) {
                                prefix = groupMemberInfo.getName();
                            } else {
                                prefix = groupMemberInfo.getNickName();
                            }
                            break;
                        }
                    }
                    if (lastMessage.getType() == ZIMMessageType.TEXT || lastMessage.getType() == ZIMMessageType.IMAGE
                        || lastMessage.getType() == ZIMMessageType.AUDIO
                        || lastMessage.getType() == ZIMMessageType.VIDEO
                        || lastMessage.getType() == ZIMMessageType.FILE) {
                        prefix = prefix + ":";
                    } else if (lastMessage.getType() == ZIMMessageType.REVOKE
                        || lastMessage.getType() == ZIMMessageType.TIPS
                        || lastMessage.getType() == ZIMMessageType.CUSTOM) {
                        prefix = prefix + " ";
                    }
                    updateLastMessage(lastMessage, prefix);
                }
            }
        }
    }

    private static final String TAG = "ZIMKitConversationModel";

    private void updateLastMessage(ZIMMessage message, String prefix) {
        if (message.getType() == ZIMMessageType.TEXT) {
            ZIMTextMessage textMessage = (ZIMTextMessage) message;
            mLastMsgContent = prefix + textMessage.message;
        } else if (message.getType() == ZIMMessageType.IMAGE) {
            mLastMsgContent =
                prefix + ZIMKitCore.getInstance().getApplication().getString(R.string.zimkit_message_photo);
        } else if (message.getType() == ZIMMessageType.VIDEO) {
            mLastMsgContent =
                prefix + ZIMKitCore.getInstance().getApplication().getString(R.string.zimkit_message_video);
        } else if (message.getType() == ZIMMessageType.AUDIO) {
            mLastMsgContent =
                prefix + ZIMKitCore.getInstance().getApplication().getString(R.string.zimkit_message_audio);
        } else if (message.getType() == ZIMMessageType.FILE) {
            mLastMsgContent =
                prefix + ZIMKitCore.getInstance().getApplication().getString(R.string.zimkit_message_file);
        } else if (message.getType() == ZIMMessageType.REVOKE) {
            mLastMsgContent =
                prefix + ZIMKitCore.getInstance().getApplication().getString(R.string.zimkit_message_revoke);
        } else if (message.getType() == ZIMMessageType.TIPS) {
            TipsMessageModel tipsMessageModel = (TipsMessageModel) ChatMessageParser.parseMessage(message);
            mLastMsgContent = tipsMessageModel.getContent();
        } else if (message.getType() == ZIMMessageType.CUSTOM) {
            CustomMessageModel customMessageModel = (CustomMessageModel) ChatMessageParser.parseMessage(message);
            mLastMsgContent = customMessageModel.getContent();
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

    @Bindable
    public boolean isPinned() {
        return pinned;
    }

    @Bindable
    public boolean isDoNotDisturb() {
        return doNotDisturb;
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
        boolean conversationEvent = getConversationEvent().value() == ZIMConversationEvent.DISABLED.value()
            || getConversationEvent().value() == ZIMConversationEvent.UNKNOWN.value();
        return sentStatus || conversationEvent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ZIMKitConversationModel model = (ZIMKitConversationModel) o;
        return Objects.equals(model.getConversation().conversationID, mConversation.conversationID) && Objects.equals(
            model.getConversation().orderKey, mConversation.orderKey) && Objects.equals(
            model.getConversation().type.value(), mConversation.type.value()) && Objects.equals(mLastMsgContent,
            model.mLastMsgContent);
    }

    @Override
    public int hashCode() {
        if (mConversation != null) {
            return Objects.hash(mConversation.conversationID, mConversation.orderKey, mConversation.type.value());
        } else {
            return super.hashCode();
        }
    }

}
