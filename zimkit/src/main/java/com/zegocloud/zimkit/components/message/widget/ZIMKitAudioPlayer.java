package com.zegocloud.zimkit.components.message.widget;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.CountDownTimer;
import android.text.TextUtils;

import com.zegocloud.zimkit.common.utils.ZIMKitToastUtils;
import java.io.File;

import com.zegocloud.zimkit.R;
import com.zegocloud.zimkit.services.internal.ZIMKitCore;

public class ZIMKitAudioPlayer {

    private static final String RECORD_DIR_SUFFIX = "/record/";

    //Maximum recording time limit, for error-proof plus 500
    public final static int DEFAULT_AUDIO_RECORD_MAX_TIME = 1000 * 60 + 500;

    private static ZIMKitAudioPlayer sInstance = new ZIMKitAudioPlayer();
    private static String CURRENT_RECORD_FILE = getRecordDir() + "auto_";
    private static int MAGIC_NUMBER = 500;
    private RecordCallback mRecordCallback;
    private PlayCallback mPlayCallback;

    private String mAudioRecordPath;
    private MediaPlayer mPlayer;
    private MediaRecorder mRecorder;
    //Recording Timer
    private RecordCountDownTimer timer;

    public static ZIMKitAudioPlayer getInstance() {
        return sInstance;
    }

    public static String getRecordDir() {
        File dir = new File(getCacheDir());
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir.getAbsolutePath();
    }

    private static String getCacheDir() {
        return ZIMKitCore.getInstance().getApplication().getFilesDir().getAbsolutePath() + RECORD_DIR_SUFFIX;
    }

    public void startRecord(RecordCallback callback) {
        mRecordCallback = callback;
        try {
            if (timer == null) {
                timer = new RecordCountDownTimer(DEFAULT_AUDIO_RECORD_MAX_TIME, 1000);
            }
            mAudioRecordPath = CURRENT_RECORD_FILE + System.currentTimeMillis() + ".m4a";
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mRecorder.setOutputFile(mAudioRecordPath);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mRecorder.prepare();
            mRecorder.start();
            timer.start();
        } catch (Exception e) {
            stopInternalRecord();
            onRecordCompleted(false);
        }
    }

    public void stopRecord() {
        stopInternalRecord();
        onRecordCompleted(true);
        mRecordCallback = null;
        if (timer != null) {
            timer.cancel();
        }
        timer = null;
    }

    private void stopInternalRecord() {
        if (mRecorder == null) {
            return;
        }
        mRecorder.release();
        mRecorder = null;
    }

    public void startPlay(String filePath, PlayCallback callback) {
        mAudioRecordPath = filePath;
        mPlayCallback = callback;
        try {
            mPlayer = new MediaPlayer();
            mPlayer.setDataSource(filePath);
            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stopInternalPlay();
                    onPlayCompleted(true);
                }
            });
            mPlayer.prepare();
            mPlayer.start();
        } catch (Exception e) {
            ZIMKitToastUtils.showToast(R.string.zimkit_play_error_tip);
            stopInternalPlay();
            onPlayCompleted(false);
        }
    }

    public void stopPlay() {
        stopInternalPlay();
        onPlayCompleted(false);
        mPlayCallback = null;
    }

    private void stopInternalPlay() {
        if (mPlayer == null) {
            return;
        }
        mPlayer.release();
        mPlayer = null;
    }

    public boolean isPlaying() {
        if (mPlayer != null && mPlayer.isPlaying()) {
            return true;
        }
        return false;
    }

    private void onPlayCompleted(boolean success) {
        if (mPlayCallback != null) {
            mPlayCallback.onCompletion(success);
        }
        mPlayer = null;
    }

    private void onRecordCompleted(boolean success) {
        if (mRecordCallback != null) {
            mRecordCallback.onCompletion(success);
        }
        mRecorder = null;
    }

    public String getPath() {
        return mAudioRecordPath;
    }

    public int getDuration() {
        if (TextUtils.isEmpty(mAudioRecordPath)) {
            return 0;
        }
        int duration = 0;
        // Get the real audio length by initializing the player
        try {
            MediaPlayer mp = new MediaPlayer();
            mp.setDataSource(mAudioRecordPath);
            mp.prepare();
            duration = mp.getDuration();
            // If the length of the audio is 59s more, because the external will /1000 rounding,
            // will always show 59', so here the length is processed to achieve the effect of rounding
            if (duration < 0) {
                duration = 0;
            } else {
                duration = duration + MAGIC_NUMBER;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (duration < 0) {
            duration = 0;
        }
        return duration;
    }

    //Record Video Timer
    private class RecordCountDownTimer extends CountDownTimer {
        RecordCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            long countdown = millisUntilFinished / 1000;
            if (mRecordCallback != null && countdown <= 10) {
                mRecordCallback.onRecordCountDownTimer(millisUntilFinished);
            }
        }

        @Override
        public void onFinish() {
            stopInternalRecord();
            onRecordCompleted(true);
            mRecordCallback = null;
        }
    }

    public interface RecordCallback {
        void onCompletion(Boolean success);

        void onRecordCountDownTimer(long recordTime);
    }

    public interface PlayCallback {
        void onCompletion(Boolean success);
    }

}
