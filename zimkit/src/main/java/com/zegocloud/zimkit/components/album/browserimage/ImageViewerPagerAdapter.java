package com.zegocloud.zimkit.components.album.browserimage;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.github.chrisbanes.photoview.OnOutsidePhotoTapListener;
import com.github.chrisbanes.photoview.OnPhotoTapListener;
import com.github.chrisbanes.photoview.PhotoView;
import com.zegocloud.zimkit.R;
import com.zegocloud.zimkit.common.glide.ZIMKitGlideLoader;
import com.zegocloud.zimkit.common.utils.ZIMKitFileUtils;
import com.zegocloud.zimkit.components.album.browserimage.ZIMKitBrowserImageActivity.MsgImage;
import com.zegocloud.zimkit.components.album.browserimage.download.GlideDownloadHelper;
import com.zegocloud.zimkit.components.album.browserimage.view.BrowserProcessView;
import java.util.ArrayList;

public class ImageViewerPagerAdapter extends PagerAdapter {

    private Context mContext;
    private ArrayList<MsgImage> imageList = new ArrayList<>();
    private PhotoView imageView;
    private String suffixStr;

    // loading
    private BrowserProcessView processView;
    private ImgBrowserViewPager mViewPager;

    public ImageViewerPagerAdapter(Context context, ArrayList<MsgImage> imageList, ImgBrowserViewPager mViewPager) {
        this.mContext = context;
        this.imageList.addAll(imageList);
        this.mViewPager = mViewPager;
        processView = new BrowserProcessView(mContext);

        //        processView.setOnTouchListener(new View.OnTouchListener() {
        //            @Override
        //            public boolean onTouch(View v, MotionEvent event) {
        //                if (event.getAction() == MotionEvent.ACTION_DOWN) {
        //                    processView.dismiss();
        //                    if (mContext instanceof Activity) {
        //                        ((Activity) mContext).finish();
        //                    }
        //                }
        //                return false;
        //            }
        //        });
    }

    @Override
    public int getCount() {
        return imageList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
        imageView = new PhotoView(mContext);

        loadImage(position);

        container.addView(imageView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        imageView.setOnPhotoTapListener(new OnPhotoTapListener() {
            @Override
            public void onPhotoTap(ImageView view, float x, float y) {
                if (mContext instanceof Activity) {
                    ((Activity) mContext).finish();
                }
            }
        });

        imageView.setOnOutsidePhotoTapListener(new OnOutsidePhotoTapListener() {
            @Override
            public void onOutsidePhotoTap(ImageView imageView) {
                if (mContext instanceof Activity) {
                    ((Activity) mContext).finish();
                }
            }
        });

        return imageView;
    }

    public void loadImage(int position) {
        MsgImage msgImage = imageList.get(position);

        String loadTxt = mContext.getString(R.string.zimkit_album_loading_txt);
        processView.setIsShowShade(false).setCancelable(true).setLoadingTxt(loadTxt)
            .show((ViewGroup) mViewPager.getRootView());

        // if local is empty,use large url
        String loadUrl =
            TextUtils.isEmpty(msgImage.mImageLocalPath) ? msgImage.mImageLargePath : msgImage.mImageLocalPath;

        RequestOptions options = new RequestOptions().diskCacheStrategy(
                DiskCacheStrategy.ALL) // Setting the policy for caching
            .error(R.drawable.zimkit_icon_image_placeholder);
        Drawable drawable = null;
        if (!TextUtils.isEmpty(msgImage.mImageLocalPath)) {
            drawable = ZIMKitGlideLoader.getDrawableFromLocalPath(msgImage.mImageLocalPath);
        }
        if (drawable == null) {
            options = options.placeholder(R.drawable.zimkit_icon_image_placeholder);
        } else {
            options = options.placeholder(drawable);
        }
        if (msgImage.isGif) {

            Glide.with(mContext).asGif().load(loadUrl).apply(options).listener(new RequestListener<GifDrawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<GifDrawable> target,
                    boolean isFirstResource) {
                    if (processView != null) {
                        processView.dismiss();
                    }
                    if (mOnLoadListener != null) {
                        mOnLoadListener.onLoadStatus(false);
                    }
                    return false;
                }

                @Override
                public boolean onResourceReady(GifDrawable resource, Object model, Target<GifDrawable> target,
                    DataSource dataSource, boolean isFirstResource) {
                    if (processView != null) {
                        processView.dismiss();
                    }
                    if (mOnLoadListener != null) {
                        mOnLoadListener.onLoadStatus(true);
                    }
                    return false;
                }
            }).into(imageView);

        } else {
            Glide.with(mContext).load(loadUrl).apply(options).thumbnail(0.2f).dontAnimate()
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target,
                        boolean isFirstResource) {
                        if (processView != null) {
                            processView.dismiss();
                        }
                        if (mOnLoadListener != null) {
                            mOnLoadListener.onLoadStatus(false);
                        }
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target,
                        DataSource dataSource, boolean isFirstResource) {
                        if (processView != null) {
                            processView.dismiss();
                        }
                        if (mOnLoadListener != null) {
                            mOnLoadListener.onLoadStatus(true);
                        }
                        return false;
                    }
                }).into(imageView);
        }
    }

    public interface IOnLoadStatusListener {

        void onLoadStatus(boolean isSuccess);
    }

    private IOnLoadStatusListener mOnLoadListener;

    public void setOnLoadListener(IOnLoadStatusListener mOnLoadListener) {
        this.mOnLoadListener = mOnLoadListener;
    }

    public void downloadImage() {

        int currentItem = mViewPager.getCurrentItem();
        MsgImage msgImage = imageList.get(currentItem);
        String downUrl = msgImage.mImageLargePath;
        String downloadTxt = mContext.getString(R.string.zimkit_album_downloading_txt);
        suffixStr = ZIMKitFileUtils.getFileSuffix(msgImage.mFileName);

        processView.setIsShowShade(false).setCancelable(true).setLoadingTxt(downloadTxt)
            .show((ViewGroup) mViewPager.getRootView());
        GlideDownloadHelper.with(downUrl).setSuffix(suffixStr).setListener(new GlideDownloadHelper.DownloadListener() {
            @Override
            public void success(String url, String localFilePath, String localFileName) {
                if (processView != null) {
                    processView.dismiss();
                }
                Toast.makeText(mContext, mContext.getString(R.string.zimkit_album_save_success), Toast.LENGTH_SHORT)
                    .show();
            }

            @Override
            public void error(String url, Exception e) {
                if (processView != null) {
                    processView.dismiss();
                }
                Toast.makeText(mContext, mContext.getString(R.string.zimkit_album_save_fail), Toast.LENGTH_SHORT)
                    .show();
            }
        }).execute();
    }

}
