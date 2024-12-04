package com.zegocloud.zimkit.components.forward;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.zegocloud.zimkit.R;
import com.zegocloud.zimkit.common.glide.ZIMKitGlideLoader;
import com.zegocloud.zimkit.components.message.model.ZIMKitMessageModel;
import com.zegocloud.zimkit.databinding.ZimkitDialogConfirmForwardBinding;
import com.zegocloud.zimkit.services.ZIMKit;
import com.zegocloud.zimkit.services.internal.ZIMKitCore;
import com.zegocloud.zimkit.services.model.ZIMKitConversation;
import com.zegocloud.zimkit.services.model.ZIMKitUser;
import com.zegocloud.zimkit.services.utils.ZIMMessageUtil;
import im.zego.zim.enums.ZIMConversationType;
import java.util.List;

public class ForwardConfirmDialog extends Dialog {

    private ZimkitDialogConfirmForwardBinding binding;
    private ZIMKitConversation conversation;
    private Callback callback;

    public ForwardConfirmDialog(@NonNull Context context) {
        super(context, R.style.Call_TransparentDialog);
        Window window = getWindow();
        window.requestFeature(Window.FEATURE_NO_TITLE);
    }

    public ForwardConfirmDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        Window window = getWindow();
        window.requestFeature(Window.FEATURE_NO_TITLE);
    }

    protected ForwardConfirmDialog(@NonNull Context context, boolean cancelable,
        @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        Window window = getWindow();
        window.requestFeature(Window.FEATURE_NO_TITLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ZimkitDialogConfirmForwardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Window window = getWindow();
        LayoutParams lp = window.getAttributes();
        lp.width = LayoutParams.WRAP_CONTENT;
        lp.height = LayoutParams.WRAP_CONTENT;
        lp.dimAmount = 0.5f;
        window.setAttributes(lp);
        setCanceledOnTouchOutside(true);
        window.setBackgroundDrawable(new ColorDrawable());

        binding.cancel.setOnClickListener(v -> {
            if (callback != null) {
                callback.onClickCancel(this);
            }
        });
        binding.confirm.setOnClickListener(v -> {
            if (callback != null) {
                callback.onClickConfirm(this);
            }
        });

        ZIMKitGlideLoader.displayConversationAvatarImage(binding.forwardIcon, conversation.getAvatarUrl(),
            conversation.getType());
        binding.forwardName.setText(conversation.getName());

        binding.forwardContent.setOnClickListener(v -> {
            if (callback != null) {
                callback.onClickContent(this);
            }
        });

        List<ZIMKitMessageModel> forwardMessages = ZIMKitCore.getInstance().getForwardMessages();
        ZIMKitForwardType forwardType = ZIMKitCore.getInstance().getForwardType();
        if (forwardType == ZIMKitForwardType.SINGLE) {
            ZIMKitMessageModel messageModel = forwardMessages.get(0);
            String content = ZIMMessageUtil.simplifyZIMMessageContent(messageModel.getMessage());
            binding.forwardContent.setText(content);
        } else if (forwardType == ZIMKitForwardType.INDIVIDUAL) {
            String conversationID = forwardMessages.get(0).getMessage().getConversationID();
            ZIMKitConversation forwardConversation = ZIMKitCore.getInstance().getZIMKitConversation(conversationID);
            if (forwardConversation.getType() == ZIMConversationType.GROUP) {
                String string1 = getContext().getString(R.string.zimkit_forward_onebyone);
                String string2 = getContext().getString(R.string.zimkit_title_group_chat);
                binding.forwardContent.setText(
                    getContext().getString(R.string.zimkit_forward_confirm_content, string1, string2));
            } else {
                String string1 = getContext().getString(R.string.zimkit_forward_onebyone);
                ZIMKitUser localUser = ZIMKit.getLocalUser();
                String string2 = getContext().getString(R.string.zimkit_forward_content_s2, localUser.getName(),
                    forwardConversation.getName());
                binding.forwardContent.setText(
                    getContext().getString(R.string.zimkit_forward_confirm_content, string1, string2));
            }
        } else if (forwardType == ZIMKitForwardType.MERGE) {
            String conversationID = forwardMessages.get(0).getMessage().getConversationID();
            ZIMKitConversation forwardConversation = ZIMKitCore.getInstance().getZIMKitConversation(conversationID);
            if (forwardConversation.getType() == ZIMConversationType.GROUP) {
                String string1 = getContext().getString(R.string.zimkit_forward_merge);
                String string2 = getContext().getString(R.string.zimkit_title_group_chat);
                binding.forwardContent.setText(
                    getContext().getString(R.string.zimkit_forward_confirm_content, string1, string2));
            } else {
                String string1 = getContext().getString(R.string.zimkit_forward_merge);
                ZIMKitUser localUser = ZIMKit.getLocalUser();
                String string2 = getContext().getString(R.string.zimkit_forward_content_s2, localUser.getName(),
                    forwardConversation.getName());
                binding.forwardContent.setText(
                    getContext().getString(R.string.zimkit_forward_confirm_content, string1, string2));
            }
        }
    }

    public void setClickConversation(ZIMKitConversation itemData) {
        this.conversation = itemData;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    interface Callback {

        void onClickConfirm(ForwardConfirmDialog dialog);

        void onClickCancel(ForwardConfirmDialog dialog);

        void onClickContent(ForwardConfirmDialog dialog);
    }
}
