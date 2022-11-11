package im.zego.zimkitmessages.utils;

import static android.content.Context.AUDIO_SERVICE;
import static android.content.Context.POWER_SERVICE;
import static android.content.Context.SENSOR_SERVICE;

import android.annotation.SuppressLint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.os.Build;
import android.os.PowerManager;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import java.lang.ref.WeakReference;

import im.zego.zimkitcommon.utils.ZIMKitSPUtils;
import im.zego.zimkitcommon.utils.ZLog;
import im.zego.zimkitmessages.widget.ZIMKitAudioPlayer;

public class AudioSensorBinder implements LifecycleObserver, SensorEventListener {
    public final String TAG = this.getClass().getSimpleName();

    private final AudioManager audioManager;
    private final PowerManager powerManager;

    @Nullable
    private WeakReference<AppCompatActivity> activity;
    private SensorManager sensorManager;
    private Sensor sensor;
    private PowerManager.WakeLock wakeLock;

    @RequiresApi(api = Build.VERSION_CODES.M)
    public AudioSensorBinder(@Nullable AppCompatActivity mActivity) {
        this.activity = new WeakReference<>(mActivity);
        //Can listen to the life cycle
        if (getActivity() != null) {
            getActivity().getLifecycle().addObserver(this);
        }
        audioManager = (AudioManager) getActivity().getSystemService(AUDIO_SERVICE);
        powerManager = (PowerManager) getActivity().getSystemService(POWER_SERVICE);
        registerProximitySensorListener();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onResume() {

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroy() {
        ZLog.i(TAG, "onDestroy: onDestroy");
        sensorManager.unregisterListener(this);
        sensorManager = null;
        wakeLock = null;
        activity = null;
    }

    /**
     * Register a distance sensor listener to monitor the user's proximity to the phone handset
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void registerProximitySensorListener() {
        if (getActivity() == null) {
            return;
        }
        sensorManager = (SensorManager) getActivity().getSystemService(SENSOR_SERVICE);
        if (sensorManager == null) {
            return;
        }
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }


    private AppCompatActivity getActivity() {
        if (activity != null) {
            return activity.get();
        }
        return null;
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (audioManager == null) {
            return;
        }
        if (isHeadphonesPlugged()) {
            // If the headset is plugged in, set the distance sensor to fail
            return;
        }

        if (ZIMKitAudioPlayer.getInstance().isPlaying()) {
            // If the audio is playing
            float distance = event.values[0];
            if (distance >= sensor.getMaximumRange()) {
                boolean isSpeaker = ZIMKitSPUtils.getBoolean(ZIMKitSPUtils.KEY_AUDIO_PLAY_MODE, true);
                setScreenOn();
                if (isSpeaker) {
                    changeToSpeaker();
                } else {
                    changeToReceiver();
                }
            } else {
                // Users close to the earpiece, switch audio to the earpiece output, and turn off the screen to prevent accidental touch
                setScreenOff();
                changeToReceiver();
                audioManager.setSpeakerphoneOn(false);
            }
        } else {
            boolean isSpeaker = ZIMKitSPUtils.getBoolean(ZIMKitSPUtils.KEY_AUDIO_PLAY_MODE, true);
            setScreenOn();
            if (isSpeaker) {
                changeToSpeaker();
            } else {
                changeToReceiver();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @SuppressLint("WrongConstant")
    private boolean isHeadphonesPlugged() {
        if (audioManager == null) {
            return false;
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            AudioDeviceInfo[] audioDevices = audioManager.getDevices(AudioManager.GET_DEVICES_ALL);
            for (AudioDeviceInfo deviceInfo : audioDevices) {
                if (deviceInfo.getType() == AudioDeviceInfo.TYPE_WIRED_HEADPHONES
                        || deviceInfo.getType() == AudioDeviceInfo.TYPE_WIRED_HEADSET) {
                    return true;
                }
            }
            return false;
        } else {
            return audioManager.isWiredHeadsetOn();
        }
    }

    /**
     * Switch to external playback
     */
    public void changeToSpeaker() {
        if (audioManager == null) {
            return;
        }
        audioManager.setMode(AudioManager.MODE_NORMAL);
        audioManager.setSpeakerphoneOn(true);
    }

    /**
     * Switch to headset mode
     */
    public void changeToHeadset() {
        if (audioManager == null) {
            return;
        }
        audioManager.setSpeakerphoneOn(false);
    }

    /**
     * Switching to handset
     */
    public void changeToReceiver() {
        if (audioManager == null) {
            return;
        }
        audioManager.setSpeakerphoneOn(false);
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
    }

    private void setScreenOff() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (wakeLock == null) {
                wakeLock = powerManager.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, TAG);
            }
            wakeLock.acquire(10 * 60 * 1000L /*10 minutes*/);
        }
    }

    private void setScreenOn() {
        if (wakeLock != null) {
            wakeLock.setReferenceCounted(false);
            wakeLock.release();
            wakeLock = null;
        }
    }
}
