package com.zegocloud.zimkit.components.message.widget.viewholder;

import androidx.databinding.ViewDataBinding;

import im.zego.zim.enums.ZIMMessageDirection;
import im.zego.zim.enums.ZIMMessageSentStatus;
import com.zegocloud.zimkit.components.album.browserimage.ZIMKitBrowserImageActivity;
import com.zegocloud.zimkit.components.message.model.ImageMessageModel;
import com.zegocloud.zimkit.components.message.model.ZIMKitMessageModel;
import com.zegocloud.zimkit.databinding.ZimkitItemMessageReceivePhotoBinding;
import com.zegocloud.zimkit.databinding.ZimkitItemMessageSendPhotoBinding;

public class ImageMessageHolder extends MessageViewHolder {

    private ZimkitItemMessageSendPhotoBinding sendPhotoBinding;
    private ZimkitItemMessageReceivePhotoBinding receivePhotoBinding;

    public ImageMessageHolder(ViewDataBinding binding) {
        super(binding);
        if (binding instanceof ZimkitItemMessageSendPhotoBinding) {
            sendPhotoBinding = (ZimkitItemMessageSendPhotoBinding) binding;
        } else if (binding instanceof ZimkitItemMessageReceivePhotoBinding) {
            receivePhotoBinding = (ZimkitItemMessageReceivePhotoBinding) binding;
        }
    }

    @Override
    public void bind(int id, int position, ZIMKitMessageModel model) {
        super.bind(id, position, model);
        if (model instanceof ImageMessageModel) {
            ImageMessageModel imageMessageModel = (ImageMessageModel) model;
            boolean isSend = model.getDirection() == ZIMMessageDirection.SEND;
            setLayoutParams(imageMessageModel.getImgWidth(), imageMessageModel.getImgHeight(), isSend ? sendPhotoBinding.sendPhoto : receivePhotoBinding.receivePhoto);
            mMutiSelectCheckBox = isSend ? sendPhotoBinding.selectCheckbox : receivePhotoBinding.selectCheckbox;
            msgContent = isSend ? sendPhotoBinding.sendPhoto : receivePhotoBinding.receivePhoto;
            if (isSend) {
                sendPhotoBinding.sendPhoto.setOnClickListener(v -> gotoImageActivity(imageMessageModel));
                sendPhotoBinding.sendPhoto.setOnLongClickListener(v -> {
                    initLongClickListener(v, position, model);
                    return true;
                });
            } else {
                receivePhotoBinding.receivePhoto.setOnClickListener(v -> gotoImageActivity(imageMessageModel));
                receivePhotoBinding.receivePhoto.setOnLongClickListener(v -> {
                    initLongClickListener(v, position, model);
                    return true;
                });
            }
        }
    }

    /**
     * View Image
     *
     * @param imageMessageModel
     */
    private void gotoImageActivity(ImageMessageModel imageMessageModel) {
        if (imageMessageModel.getMessage().getSentStatus() == ZIMMessageSentStatus.SUCCESS) {
            ZIMKitBrowserImageActivity.startActivity(context, imageMessageModel.getFileDownloadUrl(), imageMessageModel.getLargeImageDownloadUrl(), imageMessageModel.getFileName());
        }
    }

}
