package com.zegocloud.zimkit.common.glide;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;
import com.zegocloud.zimkit.R;
import im.zego.zim.enums.ZIMConversationType;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;

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

    static class PicassoRoundTransform implements Transformation {

        @Override
        public Bitmap transform(Bitmap source) {
            int widthLight = source.getWidth();
            int heightLight = source.getHeight();
            Bitmap output = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(output);
            Paint paintColor = new Paint();
            paintColor.setFlags(Paint.ANTI_ALIAS_FLAG);
            RectF rectF = new RectF(new Rect(0, 0, widthLight, heightLight));
            canvas.drawRoundRect(rectF, widthLight / 5, heightLight / 5, paintColor);
            Paint paintImage = new Paint();
            paintImage.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
            canvas.drawBitmap(source, 0, 0, paintImage);
            source.recycle();
            return output;
        }

        @Override
        public String key() {
            return "roundcorner";
        }
    }

    /**
     * Show chat images
     *
     * @param imageView
     * @param url
     * @param fileLocalPath
     * @param width
     * @param height
     */
    public static void displayMessageImage(ImageView imageView, String url, String fileLocalPath, int width,
        int height) {
        // if sending, no download url,so use file localPath to show.
        // when send finished,use download url
        if (TextUtils.isEmpty(url)) {
            url = fileLocalPath;
        }
        int corner = dp2px(8, imageView.getResources().getDisplayMetrics());
        RequestOptions options = new RequestOptions().override(width, height)
            .transform(new CenterCrop(), new RoundedCorners(corner)).dontAnimate()
            .diskCacheStrategy(DiskCacheStrategy.ALL) // Setting the policy for caching
            .error(R.drawable.zimkit_icon_image_placeholder);
        Drawable drawable = null;
        if (!TextUtils.isEmpty(fileLocalPath)) {
            drawable = getDrawableFromLocalPath(fileLocalPath);
        }
        if (drawable == null) {
            options = options.placeholder(R.drawable.zimkit_icon_image_placeholder);
        } else {
            options = options.placeholder(drawable);
        }
        Glide.with(imageView).load(url).apply(options).into(imageView);
    }

    private static final String TAG = "ZIMKitGlideLoader";

    public static int dp2px(float v, DisplayMetrics displayMetrics) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, displayMetrics);
    }

    /**
     * Show gif image message
     *
     * @param imageView
     * @param url
     * @param fileLocalPath
     * @param width
     * @param height
     */
    public static void displayMessageGifImage(ImageView imageView, String url, String fileLocalPath, int width,
        int height) {
        if (TextUtils.isEmpty(url)) {
            url = fileLocalPath;
        }
        int corner = dp2px(8, imageView.getResources().getDisplayMetrics());
        Drawable drawable = null;
        if (!TextUtils.isEmpty(fileLocalPath)) {
            drawable = getDrawableFromLocalPath(fileLocalPath);
        }

        RequestOptions options = new RequestOptions().override(width, height).transform(new RoundedCorners(corner))
            .diskCacheStrategy(DiskCacheStrategy.ALL) // Setting the policy for caching
            .error(R.drawable.zimkit_icon_image_placeholder).transform(new RoundedCorners(corner));
        if (drawable == null) {
            options = options.placeholder(R.drawable.zimkit_icon_image_placeholder);
        } else {
            options = options.placeholder(drawable);
        }
        Glide.with(imageView).asGif().load(url).apply(options).into(imageView);
    }

    public static Drawable getDrawableFromLocalPath(String fileLocalPath) {
        try {
            InputStream inputStream = new FileInputStream(fileLocalPath);
            Drawable drawable = Drawable.createFromStream(inputStream, fileLocalPath);
            return drawable;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
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
        //        Glide.with(imageView).load(type == ZIMConversationType.PEER ? url : R.drawable.zimkit_icon_group)
        //            .placeholder(R.drawable.zimkit_icon_avatar_placeholder).error(R.drawable.zimkit_icon_avatar_placeholder)
        //            .diskCacheStrategy(DiskCacheStrategy.ALL) // Setting the policy for caching
        //            .into(imageView);

        if (type == ZIMConversationType.PEER) {
            if (!TextUtils.isEmpty(url)) {
                Picasso.get().load(url).transform(new PicassoRoundTransform()).fit()
                    .placeholder(R.drawable.zimkit_icon_avatar_placeholder)
                    .error(R.drawable.zimkit_icon_avatar_placeholder).centerCrop().into(imageView);
            }
        } else {
            Picasso.get().load(R.drawable.zimkit_icon_group).transform(new PicassoRoundTransform()).fit()
                .placeholder(R.drawable.zimkit_icon_avatar_placeholder).error(R.drawable.zimkit_icon_avatar_placeholder)
                .centerCrop().into(imageView);
        }

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
            Picasso.get().load(url).transform(new PicassoRoundTransform()).fit()
                .placeholder(R.drawable.zimkit_icon_avatar_placeholder).centerCrop().into(imageView);
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
