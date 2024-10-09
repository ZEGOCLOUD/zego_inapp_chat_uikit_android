package com.zegocloud.zimkit.common.glide;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;

import com.squareup.picasso.Transformation;
import java.io.File;
import java.util.concurrent.ExecutionException;

import im.zego.zim.enums.ZIMConversationType;

import com.bumptech.glide.request.target.Target;
import com.squareup.picasso.Picasso;
import com.zegocloud.zimkit.R;

public class ZIMKitGlideLoader {

    static class CircleTransform implements Transformation {

        @Override
        public Bitmap transform(Bitmap source) {
            int size = Math.min(source.getWidth(), source.getHeight());

            int x = (source.getWidth() - size) / 2;
            int y = (source.getHeight() - size) / 2;

            Bitmap squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);
            if (squaredBitmap != source) {
                source.recycle();
            }

            Bitmap bitmap = Bitmap.createBitmap(size, size, source.getConfig());

            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();
            BitmapShader shader = new BitmapShader(squaredBitmap, BitmapShader.TileMode.CLAMP,
                BitmapShader.TileMode.CLAMP);
            paint.setShader(shader);
            paint.setAntiAlias(true);

            float r = size / 2f;
            canvas.drawCircle(r, r, r, paint);

            squaredBitmap.recycle();
            return bitmap;
        }

        @Override
        public String key() {
            return "circle";
        }
    }

    /**
     * Show chat images
     *
     * @param imageView
     * @param url
     * @param width
     * @param height
     */
    public static void displayMessageImage(ImageView imageView, String url, int width, int height) {
        RequestOptions options = new RequestOptions().override(width, height).transform(new CenterCrop()).dontAnimate();
        //        Picasso.get().load(url).resize(width,height).centerCrop()
        //            .placeholder(R.drawable.zimkit_icon_image_placeholder).into(imageView);
        Glide.with(imageView).load(url).placeholder(R.drawable.zimkit_icon_image_placeholder)
            .error(R.drawable.zimkit_icon_image_placeholder).apply(options)
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
        RequestOptions options = new RequestOptions().override(width, height).transform(new CenterCrop());
        Glide.with(imageView).asGif().load(url).placeholder(R.drawable.zimkit_icon_image_placeholder)
            .error(R.drawable.zimkit_icon_image_placeholder).apply(options)
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
    public static File downloadNetWorkResource(Context context, String imageUrl)
        throws ExecutionException, InterruptedException {
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
        Glide.with(imageView).load(type == ZIMConversationType.PEER ? url : R.drawable.zimkit_icon_group)
            .placeholder(R.drawable.zimkit_icon_avatar_placeholder).error(R.drawable.zimkit_icon_avatar_placeholder)
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
        //        Glide.with(imageView)
        //                .load(url)
        //                .placeholder(R.drawable.zimkit_icon_avatar_placeholder)
        //                .error(R.drawable.zimkit_icon_avatar_placeholder)
        //                .diskCacheStrategy(DiskCacheStrategy.ALL) // Setting the policy for caching
        //                })
        //                .into(imageView);
        if (!TextUtils.isEmpty(url)) {
            Picasso.get().load(url).fit().placeholder(R.drawable.zimkit_icon_avatar_placeholder).into(imageView);
        }
    }


    /**
     * Loading gifs
     *
     * @param imageView
     * @param drawableGif
     */
    public static void displayGifImage(ImageView imageView, int drawableGif) {
        Glide.with(imageView).asGif().load(drawableGif).diskCacheStrategy(DiskCacheStrategy.ALL).into(imageView);
    }

    /**
     * Load Local Pictures
     *
     * @param imageView
     * @param drawable
     */
    public static void displayLocalImage(ImageView imageView, int drawable) {
        Glide.with(imageView).load(drawable).diskCacheStrategy(DiskCacheStrategy.ALL).into(imageView);
    }

}
