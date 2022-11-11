package im.zego.zimkitmessages.widget.message.viewholder;

import android.graphics.drawable.AnimationDrawable;
import android.text.TextUtils;

import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.FragmentActivity;

import im.zego.zim.callback.ZIMMediaDownloadedCallback;
import im.zego.zim.entity.ZIMAudioMessage;
import im.zego.zim.entity.ZIMError;
import im.zego.zim.entity.ZIMMediaMessage;
import im.zego.zim.enums.ZIMErrorCode;
import im.zego.zim.enums.ZIMMediaFileType;
import im.zego.zim.enums.ZIMMessageDirection;
import im.zego.zimkitcommon.ZIMKitManager;
import im.zego.zimkitcommon.utils.ZIMKitToastUtils;
import im.zego.zimkitcommon.utils.ZIMKitFileUtils;
import im.zego.zimkitcommon.utils.ZIMKitSPUtils;
import im.zego.zimkitcommon.utils.ZIMKitScreenUtils;
import im.zego.zimkitmessages.R;
import im.zego.zimkitmessages.ZIMKitMessageManager;
import im.zego.zimkitmessages.databinding.MessageItemReceiveAudioBinding;
import im.zego.zimkitmessages.databinding.MessageItemSendAudioBinding;
import im.zego.zimkitmessages.interfaces.NetworkConnectionListener;
import im.zego.zimkitmessages.model.message.AudioMessageModel;
import im.zego.zimkitmessages.model.message.ZIMKitMessageModel;
import im.zego.zimkitmessages.widget.ZIMKitAudioPlayer;

public class AudioMessageHolder extends MessageViewHolder {

    //Minimum width of speech bubbles
    private static final int AUDIO_MIN_WIDTH = ZIMKitScreenUtils.getPxByDp(70);
    //Height of audio bubbles
    private static final int AUDIO_HEIGHT = ZIMKitScreenUtils.getPxByDp(43);

    private MessageItemSendAudioBinding sendAudioBinding;
    private MessageItemReceiveAudioBinding receiveAudioBinding;

    private AnimationDrawable animationReceiveBgDrawable = null;
    private AnimationDrawable animationSendBgDrawable = null;
    private AnimationDrawable animationDrawable = null;

    private NetworkConnectionListener networkConnectionListener;
    private AudioMessageModel modelPlaying;

    public AudioMessageHolder(ViewDataBinding binding) {
        super(binding);
        if (binding instanceof MessageItemSendAudioBinding) {
            sendAudioBinding = (MessageItemSendAudioBinding) binding;
        } else if (binding instanceof MessageItemReceiveAudioBinding) {
            receiveAudioBinding = (MessageItemReceiveAudioBinding) binding;
        }
    }

    @Override
    public void bind(int id, int position, ZIMKitMessageModel model) {
        super.bind(id, position, model);
        if (model instanceof AudioMessageModel) {
            AudioMessageModel audioMessageModel = (AudioMessageModel) model;
            boolean isSend = model.getDirection() == ZIMMessageDirection.SEND;
            int conWidth = AUDIO_MIN_WIDTH + ZIMKitScreenUtils.getPxByDp(audioMessageModel.getAudioDuration() * 3);
            int maxWidth = getAudioMaxWidth();
            if (conWidth > maxWidth) {
                conWidth = maxWidth;
            }
            setLayoutParams(conWidth, AUDIO_HEIGHT, isSend ? sendAudioBinding.audioContentLl : receiveAudioBinding.audioContentLl);
            mMutiSelectCheckBox = isSend ? sendAudioBinding.selectCheckbox : receiveAudioBinding.selectCheckbox;
            msgContent = isSend ? sendAudioBinding.audioContentLl : receiveAudioBinding.audioContentLl;
            if (isSend) {
                sendAudioBinding.audioContentLl.setOnClickListener(v -> initClickListener(audioMessageModel, true));
                sendAudioBinding.audioContentLl.setOnLongClickListener(v -> {
                    initLongClickListener(v, position, model);
                    return true;
                });
            } else {
                receiveAudioBinding.audioContentLl.setOnClickListener(v -> initClickListener(audioMessageModel, false));
                receiveAudioBinding.audioContentLl.setOnLongClickListener(v -> {
                    initLongClickListener(v, position, model);
                    return true;
                });
            }

            //The bubble animation flashes when the audio is not downloaded successfully
            setBgAnimation(audioMessageModel, isSend);

            //Automatic audio download
            if (TextUtils.isEmpty(audioMessageModel.getFileLocalPath()) && !TextUtils.isEmpty(audioMessageModel.getFileDownloadUrl())) {
                networkConnectionListener = new NetworkConnectionListener() {
                    @Override
                    public void onConnected() {
                        // Network reconnect, re-download
                        downloadMediaFile(audioMessageModel);
                    }
                };
                downloadMediaFile(audioMessageModel);
                ZIMKitMessageManager.share().registerNetworkListener(networkConnectionListener);
            }

            //Avoid pausing the audio animation when the page is refreshed or when the page is swiped
            if (audioMessageModel.isPlaying.getValue()) {
                if (isSend) {
                    sendAudioBinding.audioPlayIv.setBackgroundResource(R.drawable.message_play_send_audio);
                    animationDrawable = (AnimationDrawable) sendAudioBinding.audioPlayIv.getBackground();
                } else {
                    receiveAudioBinding.audioPlayIv.setBackgroundResource(R.drawable.message_play_receive_audio);
                    animationDrawable = (AnimationDrawable) receiveAudioBinding.audioPlayIv.getBackground();
                }
                animationDrawable.start();
            }

            audioMessageModel.isPlaying.observe((FragmentActivity) context, aBoolean -> {
                //Playback animation pauses when the audio is paused and restores the default display icon
                if (!aBoolean) {
                    if (animationDrawable != null) {
                        animationDrawable.stop();
                    }
                    if (isSend) {
                        sendAudioBinding.audioPlayIv.setBackgroundResource(R.drawable.message_ic_audio_send_3);
                    } else {
                        receiveAudioBinding.audioPlayIv.setBackgroundResource(R.drawable.message_ic_audio_receive_3);
                    }
                }
            });
        }
    }

    private void setBgAnimation(AudioMessageModel audioMessageModel, boolean isSend) {
        if (TextUtils.isEmpty(audioMessageModel.getFileLocalPath())) {
            audioMessageModel.isDownloadComplete.observe((FragmentActivity) context, aBoolean -> {
                if (aBoolean) {
                    if (isSend) {
                        if (animationSendBgDrawable != null) {
                            animationSendBgDrawable.stop();
                        }
                        sendAudioBinding.audioContentLl.setBackgroundResource(im.zego.zimkitcommon.R.drawable.common_shape_8dp_ff3478fc);
                    } else {
                        if (animationReceiveBgDrawable != null) {
                            animationReceiveBgDrawable.stop();
                        }
                        receiveAudioBinding.audioContentLl.setBackgroundResource(im.zego.zimkitcommon.R.drawable.common_shape_8dp_white);
                    }
                } else {
                    if (isSend) {
                        sendAudioBinding.audioContentLl.setBackgroundResource(R.drawable.message_send_audio_exception_bg);
                        animationSendBgDrawable = (AnimationDrawable) sendAudioBinding.audioContentLl.getBackground();
                        animationSendBgDrawable.start();
                    } else {
                        receiveAudioBinding.audioContentLl.setBackgroundResource(R.drawable.message_receive_audio_exception_bg);
                        animationReceiveBgDrawable = (AnimationDrawable) receiveAudioBinding.audioContentLl.getBackground();
                        animationReceiveBgDrawable.start();
                    }
                }
            });
        } else {
            if (isSend) {
                if (animationSendBgDrawable != null) {
                    animationSendBgDrawable.stop();
                }
                sendAudioBinding.audioContentLl.setBackgroundResource(im.zego.zimkitcommon.R.drawable.common_shape_8dp_ff3478fc);
            } else {
                if (animationReceiveBgDrawable != null) {
                    animationReceiveBgDrawable.stop();
                }
                receiveAudioBinding.audioContentLl.setBackgroundResource(im.zego.zimkitcommon.R.drawable.common_shape_8dp_white);
            }
        }

    }

    private void initClickListener(AudioMessageModel audioMessageModel, boolean isSend) {

        if (TextUtils.isEmpty(audioMessageModel.getFileLocalPath())) {
            downloadMediaFile(audioMessageModel);
            return;
        }

        boolean isExists = ZIMKitFileUtils.fileIsExists(audioMessageModel.getFileLocalPath());
        if (!isExists) {
            ZIMKitToastUtils.showToast(R.string.message_play_error_tip);
            downloadMediaFile(audioMessageModel);
            return;
        }

        if (ZIMKitAudioPlayer.getInstance().isPlaying()) {
            if (modelPlaying != null) {
                modelPlaying.setPlaying(false);
            }
            ZIMKitAudioPlayer.getInstance().stopPlay();
            // Same audio message, stop playing, different audio message, replay
            if (TextUtils.equals(ZIMKitAudioPlayer.getInstance().getPath(), audioMessageModel.getFileLocalPath())) {
                return;
            }
        }
        this.modelPlaying = audioMessageModel;

        if (isSend) {
            sendAudioBinding.audioPlayIv.setBackgroundResource(R.drawable.message_play_send_audio);
            animationDrawable = (AnimationDrawable) sendAudioBinding.audioPlayIv.getBackground();
        } else {
            receiveAudioBinding.audioPlayIv.setBackgroundResource(R.drawable.message_play_receive_audio);
            animationDrawable = (AnimationDrawable) receiveAudioBinding.audioPlayIv.getBackground();
        }
        animationDrawable.start();

        audioMessageModel.setPlaying(true);
        //Whether it is speaker mode
        boolean isSpeaker = ZIMKitSPUtils.getBoolean(ZIMKitSPUtils.KEY_AUDIO_PLAY_MODE, true);
        if (mAdapter != null) {
            mAdapter.setAudioPlayByEarPhone(isSpeaker);
        }
        //Audio starts playing
        ZIMKitAudioPlayer.getInstance().startPlay(audioMessageModel.getFileLocalPath(), new ZIMKitAudioPlayer.PlayCallback() {
            @Override
            public void onCompletion(Boolean success) {
                audioMessageModel.setPlaying(false);
            }
        });
    }

    /**
     * audio Download
     *
     * @param audioMessageModel
     */
    private void downloadMediaFile(AudioMessageModel audioMessageModel) {
        ZIMKitManager.share().zim().downloadMediaFile((ZIMAudioMessage) audioMessageModel.getMessage(), ZIMMediaFileType.ORIGINAL_FILE, new ZIMMediaDownloadedCallback() {
            @Override
            public void onMediaDownloaded(ZIMMediaMessage message, ZIMError errorInfo) {
                if (errorInfo.code == ZIMErrorCode.SUCCESS) {
                    audioMessageModel.setFileLocalPath(message.getFileLocalPath());
                    ZIMKitMessageManager.share().unRegisterNetworkListener(networkConnectionListener);
                }
            }

            @Override
            public void onMediaDownloadingProgress(ZIMMediaMessage message, long currentFileSize, long totalFileSize) {

            }
        });
    }

    /**
     * Maximum width of speech bubbles
     *
     * @return
     */
    private int getAudioMaxWidth() {
        int mScreenWidth = ZIMKitScreenUtils.getScreenWidth(ZIMKitManager.share().getApplication());
        return (int) (mScreenWidth * 0.5);
    }

}
