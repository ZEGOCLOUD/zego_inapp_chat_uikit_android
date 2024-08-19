package com.zegocloud.zimkit.components.message.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.ComponentActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AlertDialog.Builder;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import com.zegocloud.zimkit.R;
import com.zegocloud.zimkit.common.ZIMKitConstant;
import com.zegocloud.zimkit.common.utils.ZIMKitToastUtils;
import com.zegocloud.zimkit.components.group.bean.ZIMKitGroupMemberInfo;
import com.zegocloud.zimkit.components.group.ui.ZIMKitGroupMembersActivity;
import com.zegocloud.zimkit.components.message.adapter.GroupMemberShortcutAdapter;
import com.zegocloud.zimkit.components.message.ui.AsynchronousSwitch.Asynchronous;
import com.zegocloud.zimkit.components.message.utils.OnRecyclerViewItemTouchListener;
import com.zegocloud.zimkit.databinding.ActivityGroupChatSettingBinding;
import com.zegocloud.zimkit.services.ZIMKit;
import com.zegocloud.zimkit.services.ZIMKitDelegate;
import com.zegocloud.zimkit.services.callback.InviteUsersToJoinGroupCallback;
import com.zegocloud.zimkit.services.callback.QueryGroupMemberListCallback;
import com.zegocloud.zimkit.services.internal.ZIMKitCore;
import com.zegocloud.zimkit.services.model.ZIMKitConversation;
import im.zego.zim.callback.ZIMConversationNotificationStatusSetCallback;
import im.zego.zim.callback.ZIMConversationPinnedStateUpdatedCallback;
import im.zego.zim.entity.ZIMError;
import im.zego.zim.entity.ZIMErrorUserInfo;
import im.zego.zim.entity.ZIMGroupMemberInfo;
import im.zego.zim.entity.ZIMGroupMemberQueryConfig;
import im.zego.zim.entity.ZIMGroupOperatedInfo;
import im.zego.zim.enums.ZIMConversationNotificationStatus;
import im.zego.zim.enums.ZIMConversationType;
import im.zego.zim.enums.ZIMErrorCode;
import im.zego.zim.enums.ZIMGroupMemberEvent;
import im.zego.zim.enums.ZIMGroupMemberState;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ZIMKitGroupChatSettingActivity extends ComponentActivity {


    private ActivityGroupChatSettingBinding binding;
    private String mId;

    private static final String TAG = "ZIMKitGroupChatSettingA";
    private ZIMKitDelegate zimKitDelegate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_group_chat_setting);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Bundle bundle = getIntent().getBundleExtra(ZIMKitConstant.RouterConstant.KEY_BUNDLE);
        String title = bundle.getString(ZIMKitConstant.MessagePageConstant.KEY_TITLE);
        mId = bundle.getString(ZIMKitConstant.MessagePageConstant.KEY_ID);
        binding.groupSetTitleBar.hideRightButton();
        binding.groupSetTitleBar.setTitle(getString(R.string.chat_setting));
        //        binding.groupChatMembersShortcut.setAdapter();
        GroupMemberShortcutAdapter shortcutAdapter = new GroupMemberShortcutAdapter();
        List<ZIMKitGroupMemberInfo> groupMemberList = ZIMKitCore.getInstance().getGroupMemberList(mId);
        if (groupMemberList != null) {
            binding.groupMembersCount.setText(getString(R.string.group_members_detail, groupMemberList.size()));
            shortcutAdapter.setMemberList(groupMemberList);
        }
        ZIMGroupMemberQueryConfig config = new ZIMGroupMemberQueryConfig();
        config.count = 100;
        ZIMKitCore.getInstance().queryGroupMemberList(mId, config, new QueryGroupMemberListCallback() {
            @Override
            public void onGroupMemberListQueried(String groupID, ArrayList<ZIMKitGroupMemberInfo> userList,
                int nextFlag, ZIMError errorInfo) {
                binding.groupMembersCount.setText(getString(R.string.group_members_detail, userList.size()));
                shortcutAdapter.setMemberList(userList);
            }
        });
        binding.groupChatMembersRecyclerview.setAdapter(shortcutAdapter);
        binding.groupChatMembersRecyclerview.setLayoutManager(new GridLayoutManager(this, 5));
        binding.groupChatMembersRecyclerview.addOnItemTouchListener(
            new OnRecyclerViewItemTouchListener(binding.groupChatMembersRecyclerview) {
                @Override
                public void onItemClick(ViewHolder vh) {
                    super.onItemClick(vh);
                    int position = vh.getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        ZIMKitGroupMemberInfo groupMember = shortcutAdapter.getItemData(position);
                        if (groupMember.getId() == null) {
                            AlertDialog.Builder builder = new Builder(ZIMKitGroupChatSettingActivity.this);
                            ViewGroup viewGroup = (ViewGroup) View.inflate(ZIMKitGroupChatSettingActivity.this,
                                R.layout.zimkit_dialog_confirm_content_edittext, null);
                            TextView title = viewGroup.findViewById(R.id.title);
                            title.setText(R.string.zimkit_add_group_member);
                            EditText inputLayout = viewGroup.findViewById(R.id.content);
                            inputLayout.setHint(R.string.zimkit_input_member_id);
                            builder.setView(viewGroup);
                            AlertDialog dialog = builder.create();
                            viewGroup.findViewById(R.id.confirm).setOnClickListener(v -> {
                                String userID = inputLayout.getText().toString();
                                if (TextUtils.isEmpty(userID)) {
                                    inputLayout.setError(getString(R.string.zimkit_input_member_id));
                                    return;
                                }
                                ZIMKit.inviteUsersToJoinGroup(Collections.singletonList(userID), mId,
                                    new InviteUsersToJoinGroupCallback() {
                                        @Override
                                        public void onInviteUsersToJoinGroup(
                                            ArrayList<ZIMKitGroupMemberInfo> groupMembers,
                                            ArrayList<ZIMErrorUserInfo> inviteUserErrors, ZIMError error) {
                                            if (error.code != ZIMErrorCode.SUCCESS) {
                                                ZIMKitToastUtils.showToast( error.message);
                                            }
                                            dialog.dismiss();
                                        }
                                    });
                            });
                            viewGroup.findViewById(R.id.cancel).setOnClickListener(v -> {
                                dialog.dismiss();
                            });
                            dialog.show();
                            Window window = dialog.getWindow();
                            window.setBackgroundDrawableResource(R.drawable.zimkit_shape_12dp_white);
                            WindowManager.LayoutParams lp = window.getAttributes();
                            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                            lp.width = dp2px(270, displayMetrics);
                            lp.height = dp2px(165, displayMetrics);
                            window.setAttributes(lp);
                        }
                    }
                }
            });

        binding.groupMembersCount.setOnClickListener(v -> {
            Intent intent = new Intent(ZIMKitGroupChatSettingActivity.this, ZIMKitGroupMembersActivity.class);
            intent.putExtra(ZIMKitConstant.MessagePageConstant.KEY_ID, mId);
            startActivity(intent);
        });
        ZIMKitConversation conversation = ZIMKitCore.getInstance().getZIMKitConversation(mId);
        if (conversation != null) {
            binding.pinChat.realSetChecked(conversation.getZimConversation().isPinned);
            binding.pinChat.setAsynchronous(new Asynchronous() {
                @Override
                public void beforeApplyState(AsynchronousSwitch aSwitch, boolean originalCheck) {
                    ZIMKitCore.getInstance().setConversationPinnedState(originalCheck, mId, ZIMConversationType.GROUP,
                        new ZIMConversationPinnedStateUpdatedCallback() {
                            @Override
                            public void onConversationPinnedStateUpdated(String conversationID,
                                ZIMConversationType conversationType, ZIMError errorInfo) {
                                ZIMKitConversation conversation1 = ZIMKitCore.getInstance().getZIMKitConversation(mId);
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
                    ZIMKitCore.getInstance().setConversationNotificationStatus(target, mId, ZIMConversationType.GROUP,
                        new ZIMConversationNotificationStatusSetCallback() {
                            @Override
                            public void onConversationNotificationStatusSet(String conversationID,
                                ZIMConversationType conversationType, ZIMError errorInfo) {
                                ZIMKitConversation conversation1 = ZIMKitCore.getInstance().getZIMKitConversation(mId);
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

        zimKitDelegate = new ZIMKitDelegate() {
            @Override
            public void onGroupMemberStateChanged(ZIMGroupMemberState state, ZIMGroupMemberEvent event,
                ArrayList<ZIMGroupMemberInfo> userList, ZIMGroupOperatedInfo operatedInfo, String groupID) {
                List<ZIMKitGroupMemberInfo> groupMemberList = ZIMKitCore.getInstance().getGroupMemberList(mId);
                binding.groupMembersCount.setText(getString(R.string.group_members_detail, groupMemberList.size()));
                shortcutAdapter.setMemberList(groupMemberList);
            }
        };
        ZIMKit.registerZIMKitDelegate(zimKitDelegate);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ZIMKit.unRegisterZIMKitDelegate(zimKitDelegate);
    }

    public static int dp2px(float v, DisplayMetrics displayMetrics) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, displayMetrics);
    }
}