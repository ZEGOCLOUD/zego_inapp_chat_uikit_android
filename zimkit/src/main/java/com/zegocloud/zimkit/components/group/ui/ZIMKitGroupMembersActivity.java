package com.zegocloud.zimkit.components.group.ui;

import android.graphics.Color;
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
import androidx.activity.ComponentActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AlertDialog.Builder;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.zegocloud.zimkit.R;
import com.zegocloud.zimkit.common.ZIMKitConstant.MessagePageConstant;
import com.zegocloud.zimkit.common.utils.ZIMKitToastUtils;
import com.zegocloud.zimkit.components.group.adapter.GroupMemberAdapter;
import com.zegocloud.zimkit.components.group.bean.ZIMKitGroupMemberInfo;
import com.zegocloud.zimkit.components.message.utils.CustomDividerItemDecoration;
import com.zegocloud.zimkit.databinding.ActivityGroupMembersBinding;
import com.zegocloud.zimkit.services.ZIMKit;
import com.zegocloud.zimkit.services.ZIMKitDelegate;
import com.zegocloud.zimkit.services.callback.InviteUsersToJoinGroupCallback;
import com.zegocloud.zimkit.services.internal.ZIMKitCore;
import im.zego.zim.entity.ZIMError;
import im.zego.zim.entity.ZIMErrorUserInfo;
import im.zego.zim.entity.ZIMGroupMemberInfo;
import im.zego.zim.entity.ZIMGroupOperatedInfo;
import im.zego.zim.enums.ZIMErrorCode;
import im.zego.zim.enums.ZIMGroupMemberEvent;
import im.zego.zim.enums.ZIMGroupMemberState;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ZIMKitGroupMembersActivity extends ComponentActivity {

    private ActivityGroupMembersBinding binding;
    private String mID;
    private ZIMKitDelegate zimKitDelegate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupMembersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mID = getIntent().getStringExtra(MessagePageConstant.KEY_ID);

        binding.title.hideRightButton();
        binding.title.setTitle(getString(R.string.zimkit_group_member_list));
        binding.add.setOnClickListener(view -> {
            AlertDialog.Builder builder = new Builder(ZIMKitGroupMembersActivity.this);
            ViewGroup viewGroup = (ViewGroup) View.inflate(ZIMKitGroupMembersActivity.this,
                R.layout.zimkit_dialog_confirm_content, null);
            TextView title = viewGroup.findViewById(R.id.title);
            title.setText(R.string.zimkit_add_group_member);
            EditText inputLayout = viewGroup.findViewById(R.id.content_edit_text);
            viewGroup.findViewById(R.id.content_text_view).setVisibility(View.GONE);
            inputLayout.setHint(R.string.zimkit_input_member_id);
            builder.setView(viewGroup);
            AlertDialog dialog = builder.create();
            viewGroup.findViewById(R.id.confirm).setOnClickListener(v -> {
                String userID = inputLayout.getText().toString();
                if (TextUtils.isEmpty(userID)) {
                    inputLayout.setError(getString(R.string.zimkit_input_member_id));
                    return;
                }
                ZIMKit.inviteUsersToJoinGroup(Collections.singletonList(userID), mID,
                    new InviteUsersToJoinGroupCallback() {
                        @Override
                        public void onInviteUsersToJoinGroup(ArrayList<ZIMKitGroupMemberInfo> groupMembers,
                            ArrayList<ZIMErrorUserInfo> inviteUserErrors, ZIMError error) {
                            if (error.code != ZIMErrorCode.SUCCESS) {
                                ZIMKitToastUtils.showToast(error.message);
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
        });

        List<ZIMKitGroupMemberInfo> groupMemberList = ZIMKitCore.getInstance().getGroupMemberList(mID);
        GroupMemberAdapter groupMemberAdapter = new GroupMemberAdapter();
        groupMemberAdapter.setMemberList(groupMemberList);
        binding.recyclerview.setAdapter(groupMemberAdapter);
        binding.recyclerview.setLayoutManager(new LinearLayoutManager(this));
        CustomDividerItemDecoration decoration = new CustomDividerItemDecoration(this,
            CustomDividerItemDecoration.VERTICAL);
        decoration.drawLastChildDivider(false);
        decoration.setVerticalDividerMargin(dp2px(16, getResources().getDisplayMetrics()), 0);
        decoration.setDividerColor(Color.parseColor("#E6E6E6"));
        binding.recyclerview.addItemDecoration(decoration);

        zimKitDelegate = new ZIMKitDelegate() {
            @Override
            public void onGroupMemberStateChanged(ZIMGroupMemberState state, ZIMGroupMemberEvent event,
                ArrayList<ZIMGroupMemberInfo> userList, ZIMGroupOperatedInfo operatedInfo, String groupID) {
                List<ZIMKitGroupMemberInfo> groupMemberList = ZIMKitCore.getInstance().getGroupMemberList(mID);
                groupMemberAdapter.setMemberList(groupMemberList);
            }
        };
        ZIMKit.registerZIMKitDelegate(zimKitDelegate);
    }

    public static int dp2px(float v, DisplayMetrics displayMetrics) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, displayMetrics);
    }
}