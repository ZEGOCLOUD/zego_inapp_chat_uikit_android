package im.zego.zimkitmessages.widget.message.input;

import static android.content.Context.VIBRATOR_SERVICE;
import static im.zego.zimkitmessages.widget.message.input.ZIMKitInputView.ChatRecordHandler.RECORD_CANCEL;
import static im.zego.zimkitmessages.widget.message.input.ZIMKitInputView.ChatRecordHandler.RECORD_FAILED;
import static im.zego.zimkitmessages.widget.message.input.ZIMKitInputView.ChatRecordHandler.RECORD_START;
import static im.zego.zimkitmessages.widget.message.input.ZIMKitInputView.ChatRecordHandler.RECORD_STOP;
import static im.zego.zimkitmessages.widget.message.input.ZIMKitInputView.ChatRecordHandler.RECORD_TOO_SHORT;

import android.content.Context;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.DataBindingUtil;

import java.util.Formatter;

import im.zego.zimkitcommon.glide.ZIMKitGlideLoader;
import im.zego.zimkitcommon.utils.ZIMKitCustomToastUtil;
import im.zego.zimkitmessages.R;
import im.zego.zimkitmessages.databinding.MessageLayoutRecordAudioBinding;
import im.zego.zimkitmessages.widget.ZIMKitAudioPlayer;

public class RecordAudioView extends ConstraintLayout {

    private MessageLayoutRecordAudioBinding mBinding;
    private Context context;
    /**
     * Cell phone vibrator
     */
    private Vibrator vibrator;

    public RecordAudioView(Context context) {
        super(context);
        initView(context);
    }

    public RecordAudioView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public RecordAudioView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        this.context = context;
        mBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.message_layout_record_audio, this, true);

        // System services for vibration effects
        vibrator = (Vibrator) context.getSystemService(VIBRATOR_SERVICE);
    }

    public void recordStatus(int status) {
        switch (status) {
            case RECORD_START:
                startRecording();
                break;
            case RECORD_STOP:
                stopRecording();
                break;
            case RECORD_CANCEL:
                cancelRecording();
                break;
            case RECORD_TOO_SHORT:
            case RECORD_FAILED:
                stopAbnormally(status);
                break;
            default:
                break;
        }
    }

    public void onRecordCountDownTimer(long recordTime) {
        long countdownTime = recordTime / 1000;
        if (countdownTime == 9) {
            mBinding.ivGif.setVisibility(GONE);
            mBinding.tvRecordCountdown.setVisibility(VISIBLE);
            vibrator.vibrate(new long[]{200, 2000, 2000, 200, 200, 200}, -1);
        }
        if (countdownTime <= 9) {
            mBinding.tvRecordCountdown.setText(String.format(context.getString(R.string.message_audio_record_stop), countdownTime + 1));
        }
    }

    public String stringForTime(long timeMs) {
        long totalSeconds = timeMs / 1000;
        long seconds = totalSeconds % 60;
        long minutes = (totalSeconds / 60) % 60;
        return new Formatter().format("%02d:%02d", minutes, seconds).toString();
    }

    private void startRecording() {
        mBinding.clRecording.post(new Runnable() {
            @Override
            public void run() {
                setVisibility(View.VISIBLE);
                ZIMKitAudioPlayer.getInstance().stopPlay();
                mBinding.tvRecordTip.setText(context.getString(R.string.message_audio_record_tip1));
                ZIMKitGlideLoader.displayGifImage(mBinding.ivGif, R.mipmap.message_ic_audio_play_start);
            }
        });
    }

    private void stopRecording() {
        mBinding.clRecording.post(new Runnable() {
            @Override
            public void run() {
                setVisibility(View.GONE);
                mBinding.ivGif.setVisibility(VISIBLE);
            }
        });
    }

    private void cancelRecording() {
        mBinding.clRecording.post(new Runnable() {
            @Override
            public void run() {
                mBinding.tvRecordTip.setText(context.getString(R.string.message_audio_record_tip2));
                ZIMKitGlideLoader.displayGifImage(mBinding.ivGif, R.mipmap.message_ic_audio_play_cancel);
            }
        });
    }

    private void stopAbnormally(final int status) {
        mBinding.clRecording.post(new Runnable() {
            @Override
            public void run() {
                setVisibility(View.GONE);
                if (status == RECORD_TOO_SHORT) {
                    // toast tip
                    ZIMKitCustomToastUtil.showToast(context, context.getString(R.string.message_audio_record_too_short));
                } else {
                    //fail
                    ZIMKitCustomToastUtil.showToast(context, context.getString(R.string.message_record_fail));
                }
            }
        });
    }

}
