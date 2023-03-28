package com.zegocloud.zimkit.common.adapter;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import androidx.databinding.BindingAdapter;

import im.zego.zim.enums.ZIMConversationType;
import com.zegocloud.zimkit.R;
import com.zegocloud.zimkit.common.components.widget.UnreadCountView;
import com.zegocloud.zimkit.common.glide.ZIMKitGlideLoader;
import com.zegocloud.zimkit.common.utils.ZIMKitFileUtils;
import com.zegocloud.zimkit.services.internal.ZIMKitCore;

public class ZIMKitBindingAdapter {
    @BindingAdapter("unReadCount")
    public static void setCount(UnreadCountView view, int count) {
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

    @BindingAdapter("backgroundColor")
    public static void setBackgroundColor(View view, boolean isShowWhite) {
        if (isShowWhite) {
            view.setBackgroundColor(ZIMKitCore.getInstance().getApplication().getResources().getColor(R.color.white));
        } else {
            view.setBackgroundColor(ZIMKitCore.getInstance().getApplication().getResources().getColor(R.color.color_f2f2f2));
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
