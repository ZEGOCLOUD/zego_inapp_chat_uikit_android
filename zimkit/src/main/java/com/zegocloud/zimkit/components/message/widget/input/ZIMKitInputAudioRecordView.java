package com.zegocloud.zimkit.components.message.widget.input;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import com.zegocloud.zimkit.R;
import com.zegocloud.zimkit.components.message.widget.ZIMKitAudioPlayer;
import com.zegocloud.zimkit.components.message.widget.ZIMKitAudioPlayer.MediaRecordCallback;
import com.zegocloud.zimkit.databinding.ZimkitLayoutAudioRecordBinding;

public class ZIMKitInputAudioRecordView extends FrameLayout {

    /**
     * start record after RECORD_START_MIN_TIME,not right now. so quick down and up will not change UI.
     */
    private static final int RECORD_START_MIN_TIME = 300;
    private ZimkitLayoutAudioRecordBinding binding;
    private boolean isRecording;
    private boolean readyCancel;
    private Handler handler;
    private Runnable startRecordRunable;
    private MediaRecordCallback audioRecordCallback;

    public ZIMKitInputAudioRecordView(@NonNull Context context) {
        super(context);
        initView();
    }

    public ZIMKitInputAudioRecordView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public ZIMKitInputAudioRecordView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.zimkit_layout_audio_record, this,
            true);
        handler = new Handler(Looper.getMainLooper());
        startRecordRunable = new Runnable() {
            @Override
            public void run() {
                startAudioRecord();
                recordingContent();
                recordingVisibility();
            }
        };
        audioRecordCallback = new MediaRecordCallback() {
            @Override
            public void onRecordTimePassed(long recordTime) {
                long time = recordTime / 1000;
                int minutes = (int) (time / 60);
                int seconds = (int) (time % 60);
                String timeText = String.format("%02d:%02d", minutes, seconds);
                binding.audioRecordTime.setText(timeText);
            }

            @Override
            public void onRecordStopped(int status) {
                ZIMKitAudioPlayer.getInstance().removeAudioRecordCallback(audioRecordCallback);
                binding.audioRecordWave.reset();
            }

            @Override
            public void onRecordSoundVolume(double volume) {
                binding.audioRecordWave.updateVolume((int) volume);
            }
        };

    }

    private static final String TAG = "ZIMKitAudioRecordView";

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //        Log.d(TAG, "onTouchEvent() called with: event = [" + event + "]");
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                boolean inRangeOfView = inRangeOfView(binding.audioRecordPressIcon, event);
                if (inRangeOfView) {
                    handler.removeCallbacks(startRecordRunable);
                    handler.postDelayed(startRecordRunable, RECORD_START_MIN_TIME);
                    return true;
                }
            }
            break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                if (isRecording) {
                    if (readyCancel) {
                        cancelAudioRecordBecauseTouch();
                    } else {
                        finishAndSendAudioRecord();
                    }
                } else {
                    // quick touch down and up,not start.
                    stopPendingStartRecord();
                }
                isRecording = false;
                defaultContent();
                defaultVisibility();
            }
            break;
            case MotionEvent.ACTION_MOVE:
                float currentY = event.getY();
                float distanceY;
                int waveBottom = binding.audioRecordWave.getBottom();
                float pressIconTop = binding.audioRecordPressIcon.getTop();
                float cancelIconTop = binding.audioRecordCancelIcon.getTop();
                float maxDistance = cancelIconTop - waveBottom - binding.audioRecordCancelIcon.getHeight() * 0.5f;
                if (isRecording) {
                    if (currentY <= pressIconTop - maxDistance) {
                        distanceY = maxDistance;
                        readyCancel = true;
                    } else if (currentY <= pressIconTop) {
                        // above audio icon
                        distanceY = Math.abs(currentY - pressIconTop);
                        readyCancel = false;
                    } else {
                        // still in range of icon or below audio icon
                        distanceY = 0;
                        readyCancel = false;
                    }
                    updateCancelButton(distanceY, maxDistance);
                    if (readyCancel) {
                        releaseToCancelVisibility();
                    } else {
                        recordingVisibility();
                    }
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    private void defaultVisibility() {
        binding.audioRecordTime.setVisibility(View.GONE);
        binding.audioRecordWave.setVisibility(View.INVISIBLE);
        binding.audioRecordCancelTips.setVisibility(View.GONE);
        binding.audioRecordCancelIcon.setVisibility(View.INVISIBLE);
        binding.audioRecordCancelIconLarge.setVisibility(View.GONE);
        binding.audioRecordPressText.setVisibility(View.VISIBLE);
    }

    private void defaultContent() {
        binding.audioRecordPressText.setText(R.string.input_audio_press_hint);
        binding.audioRecordPressIcon.setImageResource(R.drawable.zimkit_input_audio_record);
        setBackgroundColor(Color.TRANSPARENT);
        updateCancelButton(0, 1);
    }

    private void recordingVisibility() {
        binding.audioRecordTime.setVisibility(View.VISIBLE);
        binding.audioRecordWave.setVisibility(View.VISIBLE);
        binding.audioRecordCancelTips.setVisibility(View.GONE);
        binding.audioRecordCancelIcon.setVisibility(View.VISIBLE);
        binding.audioRecordPressText.setVisibility(View.VISIBLE);
    }

    private void recordingContent() {
        updateCancelButton(0, 1);
        binding.audioRecordPressText.setText(R.string.input_audio_cancel_hint);
        binding.audioRecordPressIcon.setImageResource(R.drawable.zimkit_input_audio_record_ing);
        setBackgroundColor(ContextCompat.getColor(getContext(), R.color.color_F5F6F7));
    }

    private void releaseToCancelVisibility() {
        binding.audioRecordTime.setVisibility(View.GONE);
        binding.audioRecordWave.setVisibility(View.INVISIBLE);
        binding.audioRecordCancelTips.setVisibility(View.VISIBLE);
        binding.audioRecordCancelIcon.setVisibility(View.VISIBLE);
        binding.audioRecordPressText.setVisibility(View.GONE);
    }

    private void stopPendingStartRecord() {
        handler.removeCallbacks(startRecordRunable);
    }

    private void cancelAudioRecordBecauseTouch() {
        ZIMKitAudioPlayer.getInstance().cancelAudioRecordBecauseTouch();
    }

    private void startAudioRecord() {
        isRecording = true;
        ZIMKitAudioPlayer.getInstance().addAudioRecordCallback(audioRecordCallback);
        ZIMKitAudioPlayer.getInstance().startRecord();
    }

    private void finishAndSendAudioRecord() {
        ZIMKitAudioPlayer.getInstance().finishAndSendAudioRecord();
    }

    private void updateCancelButton(float distance, float maxDistance) {
        float scale = 1 + (distance / maxDistance) * 1.5f;
        binding.audioRecordCancelIcon.setScaleX(scale);
        binding.audioRecordCancelIcon.setScaleY(scale);
        if (distance == maxDistance) {
            binding.audioRecordCancelIconLarge.setVisibility(View.VISIBLE);
            binding.audioRecordCancelIcon.setVisibility(View.INVISIBLE);
            binding.audioRecordPressIcon.setImageResource(R.drawable.zimkit_input_audio_record_cancel);
            binding.audioRecordPressIcon.setBackgroundResource(R.drawable.zimkit_shape_circular_white);
        } else {
            binding.audioRecordCancelIconLarge.setVisibility(View.GONE);
            binding.audioRecordCancelIcon.setVisibility(View.VISIBLE);
            if (isRecording) {
                binding.audioRecordPressIcon.setImageResource(R.drawable.zimkit_input_audio_record_ing);
            } else {
                binding.audioRecordPressIcon.setImageResource(R.drawable.zimkit_input_audio_record);
            }
            binding.audioRecordPressIcon.setBackgroundResource(R.drawable.zimkit_input_audio_layout_icon);
        }
    }

    private boolean inRangeOfView(View view, MotionEvent ev) {
        final float translationX = view.getTranslationX();
        final float translationY = view.getTranslationY();
        float x = ev.getX();
        float y = ev.getY();
        if (x >= view.getLeft() + translationX && x <= view.getRight() + translationX
            && y >= view.getTop() + translationY && y <= view.getBottom() + translationY) {
            return true;
        }
        return false;
    }

    public void updateReplyMargin(boolean reply) {
        if (binding != null) {
            if (reply) {
                binding.audioReplyMargin.setVisibility(View.VISIBLE);
            } else {
                binding.audioReplyMargin.setVisibility(View.GONE);
            }
        }
    }
}
