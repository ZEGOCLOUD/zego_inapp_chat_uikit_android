package com.zegocloud.zimkit.components.message.widget.viewholder;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup;
import androidx.databinding.ViewDataBinding;
import com.zegocloud.zimkit.R;
import com.zegocloud.zimkit.components.album.browserimage.ZIMKitBrowserImageActivity;
import com.zegocloud.zimkit.components.message.model.ImageMessageModel;
import com.zegocloud.zimkit.components.message.model.ZIMKitMessageModel;
import com.zegocloud.zimkit.databinding.ZimkitItemMessageReceivePhotoBinding;
import com.zegocloud.zimkit.databinding.ZimkitItemMessageSendPhotoBinding;
import im.zego.zim.enums.ZIMMessageDirection;
import im.zego.zim.enums.ZIMMessageSentStatus;

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
            boolean isSend;
            if (mAdapter.isOneSideForwardMode()) {
                isSend = false;
            } else {
                isSend = model.getDirection() == ZIMMessageDirection.SEND;
            }
            setLayoutParams(imageMessageModel.getImgWidth(), imageMessageModel.getImgHeight(),
                isSend ? sendPhotoBinding.msgContentPhoto : receivePhotoBinding.msgContentPhoto);

            ViewGroup itemMessageLayout = itemView.findViewById(R.id.item_message_layout);
            if (model.getReactions().isEmpty() && model.getMessage().getRepliedInfo() == null) {
                itemMessageLayout.setPadding(0, 0, 0, 0);
            } else {
                DisplayMetrics displayMetrics = itemMessageLayout.getContext().getResources().getDisplayMetrics();
                itemMessageLayout.setPadding(dp2px(12, displayMetrics), dp2px(10, displayMetrics),
                    dp2px(12, displayMetrics), dp2px(10, displayMetrics));
            }
        }
    }

    /**
     * View Image
     *
     * @param imageMessageModel
     */
    public void onMessageLayoutClicked(ImageMessageModel imageMessageModel) {
        if (imageMessageModel.getMessage().getSentStatus() == ZIMMessageSentStatus.SUCCESS) {
            ZIMKitBrowserImageActivity.startActivity(itemView.getContext(), imageMessageModel.getFileLocalPath(),
                imageMessageModel.getThumbnailDownloadUrl(), imageMessageModel.getLargeImageDownloadUrl(),
                imageMessageModel.getFileName());
        }
    }

}
