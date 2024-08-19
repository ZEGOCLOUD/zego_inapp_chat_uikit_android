package com.zegocloud.zimkit.common.adapter;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.databinding.BindingAdapter;
import com.zegocloud.zimkit.R;
import com.zegocloud.zimkit.common.glide.ZIMKitGlideLoader;
import com.zegocloud.zimkit.common.utils.ZIMKitFileUtils;
import com.zegocloud.zimkit.services.internal.ZIMKitCore;
import im.zego.zim.enums.ZIMConversationNotificationStatus;
import im.zego.zim.enums.ZIMConversationType;

public class ZIMKitBindingAdapter {

    @BindingAdapter("unReadCount")
    public static void setCount(TextView view, int count) {
        if (count <= 0) {
            view.setVisibility(View.GONE);
        } else {
            view.setVisibility(View.VISIBLE);
            String strCount = count + "";
            if (count > 99) {
                strCount = "99+";
            }
            view.setText(strCount);
        }
    }

    @BindingAdapter("unReadCountBackground")
    public static void setUnReadCountBackground(View view, ZIMConversationNotificationStatus notificationStatus) {
        if (notificationStatus == ZIMConversationNotificationStatus.NOTIFY) {
            view.setBackgroundResource(R.drawable.zimkit_shape_oval_ff4a50);
        } else if (notificationStatus == ZIMConversationNotificationStatus.DO_NOT_DISTURB) {
            view.setBackgroundResource(R.drawable.zimkit_shape_oval_babbc0);
        }
    }

    @BindingAdapter("conversationItemBackground")
    public static void setConversationItemBackground(View view, boolean isPinned) {
        if (isPinned) {
            view.setBackgroundColor(view.getResources().getColor(R.color.color_f8f8f8));
        } else {
            view.setBackgroundColor(view.getResources().getColor(R.color.color_ffffff));
        }
    }

    @BindingAdapter("backgroundColor")
    public static void setBackgroundColor(View view, boolean isShowWhite) {
        if (isShowWhite) {
            view.setBackgroundColor(ZIMKitCore.getInstance().getApplication().getResources().getColor(R.color.white));
        } else {
            view.setBackgroundColor(
                ZIMKitCore.getInstance().getApplication().getResources().getColor(R.color.color_eff0f2));
        }
    }

    /**
     * Load message image
     *
     * @param imageView
     * @param url
     * @param width
     * @param height
     * @param fileName
     */
    @BindingAdapter(value = {"messageImageUrl", "width", "height", "fileName"}, requireAll = false)
    public static void loadMessageImage(ImageView imageView, String url, int width, int height, String fileName) {
        String suffixStr = ZIMKitFileUtils.getFileSuffix(fileName);
        if (!TextUtils.isEmpty(fileName) && suffixStr.equals("gif")) {
            ZIMKitGlideLoader.displayMessageGifImage(imageView, url, width, height);
        } else {
            ZIMKitGlideLoader.displayMessageImage(imageView, url, width, height);
        }
    }

    /**
     * Loading session list avatars
     *
     * @param imageView
     * @param url
     * @param type
     */
    @BindingAdapter(value = {"avatarConversationUrl", "conversationType"}, requireAll = false)
    public static void loadConversationAvatar(ImageView imageView, String url, ZIMConversationType type) {
        ZIMKitGlideLoader.displayConversationAvatarImage(imageView, url, type);
    }

    /**
     * Loading message content avatar
     *
     * @param imageView
     * @param url
     */
    @BindingAdapter(value = {"avatarMessageUrl"}, requireAll = false)
    public static void loadMessageAvatar(ImageView imageView, String url) {
        ZIMKitGlideLoader.displayMessageAvatarImage(imageView, url);
    }

    @BindingAdapter(value = {"fileIcon"}, requireAll = false)
    public static void loadFileIcon(ImageView imageView, int url) {
        ZIMKitGlideLoader.displayLocalImage(imageView, url);
    }

}
