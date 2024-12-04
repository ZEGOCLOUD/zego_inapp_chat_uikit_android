package com.zegocloud.zimkit.components.message.widget.input;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;
import com.permissionx.guolindev.callback.RequestCallback;
import com.zegocloud.zimkit.R;
import com.zegocloud.zimkit.common.base.BaseDialog;
import com.zegocloud.zimkit.common.utils.PermissionHelper;
import com.zegocloud.zimkit.common.utils.ZIMKitToastUtils;
import com.zegocloud.zimkit.components.message.model.ZIMKitEmojiItemModel;
import com.zegocloud.zimkit.components.message.model.ZIMKitInputButtonModel;
import com.zegocloud.zimkit.components.message.model.ZIMKitMessageModel;
import com.zegocloud.zimkit.components.message.widget.ZIMKitAudioPlayer;
import com.zegocloud.zimkit.components.message.widget.ZIMKitAudioPlayer.MediaRecordCallback;
import com.zegocloud.zimkit.databinding.ZimkitLayoutInputViewBinding;
import com.zegocloud.zimkit.services.ZIMKitConfig;
import com.zegocloud.zimkit.services.config.ZIMKitInputButtonName;
import com.zegocloud.zimkit.services.internal.ZIMKitCore;
import com.zegocloud.zimkit.services.utils.ZIMMessageUtil;
import java.util.ArrayList;
import java.util.List;


public class ZIMKitMessageInputView extends LinearLayout {

    private boolean isInputShow;
    private ZimkitLayoutInputViewBinding binding;
    private InputCallback callback;
    private ZIMKitMessageModel repliedMessage;
    private OnGlobalLayoutListener globalLayoutListener;

    public ZIMKitMessageInputView(Context context) {
        super(context);
        initView(context);
    }

    public ZIMKitMessageInputView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public ZIMKitMessageInputView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.zimkit_layout_input_view, this, true);

        globalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            private final Rect r = new Rect();
            DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
            private final int visibleThreshold = dp2px(150, displayMetrics);

            @Override
            public void onGlobalLayout() {
                getRootView().getWindowVisibleDisplayFrame(r);
                int heightDiff = getRootView().getHeight() - r.height();
                boolean isOpen = heightDiff > visibleThreshold;
                isInputShow = isOpen;
            }
        };

        postDelayed(new Runnable() {
            @Override
            public void run() {
                View rootView = getRootView();
                getRootView().getViewTreeObserver().addOnGlobalLayoutListener(globalLayoutListener);

            }
        }, 100);

        binding.inputEdittext.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    resetAndShowInput();
                    scrollMessageView();
                }
                return false;
            }
        });

        int maxButtons = 4;
        ZIMKitConfig zimKitConfig = ZIMKitCore.getInstance().getZimKitConfig();
        if (zimKitConfig != null && zimKitConfig.inputConfig != null && zimKitConfig.inputConfig.inputHint != null) {
            binding.inputEdittext.setHint(zimKitConfig.inputConfig.inputHint);
        }
        List<ZIMKitInputButtonModel> buttonModels = new ArrayList<>();
        if (zimKitConfig != null && zimKitConfig.inputConfig != null) {
            List<ZIMKitInputButtonName> buttonNames = new ArrayList<>(zimKitConfig.inputConfig.smallButtons);
            if (buttonNames.contains(ZIMKitInputButtonName.VOICE_CALL) && buttonNames.contains(
                ZIMKitInputButtonName.VIDEO_CALL)) {
                buttonNames.remove(ZIMKitInputButtonName.VOICE_CALL);
            }
            if (buttonNames.size() > maxButtons) {
                buttonNames = buttonNames.subList(0, maxButtons);
            }
            for (ZIMKitInputButtonName buttonName : buttonNames) {
                ZIMKitInputButtonModel inputButtonModel = ZIMKitCore.getInstance().getInputButtonModel(buttonName);
                buttonModels.add(inputButtonModel);
            }
        }
        for (int i = 0; i < buttonModels.size(); i++) {
            ZIMKitInputButtonModel inputButtonModel = buttonModels.get(i);
            ImageView imageView = new ImageView(getContext());
            LinearLayout.LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
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
                case AUDIO:
                    initAudioButton(imageView, i, inputButtonModel);
                    break;
                case EMOJI:
                    initEmojiButton(imageView, i, inputButtonModel);
                    break;
                case PICTURE:
                    initSelectPicButton(imageView, i, inputButtonModel);
                    break;
                case EXPAND:
                    initMoreButton(imageView, i, inputButtonModel);
                    break;
                case TAKE_PHOTO:
                case VIDEO_CALL:
                case VOICE_CALL:
                case FILE:
                    final int index = i;
                    imageView.setOnClickListener(v -> {
                        if (callback != null) {
                            callback.onClickSmallItem(index, inputButtonModel, repliedMessage);
                        }
                    });
                    break;
            }
        }

        ZIMKitAudioPlayer.getInstance().addAudioRecordCallback(new MediaRecordCallback() {
            @Override
            public void onRecordStopped(int status) {
                if (status == MediaRecordCallback.FINISHED_USER) {
                    if (callback != null) {
                        int duration = ZIMKitAudioPlayer.getInstance().getDuration();
                        String path = ZIMKitAudioPlayer.getInstance().getPath();
                        callback.onSendAudioMessage(path, duration, repliedMessage);
                    }
                } else if (status == MediaRecordCallback.STOP_TIME_SHORT) {
                    ZIMKitToastUtils.showToast(R.string.zimkit_audio_record_too_short);
                }
            }
        });

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

        binding.inputViewMoreLayout.setInputMoreCallback(new ZIMKitInputAddMoreView.InputMoreCallback() {
            @Override
            public void onClickMoreItem(int position, ZIMKitInputButtonModel itemModel) {
                if (callback != null) {
                    callback.onClickExtraItem(position, itemModel, repliedMessage);
                }
            }
        });

        binding.inputExpandPanel.setOnClickListener(v -> {
            reset();
            if (callback != null) {
                int selectionStart = binding.inputEdittext.getSelectionStart();
                int selectionEnd = binding.inputEdittext.getSelectionEnd();
                CharSequence inputMsg = binding.inputEdittext.getText().toString();
                callback.onClickExpandButton(inputMsg, selectionStart, selectionEnd, repliedMessage);
            }
        });
        initSendMessageButton();

        initReplyLayout();
    }

    private void initReplyLayout() {
        binding.inputReplyDelete.setOnClickListener(v -> {
            updateToReplyLayout(false);
            repliedMessage = null;
        });
    }

    private static final String TAG = "ZIMKitInputView";

    public static int dp2px(float v, DisplayMetrics displayMetrics) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, displayMetrics);
    }

    private void initSelectPicButton(ImageView imageView, int index, ZIMKitInputButtonModel inputButtonModel) {
        imageView.setOnClickListener(v -> {
            resetAndHideInput();
            if (callback != null) {
                callback.onClickSmallItem(index, inputButtonModel, repliedMessage);
            }
        });
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
        });
    }

    private void reset() {
        hideAllSubContentViews();
        unselectOtherButtons(null);
        //        binding.inputContentContainer.setVisibility(View.GONE);
        binding.inputViewMoreLayout.setVisibility(View.GONE);
        binding.inputViewEmojiLayout.setVisibility(View.GONE);
        binding.inputViewAudioLayout.setVisibility(View.GONE);
    }

    private void initMoreButton(ImageView imageView, int index, ZIMKitInputButtonModel inputButtonModel) {
        imageView.setOnClickListener(v -> {
            boolean extraSpaceShow = isInputExtraSpaceShow();
            if (extraSpaceShow) {
                unselectOtherButtons(imageView);
            }
            hideAllSubContentViews();

            boolean selected = v.isSelected();
            boolean newState = !selected;
            v.setSelected(newState);

            if (newState) {
                if (isInputShow) {
                    hideInputWindow(binding.inputEdittext);
                    showViewDelayed(binding.inputViewMoreLayout);
                } else {
                    showView(binding.inputViewMoreLayout);
                }
            } else {
                if (!isInputShow) {
                    showInputDelayed(binding.inputEdittext);
                }
            }
            scrollMessageView();
            if (callback != null) {
                callback.onClickSmallItem(index, inputButtonModel, repliedMessage);
            }
        });
    }

    private void initEmojiButton(ImageView imageView, int index, ZIMKitInputButtonModel inputButtonModel) {
        imageView.setOnClickListener(v -> {
            boolean extraSpaceShow = isInputExtraSpaceShow();
            if (extraSpaceShow) {
                unselectOtherButtons(imageView);
            }
            hideAllSubContentViews();

            boolean selected = v.isSelected();
            boolean newState = !selected;
            v.setSelected(newState);

            if (newState) {
                if (isInputShow) {
                    hideInputWindow(binding.inputEdittext);
                    showViewDelayed(binding.inputViewEmojiLayout);
                } else {
                    showView(binding.inputViewEmojiLayout);
                }
            } else {
                if (!isInputShow) {
                    showInputDelayed(binding.inputEdittext);
                }
            }
            scrollMessageView();

            if (callback != null) {
                callback.onClickSmallItem(index, inputButtonModel, repliedMessage);
            }
        });
    }

    private void initAudioButton(ImageView imageView, int index, ZIMKitInputButtonModel inputButtonModel) {
        imageView.setOnClickListener(v -> {
            FragmentActivity activity = (AppCompatActivity) getContext();
            PermissionHelper.requestRecordAudioPermissionIfNeed(activity, new RequestCallback() {
                @Override
                public void onResult(boolean allGranted, @NonNull List<String> grantedList,
                    @NonNull List<String> deniedList) {
                    if (allGranted) {
                        boolean extraSpaceShow = isInputExtraSpaceShow();
                        if (extraSpaceShow) {
                            unselectOtherButtons(imageView);
                        }
                        hideAllSubContentViews();

                        boolean selected = v.isSelected();
                        boolean newState = !selected;
                        v.setSelected(newState);
                        if (newState) {
                            if (isInputShow) {
                                hideInputWindow(binding.inputEdittext);
                                showViewDelayed(binding.inputViewAudioLayout);
                            } else {
                                showView(binding.inputViewAudioLayout);
                            }
                        } else {
                            if (!isInputShow) {
                                showInputDelayed(binding.inputEdittext);
                            }
                        }
                        scrollMessageView();

                        if (callback != null) {
                            callback.onClickSmallItem(index, inputButtonModel, repliedMessage);
                        }
                    } else {
                        BaseDialog baseDialog = new BaseDialog(getContext());
                        baseDialog.setMsgTitle(getContext().getString(R.string.zimkit_photo_no_mic_tip));
                        baseDialog.setMsgContent(getContext().getString(R.string.zimkit_photo_no_mic_description));
                        baseDialog.setLeftButtonContent(getContext().getString(R.string.zimkit_access_later));
                        baseDialog.setRightButtonContent(getContext().getString(R.string.zimkit_go_setting));
                        baseDialog.setSureListener(v -> {
                            baseDialog.dismiss();
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(
                                Uri.fromParts("package", activity.getPackageName(), null));
                            activity.startActivityForResult(intent, 666);
                        });
                        baseDialog.setCancelListener(v -> {
                            baseDialog.dismiss();
                        });
                    }
                }
            });
        });
    }

    private void hideView(View view) {
        view.setVisibility(View.GONE);
    }

    private void showView(View view) {
        view.setVisibility(View.VISIBLE);
    }

    private void showViewDelayed(View view) {
        postDelayed(new Runnable() {
            @Override
            public void run() {
                view.setVisibility(View.VISIBLE);
            }
        }, 100);
    }

    private void showInputDelayed(View view) {
        postDelayed(new Runnable() {
            @Override
            public void run() {
                requestInputWindow(view);
            }
        }, 100);
    }

    private void showEmojiView() {
        hideInputWindow(binding.inputEdittext);
        postDelayed(new Runnable() {
            @Override
            public void run() {
                binding.inputViewEmojiLayout.setVisibility(View.VISIBLE);
                //                binding.inputContentContainer.setVisibility(View.VISIBLE);
            }
        }, 100);

    }

    private void hideEmojiView() {
        //        binding.inputViewEmojiLayout.setVisibility(View.GONE);
        //        binding.inputContentContainer.setVisibility(View.GONE);
        //        binding.inputViewMoreLayout.setVisibility(View.GONE);
        //        binding.inputViewEmojiLayout.setVisibility(View.GONE);
        //        binding.inputViewAudioLayout.setVisibility(View.GONE);
        postDelayed(new Runnable() {
            @Override
            public void run() {
                requestInputWindow(binding.inputEdittext);
            }
        }, 100);
    }

    private void showMoreView() {
        hideInputWindow(binding.inputEdittext);
        postDelayed(new Runnable() {
            @Override
            public void run() {
                binding.inputViewMoreLayout.setVisibility(View.VISIBLE);
                //                binding.inputContentContainer.setVisibility(View.VISIBLE);
            }
        }, 100);
    }

    private void hideMoreView() {
        //        binding.inputViewMoreLayout.setVisibility(View.GONE);
        //        binding.inputContentContainer.setVisibility(View.GONE);
        binding.inputViewMoreLayout.setVisibility(View.GONE);
        binding.inputViewEmojiLayout.setVisibility(View.GONE);
        binding.inputViewAudioLayout.setVisibility(View.GONE);
        postDelayed(new Runnable() {
            @Override
            public void run() {
                requestInputWindow(binding.inputEdittext);
            }
        }, 100);
    }

    private boolean isInputExtraSpaceShow() {
        return binding.inputViewEmojiLayout.getVisibility() == VISIBLE
            || binding.inputViewMoreLayout.getVisibility() == VISIBLE
            || binding.inputViewAudioLayout.getVisibility() == VISIBLE;
    }

    private void hideAllSubContentViews() {
        binding.inputViewEmojiLayout.setVisibility(View.GONE);
        binding.inputViewMoreLayout.setVisibility(View.GONE);
        binding.inputViewAudioLayout.setVisibility(View.GONE);
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

    private void scrollMessageView() {
        postDelayed(new Runnable() {
            @Override
            public void run() {
                if (callback != null) {
                    callback.onRequestScrollToBottom();
                }
            }
        }, 200);
    }

    public void hideInputWindow(View view) {
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        view.clearFocus();
    }

    public void requestInputWindow(View view) {
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

    public void removeGlobalListener() {
        getRootView().getViewTreeObserver().removeOnGlobalLayoutListener(globalLayoutListener);
    }

    public void resetAndHideInput() {
        reset();
        hideInputWindow(binding.inputEdittext);
    }

    public void resetAndShowInput() {
        reset();
        requestInputWindow(binding.inputEdittext);
    }

    public void setInputMoreItems(List<ZIMKitInputButtonModel> inputMoreItems) {
        binding.inputViewMoreLayout.setInputMoreItems(inputMoreItems);
    }

    public void setInputMessage(CharSequence inputMsg, int selectionStart, int selectionEnd) {
        binding.inputEdittext.setText(inputMsg);
        if (!TextUtils.isEmpty(inputMsg)) {
            binding.inputEdittext.setSelection(selectionStart, selectionEnd);
        }
        binding.inputSend.setEnabled(!inputMsg.toString().trim().isEmpty());
    }


    private void updateToReplyLayout(boolean reply) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        int top = dp2px(12, displayMetrics);
        if (reply) {
            top = dp2px(49, displayMetrics);
            binding.inputReplyLayout.setVisibility(View.VISIBLE);
        } else {
            binding.inputReplyLayout.setVisibility(View.GONE);
        }
        binding.inputEdittext.setPadding(dp2px(12, displayMetrics), top, dp2px(41, displayMetrics),
            dp2px(12, displayMetrics));
        binding.inputViewAudioLayout.updateReplyMargin(reply);
    }

    public void setReplyMessage(ZIMKitMessageModel repliedMessage) {
        this.repliedMessage = repliedMessage;
        if (repliedMessage != null) {
            updateToReplyLayout(true);
            String content = ZIMMessageUtil.simplifyZIMMessageContent(repliedMessage.getMessage());
            if (!TextUtils.isEmpty(content)) {
                String nickName = repliedMessage.getNickName();
                binding.inputReplyContent.setText(
                    getContext().getString(R.string.zimkit_reply_content, nickName, content));
            }
            if (isInputExtraSpaceShow()) {
            } else {
                requestInputWindow(binding.inputEdittext);
            }
        } else {
            updateToReplyLayout(false);
        }
    }

    public ZIMKitMessageModel getRepliedMessage() {
        return repliedMessage;
    }
}
