package im.zego.zimkitcommon.glide;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.util.concurrent.ExecutionException;

import im.zego.zim.enums.ZIMConversationType;
import im.zego.zimkitcommon.R;

public class ZIMKitGlideLoader {

    /**
     * Show chat images
     *
     * @param imageView
     * @param url
     * @param width
     * @param height
     */
    public static void displayMessageImage(ImageView imageView, String url, int width, int height) {
        RequestOptions options = new RequestOptions()
                .override(width, height)
                .transform(new CenterCrop())
                .dontAnimate();
        Glide.with(imageView)
                .load(url)
                .placeholder(R.drawable.common_ic_image_placeholder)
                .error(R.drawable.common_ic_image_placeholder)
                .apply(options)
                .diskCacheStrategy(DiskCacheStrategy.ALL) // Setting the policy for caching
                .into(imageView);
    }

    /**
     * Show gif image message
     *
     * @param imageView
     * @param url
     * @param width
     * @param height
     */
    public static void displayMessageGifImage(ImageView imageView, String url, int width, int height) {
        RequestOptions options = new RequestOptions()
                .override(width, height)
                .transform(new CenterCrop());
        Glide.with(imageView)
                .asGif()
                .load(url)
                .placeholder(R.drawable.common_ic_image_placeholder)
                .error(R.drawable.common_ic_image_placeholder)
                .apply(options)
                .diskCacheStrategy(DiskCacheStrategy.ALL) // Setting the policy for caching
                .into(imageView);
    }

    /**
     * Download images
     *
     * @param context
     * @param imageUrl
     * @return
     */
    public static File downloadNetWorkResource(Context context, String imageUrl) throws ExecutionException, InterruptedException {
        return Glide.with(context).asFile().load(imageUrl).submit().get();
    }

    /**
     * Set the avatar of the session
     *
     * @param imageView
     * @param url
     * @param type
     */
    public static void displayConversationAvatarImage(ImageView imageView, String url, ZIMConversationType type) {
        Glide.with(imageView)
                .load(type == ZIMConversationType.PEER ? url : R.drawable.common_ic_group)
                .placeholder(R.drawable.common_ic_avatar_placeholder)
                .error(R.drawable.common_ic_avatar_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL) // Setting the policy for caching
                .into(imageView);
    }

    /**
     * Set the avatar display for chat content
     *
     * @param imageView
     * @param url
     */
    public static void displayMessageAvatarImage(ImageView imageView, String url) {
        Glide.with(imageView)
                .load(url)
                .placeholder(R.drawable.common_ic_avatar_placeholder)
                .error(R.drawable.common_ic_avatar_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL) // Setting the policy for caching
                .into(imageView);
    }


    /**
     * Loading gifs
     *
     * @param imageView
     * @param drawableGif
     */
    public static void displayGifImage(ImageView imageView, int drawableGif) {
        Glide.with(imageView)
                .asGif()
                .load(drawableGif)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView);
    }

    /**
     * Load Local Pictures
     *
     * @param imageView
     * @param drawable
     */
    public static void displayLocalImage(ImageView imageView, int drawable) {
        Glide.with(imageView)
                .load(drawable)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView);
    }

}
