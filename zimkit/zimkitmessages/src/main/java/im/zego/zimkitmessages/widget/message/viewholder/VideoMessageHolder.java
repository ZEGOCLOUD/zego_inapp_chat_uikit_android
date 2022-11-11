package im.zego.zimkitmessages.widget.message.viewholder;

import android.content.Intent;

import androidx.databinding.ViewDataBinding;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import im.zego.zim.callback.ZIMMediaDownloadedCallback;
import im.zego.zim.entity.ZIMError;
import im.zego.zim.entity.ZIMMediaMessage;
import im.zego.zim.entity.ZIMVideoMessage;
import im.zego.zim.enums.ZIMErrorCode;
import im.zego.zim.enums.ZIMMediaFileType;
import im.zego.zim.enums.ZIMMessageDirection;
import im.zego.zim.enums.ZIMMessageSentStatus;
import im.zego.zimkitcommon.ZIMKitConstant;
import im.zego.zimkitcommon.ZIMKitManager;
import im.zego.zimkitcommon.utils.ZIMKitFileUtils;
import im.zego.zimkitmessages.component.video.ZIMKitVideoViewActivity;
import im.zego.zimkitmessages.databinding.MessageItemReceiveVideoBinding;
import im.zego.zimkitmessages.databinding.MessageItemSendVideoBinding;
import im.zego.zimkitmessages.model.message.VideoMessageModel;
import im.zego.zimkitmessages.model.message.ZIMKitMessageModel;

public class VideoMessageHolder extends MessageViewHolder {

    private MessageItemSendVideoBinding sendVideoBinding;
    private MessageItemReceiveVideoBinding receiveVideoBinding;

    public VideoMessageHolder(ViewDataBinding binding) {
        super(binding);
        if (binding instanceof MessageItemSendVideoBinding) {
            sendVideoBinding = (MessageItemSendVideoBinding) binding;
        } else if (binding instanceof MessageItemReceiveVideoBinding) {
            receiveVideoBinding = (MessageItemReceiveVideoBinding) binding;
        }
    }

    @Override
    public void bind(int id, int position, ZIMKitMessageModel model) {
        super.bind(id, position, model);
        if (model instanceof VideoMessageModel) {
            VideoMessageModel videoMessageModel = (VideoMessageModel) model;
            boolean isSend = model.getDirection() == ZIMMessageDirection.SEND;
            setLayoutParams(videoMessageModel.getImgWidth(), videoMessageModel.getImgHeight(), isSend ? sendVideoBinding.sendVideo : receiveVideoBinding.receiveVideo);
            mMutiSelectCheckBox = isSend ? sendVideoBinding.selectCheckbox : receiveVideoBinding.selectCheckbox;
            msgContent = isSend ? sendVideoBinding.sendVideo : receiveVideoBinding.receiveVideo;
            long m = TimeUnit.SECONDS.toMinutes(videoMessageModel.getVideoDuration());
            long s = videoMessageModel.getVideoDuration() - m * 60;
            String durationStr = String.format(Locale.CHINA, "%01d:%02d", m, TimeUnit.SECONDS.toSeconds(s));
            if (isSend) {
                sendVideoBinding.tvDuration.setText(durationStr);
                sendVideoBinding.sendVideo.setOnClickListener(v -> playVideo(videoMessageModel));
                sendVideoBinding.sendVideo.setOnLongClickListener(v -> {
                    initLongClickListener(v, position, model);
                    return true;
                });
            } else {
                receiveVideoBinding.tvDuration.setText(durationStr);
                receiveVideoBinding.receiveVideo.setOnClickListener(v -> playVideo(videoMessageModel));
                receiveVideoBinding.receiveVideo.setOnLongClickListener(v -> {
                    initLongClickListener(v, position, model);
                    return true;
                });
            }
        }
    }

    /**
     * Jump to play video
     *
     * @param videoMessageModel
     */
    private void playVideo(VideoMessageModel videoMessageModel) {
        if (videoMessageModel.getMessage().getSentStatus() == ZIMMessageSentStatus.SUCCESS) {
            boolean isExists = ZIMKitFileUtils.fileIsExists(videoMessageModel.getFileLocalPath());
            String playPath = isExists ? videoMessageModel.getFileLocalPath() : videoMessageModel.getFileDownloadUrl();

            if (!isExists) {
                downloadMediaFile(videoMessageModel);
            }

            Intent intent = new Intent(context, ZIMKitVideoViewActivity.class);
            intent.putExtra(ZIMKitConstant.VideoPageConstant.KEY_VIDEO_PATH, playPath);
            context.startActivity(intent);

//            Uri uri = Uri.parse(playPath);
//            //Call the player that comes with the system
//            Intent intent = new Intent(Intent.ACTION_VIEW);
//            intent.setDataAndType(uri, "video/*");
//            try {
//                context.startActivity(intent);
//            } catch (Exception e) {
//                ToastUtils.showToast("没有默认播放器");
//            }
        }
    }

    /**
     * Video Download
     *
     * @param videoMessageModel
     */
    private void downloadMediaFile(VideoMessageModel videoMessageModel) {
        ZIMKitManager.share().zim().downloadMediaFile((ZIMVideoMessage) videoMessageModel.getMessage(), ZIMMediaFileType.ORIGINAL_FILE, new ZIMMediaDownloadedCallback() {
            @Override
            public void onMediaDownloaded(ZIMMediaMessage message, ZIMError errorInfo) {
                if (errorInfo.code == ZIMErrorCode.SUCCESS) {
                    ZIMKitVideoViewActivity.filePath = message.getFileLocalPath();
                    videoMessageModel.setFileLocalPath(message.getFileLocalPath());
                }
            }

            @Override
            public void onMediaDownloadingProgress(ZIMMediaMessage message, long currentFileSize, long totalFileSize) {

            }
        });
    }

}
