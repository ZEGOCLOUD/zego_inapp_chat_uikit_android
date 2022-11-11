package im.zego.zimkitcommon.adapter;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import androidx.databinding.BindingAdapter;

import im.zego.zim.enums.ZIMConversationType;
import im.zego.zimkitcommon.ZIMKitManager;
import im.zego.zimkitcommon.components.widget.UnreadCountView;
import im.zego.zimkitcommon.glide.ZIMKitGlideLoader;
import im.zego.zimkitcommon.utils.ZIMKitFileUtils;

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
            view.setBackgroundColor(ZIMKitManager.share().getApplication().getResources().getColor(im.zego.zimkitcommon.R.color.white));
        } else {
            view.setBackgroundColor(ZIMKitManager.share().getApplication().getResources().getColor(im.zego.zimkitcommon.R.color.color_f2f2f2));
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
    @BindingAdapter(value = {"app:messageImageUrl", "app:width", "app:height", "app:fileName"}, requireAll = false)
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
    @BindingAdapter(value = {"app:avatarConversationUrl", "app:conversationType"}, requireAll = false)
    public static void loadConversationAvatar(ImageView imageView, String url, ZIMConversationType type) {
        ZIMKitGlideLoader.displayConversationAvatarImage(imageView, url, type);
    }

    /**
     * Loading message content avatar
     *
     * @param imageView
     * @param url
     */
    @BindingAdapter(value = {"app:avatarMessageUrl"}, requireAll = false)
    public static void loadMessageAvatar(ImageView imageView, String url) {
        ZIMKitGlideLoader.displayMessageAvatarImage(imageView, url);
    }

    @BindingAdapter(value = {"app:fileIcon"}, requireAll = false)
    public static void loadFileIcon(ImageView imageView, int url) {
        ZIMKitGlideLoader.displayLocalImage(imageView, url);
    }

}
