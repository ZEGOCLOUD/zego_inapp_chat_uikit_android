package com.zegocloud.zimkit.components.message.widget.viewholder;

import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import androidx.databinding.ViewDataBinding;
import com.zegocloud.uikit.plugin.signaling.ZegoSignalingPlugin;
import com.zegocloud.zimkit.R;
import com.zegocloud.zimkit.common.utils.ZIMKitBackgroundTasks;
import com.zegocloud.zimkit.common.utils.ZIMKitFileUtils;
import com.zegocloud.zimkit.components.message.ZIMKitMessageManager;
import com.zegocloud.zimkit.components.message.interfaces.NetworkConnectionListener;
import com.zegocloud.zimkit.components.message.model.FileMessageModel;
import com.zegocloud.zimkit.components.message.model.ZIMKitMessageModel;
import com.zegocloud.zimkit.databinding.ZimkitItemMessageReceiveFileBinding;
import com.zegocloud.zimkit.databinding.ZimkitItemMessageSendFileBinding;
import im.zego.zim.callback.ZIMMediaDownloadedCallback;
import im.zego.zim.entity.ZIMError;
import im.zego.zim.entity.ZIMMediaMessage;
import im.zego.zim.enums.ZIMErrorCode;
import im.zego.zim.enums.ZIMMediaFileType;
import im.zego.zim.enums.ZIMMessageDirection;
import im.zego.zim.enums.ZIMMessageSentStatus;

public class FileMessageHolder extends MessageViewHolder {

    private ZimkitItemMessageSendFileBinding sendFileBinding;
    private ZimkitItemMessageReceiveFileBinding receiveFileBinding;
    private NetworkConnectionListener networkConnectionListener;

    public FileMessageHolder(ViewDataBinding binding) {
        super(binding);
        if (binding instanceof ZimkitItemMessageSendFileBinding) {
            sendFileBinding = (ZimkitItemMessageSendFileBinding) binding;
        } else if (binding instanceof ZimkitItemMessageReceiveFileBinding) {
            receiveFileBinding = (ZimkitItemMessageReceiveFileBinding) binding;
        }
    }

    @Override
    public void bind(int id, int position, ZIMKitMessageModel model) {
        super.bind(id, position, model);

        if (model instanceof FileMessageModel) {

            FileMessageModel fileMessageModel = (FileMessageModel) model;
            boolean isSend = isSendMessage(model);

            ViewGroup itemMessageLayout = itemView.findViewById(R.id.item_message_layout);
            if (model.getReactions().isEmpty() && model.getMessage().getRepliedInfo() == null) {
                itemMessageLayout.setPadding(0, 0, 0, 0);
            } else {
                DisplayMetrics displayMetrics = itemMessageLayout.getContext().getResources().getDisplayMetrics();
                itemMessageLayout.setPadding(dp2px(12, displayMetrics), dp2px(10, displayMetrics),
                    dp2px(12, displayMetrics), dp2px(10, displayMetrics));
            }
            if (!isSend) {
                if (model.getReactions().isEmpty()&& model.getMessage().getRepliedInfo() == null) {
                    receiveFileBinding.msgContentLayout.setBackgroundResource(R.drawable.zimkit_shape_12dp_white);
                } else {
                    receiveFileBinding.msgContentLayout.setBackgroundResource(
                        R.drawable.zimkit_shape_12dp_white_eff0f2);
                }
            }
            networkConnectionListener = new NetworkConnectionListener() {
                @Override
                public void onConnected() {
                    // Network reconnect, re-download
                    downloadMediaFile(fileMessageModel);
                }
            };

            //Automatic download of files less than 10M
            if (TextUtils.isEmpty(fileMessageModel.getFileLocalPath()) && !TextUtils.isEmpty(
                fileMessageModel.getFileDownloadUrl()) && !fileMessageModel.isSizeLimit()) {
                downloadMediaFile(fileMessageModel);
                ZIMKitMessageManager.share().registerNetworkListener(networkConnectionListener);
            }

            if (!TextUtils.isEmpty(fileMessageModel.getFileLocalPath()) && fileMessageModel.isSizeLimit()) {
                ZIMKitMessageManager.share().removeLimitFile(fileMessageModel.getMessage().getMessageID());
            }

            if (TextUtils.isEmpty(fileMessageModel.getFileLocalPath()) && fileMessageModel.isSizeLimit()) {
                //Download loading is displayed while the file is still being downloaded
                if (ZIMKitMessageManager.share().isDownloading(fileMessageModel.getMessage().getMessageID())) {
                    ZIMKitBackgroundTasks.getInstance().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (isSend) {
                                sendFileBinding.viewDownload.setVisibility(View.VISIBLE);
                                sendFileBinding.fileDownloadPb.setVisibility(View.VISIBLE);
                            } else {
                                receiveFileBinding.viewDownload.setVisibility(View.VISIBLE);
                                receiveFileBinding.fileDownloadPb.setVisibility(View.VISIBLE);
                            }
                            downloadMediaFile(fileMessageModel);
                        }
                    }, 200);
                }
            }

        }
    }

    private boolean isSendMessage(ZIMKitMessageModel model) {
        boolean isSend;
        if (mAdapter.isOneSideForwardMode()) {
            isSend = false;
        } else {
            isSend = model.getDirection() == ZIMMessageDirection.SEND;
        }
        return isSend;
    }

    public void onMessageLayoutClicked(FileMessageModel fileMessageModel) {
        if (TextUtils.isEmpty(fileMessageModel.getFileLocalPath())) {
            if (isSendMessage(model)) {
                if (fileMessageModel.getSentStatus() == ZIMMessageSentStatus.SUCCESS) {
                    addLimitFile(fileMessageModel);
                }
                sendFileBinding.viewDownload.setVisibility(View.VISIBLE);
                sendFileBinding.fileDownloadPb.setVisibility(View.VISIBLE);
            } else {
                addLimitFile(fileMessageModel);
                receiveFileBinding.viewDownload.setVisibility(View.VISIBLE);
                receiveFileBinding.fileDownloadPb.setVisibility(View.VISIBLE);
            }
            ZIMKitMessageManager.share().registerNetworkListener(networkConnectionListener);
            downloadMediaFile(fileMessageModel);
        } else {
            toOpenFile(fileMessageModel);
        }
    }

    /**
     * Open file
     *
     * @param fileMessageModel
     */
    private void toOpenFile(FileMessageModel fileMessageModel) {
        boolean isExists = ZIMKitFileUtils.fileIsExists(fileMessageModel.getFileLocalPath());
        if (isExists) {
            ZIMKitFileUtils.openFile(fileMessageModel.getFileLocalPath(), fileMessageModel.getFileName());
        } else {
            fileMessageModel.setFileLocalPath("");
            downloadMediaFile(fileMessageModel);
        }
    }

    /**
     * File Download
     *
     * @param fileMessageModel
     */
    private void downloadMediaFile(FileMessageModel fileMessageModel) {
        ZIMMediaMessage mediaMessage = (ZIMMediaMessage) fileMessageModel.getMessage();
        ZIMMediaFileType mediaType = ZIMMediaFileType.ORIGINAL_FILE;
        ZegoSignalingPlugin.getInstance().downloadMediaFile(mediaMessage, mediaType, new ZIMMediaDownloadedCallback() {
            @Override
            public void onMediaDownloaded(ZIMMediaMessage message, ZIMError errorInfo) {
                if (errorInfo.code == ZIMErrorCode.SUCCESS) {
                    fileMessageModel.setFileLocalPath(message.getFileLocalPath());
                    fileMessageModel.setFileSize(message.getFileSize());
                    fileMessageModel.setFileName(message.getFileName());
                    ZIMKitMessageManager.share().unRegisterNetworkListener(networkConnectionListener);
                }
            }

            @Override
            public void onMediaDownloadingProgress(ZIMMediaMessage message, long currentFileSize, long totalFileSize) {

            }
        });
    }

    /**
     * Record file download Exit and come back in to know what files are being downloaded
     *
     * @param fileMessageModel
     */
    private void addLimitFile(FileMessageModel fileMessageModel) {
        if (fileMessageModel.isSizeLimit() && fileMessageModel.getMessage().getMessageID() != 0) {
            ZIMKitMessageManager.share().addLimitFile(fileMessageModel.getMessage().getMessageID());
        }
    }

}
