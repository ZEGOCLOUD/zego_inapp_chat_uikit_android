package com.zegocloud.zimkit.components.conversation.model;

import android.text.TextUtils;
import android.util.Log;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import com.zegocloud.uikit.plugin.signaling.ZegoSignalingPlugin;
import com.zegocloud.zimkit.BR;
import com.zegocloud.zimkit.R;
import com.zegocloud.zimkit.common.utils.ZIMKitDateUtils;
import com.zegocloud.zimkit.components.group.bean.ZIMKitGroupMemberInfo;
import com.zegocloud.zimkit.services.ZIMKit;
import com.zegocloud.zimkit.services.callback.QueryGroupMemberInfoCallback;
import com.zegocloud.zimkit.services.internal.ZIMKitCore;
import com.zegocloud.zimkit.services.utils.ZIMMessageUtil;
import im.zego.zim.callback.ZIMUsersInfoQueriedCallback;
import im.zego.zim.entity.ZIMConversation;
import im.zego.zim.entity.ZIMError;
import im.zego.zim.entity.ZIMErrorUserInfo;
import im.zego.zim.entity.ZIMMessage;
import im.zego.zim.entity.ZIMUserFullInfo;
import im.zego.zim.entity.ZIMUsersInfoQueryConfig;
import im.zego.zim.enums.ZIMConversationEvent;
import im.zego.zim.enums.ZIMConversationNotificationStatus;
import im.zego.zim.enums.ZIMConversationType;
import im.zego.zim.enums.ZIMErrorCode;
import im.zego.zim.enums.ZIMMessageSentStatus;
import im.zego.zim.enums.ZIMMessageType;
import java.util.ArrayList;
import java.util.Collections;
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

    private static final String TAG = "ZIMKitConversationModel";

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
        Log.d(TAG, "setDoNotDisturb() called with: doNotDisturb = [" + doNotDisturb + "]，conversation：" + mConversation);
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
                if (Objects.equals(lastMessage.getSenderUserID(), ZIMKitCore.getInstance().getLocalUser().getId())) {
                    String prefix = ZIMKitCore.getInstance().getApplication().getString(R.string.zimkit_you);
                    updateLastMessage(lastMessage, prefix + " ");
                } else {
                    ZegoSignalingPlugin.getInstance()
                        .queryUserInfo(Collections.singletonList(lastMessage.getSenderUserID()),
                            new ZIMUsersInfoQueryConfig(), new ZIMUsersInfoQueriedCallback() {
                                @Override
                                public void onUsersInfoQueried(ArrayList<ZIMUserFullInfo> userList,
                                    ArrayList<ZIMErrorUserInfo> errorUserList, ZIMError errorInfo) {
                                    if (errorInfo.code == ZIMErrorCode.SUCCESS) {
                                        String prefix;
                                        ZIMUserFullInfo memoryUserInfo = ZIMKitCore.getInstance()
                                            .getMemoryUserInfo(lastMessage.getSenderUserID());
                                        if (memoryUserInfo == null) {
                                            prefix = lastMessage.getSenderUserID();
                                        } else {
                                            prefix = memoryUserInfo.baseInfo.userName;
                                        }
                                        updateLastMessage(lastMessage, prefix + " ");
                                    }
                                }
                            });

                }
            } else {
                updateLastMessage(lastMessage, "");
            }
        } else {
            if (Objects.equals(lastMessage.getSenderUserID(), ZIMKit.getLocalUser().getId())) {
                String operatorName = ZIMKitCore.getInstance().getApplication().getString(R.string.zimkit_you);
                if (lastMessage.getType() == ZIMMessageType.TEXT || lastMessage.getType() == ZIMMessageType.IMAGE
                    || lastMessage.getType() == ZIMMessageType.AUDIO || lastMessage.getType() == ZIMMessageType.VIDEO
                    || lastMessage.getType() == ZIMMessageType.FILE
                    || lastMessage.getType() == ZIMMessageType.COMBINE) {
                    operatorName = operatorName + ":";
                } else if (lastMessage.getType() == ZIMMessageType.TIPS
                    || lastMessage.getType() == ZIMMessageType.CUSTOM) {
                    operatorName = "";
                } else if (lastMessage.getType() == ZIMMessageType.REVOKE) {
                }
                updateLastMessage(lastMessage, operatorName);
            } else {
                List<ZIMKitGroupMemberInfo> groupMemberList = ZIMKitCore.getInstance()
                    .getGroupMemberList(conversation.conversationID);
                if (groupMemberList == null || groupMemberList.isEmpty()) {
                    ZIMKitCore.getInstance()
                        .queryGroupMemberInfo(lastMessage.getSenderUserID(), conversation.conversationID,
                            new QueryGroupMemberInfoCallback() {
                                @Override
                                public void onQueryGroupMemberInfo(ZIMKitGroupMemberInfo member, ZIMError error) {
                                    String operatorName;
                                    if (TextUtils.isEmpty(member.getNickName())) {
                                        operatorName = member.getName();
                                    } else {
                                        operatorName = member.getNickName();
                                    }
                                    if (lastMessage.getType() == ZIMMessageType.TEXT
                                        || lastMessage.getType() == ZIMMessageType.IMAGE
                                        || lastMessage.getType() == ZIMMessageType.AUDIO
                                        || lastMessage.getType() == ZIMMessageType.VIDEO
                                        || lastMessage.getType() == ZIMMessageType.FILE
                                        || lastMessage.getType() == ZIMMessageType.COMBINE) {
                                        operatorName = operatorName + ":";
                                    } else if (lastMessage.getType() == ZIMMessageType.TIPS
                                        || lastMessage.getType() == ZIMMessageType.CUSTOM) {
                                        operatorName = "";
                                    } else if (lastMessage.getType() == ZIMMessageType.REVOKE) {
                                    }
                                    updateLastMessage(lastMessage, operatorName);
                                }
                            });
                } else {
                    String operatorName = "";
                    for (ZIMKitGroupMemberInfo groupMemberInfo : groupMemberList) {
                        if (lastMessage.getSenderUserID().equals(groupMemberInfo.getId())) {
                            if (TextUtils.isEmpty(groupMemberInfo.getNickName())) {
                                operatorName = groupMemberInfo.getName();
                            } else {
                                operatorName = groupMemberInfo.getNickName();
                            }
                            break;
                        }
                    }
                    if (TextUtils.isEmpty(operatorName)) {
                        operatorName = lastMessage.getSenderUserID();
                    }
                    if (lastMessage.getType() == ZIMMessageType.TEXT || lastMessage.getType() == ZIMMessageType.IMAGE
                        || lastMessage.getType() == ZIMMessageType.AUDIO
                        || lastMessage.getType() == ZIMMessageType.VIDEO
                        || lastMessage.getType() == ZIMMessageType.COMBINE
                        || lastMessage.getType() == ZIMMessageType.FILE) {
                        operatorName = operatorName + ":";
                    } else if (lastMessage.getType() == ZIMMessageType.TIPS
                        || lastMessage.getType() == ZIMMessageType.CUSTOM) {
                        operatorName = "";
                    } else if (lastMessage.getType() == ZIMMessageType.REVOKE) {

                    }
                    updateLastMessage(lastMessage, operatorName);
                }
            }
        }
    }

    private void updateLastMessage(ZIMMessage message, String prefix) {
        String content = ZIMMessageUtil.simplifyZIMMessageContent(message);
        if (!TextUtils.isEmpty(content)) {
            mLastMsgContent = prefix + content;
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
