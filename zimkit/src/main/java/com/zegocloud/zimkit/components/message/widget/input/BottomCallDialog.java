package com.zegocloud.zimkit.components.message.widget.input;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.zegocloud.uikit.plugin.adapter.ZegoPluginAdapter;
import com.zegocloud.uikit.plugin.adapter.plugins.call.PluginCallType;
import com.zegocloud.uikit.plugin.adapter.plugins.call.PluginCallUser;
import com.zegocloud.uikit.plugin.adapter.plugins.call.ZegoCallPluginProtocol;
import com.zegocloud.uikit.plugin.adapter.plugins.common.ZegoPluginCallback;
import com.zegocloud.uikit.plugin.adapter.plugins.signaling.ZegoSignalingPluginNotificationConfig;
import com.zegocloud.zimkit.R;
import com.zegocloud.zimkit.databinding.ZimkitLayoutSeletectCallTypeBinding;
import com.zegocloud.zimkit.services.ZIMKitConfig;
import com.zegocloud.zimkit.services.config.ZIMKitInputButtonName;
import com.zegocloud.zimkit.services.internal.ZIMKitCore;
import im.zego.zim.callback.ZIMUsersInfoQueriedCallback;
import im.zego.zim.entity.ZIMError;
import im.zego.zim.entity.ZIMErrorUserInfo;
import im.zego.zim.entity.ZIMUserFullInfo;
import im.zego.zim.entity.ZIMUsersInfoQueryConfig;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class BottomCallDialog extends BottomSheetDialogFragment {

    private ZimkitLayoutSeletectCallTypeBinding binding;
    private String conversationID;
    private boolean fromExpand;

    public BottomCallDialog(String conversationID) {
        this.conversationID = conversationID;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.zimkit_layout_seletect_call_type, container, false);

        List<ZIMKitInputButtonName> buttonNames;
        ZIMKitConfig zimKitConfig = ZIMKitCore.getInstance().getZimKitConfig();
        if (fromExpand) {
            buttonNames = new ArrayList<>(zimKitConfig.inputConfig.expandButtons);
        } else {
            buttonNames = new ArrayList<>(zimKitConfig.inputConfig.smallButtons);
        }
        if (buttonNames.contains(ZIMKitInputButtonName.VOICE_CALL)) {
            binding.audioCall.setVisibility(View.VISIBLE);
        } else {
            binding.audioCall.setVisibility(View.GONE);
        }
        if (buttonNames.contains(ZIMKitInputButtonName.VIDEO_CALL)) {
            binding.videoCall.setVisibility(View.VISIBLE);
        } else {
            binding.videoCall.setVisibility(View.GONE);
        }
        binding.audioCall.setOnClickListener(v -> {
            sendCall(PluginCallType.VOICE_CALL);
            dismiss();
        });
        binding.videoCall.setOnClickListener(v -> {
            dismiss();
            sendCall(PluginCallType.VIDEO_CALL);
        });
        binding.cancel.setOnClickListener(v -> {
            dismiss();
        });

        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Get the dialog object
        if (getDialog() != null) {
            FrameLayout frameLayout = getDialog().getWindow()
                .findViewById(com.google.android.material.R.id.design_bottom_sheet);
            frameLayout.setBackground(new ColorDrawable(Color.TRANSPARENT));

            Window dialogWindow = getDialog().getWindow();
            WindowManager.LayoutParams lp = dialogWindow.getAttributes();
            //Peripheral mask transparency 0.0f-1.0f
            lp.dimAmount = 0.2f;
            dialogWindow.setAttributes(lp);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        if (getContext() != null) {
            return new BottomSheetDialog(getContext());
        }
        return super.onCreateDialog(savedInstanceState);
    }

    private void sendCall(PluginCallType callType) {
        ZIMKitCore.getInstance()
            .queryUsersInfo(new ArrayList<>(Collections.singletonList(conversationID)), new ZIMUsersInfoQueryConfig(),
                new ZIMUsersInfoQueriedCallback() {
                    @Override
                    public void onUsersInfoQueried(ArrayList<ZIMUserFullInfo> userList,
                        ArrayList<ZIMErrorUserInfo> errorUserList, ZIMError errorInfo) {
                        if (!userList.isEmpty()) {
                            PluginCallUser callUser = new PluginCallUser();
                            callUser.userID = userList.get(0).baseInfo.userID;
                            callUser.userName = userList.get(0).baseInfo.userName;
                            callUser.avatar = userList.get(0).baseInfo.userAvatarUrl;
                            List<PluginCallUser> callUsers = new ArrayList<>();
                            callUsers.add(callUser);

                            ZIMKitConfig zimKitConfig = ZIMKitCore.getInstance().getZimKitConfig();
                            ZegoSignalingPluginNotificationConfig notificationConfig = null;
                            if (zimKitConfig != null && zimKitConfig.callPluginConfig != null && !TextUtils.isEmpty(
                                zimKitConfig.callPluginConfig.resourceID)) {
                                notificationConfig = new ZegoSignalingPluginNotificationConfig();
                                notificationConfig.setResourceID(zimKitConfig.callPluginConfig.resourceID);
                            }
                            Activity activity = (Activity) getContext();
                            ZegoCallPluginProtocol callkitPlugin = ZegoPluginAdapter.callkitPlugin();
                            if (callkitPlugin != null && zimKitConfig != null
                                && zimKitConfig.callPluginConfig != null) {
                                callkitPlugin.sendInvitationWithUIChange(activity, callUsers, callType, "", 60, null,
                                    notificationConfig, new ZegoPluginCallback() {
                                        @Override
                                        public void onSuccess() {
                                        }

                                        @Override
                                        public void onError(int errorCode, String errorMessage) {

                                        }
                                    });
                            }
                        }

                    }
                });
    }

    public void showDialog(FragmentManager supportFragmentManager, boolean fromExpand) {
        show(supportFragmentManager, "callType");
        this.fromExpand = fromExpand;
    }
}
