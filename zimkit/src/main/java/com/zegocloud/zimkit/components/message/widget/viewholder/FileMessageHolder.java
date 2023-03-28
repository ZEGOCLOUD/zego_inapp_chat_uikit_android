package com.zegocloud.zimkit.components.message.widget.viewholder;

import android.text.TextUtils;
import android.view.View;

import androidx.databinding.ViewDataBinding;

import com.zegocloud.zimkit.common.utils.ZIMKitBackgroundTasks;
import com.zegocloud.zimkit.common.utils.ZIMKitFileUtils;
import com.zegocloud.zimkit.services.ZIMKit;
import com.zegocloud.zimkit.services.callback.DownloadMediaFileCallback;
import com.zegocloud.zimkit.services.utils.MessageTransform;
import im.zego.zim.entity.ZIMError;
import im.zego.zim.enums.ZIMErrorCode;
import im.zego.zim.enums.ZIMMessageDirection;
import im.zego.zim.enums.ZIMMessageSentStatus;
import com.zegocloud.zimkit.components.message.ZIMKitMessageManager;
import com.zegocloud.zimkit.components.message.interfaces.NetworkConnectionListener;
import com.zegocloud.zimkit.components.message.model.FileMessageModel;
import com.zegocloud.zimkit.components.message.model.ZIMKitMessageModel;
import com.zegocloud.zimkit.databinding.ZimkitItemMessageReceiveFileBinding;
import com.zegocloud.zimkit.databinding.ZimkitItemMessageSendFileBinding;

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
            boolean isSend = model.getDirection() == ZIMMessageDirection.SEND;
            mMutiSelectCheckBox = isSend ? sendFileBinding.selectCheckbox : receiveFileBinding.selectCheckbox;
            msgContent = isSend ? sendFileBinding.fileContentCl : receiveFileBinding.fileContentCl;
            if (isSend) {
                sendFileBinding.fileContentCl.setOnClickListener(v -> {
                    if (TextUtils.isEmpty(fileMessageModel.getFileLocalPath())) {
                        if (fileMessageModel.getSentStatus() == ZIMMessageSentStatus.SUCCESS) {
                            addLimitFile(fileMessageModel);
                        }
                        sendFileBinding.viewDownload.setVisibility(View.VISIBLE);
                        sendFileBinding.fileDownloadPb.setVisibility(View.VISIBLE);
                        ZIMKitMessageManager.share().registerNetworkListener(networkConnectionListener);
                        downloadMediaFile(fileMessageModel);
                    } else {
                        toOpenFile(fileMessageModel);
                    }
                });
                sendFileBinding.fileContentCl.setOnLongClickListener(v -> {
                    initLongClickListener(v, position, model);
                    return true;
                });
            } else {
                receiveFileBinding.fileContentCl.setOnClickListener(v -> {
                    if (TextUtils.isEmpty(fileMessageModel.getFileLocalPath())) {
                        addLimitFile(fileMessageModel);
                        receiveFileBinding.viewDownload.setVisibility(View.VISIBLE);
                        receiveFileBinding.fileDownloadPb.setVisibility(View.VISIBLE);
                        ZIMKitMessageManager.share().registerNetworkListener(networkConnectionListener);
                        downloadMediaFile(fileMessageModel);
                    } else {
                        toOpenFile(fileMessageModel);
                    }
                });
                receiveFileBinding.fileContentCl.setOnLongClickListener(v -> {
                    initLongClickListener(v, position, model);
                    return true;
                });
            }
            networkConnectionListener = new NetworkConnectionListener() {
                @Override
                public void onConnected() {
                    // Network reconnect, re-download
                    downloadMediaFile(fileMessageModel);
                }
            };

            //Automatic download of files less than 10M
            if (TextUtils.isEmpty(fileMessageModel.getFileLocalPath()) && !TextUtils.isEmpty(fileMessageModel.getFileDownloadUrl()) &&
                    !fileMessageModel.isSizeLimit()) {
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
        ZIMKit.downloadMediaFile(
            MessageTransform.parseMessage(fileMessageModel.getMessage()), new DownloadMediaFileCallback() {
            @Override
            public void onDownloadMediaFile(ZIMError error) {
                if (error.code == ZIMErrorCode.SUCCESS) {
                    ZIMKitMessageManager.share().unRegisterNetworkListener(networkConnectionListener);
                }
            }
        });
    }

    /**
     * Record file download
     * Exit and come back in to know what files are being downloaded
     *
     * @param fileMessageModel
     */
    private void addLimitFile(FileMessageModel fileMessageModel) {
        if (fileMessageModel.isSizeLimit() && fileMessageModel.getMessage().getMessageID() != 0) {
            ZIMKitMessageManager.share().addLimitFile(fileMessageModel.getMessage().getMessageID());
        }
    }

}
