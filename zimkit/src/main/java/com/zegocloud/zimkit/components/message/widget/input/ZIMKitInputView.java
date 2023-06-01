package com.zegocloud.zimkit.components.message.widget.input;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.provider.Settings;
import android.text.Editable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentManager;
import com.zegocloud.zimkit.R;
import com.zegocloud.zimkit.common.base.BaseDialog;
import com.zegocloud.zimkit.common.utils.PermissionHelper;
import com.zegocloud.zimkit.common.utils.ZIMKitToastUtils;
import com.zegocloud.zimkit.components.message.model.ZIMKitEmojiItemModel;
import com.zegocloud.zimkit.components.message.model.ZIMKitInputModel;
import com.zegocloud.zimkit.components.message.model.ZIMKitMessageModel;
import com.zegocloud.zimkit.components.message.ui.ZIMKitEmojiFragment;
import com.zegocloud.zimkit.components.message.ui.ZIMKitInputMoreFragment;
import com.zegocloud.zimkit.components.message.utils.ChatMessageBuilder;
import com.zegocloud.zimkit.components.message.widget.ZIMKitAudioPlayer;
import com.zegocloud.zimkit.databinding.ZimkitLayoutInputBinding;
import com.zegocloud.zimkit.services.internal.ZIMKitCore;


public class ZIMKitInputView extends LinearLayout {

    private ZimkitLayoutInputBinding mBinding;
    private AppCompatActivity mActivity;
    private ZIMKitInputModel mInputModel;
    private MessageHandler mMessageHandler;
    private ChatRecordHandler mChatRecordHandler;

    private FragmentManager mFragmentManager;
    private ZIMKitEmojiFragment mEmojiFragment;
    private ZIMKitInputMoreFragment mInputMoreFragment;

    private boolean mAudioCancel;
    private float mStartRecordY;

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
        mActivity = (AppCompatActivity) getContext();
        mBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.zimkit_layout_input, this, true);
        mInputModel = new ZIMKitInputModel();
        mBinding.setModel(mInputModel);
        mBinding.setConfig(ZIMKitCore.getInstance().getInputConfig());

        initListener();
    }

    private void initListener() {
        mBinding.etMessage.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    removeFragment();
                    showSoftInput();
                }
                return false;
            }
        });

        //Input Box - Emoji Button
        mBinding.btnEmoji.setOnClickListener(v -> {
            boolean emojiStatus = !mInputModel.isShowEmoji.get();
            mBinding.btnEmoji.setBackgroundResource(emojiStatus ? R.mipmap.zimkit_icon_emoji_audio_show : R.mipmap.zimkit_icon_emoji_close);
            mInputModel.fileClick(false);
            mInputModel.audioClick(false);
            removeFragment();
            if (emojiStatus) {
                mInputModel.emojiClick(true);
                showEmojiViewGroup();
                mBinding.btnAudio.setBackgroundResource(R.mipmap.zimkit_icon_audio_close);
            } else {
                showSoftInput();
            }
        });

        //Input Box - more Button
        mBinding.btnMore.setOnClickListener(v -> {
            boolean moreStatus = !mInputModel.isShowFile.get();
            mInputModel.emojiClick(false);
            mInputModel.audioClick(false);
            removeFragment();
            if (moreStatus) {
                mInputModel.fileClick(true);
                //Show more message delivery layout
                showInputMoreLayout();
                mBinding.btnEmoji.setBackgroundResource(R.mipmap.zimkit_icon_emoji_close);
                mBinding.btnAudio.setBackgroundResource(R.mipmap.zimkit_icon_audio_close);
            } else {
                showSoftInput();
            }
        });

        //Input Box - audio Button
        mBinding.btnAudio.setOnClickListener(v -> {
            PermissionHelper.onMicrophonePermissionGranted(mActivity, new PermissionHelper.GrantResult() {
                @Override
                public void onGrantResult(boolean allGranted) {
                    if (allGranted) {
                        boolean audioStatus = !mInputModel.isShowAudio.get();
                        mBinding.btnAudio.setBackgroundResource(audioStatus ? R.mipmap.zimkit_icon_emoji_audio_show : R.mipmap.zimkit_icon_audio_close);
                        mInputModel.fileClick(false);
                        mInputModel.emojiClick(false);
                        removeFragment();
                        if (audioStatus) {
                            mInputModel.audioClick(true);
                            hideSoftInput();
                            mBinding.btnEmoji.setBackgroundResource(R.mipmap.zimkit_icon_emoji_close);
                        } else {
                            mInputModel.audioClick(false);
                            mBinding.etMessage.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    showSoftInput();
                                }
                            }, 100);
                        }
                    } else {
                        BaseDialog baseDialog = new BaseDialog(mActivity);
                        baseDialog.setMsgTitle(mActivity.getString(R.string.zimkit_photo_no_mic_tip));
                        baseDialog.setMsgContent(mActivity.getString(R.string.zimkit_photo_no_mic_description));
                        baseDialog.setLeftButtonContent(mActivity.getString(R.string.zimkit_access_later));
                        baseDialog.setRightButtonContent(mActivity.getString(R.string.zimkit_go_setting));
                        baseDialog.setSureListener(v -> {
                            baseDialog.dismiss();
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                    .setData(Uri.fromParts("package", mActivity.getPackageName(), null));
                            mActivity.startActivityForResult(intent, 666);
                        });
                        baseDialog.setCancelListener(v -> {
                            baseDialog.dismiss();
                        });
                    }
                }
            });
        });

        //Messaging
        mBinding.btnSend.setOnClickListener(v -> {
            String inputMsg = mInputModel.inputMessage.get();
            if (inputMsg == null || inputMsg.trim().isEmpty()) {
                ZIMKitToastUtils.showToast(mActivity.getString(R.string.zimkit_cant_send_empty_msg));
                return;
            }
            if (mMessageHandler != null) {
                mMessageHandler.sendMessage(ChatMessageBuilder.buildTextMessage(inputMsg));
            }
            mInputModel.inputMessage.set("");
        });

        //audio Recording
        mBinding.chatAudioInput.setOnTouchListener(new OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mAudioCancel = true;
                        mStartRecordY = motionEvent.getY();
                        if (mChatRecordHandler != null) {
                            mChatRecordHandler.onRecordStatusChanged(ChatRecordHandler.RECORD_START);
                        }
                        mInputModel.setAudioRecordBtn(true);
                        mBinding.chatAudioInput.setText(mActivity.getString(R.string.zimkit_audio_record_release_to_send));
                        ZIMKitAudioPlayer.getInstance().startRecord(new ZIMKitAudioPlayer.RecordCallback() {
                            @Override
                            public void onCompletion(Boolean success) {
                                mInputModel.setAudioRecordBtn(false);
                                recordComplete(success);
                            }

                            @Override
                            public void onRecordCountDownTimer(long recordTime) {
                                if (mChatRecordHandler != null) {
                                    mChatRecordHandler.onRecordCountDownTimer(recordTime);
                                }
                            }
                        });
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (motionEvent.getY() - mStartRecordY < -100) {
                            mAudioCancel = true;
                            mBinding.chatAudioInput.setText(mActivity.getString(R.string.zimkit_audio_record_release_to_cancel));
                            if (mChatRecordHandler != null) {
                                mChatRecordHandler.onRecordStatusChanged(ChatRecordHandler.RECORD_CANCEL);
                            }
                        } else {
                            if (mAudioCancel) {
                                mBinding.chatAudioInput.setText(mActivity.getString(R.string.zimkit_audio_record_release_to_send));
                                if (mChatRecordHandler != null) {
                                    mChatRecordHandler.onRecordStatusChanged(ChatRecordHandler.RECORD_START);
                                }
                            }
                            mAudioCancel = false;
                        }
                        break;
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        mAudioCancel = motionEvent.getY() - mStartRecordY < -100;
                        if (mChatRecordHandler != null) {
                            mChatRecordHandler.onRecordStatusChanged(ChatRecordHandler.RECORD_STOP);
                        }
                        ZIMKitAudioPlayer.getInstance().stopRecord();
                        mBinding.chatAudioInput.setText(mActivity.getString(R.string.zimkit_audio_record_normal));
                        break;
                    default:
                        break;
                }

                return false;
            }
        });

        //Multiple choice deletion
        mBinding.tvMultiDelete.setOnClickListener(view -> {
            if (mMessageHandler != null) {
                mMessageHandler.deleteMultiSelect();
            }
        });
    }

    private void removeFragment() {
        if (mEmojiFragment != null) {
            mFragmentManager.beginTransaction().remove(mEmojiFragment).commitAllowingStateLoss();
            mEmojiFragment = null;
        }
        if (mInputMoreFragment != null) {
            mInputMoreFragment.setInputMoreCallback(null);
            mFragmentManager.beginTransaction().remove(mInputMoreFragment).commitAllowingStateLoss();
            mInputMoreFragment = null;
        }
    }

    // show emoji
    private void showEmojiViewGroup() {
        if (mFragmentManager == null) {
            mFragmentManager = mActivity.getSupportFragmentManager();
        }
        if (mEmojiFragment == null) {
            mEmojiFragment = new ZIMKitEmojiFragment();
        }
        hideSoftInput();

        mBinding.etMessage.postDelayed(new Runnable() {
            @Override
            public void run() {
                mBinding.inputMoreView.setVisibility(View.VISIBLE);
                mBinding.etMessage.requestFocus();
                mFragmentManager.beginTransaction().replace(R.id.input_more_view, mEmojiFragment).commitAllowingStateLoss();
            }
        }, 100);

        mEmojiFragment.setEmojiListener(new ZIMKitEmojiFragment.OnEmojiClickListener() {
            @Override
            public void onEmojiDelete() {
                int action = KeyEvent.ACTION_DOWN;
                int code = KeyEvent.KEYCODE_DEL;
                KeyEvent event = new KeyEvent(action, code);
                mBinding.etMessage.onKeyDown(KeyEvent.KEYCODE_DEL, event);
            }

            @Override
            public void onEmojiClick(ZIMKitEmojiItemModel emoji) {
                int index = mBinding.etMessage.getSelectionStart();
                Editable editable = mBinding.etMessage.getText();
                editable.insert(index, emoji.getEmojiContent());
            }
        });

        if (mMessageHandler != null) {
            mMessageHandler.scrollToEnd();
        }
    }

    /**
     * Show more
     */
    private void showInputMoreLayout() {
        if (mFragmentManager == null) {
            mFragmentManager = mActivity.getSupportFragmentManager();
        }
        if (mInputMoreFragment == null) {
            mInputMoreFragment = new ZIMKitInputMoreFragment();
        }
        hideSoftInput();

        mBinding.etMessage.postDelayed(new Runnable() {
            @Override
            public void run() {
                mBinding.inputMoreView.setVisibility(View.VISIBLE);
                mFragmentManager.beginTransaction().replace(R.id.input_more_view, mInputMoreFragment).commitAllowingStateLoss();
            }
        }, 100);

        if (mMessageHandler != null) {
            mMessageHandler.scrollToEnd();
        }

        if (mInputMoreFragment != null) {
            mInputMoreFragment.setInputMoreCallback(new ZIMKitInputMoreFragment.InputMoreCallback() {
                @Override
                public void selectFile(Uri uri) {
                    ZIMKitMessageModel info = ChatMessageBuilder.buildFileMessage(uri);
                    if (mMessageHandler != null && info != null) {
                        mMessageHandler.sendMediaMessage(info);
                    }
                }

                @Override
                public void selectPhoto() {
                    if (mMessageHandler != null) {
                        mMessageHandler.openPhoto();
                    }
                }
            });
        }
    }

    /**
     * Hide more
     */
    private void hideInputMoreLayout() {
        mBinding.inputMoreView.setVisibility(View.GONE);
    }

    /**
     * show keyboard
     */
    public void showSoftInput() {
        hideInputMoreLayout();
        mInputModel.emojiClick(false);
        mInputModel.fileClick(false);
        mInputModel.audioClick(false);
        mBinding.btnEmoji.setBackgroundResource(R.mipmap.zimkit_icon_emoji_close);
        mBinding.btnAudio.setBackgroundResource(R.mipmap.zimkit_icon_audio_close);
        mBinding.etMessage.requestFocus();
        InputMethodManager imm = (InputMethodManager) mBinding.etMessage.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mBinding.etMessage, InputMethodManager.SHOW_FORCED);

        if (mMessageHandler != null) {
            mMessageHandler.scrollToEnd();
        }
    }

    /**
     * Hide keyboard
     */
    public void hideSoftInput() {
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mBinding.etMessage.getWindowToken(), 0);
        mBinding.etMessage.clearFocus();
        mBinding.inputMoreView.setVisibility(View.GONE);
    }

    /**
     * Show multi-selected delete button
     */
    public void showMultiSelectDelete() {
        mInputModel.setBackgroundColorIsWhite(false);
        mBinding.clInputView.setVisibility(GONE);
        mBinding.tvMultiDelete.setVisibility(VISIBLE);
        mBinding.btnEmoji.setBackgroundResource(R.mipmap.zimkit_icon_emoji_close);
    }

    /**
     * Hide the multi-select delete button and restore the input box
     */
    public void hideMultiSelectDelete() {
        mInputModel.setBackgroundColorIsWhite(!mInputModel.isShowAudio.get());
        mBinding.clInputView.setVisibility(VISIBLE);
        mBinding.tvMultiDelete.setVisibility(GONE);
    }

    /**
     * Click on the blank area to put away the keyboard
     */
    public void onEmptyClick() {
        hideSoftInput();
        mBinding.btnEmoji.setBackgroundResource(R.mipmap.zimkit_icon_emoji_close);
        mInputModel.emojiClick(false);
        mInputModel.fileClick(false);
    }

    /**
     * Display Keyboard
     *
     * @return
     */
    private boolean isSoftInputShown() {
        View decorView = ((Activity) getContext()).getWindow().getDecorView();
        int screenHeight = decorView.getHeight();
        Rect rect = new Rect();
        decorView.getWindowVisibleDisplayFrame(rect);
        return screenHeight - rect.bottom - getNavigateBarHeight() >= 0;
    }

    /**
     * Compatible with navigation keys
     *
     * @return
     */
    private int getNavigateBarHeight() {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metrics);
        int usableHeight = metrics.heightPixels;
        windowManager.getDefaultDisplay().getRealMetrics(metrics);
        int realHeight = metrics.heightPixels;
        if (realHeight > usableHeight) {
            return realHeight - usableHeight;
        } else {
            return 0;
        }
    }

    private void recordComplete(boolean success) {
        int duration = ZIMKitAudioPlayer.getInstance().getDuration();
        if (mChatRecordHandler != null) {
            if (!success || duration == 0) {
                mChatRecordHandler.onRecordStatusChanged(ChatRecordHandler.RECORD_FAILED);
                return;
            }
            if (mAudioCancel) {
                mChatRecordHandler.onRecordStatusChanged(ChatRecordHandler.RECORD_CANCEL);
                return;
            }
            if (duration < 1000) {
                mChatRecordHandler.onRecordStatusChanged(ChatRecordHandler.RECORD_TOO_SHORT);
                return;
            }
            mChatRecordHandler.onRecordStatusChanged(ChatRecordHandler.RECORD_STOP);
        }

        if (mMessageHandler != null && success) {
            mMessageHandler.sendMediaMessage(ChatMessageBuilder.buildAudioMessage(ZIMKitAudioPlayer.getInstance().getPath(), duration));
        }
    }

    public void setMessageHandler(MessageHandler handler) {
        this.mMessageHandler = handler;
    }

    public void setChatRecordHandler(ChatRecordHandler handler) {
        this.mChatRecordHandler = handler;
    }

    public interface MessageHandler {
        void sendMessage(ZIMKitMessageModel model);

        void sendMediaMessage(ZIMKitMessageModel model);

        void scrollToEnd();

        void deleteMultiSelect();

        void openPhoto();
    }

    public interface ChatRecordHandler {
        int RECORD_START = 1;
        int RECORD_STOP = 2;
        int RECORD_CANCEL = 3;
        int RECORD_TOO_SHORT = 4;
        int RECORD_FAILED = 5;

        void onRecordStatusChanged(int status);

        void onRecordCountDownTimer(long recordTime);
    }

}
