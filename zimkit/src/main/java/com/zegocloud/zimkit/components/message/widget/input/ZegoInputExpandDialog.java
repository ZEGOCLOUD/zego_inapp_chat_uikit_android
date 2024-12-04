package com.zegocloud.zimkit.components.message.widget.input;


import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import com.zegocloud.zimkit.R;
import com.zegocloud.zimkit.components.message.model.ZIMKitEmojiItemModel;
import com.zegocloud.zimkit.components.message.model.ZIMKitInputButtonModel;
import com.zegocloud.zimkit.components.message.model.ZIMKitMessageModel;
import com.zegocloud.zimkit.components.message.utils.ChatMessageBuilder;
import com.zegocloud.zimkit.databinding.ZimkitLayoutInputViewExpandBinding;
import com.zegocloud.zimkit.services.ZIMKitConfig;
import com.zegocloud.zimkit.services.config.ZIMKitInputButtonName;
import com.zegocloud.zimkit.services.internal.ZIMKitCore;
import com.zegocloud.zimkit.services.utils.ZIMMessageUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ZegoInputExpandDialog extends Dialog {

    private ZimkitLayoutInputViewExpandBinding binding;
    private Handler handler = new Handler(Looper.getMainLooper());
    private CharSequence inputMsg;
    private String conversationName;
    private InputCallback callback;
    private ZIMKitMessageModel repliedMessage;
    private int selectionStart;
    private int selectionEnd;

    public ZegoInputExpandDialog(@NonNull Context context) {
        super(context, R.style.Call_TransparentDialog);
        //        super(context);
        Window window = getWindow();
        window.requestFeature(Window.FEATURE_NO_TITLE);
    }

    public ZegoInputExpandDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        Window window = getWindow();
        window.requestFeature(Window.FEATURE_NO_TITLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ZimkitLayoutInputViewExpandBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Window window = getWindow();
        LayoutParams lp = window.getAttributes();
        lp.width = LayoutParams.MATCH_PARENT;
        lp.height = LayoutParams.WRAP_CONTENT;
        lp.dimAmount = 0.5f;
        lp.gravity = Gravity.BOTTOM;
        window.setAttributes(lp);
        setCanceledOnTouchOutside(true);
        window.setBackgroundDrawable(new ColorDrawable());

        int mode = LayoutParams.SOFT_INPUT_ADJUST_RESIZE | LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE;
        window.setSoftInputMode(mode);

        handler.post(new Runnable() {
            @Override
            public void run() {
                binding.inputEdittext.requestFocus();
            }
        });

        initExpandViews();
    }

    private void initExpandViews() {
        binding.inputEdittext.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    reset();
                }
                return false;
            }
        });

        ZIMKitConfig zimKitConfig = ZIMKitCore.getInstance().getZimKitConfig();
        if (zimKitConfig != null && zimKitConfig.inputConfig != null && zimKitConfig.inputConfig.inputHint != null) {
            binding.inputEdittext.setHint(zimKitConfig.inputConfig.inputHint);
        }
        List<ZIMKitInputButtonModel> buttonModels = new ArrayList<>();
        if (zimKitConfig != null && zimKitConfig.inputConfig != null) {
            List<ZIMKitInputButtonName> collect = zimKitConfig.inputConfig.smallButtons.stream()
                .filter(inputButtonName -> (inputButtonName == ZIMKitInputButtonName.EMOJI))
                .collect(Collectors.toList());

            for (ZIMKitInputButtonName buttonName : collect) {
                ZIMKitInputButtonModel inputButtonModel = ZIMKitCore.getInstance().getInputButtonModel(buttonName);
                buttonModels.add(inputButtonModel);
            }
        }
        for (int i = 0; i < buttonModels.size(); i++) {
            ZIMKitInputButtonModel inputButtonModel = buttonModels.get(i);
            ImageView imageView = new ImageView(getContext());
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
            DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
            layoutParams.leftMargin = dp2px(8, displayMetrics);
            layoutParams.rightMargin = dp2px(20, displayMetrics);
            layoutParams.topMargin = dp2px(7, displayMetrics);
            imageView.setLayoutParams(layoutParams);
            imageView.setScaleType(ScaleType.CENTER);

            imageView.setImageDrawable(inputButtonModel.getSmallIcon());

            binding.inputButtonsLayout.addView(imageView);

            switch (inputButtonModel.getButtonName()) {
                case EMOJI:
                    initEmojiButton(imageView, i, inputButtonModel);
                    break;
                case PICTURE:
                    initSelectPicButton(imageView, i, inputButtonModel);
                    break;
            }
        }

        binding.inputViewEmojiLayout.setEmojiListener(new ZIMKitInputEmojiPagerView.OnEmojiClickListener() {
            @Override
            public void onEmojiDelete() {
                int action = KeyEvent.ACTION_DOWN;
                int code = KeyEvent.KEYCODE_DEL;
                KeyEvent event = new KeyEvent(action, code);
                binding.inputEdittext.onKeyDown(KeyEvent.KEYCODE_DEL, event);
            }

            @Override
            public void onEmojiClick(ZIMKitEmojiItemModel emoji) {
                int index = binding.inputEdittext.getSelectionStart();
                Editable editable = binding.inputEdittext.getText();
                editable.insert(index, emoji.getEmojiContent());
            }
        });

        binding.inputCollapsePanel.setOnClickListener(v -> {
            if (callback != null) {
                String inputMsg = binding.inputEdittext.getText().toString();
                int selectionStart = binding.inputEdittext.getSelectionStart();
                int selectionEnd = binding.inputEdittext.getSelectionEnd();
                callback.onClickExpandButton(inputMsg, selectionStart, selectionEnd, repliedMessage);
                dismiss();
            }
        });
        initSendMessageButton();
    }

    private void initSendMessageButton() {
        binding.inputSend.setEnabled(false);
        binding.inputEdittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                inputMsg = s;
                binding.inputSend.setEnabled(!s.toString().trim().isEmpty());
            }
        });

        binding.inputSend.setOnClickListener(v -> {
            if (callback != null) {
                String inputMsg = binding.inputEdittext.getText().toString();
                callback.onSendTextMessage(inputMsg, repliedMessage);
            }
            binding.inputEdittext.setText("");
            setReplyMessage(null);
            dismiss();
        });
    }

    private void initEmojiButton(ImageView imageView, int index, ZIMKitInputButtonModel inputButtonModel) {
        imageView.setOnClickListener(v -> {
            unselectOtherButtons(imageView);
            hideAllSubContentViews();

            boolean selected = v.isSelected();
            boolean newState = !selected;
            v.setSelected(newState);

            if (newState) {
                showEmojiView();
            } else {
                hideEmojiView();
            }

            if (callback != null) {
                callback.onClickSmallItem(index, inputButtonModel, repliedMessage);
            }
        });
    }

    private void initSelectPicButton(ImageView imageView, int index, ZIMKitInputButtonModel inputButtonModel) {
        imageView.setOnClickListener(v -> {
            reset();
            hideInputWindow(binding.inputEdittext);
            if (callback != null) {
                callback.onClickSmallItem(index, inputButtonModel, repliedMessage);
            }
        });
    }

    private void reset() {
        hideAllSubContentViews();
        unselectOtherButtons(null);
        binding.inputContentContainer.setVisibility(View.GONE);
    }

    public static int dp2px(float v, DisplayMetrics displayMetrics) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, displayMetrics);
    }


    private void showEmojiView() {
        hideInputWindow(binding.inputEdittext);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                binding.inputContentContainer.setVisibility(View.VISIBLE);
                binding.inputViewEmojiLayout.setVisibility(View.VISIBLE);
            }
        }, 100);

    }

    private void hideEmojiView() {
        binding.inputViewEmojiLayout.setVisibility(View.GONE);
        binding.inputContentContainer.setVisibility(View.GONE);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                requestInputWindow(binding.inputEdittext);
            }
        }, 100);
    }

    private void unselectOtherButtons(ImageView imageView) {
        int childCount = binding.inputButtonsLayout.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = binding.inputButtonsLayout.getChildAt(i);
            if (child != imageView) {
                child.setSelected(false);
            }
        }
    }

    public static void hideInputWindow(View view) {
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        view.clearFocus();
    }

    public static void requestInputWindow(View view) {
        view.requestFocus();
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        boolean input = false;
        if (view.isAttachedToWindow()) {
            input = imm.showSoftInput(view, 0);
        }
    }

    public void setCallback(InputCallback callback) {
        this.callback = callback;
    }

    public void hide() {
        reset();
        hideInputWindow(binding.inputEdittext);
    }

    @Override
    public void show() {
        super.show();
        binding.inputEdittext.setText(inputMsg);
        if (!TextUtils.isEmpty(inputMsg)) {
            binding.inputEdittext.setSelection(selectionStart, selectionEnd);
        }
        binding.inputSend.setEnabled(!inputMsg.toString().trim().isEmpty());
        if (repliedMessage == null) {
            binding.inputExpandTitle.setText(
                getContext().getString(R.string.zimkit_input_expand_title, conversationName));
        } else {
            String content = ZIMMessageUtil.simplifyZIMMessageContent(repliedMessage.getMessage());
            if (!TextUtils.isEmpty(content)) {
                String nickName = repliedMessage.getNickName();
                binding.inputExpandTitle.setText(
                    getContext().getString(R.string.zimkit_reply_content, nickName, content));
            }
        }
    }

    private void hideAllSubContentViews() {
        binding.inputViewEmojiLayout.setVisibility(View.GONE);
    }

    public void setInputMessage(CharSequence inputMsg, int selectionStart, int selectionEnd) {
        this.inputMsg = inputMsg;
        this.selectionStart = selectionStart;
        this.selectionEnd = selectionEnd;
    }

    public CharSequence getInputMsg() {
        return inputMsg;
    }

    public int getSelectionStart() {
        return binding.inputEdittext.getSelectionStart();
    }

    public int getSelectionEnd() {
        return binding.inputEdittext.getSelectionEnd();
    }

    public void setInputTitle(String conversationName) {
        this.conversationName = conversationName;
    }

    public void setReplyMessage(ZIMKitMessageModel repliedMessage) {
        this.repliedMessage = repliedMessage;
    }

    public ZIMKitMessageModel getRepliedMessage() {
        return repliedMessage;
    }
}
