package com.zegocloud.zimkit.components.message.widget;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import com.zegocloud.zimkit.R;
import com.zegocloud.zimkit.common.utils.ZIMKitToastUtils;
import com.zegocloud.zimkit.services.internal.ZIMKitCore;
import java.io.File;
import java.util.concurrent.CopyOnWriteArrayList;

public class ZIMKitAudioPlayer {

    private static final String RECORD_DIR_SUFFIX = "/record/";

    //Maximum recording time limit, for error-proof plus 500
    public final static int DEFAULT_AUDIO_RECORD_MAX_TIME = 1000 * 60 + 500;
    public final static int DEFAULT_AUDIO_RECORD_MIN_TIME = 1500;

    private static final ZIMKitAudioPlayer sInstance = new ZIMKitAudioPlayer();
    private static final String CURRENT_RECORD_FILE = getRecordDir() + "auto_";
    private static final int MAGIC_NUMBER = 500;
    private final CopyOnWriteArrayList<MediaRecordCallback> audioRecordCallbackList = new CopyOnWriteArrayList<>();
    private PlayCallback mPlayCallback;

    private String mAudioRecordPath;
    private MediaPlayer mPlayer;
    private MediaRecorder mMediaRecorder;
    private RecordCountDownTimer recordCountDownTimer;
    private Runnable captureSoundRunnable;
    private Handler handler;

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

    private ZIMKitAudioPlayer() {
        handler = new Handler(Looper.getMainLooper());
        captureSoundRunnable = new Runnable() {
            @Override
            public void run() {
                if (mMediaRecorder != null) {
                    int maxAmplitude = mMediaRecorder.getMaxAmplitude();
                    double ratio = (double) maxAmplitude / BASE;
                    double db = 0;//
                    if (ratio > 1) {
                        db = 20 * Math.log(ratio);
                    }
                    db = db - 100; // extra custom adjust
                    for (MediaRecordCallback audioRecordCallback : audioRecordCallbackList) {
                        audioRecordCallback.onRecordSoundVolume(db);
                    }
                    handler.postDelayed(captureSoundRunnable, CAPTURE_SOUND_INTERVAL);
                }
            }
        };
    }

    private static String getCacheDir() {
        return ZIMKitCore.getInstance().getApplication().getFilesDir().getAbsolutePath() + RECORD_DIR_SUFFIX;
    }

    public void addAudioRecordCallback(MediaRecordCallback callback) {
        audioRecordCallbackList.add(callback);
    }

    public void removeAudioRecordCallback(MediaRecordCallback callback) {
        audioRecordCallbackList.remove(callback);
    }

    public void clearAudioRecordCallbacks() {
        audioRecordCallbackList.clear();
    }

    public void startRecord() {
        try {
            if (recordCountDownTimer == null) {
                recordCountDownTimer = new RecordCountDownTimer(DEFAULT_AUDIO_RECORD_MAX_TIME, 1000);
            }
            mAudioRecordPath = CURRENT_RECORD_FILE + System.currentTimeMillis() + ".m4a";
            mMediaRecorder = new MediaRecorder();
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mMediaRecorder.setOutputFile(mAudioRecordPath);
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mMediaRecorder.prepare();
            mMediaRecorder.start();
            recordCountDownTimer.start();
            for (MediaRecordCallback audioRecordCallback : audioRecordCallbackList) {
                audioRecordCallback.onRecordStart();
            }
            startCaptureSound();
        } catch (Exception e) {
            stopRecord();
            for (MediaRecordCallback audioRecordCallback : audioRecordCallbackList) {
                audioRecordCallback.onRecordStopped(MediaRecordCallback.STOP_EXCEPTION);
            }
        }
    }

    private void stopRecord() {
        if (mMediaRecorder == null) {
            return;
        }
        mMediaRecorder.release();
        mMediaRecorder = null;

        if (recordCountDownTimer != null) {
            recordCountDownTimer.cancel();
        }
        recordCountDownTimer = null;

        handler.removeCallbacks(captureSoundRunnable);
    }

    private int BASE = 1;
    private int CAPTURE_SOUND_INTERVAL = 200;// 间隔取样时间

    public void startCaptureSound() {
        handler.postDelayed(captureSoundRunnable, CAPTURE_SOUND_INTERVAL);
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
        return mPlayer != null && mPlayer.isPlaying();
    }

    private void onPlayCompleted(boolean success) {
        if (mPlayCallback != null) {
            mPlayCallback.onCompletion(success);
        }
        mPlayer = null;
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

    private static final String TAG = "ZIMKitAudioPlayer";

    public void finishAndSendAudioRecord() {
        boolean timeShort = false;
        if (recordCountDownTimer != null) {
            timeShort = recordCountDownTimer.passedTime <= DEFAULT_AUDIO_RECORD_MIN_TIME;
        }
        stopRecord();
        for (MediaRecordCallback audioRecordCallback : audioRecordCallbackList) {
            if (timeShort) {
                audioRecordCallback.onRecordStopped(MediaRecordCallback.STOP_TIME_SHORT);
            } else {
                audioRecordCallback.onRecordStopped(MediaRecordCallback.FINISHED_USER);
            }
        }
    }

    public void cancelAudioRecordBecauseTouch() {
        stopRecord();
        for (MediaRecordCallback audioRecordCallback : audioRecordCallbackList) {
            audioRecordCallback.onRecordStopped(MediaRecordCallback.CANCELED_USER);
        }
    }

    //Record Video Timer
    private class RecordCountDownTimer extends CountDownTimer {

        private final long totalTime;
        private long passedTime;

        RecordCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
            this.totalTime = millisInFuture;
            this.passedTime = 0;
        }

        @Override
        public void onTick(long millisUntilFinished) {
            for (MediaRecordCallback audioRecordCallback : audioRecordCallbackList) {
                passedTime = (totalTime - millisUntilFinished);
                audioRecordCallback.onRecordTimePassed(passedTime);
            }
        }

        @Override
        public void onFinish() {
            stopRecord();
            for (MediaRecordCallback audioRecordCallback : audioRecordCallbackList) {
                audioRecordCallback.onRecordStopped(MediaRecordCallback.FINISH_TIME_LIMIT);
            }
        }
    }

    public interface MediaRecordCallback {

        int CANCELED_USER = 1;
        int FINISHED_USER = 2;
        int FINISH_TIME_LIMIT = 3;
        int STOP_TIME_SHORT = 4;
        int STOP_EXCEPTION = 5;

        default void onRecordStart() {
        }

        default void onRecordStopped(int status) {
        }

        default void onRecordTimePassed(long recordTime) {
        }

        default void onRecordSoundVolume(double volume) {
        }
    }

    public interface PlayCallback {

        void onCompletion(Boolean success);
    }

}
