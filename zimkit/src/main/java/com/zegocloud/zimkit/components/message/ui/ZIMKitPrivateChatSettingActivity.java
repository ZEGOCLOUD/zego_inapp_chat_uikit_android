package com.zegocloud.zimkit.components.message.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import androidx.activity.ComponentActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.databinding.DataBindingUtil;
import com.zegocloud.zimkit.R;
import com.zegocloud.zimkit.common.ZIMKitConstant;
import com.zegocloud.zimkit.common.ZIMKitConstant.MessagePageConstant;
import com.zegocloud.zimkit.common.glide.ZIMKitGlideLoader;
import com.zegocloud.zimkit.components.message.ui.AsynchronousSwitch.Asynchronous;
import com.zegocloud.zimkit.databinding.ActivityPrivateChatSettingBinding;
import com.zegocloud.zimkit.services.internal.ZIMKitCore;
import com.zegocloud.zimkit.services.model.ZIMKitConversation;
import im.zego.zim.callback.ZIMConversationNotificationStatusSetCallback;
import im.zego.zim.callback.ZIMConversationPinnedStateUpdatedCallback;
import im.zego.zim.callback.ZIMUsersInfoQueriedCallback;
import im.zego.zim.entity.ZIMError;
import im.zego.zim.entity.ZIMErrorUserInfo;
import im.zego.zim.entity.ZIMUserFullInfo;
import im.zego.zim.entity.ZIMUsersInfoQueryConfig;
import im.zego.zim.enums.ZIMConversationNotificationStatus;
import im.zego.zim.enums.ZIMConversationType;
import im.zego.zim.enums.ZIMErrorCode;
import java.util.ArrayList;
import java.util.Collections;

public class ZIMKitPrivateChatSettingActivity extends ComponentActivity {

    private ActivityPrivateChatSettingBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_private_chat_setting);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        binding.titleBar.setTitle(getString(R.string.chat_setting));
        binding.titleBar.hideRightButton();

        Bundle bundle = getIntent().getBundleExtra(ZIMKitConstant.RouterConstant.KEY_BUNDLE);
        String title = bundle.getString(ZIMKitConstant.MessagePageConstant.KEY_TITLE);
        String id = bundle.getString(ZIMKitConstant.MessagePageConstant.KEY_ID);
        String avatar = bundle.getString(MessagePageConstant.KEY_AVATAR);

        ZIMKitConversation conversation = ZIMKitCore.getInstance().getZIMKitConversation(id);

        if (TextUtils.isEmpty(avatar)) {
            ArrayList<String> userIDs = new ArrayList<>(Collections.singletonList(id));
            ZIMKitCore.getInstance()
                .queryUserInfo(userIDs, new ZIMUsersInfoQueryConfig(), new ZIMUsersInfoQueriedCallback() {
                    @Override
                    public void onUsersInfoQueried(ArrayList<ZIMUserFullInfo> userList,
                        ArrayList<ZIMErrorUserInfo> errorUserList, ZIMError errorInfo) {
                        if (errorInfo.code == ZIMErrorCode.SUCCESS) {
                            if (!userList.isEmpty()) {
                                ZIMUserFullInfo zimUserFullInfo = userList.get(0);
                                ZIMKitGlideLoader.displayMessageAvatarImage(binding.contactIcon,
                                    zimUserFullInfo.baseInfo.userAvatarUrl);
                                binding.contactName.setText(zimUserFullInfo.baseInfo.userName);
                            }
                        }
                    }
                });
        } else {
            ZIMKitGlideLoader.displayMessageAvatarImage(binding.contactIcon, avatar);
            binding.contactName.setText(title);
        }

        if (conversation != null) {
            binding.pinChat.realSetChecked(conversation.getZimConversation().isPinned);
            binding.pinChat.setAsynchronous(new Asynchronous() {
                @Override
                public void beforeApplyState(AsynchronousSwitch aSwitch, boolean originalCheck) {
                    ZIMKitCore.getInstance().setConversationPinnedState(originalCheck, id, ZIMConversationType.PEER,
                        new ZIMConversationPinnedStateUpdatedCallback() {
                            @Override
                            public void onConversationPinnedStateUpdated(String conversationID,
                                ZIMConversationType conversationType, ZIMError errorInfo) {
                                if (errorInfo.code == ZIMErrorCode.SUCCESS) {
                                    aSwitch.realSetChecked(originalCheck);
                                } else {
                                    aSwitch.realSetChecked(!originalCheck);
                                }
                            }
                        });
                }
            });
            ZIMConversationNotificationStatus notificationStatus = conversation.getZimConversation().notificationStatus;
            binding.doNotDisturb.realSetChecked(notificationStatus == ZIMConversationNotificationStatus.DO_NOT_DISTURB);
            binding.doNotDisturb.setAsynchronous(new Asynchronous() {
                @Override
                public void beforeApplyState(AsynchronousSwitch aSwitch, boolean originalCheck) {
                    ZIMConversationNotificationStatus target;
                    if (originalCheck) {
                        target = ZIMConversationNotificationStatus.DO_NOT_DISTURB;
                    } else {
                        target = ZIMConversationNotificationStatus.NOTIFY;
                    }
                    ZIMKitCore.getInstance().setConversationNotificationStatus(target, id, ZIMConversationType.PEER,
                        new ZIMConversationNotificationStatusSetCallback() {
                            @Override
                            public void onConversationNotificationStatusSet(String conversationID,
                                ZIMConversationType conversationType, ZIMError errorInfo) {
                                if (errorInfo.code == ZIMErrorCode.SUCCESS) {
                                    aSwitch.realSetChecked(originalCheck);
                                } else {
                                    aSwitch.realSetChecked(!originalCheck);
                                }
                            }
                        });
                }
            });
        } else {
            binding.pinChat.setVisibility(View.GONE);
            binding.doNotDisturb.setVisibility(View.GONE);
        }

    }

}