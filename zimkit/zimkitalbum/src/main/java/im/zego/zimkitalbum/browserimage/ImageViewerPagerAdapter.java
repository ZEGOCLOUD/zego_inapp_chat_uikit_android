package im.zego.zimkitalbum.browserimage;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.MotionEvent;
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
import com.bumptech.glide.request.target.Target;
import com.github.chrisbanes.photoview.OnOutsidePhotoTapListener;
import com.github.chrisbanes.photoview.OnPhotoTapListener;
import com.github.chrisbanes.photoview.PhotoView;

import java.io.File;
import java.util.ArrayList;

import im.zego.zimkitalbum.R;
import im.zego.zimkitalbum.browserimage.download.GlideDownloadHelper;
import im.zego.zimkitalbum.browserimage.view.BrowserProcessView;
import im.zego.zimkitcommon.utils.ZIMKitFileUtils;

public class ImageViewerPagerAdapter extends PagerAdapter {

    private Context mContext;
    private ArrayList<String> mLocalPicUrlList;
    private ArrayList<String> mLargePicList;
    private ArrayList<String> mFileNameList;
    private PhotoView imageView;
    private boolean isGif;
    private String suffixStr;

    // loading
    private BrowserProcessView processView;
    private ImgBrowserViewPager mViewPager;
    private GlideDownloadHelper.DownloadTask mDownloadTask;

    public ImageViewerPagerAdapter(Context context, ArrayList<String> mLocalPicUrlList, ArrayList<String> mLargePicList,
                                   ArrayList<String> mFileNameList, ImgBrowserViewPager mViewPager) {
        this.mContext = context;
        this.mLocalPicUrlList = mLocalPicUrlList;
        this.mLargePicList = mLargePicList;
        this.mFileNameList = mFileNameList;
        this.mViewPager = mViewPager;
        processView = new BrowserProcessView(mContext);

        suffixStr = ZIMKitFileUtils.getFileSuffix(mFileNameList.get(0));
        isGif = suffixStr.equals("gif");

        processView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    processView.dismiss();
                    if (mContext instanceof Activity) {
                        ((Activity) mContext).finish();
                    }
                }
                return false;
            }
        });
    }

    @Override
    public int getCount() {
        return mLocalPicUrlList == null ? 0 : mLocalPicUrlList.size();
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
        String fileLocalURL = mLocalPicUrlList.get(position);
        String fileLargeURL = mLargePicList.get(position);

        String loadTxt = mContext.getString(R.string.album_loading_txt);
        processView.setIsShowShade(false).setCancelable(true).setLoadingTxt(loadTxt).show((ViewGroup) mViewPager.getRootView());
        if (isGif) {
            Glide.with(mContext)
                    .asGif()
                    .load(fileLargeURL)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(im.zego.zimkitcommon.R.drawable.common_ic_image_placeholder)
                    .error(im.zego.zimkitcommon.R.drawable.common_ic_image_placeholder)
                    .listener(new RequestListener<GifDrawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<GifDrawable> target, boolean isFirstResource) {
                            if (processView != null) {
                                processView.dismiss();
                            }
                            if (mOnLoadListener != null) {
                                mOnLoadListener.onLoadStatus(false);
                            }
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GifDrawable resource, Object model, Target<GifDrawable> target, DataSource dataSource, boolean isFirstResource) {
                            if (processView != null) {
                                processView.dismiss();
                            }
                            if (mOnLoadListener != null) {
                                mOnLoadListener.onLoadStatus(true);
                            }
                            return false;
                        }
                    })
                    .into(imageView);

        } else {
            Glide.with(mContext)
                    .load(fileLargeURL)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .thumbnail(0.2f)
                    .placeholder(im.zego.zimkitcommon.R.drawable.common_ic_image_placeholder)
                    .error(im.zego.zimkitcommon.R.drawable.common_ic_image_placeholder)
                    .dontAnimate()
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            if (processView != null) {
                                processView.dismiss();
                            }
                            if (mOnLoadListener != null) {
                                mOnLoadListener.onLoadStatus(false);
                            }
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            if (processView != null) {
                                processView.dismiss();
                            }
                            if (mOnLoadListener != null) {
                                mOnLoadListener.onLoadStatus(true);
                            }
                            return false;
                        }
                    })
                    .into(imageView);
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

        String downUrl = mLocalPicUrlList.get(0);
        String downloadTxt = mContext.getString(R.string.album_downloading_txt);
        processView.setIsShowShade(false).setCancelable(true).setLoadingTxt(downloadTxt).show((ViewGroup) mViewPager.getRootView());
        mDownloadTask = GlideDownloadHelper.with(downUrl).setSuffix(suffixStr).setListener(new GlideDownloadHelper.DownloadListener() {
            @Override
            public void success(String url, String localFilePath, String localFileName) {
                if (processView != null) {
                    processView.dismiss();
                }
                Toast.makeText(mContext, mContext.getString(R.string.album_save_success), Toast.LENGTH_SHORT).show();
                // The following steps must be there or you won't find the image in the album.
                // If you don't need to let the user know that you have saved the image,
                // you can leave out the following code.
                // Inserting files into the system gallery
//                try {
//                    MediaStore.Images.Media.insertImage(mContext.getContentResolver(),
//                            localFilePath, localFileName, null);
//                    Toast.makeText(mContext, mContext.getString(R.string.album_save_success), Toast.LENGTH_SHORT).show();
//                } catch (Exception e) {
//                    Toast.makeText(mContext, mContext.getString(R.string.album_save_fail), Toast.LENGTH_SHORT).show();
//                    e.printStackTrace();
//                }

                Toast.makeText(mContext, mContext.getString(R.string.album_save_success), Toast.LENGTH_SHORT).show();
                // Final notification of gallery update
                mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                        Uri.fromFile(new File(localFilePath))));
            }

            @Override
            public void error(String url, Exception e) {
                if (processView != null) {
                    processView.dismiss();
                }
                Toast.makeText(mContext, mContext.getString(R.string.album_save_fail), Toast.LENGTH_SHORT).show();
            }
        }).execute();
    }

}
