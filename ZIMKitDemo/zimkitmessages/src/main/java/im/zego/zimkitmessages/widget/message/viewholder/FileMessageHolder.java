package im.zego.zimkitmessages.widget.message.viewholder;

import android.text.TextUtils;
import android.view.View;

import androidx.databinding.ViewDataBinding;

import im.zego.zim.callback.ZIMMediaDownloadedCallback;
import im.zego.zim.entity.ZIMError;
import im.zego.zim.entity.ZIMFileMessage;
import im.zego.zim.entity.ZIMMediaMessage;
import im.zego.zim.enums.ZIMErrorCode;
import im.zego.zim.enums.ZIMMediaFileType;
import im.zego.zim.enums.ZIMMessageDirection;
import im.zego.zim.enums.ZIMMessageSentStatus;
import im.zego.zimkitcommon.ZIMKitManager;
import im.zego.zimkitcommon.utils.ZIMKitBackgroundTasks;
import im.zego.zimkitcommon.utils.ZIMKitFileUtils;
import im.zego.zimkitmessages.ZIMKitMessageManager;
import im.zego.zimkitmessages.databinding.MessageItemReceiveFileBinding;
import im.zego.zimkitmessages.databinding.MessageItemSendFileBinding;
import im.zego.zimkitmessages.interfaces.NetworkConnectionListener;
import im.zego.zimkitmessages.model.message.FileMessageModel;
import im.zego.zimkitmessages.model.message.ZIMKitMessageModel;

public class FileMessageHolder extends MessageViewHolder {

    private MessageItemSendFileBinding sendFileBinding;
    private MessageItemReceiveFileBinding receiveFileBinding;
    private NetworkConnectionListener networkConnectionListener;

    public FileMessageHolder(ViewDataBinding binding) {
        super(binding);
        if (binding instanceof MessageItemSendFileBinding) {
            sendFileBinding = (MessageItemSendFileBinding) binding;
        } else if (binding instanceof MessageItemReceiveFileBinding) {
            receiveFileBinding = (MessageItemReceiveFileBinding) binding;
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
        ZIMKitManager.share().zim().downloadMediaFile((ZIMFileMessage) fileMessageModel.getMessage(), ZIMMediaFileType.ORIGINAL_FILE, new ZIMMediaDownloadedCallback() {
            @Override
            public void onMediaDownloaded(ZIMMediaMessage message, ZIMError errorInfo) {
                if (errorInfo.code == ZIMErrorCode.SUCCESS) {
                    fileMessageModel.setFileLocalPath(message.getFileLocalPath());
                    if (fileMessageModel.isSizeLimit() && fileMessageModel.getMessage() != null) {
                        ZIMKitMessageManager.share().removeLimitFile(fileMessageModel.getMessage().getMessageID());
                    }
                    ZIMKitMessageManager.share().unRegisterNetworkListener(networkConnectionListener);
                }
            }

            @Override
            public void onMediaDownloadingProgress(ZIMMediaMessage message, long currentFileSize, long totalFileSize) {

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
