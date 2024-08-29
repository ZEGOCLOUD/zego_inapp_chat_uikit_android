package com.zegocloud.zimkit.components.message.widget.input;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.SimpleOnItemTouchListener;
import com.permissionx.guolindev.callback.RequestCallback;
import com.zegocloud.zimkit.R;
import com.zegocloud.zimkit.common.base.BaseDialog;
import com.zegocloud.zimkit.common.utils.PermissionHelper;
import com.zegocloud.zimkit.common.utils.ZIMKitToastUtils;
import com.zegocloud.zimkit.components.message.model.ZIMKitEmojiItemModel;
import com.zegocloud.zimkit.components.message.model.ZIMKitInputButtonModel;
import com.zegocloud.zimkit.components.message.model.ZIMKitMessageModel;
import com.zegocloud.zimkit.components.message.utils.ChatMessageBuilder;
import com.zegocloud.zimkit.components.message.widget.MessageRecyclerView;
import com.zegocloud.zimkit.components.message.widget.ZIMKitAudioPlayer;
import com.zegocloud.zimkit.components.message.widget.ZIMKitAudioPlayer.MediaRecordCallback;
import com.zegocloud.zimkit.databinding.ZimkitLayoutInputViewBinding;
import com.zegocloud.zimkit.services.ZIMKitConfig;
import com.zegocloud.zimkit.services.config.ZIMKitInputButtonName;
import com.zegocloud.zimkit.services.internal.ZIMKitCore;
import java.util.ArrayList;
import java.util.List;


public class ZIMKitInputView extends LinearLayout {

    private ZimkitLayoutInputViewBinding binding;
    private GestureDetectorCompat gestureDetectorCompat;
    private MessageRecyclerView recyclerView;
    private InputCallback callback;

    public ZIMKitInputView(Context context) {
        super(context);
        initView(context);
    }

    public ZIMKitInputView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public ZIMKitInputView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.zimkit_layout_input_view, this, true);
        gestureDetectorCompat = new GestureDetectorCompat(getContext(), new SimpleOnGestureListener() {

            @Override
            public boolean onScroll(@NonNull MotionEvent e1, @NonNull MotionEvent e2, float distanceX,
                float distanceY) {
                reset();
                hideInputWindow(binding.inputEdittext);
                return true;
            }
        });

        binding.inputEdittext.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    reset();
                    requestInputWindow(view);
                    scrollMessageView();
                }
                return false;
            }
        });

        int maxButtons = 4;
        ZIMKitConfig zimKitConfig = ZIMKitCore.getInstance().getZimKitConfig();
        if (zimKitConfig.inputConfig.inputHint != null) {
            binding.inputEdittext.setHint(zimKitConfig.inputConfig.inputHint);
        }
        List<ZIMKitInputButtonModel> buttonModels = new ArrayList<>();
        if (zimKitConfig.inputConfig != null) {
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

            StateListDrawable stateListDrawable = new StateListDrawable();
            stateListDrawable.addState(new int[]{android.R.attr.state_selected},
                inputButtonModel.getSmallIconSelected());
            stateListDrawable.addState(new int[]{}, inputButtonModel.getSmallIcon());
            imageView.setImageDrawable(stateListDrawable);

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
                            callback.onClickSmallItem(index, inputButtonModel);
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
                        ZIMKitMessageModel messageModel = ChatMessageBuilder.buildAudioMessage(
                            ZIMKitAudioPlayer.getInstance().getPath(), duration);
                        callback.onSendAudioMessage(messageModel);
                    }
                } else if (status == MediaRecordCallback.STOP_TIME_SHORT) {
                    ZIMKitToastUtils.showToast(R.string.zimkit_audio_record_too_short);
                }
            }
        });

        binding.inputViewEmojiLayout.setEmojiListener(new ZIMKitEmojiView.OnEmojiClickListener() {
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

        binding.inputViewMoreLayout.setInputMoreCallback(new ZIMKitMoreView.InputMoreCallback() {
            @Override
            public void onClickMoreItem(int position, ZIMKitInputButtonModel itemModel) {
                if (callback != null) {
                    callback.onClickExpandItem(position, itemModel);
                }
            }
        });
        initSendMessageButton();
    }

    private static final String TAG = "ZIMKitInputView";

    public static int dp2px(float v, DisplayMetrics displayMetrics) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, displayMetrics);
    }

    private void initSelectPicButton(ImageView imageView, int index, ZIMKitInputButtonModel inputButtonModel) {
        imageView.setOnClickListener(v -> {
            reset();
            hideInputWindow(binding.inputEdittext);
            if (callback != null) {
                callback.onClickSmallItem(index, inputButtonModel);
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
                binding.inputSend.setEnabled(s.length() > 0);
            }
        });

        binding.inputSend.setOnClickListener(v -> {
            if (callback != null) {
                String inputMsg = binding.inputEdittext.getText().toString();
                ZIMKitMessageModel messageModel = ChatMessageBuilder.buildTextMessage(inputMsg);
                callback.onSendTextMessage(messageModel);
            }
            binding.inputEdittext.setText("");
        });
    }

    private void reset() {
        hideAllSubContentViews();
        unselectOtherButtons(null);
        binding.inputContentContainer.setVisibility(View.GONE);
    }

    private void initMoreButton(ImageView imageView, int index, ZIMKitInputButtonModel inputButtonModel) {
        imageView.setOnClickListener(v -> {
            unselectOtherButtons(imageView);
            hideAllSubContentViews();

            boolean selected = v.isSelected();
            boolean newState = !selected;
            v.setSelected(newState);

            if (newState) {
                showMoreView();
            } else {
                hideMoreView();
            }
            scrollMessageView();
            if (callback != null) {
                callback.onClickSmallItem(index, inputButtonModel);
            }
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
            scrollMessageView();

            if (callback != null) {
                callback.onClickSmallItem(index, inputButtonModel);
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
                        unselectOtherButtons(imageView);
                        hideAllSubContentViews();

                        boolean selected = v.isSelected();
                        boolean newState = !selected;
                        v.setSelected(newState);
                        if (newState) {
                            showAudioRecordView();
                        } else {
                            hideRecordView();
                        }
                        scrollMessageView();

                        if (callback != null) {
                            callback.onClickSmallItem(index, inputButtonModel);
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

    private void showAudioRecordView() {
        hideInputWindow(binding.inputEdittext);
        postDelayed(new Runnable() {
            @Override
            public void run() {
                binding.inputViewAudioLayout.setVisibility(View.VISIBLE);
                binding.inputContentContainer.setVisibility(View.VISIBLE);
            }
        }, 100);

    }

    private void hideRecordView() {
        binding.inputViewAudioLayout.setVisibility(View.GONE);
        binding.inputContentContainer.setVisibility(View.GONE);
        postDelayed(new Runnable() {
            @Override
            public void run() {
                requestInputWindow(binding.inputEdittext);
            }
        }, 100);
    }

    private void showEmojiView() {
        hideInputWindow(binding.inputEdittext);
        postDelayed(new Runnable() {
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
                binding.inputContentContainer.setVisibility(View.VISIBLE);
                binding.inputViewMoreLayout.setVisibility(View.VISIBLE);
            }
        }, 100);
    }

    private void hideMoreView() {
        binding.inputViewMoreLayout.setVisibility(View.GONE);
        binding.inputContentContainer.setVisibility(View.GONE);
        postDelayed(new Runnable() {
            @Override
            public void run() {
                requestInputWindow(binding.inputEdittext);
            }
        }, 100);
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
                recyclerView.scrollToEnd();
            }
        }, 200);
    }

    public void attach(MessageRecyclerView recyclerView, ViewGroup parent) {
        this.recyclerView = recyclerView;
        recyclerView.addOnItemTouchListener(new SimpleOnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                gestureDetectorCompat.onTouchEvent(e);
                return super.onInterceptTouchEvent(rv, e);
            }
        });
        //        parent.addOnLayoutChangeListener(new OnLayoutChangeListener() {
        //            @Override
        //            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop,
        //                int oldRight, int oldBottom) {
        //                //                Log.d(TAG, "onLayoutChange() called with: v = [" + v + "], left = [" + left + "], top = [" + top
        //                //                    + "], right = [" + right + "], bottom = [" + bottom + "], oldLeft = [" + oldLeft + "], oldTop = ["
        //                //                    + oldTop + "], oldRight = [" + oldRight + "], oldBottom = [" + oldBottom + "]");
        //            }
        //        });
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

    public void setInputMoreItems(List<ZIMKitInputButtonModel> inputMoreItems) {
        binding.inputViewMoreLayout.setInputMoreItems(inputMoreItems);
    }
}
