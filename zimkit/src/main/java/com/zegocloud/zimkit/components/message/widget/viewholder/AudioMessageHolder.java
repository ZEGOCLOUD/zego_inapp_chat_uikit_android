package com.zegocloud.zimkit.components.message.widget.viewholder;

import android.graphics.drawable.AnimationDrawable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.FragmentActivity;
import com.zegocloud.uikit.plugin.signaling.ZegoSignalingPlugin;
import com.zegocloud.zimkit.R;
import com.zegocloud.zimkit.common.utils.ZIMKitFileUtils;
import com.zegocloud.zimkit.common.utils.ZIMKitSPUtils;
import com.zegocloud.zimkit.common.utils.ZIMKitScreenUtils;
import com.zegocloud.zimkit.common.utils.ZIMKitToastUtils;
import com.zegocloud.zimkit.components.message.ZIMKitMessageManager;
import com.zegocloud.zimkit.components.message.adapter.ZIMKitMessageAdapter;
import com.zegocloud.zimkit.components.message.interfaces.NetworkConnectionListener;
import com.zegocloud.zimkit.components.message.model.AudioMessageModel;
import com.zegocloud.zimkit.components.message.model.ZIMKitMessageModel;
import com.zegocloud.zimkit.components.message.widget.ZIMKitAudioPlayer;
import com.zegocloud.zimkit.databinding.ZimkitItemMessageReceiveAudioBinding;
import com.zegocloud.zimkit.databinding.ZimkitItemMessageSendAudioBinding;
import com.zegocloud.zimkit.services.ZIMKit;
import com.zegocloud.zimkit.services.callback.DownloadMediaFileCallback;
import com.zegocloud.zimkit.services.internal.ZIMKitCore;
import com.zegocloud.zimkit.services.utils.ZIMMessageUtil;
import im.zego.zim.callback.ZIMMediaDownloadedCallback;
import im.zego.zim.entity.ZIMAudioMessage;
import im.zego.zim.entity.ZIMError;
import im.zego.zim.entity.ZIMMediaMessage;
import im.zego.zim.enums.ZIMErrorCode;
import im.zego.zim.enums.ZIMMediaFileType;
import im.zego.zim.enums.ZIMMessageDirection;

public class AudioMessageHolder extends MessageViewHolder {

    //Minimum width of speech bubbles
    private static final int AUDIO_MIN_WIDTH = ZIMKitScreenUtils.getPxByDp(70);
    //Height of audio bubbles
    private static final int AUDIO_HEIGHT = ZIMKitScreenUtils.getPxByDp(43);

    private ZimkitItemMessageSendAudioBinding sendAudioBinding;
    private ZimkitItemMessageReceiveAudioBinding receiveAudioBinding;

    private AnimationDrawable animationReceiveBgDrawable = null;
    private AnimationDrawable animationSendBgDrawable = null;
    private AnimationDrawable animationDrawable = null;

    private NetworkConnectionListener networkConnectionListener;
    private AudioMessageModel modelPlaying;

    public AudioMessageHolder(ViewDataBinding binding) {
        super(binding);
        if (binding instanceof ZimkitItemMessageSendAudioBinding) {
            sendAudioBinding = (ZimkitItemMessageSendAudioBinding) binding;
        } else if (binding instanceof ZimkitItemMessageReceiveAudioBinding) {
            receiveAudioBinding = (ZimkitItemMessageReceiveAudioBinding) binding;
        }
    }

    @Override
    public void bind(int id, int position, ZIMKitMessageModel model) {
        super.bind(id, position, model);

        if (model instanceof AudioMessageModel) {
            AudioMessageModel audioMessageModel = (AudioMessageModel) model;
            boolean isSend = isSendMessage(model);
            int conWidth = AUDIO_MIN_WIDTH + ZIMKitScreenUtils.getPxByDp(audioMessageModel.getAudioDuration() * 3);
            int maxWidth = getAudioMaxWidth();
            if (conWidth > maxWidth) {
                conWidth = maxWidth;
            }
            ViewGroup audioLayout = itemView.findViewById(R.id.msg_content_layout);
            setLayoutParams(conWidth, AUDIO_HEIGHT, audioLayout);

            ViewGroup itemMessageLayout = itemView.findViewById(R.id.item_message_layout);
            if (model.getReactions().isEmpty() && model.getMessage().getRepliedInfo() == null) {
                itemMessageLayout.setPadding(0, 0, 0, 0);
            } else {
                DisplayMetrics displayMetrics = itemMessageLayout.getContext().getResources().getDisplayMetrics();
                itemMessageLayout.setPadding(dp2px(12, displayMetrics), dp2px(10, displayMetrics),
                    dp2px(12, displayMetrics), dp2px(10, displayMetrics));
            }

            //The bubble animation flashes when the audio is not downloaded successfully
            setBgAnimation(audioMessageModel, isSend);

            //Automatic audio download
            if (TextUtils.isEmpty(audioMessageModel.getFileLocalPath()) && !TextUtils.isEmpty(
                audioMessageModel.getFileDownloadUrl())) {
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
                    sendAudioBinding.msgContentPlayIv.setBackgroundResource(R.drawable.zimkit_play_send_audio);
                    animationDrawable = (AnimationDrawable) sendAudioBinding.msgContentPlayIv.getBackground();
                } else {
                    receiveAudioBinding.msgContentPlayIv.setBackgroundResource(R.drawable.zimkit_play_receive_audio);
                    animationDrawable = (AnimationDrawable) receiveAudioBinding.msgContentPlayIv.getBackground();
                }
                animationDrawable.start();
            }

            audioMessageModel.isPlaying.observe((FragmentActivity) itemView.getContext(), aBoolean -> {
                //Playback animation pauses when the audio is paused and restores the default display icon
                if (!aBoolean) {
                    if (animationDrawable != null) {
                        animationDrawable.stop();
                    }
                    if (isSend) {
                        sendAudioBinding.msgContentPlayIv.setBackgroundResource(R.drawable.zimkit_icon_audio_send_3);
                    } else {
                        receiveAudioBinding.msgContentPlayIv.setBackgroundResource(
                            R.drawable.zimkit_icon_audio_receive_3);
                    }
                }
            });
        }
    }

    public boolean isSendMessage(ZIMKitMessageModel model) {
        boolean isSend;
        if (mAdapter.isOneSideForwardMode()) {
            isSend = false;
        } else {
            isSend = model.getDirection() == ZIMMessageDirection.SEND;
        }
        return isSend;
    }

    private void setBgAnimation(AudioMessageModel audioMessageModel, boolean isSend) {
        if (TextUtils.isEmpty(audioMessageModel.getFileLocalPath())) {
            audioMessageModel.isDownloadComplete.observe((FragmentActivity) itemView.getContext(), aBoolean -> {
                if (aBoolean) {
                    if (isSend) {
                        if (animationSendBgDrawable != null) {
                            animationSendBgDrawable.stop();
                        }
                        if (audioMessageModel.getReactions().isEmpty() && model.getMessage().getRepliedInfo() == null) {
                            sendAudioBinding.msgContentLayout.setBackgroundResource(
                                R.drawable.zimkit_shape_12dp_ff3478fc);
                        } else {
                            sendAudioBinding.msgContentLayout.setBackgroundResource(R.drawable.zimkit_shape_8dp_1a63f1);
                        }
                    } else {
                        if (animationReceiveBgDrawable != null) {
                            animationReceiveBgDrawable.stop();
                        }
                        if (audioMessageModel.getReactions().isEmpty() && model.getMessage().getRepliedInfo() == null) {
                            receiveAudioBinding.msgContentLayout.setBackgroundResource(
                                R.drawable.zimkit_shape_8dp_white);
                        } else {
                            receiveAudioBinding.msgContentLayout.setBackgroundResource(
                                R.drawable.zimkit_shape_8dp_eff0f2);
                        }
                    }
                } else {
                    if (isSend) {
                        sendAudioBinding.msgContentLayout.setBackgroundResource(
                            R.drawable.zimkit_send_audio_exception_bg);
                        animationSendBgDrawable = (AnimationDrawable) sendAudioBinding.msgContentLayout.getBackground();
                        animationSendBgDrawable.start();
                    } else {
                        receiveAudioBinding.msgContentLayout.setBackgroundResource(
                            R.drawable.zimkit_receive_audio_exception_bg);
                        animationReceiveBgDrawable = (AnimationDrawable) receiveAudioBinding.msgContentLayout.getBackground();
                        animationReceiveBgDrawable.start();
                    }
                }
            });
        } else {
            if (isSend) {
                if (animationSendBgDrawable != null) {
                    animationSendBgDrawable.stop();
                }
                if (audioMessageModel.getReactions().isEmpty() && model.getMessage().getRepliedInfo() == null) {
                    sendAudioBinding.msgContentLayout.setBackgroundResource(R.drawable.zimkit_shape_12dp_ff3478fc);
                } else {
                    sendAudioBinding.msgContentLayout.setBackgroundResource(R.drawable.zimkit_shape_8dp_1a63f1);
                }
            } else {
                if (animationReceiveBgDrawable != null) {
                    animationReceiveBgDrawable.stop();
                }
                if (audioMessageModel.getReactions().isEmpty()&& model.getMessage().getRepliedInfo() == null) {
                    receiveAudioBinding.msgContentLayout.setBackgroundResource(R.drawable.zimkit_shape_8dp_white);
                } else {
                    receiveAudioBinding.msgContentLayout.setBackgroundResource(R.drawable.zimkit_shape_8dp_eff0f2);
                }
            }
        }

    }

    public void onMessageLayoutClicked(AudioMessageModel audioMessageModel) {

        if (TextUtils.isEmpty(audioMessageModel.getFileLocalPath())) {
            downloadMediaFile(audioMessageModel);
            return;
        }

        boolean isExists = ZIMKitFileUtils.fileIsExists(audioMessageModel.getFileLocalPath());
        if (!isExists) {
            ZIMKitToastUtils.showToast(R.string.zimkit_play_error_tip);
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

        if (isSendMessage(audioMessageModel)) {
            sendAudioBinding.msgContentPlayIv.setBackgroundResource(R.drawable.zimkit_play_send_audio);
            animationDrawable = (AnimationDrawable) sendAudioBinding.msgContentPlayIv.getBackground();
        } else {
            receiveAudioBinding.msgContentPlayIv.setBackgroundResource(R.drawable.zimkit_play_receive_audio);
            animationDrawable = (AnimationDrawable) receiveAudioBinding.msgContentPlayIv.getBackground();
        }
        animationDrawable.start();

        audioMessageModel.setPlaying(true);
        //Whether it is speaker mode
        boolean isSpeaker = ZIMKitSPUtils.getBoolean(ZIMKitSPUtils.KEY_AUDIO_PLAY_MODE, true);
        if (mAdapter != null) {
            ZIMKitMessageAdapter.setAudioPlayByEarPhone(itemView.getContext(), isSpeaker);
        }
        //Audio starts playing
        ZIMKitAudioPlayer.getInstance()
            .startPlay(audioMessageModel.getFileLocalPath(), new ZIMKitAudioPlayer.PlayCallback() {
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
        ZIMMediaMessage mediaMessage = (ZIMMediaMessage) audioMessageModel.getMessage();
        ZIMMediaFileType mediaType = ZIMMediaFileType.ORIGINAL_FILE;
        ZegoSignalingPlugin.getInstance().downloadMediaFile(mediaMessage, mediaType, new ZIMMediaDownloadedCallback() {
            @Override
            public void onMediaDownloaded(ZIMMediaMessage message, ZIMError errorInfo) {
                ZIMKitMessageManager.share().unRegisterNetworkListener(networkConnectionListener);
                if (errorInfo.code == ZIMErrorCode.SUCCESS) {
                    ZIMAudioMessage audioMessage = (ZIMAudioMessage) message;
                    audioMessageModel.setFileLocalPath(message.getFileLocalPath());
                    audioMessageModel.setAudioDuration(audioMessage.getAudioDuration());
                    audioMessageModel.setFileDownloadUrl(mediaMessage.getFileDownloadUrl());

                    ZIMKitMessageManager.share().unRegisterNetworkListener(networkConnectionListener);
                }
            }

            @Override
            public void onMediaDownloadingProgress(ZIMMediaMessage message, long currentFileSize, long totalFileSize) {

            }
        });
    }

    private static final String TAG = "AudioMessageHolder";

    /**
     * Maximum width of speech bubbles
     *
     * @return
     */
    private int getAudioMaxWidth() {
        int mScreenWidth = ZIMKitScreenUtils.getScreenWidth(ZIMKitCore.getInstance().getApplication());
        return (int) (mScreenWidth * 0.5);
    }
}
