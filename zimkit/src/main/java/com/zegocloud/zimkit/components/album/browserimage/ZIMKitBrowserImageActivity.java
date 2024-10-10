package com.zegocloud.zimkit.components.album.browserimage;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.permissionx.guolindev.callback.RequestCallback;
import com.zegocloud.zimkit.common.base.BaseDialog;
import com.zegocloud.zimkit.common.utils.PermissionHelper;
import com.zegocloud.zimkit.common.utils.ZIMKitCheckDoubleClick;
import com.zegocloud.zimkit.components.album.browserimage.download.GlideDownloadHelper;
import com.zegocloud.zimkit.components.album.internal.entity.SelectionSpec;
import com.zegocloud.zimkit.components.album.internal.utils.Platform;
import java.util.ArrayList;

import com.zegocloud.zimkit.R;
import java.util.List;

/**
 * View larger image
 */
public class ZIMKitBrowserImageActivity extends AppCompatActivity {

    public static final String KEY_INDEX = "INDEX";
    public static final String KEY_PIC_LIST = "PIC_LIST";
    public static final String KEY_PIC_LOCAL_PATH = "PIC_LOCAL_PATH";
    public static final String KEY_PIC_LARGE_PATH = "PIC_LARGE_PATH";
    public static final String KEY_FILE_NAME = "FILE_NAME";
    public static final String KEY_PIC_IS_GIF = "PIC_IS_GIF";

    private ImgBrowserViewPager mViewPager;
    private ImageViewerPagerAdapter mPagerAdapter;
    private TextView mDownloadImage;
    private LinearLayout mLlBottomBtn;

    private ArrayList<String> mLocalPicList = new ArrayList<>();
    private ArrayList<String> mLargePicList = new ArrayList<>();
    private ArrayList<String> mFileNameList = new ArrayList<>();

    private int mIndex = 0;
    private int mDefaultResId;
    private String mImageLocalPath;
    private String mImageLargePath;
    private String mFileName;
    private boolean isGif;
    private boolean isSuccess = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(SelectionSpec.getInstance().themeId);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zimkit_activity_album_image_browser);
        if (Platform.hasKitKat()) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        GlideDownloadHelper.init(this);

        mIndex = getIntent().getIntExtra(KEY_INDEX, 0);
        mImageLocalPath = getIntent().getStringExtra(KEY_PIC_LOCAL_PATH);
        mImageLargePath = getIntent().getStringExtra(KEY_PIC_LARGE_PATH);
        mFileName = getIntent().getStringExtra(KEY_FILE_NAME);
        isGif = getIntent().getBooleanExtra(KEY_PIC_IS_GIF, false);
        mLocalPicList.add(mImageLocalPath);
        mLargePicList.add(mImageLargePath);
        mFileNameList.add(mFileName);
        mViewPager = findViewById(R.id.img_browser_viewpager);
        mDownloadImage = findViewById(R.id.download_image);
        mLlBottomBtn = findViewById(R.id.ll_bottom_btn);
        mPagerAdapter = new ImageViewerPagerAdapter(this, mLocalPicList, mLargePicList, mFileNameList, mViewPager);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setCurrentItem(mIndex);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mPagerAdapter.setOnLoadListener(isSuccess -> {
            this.isSuccess = isSuccess;
            mLlBottomBtn.setVisibility(View.VISIBLE);
            String buttonText = isSuccess ? getString(R.string.zimkit_album_download_image) : getString(R.string.zimkit_album_redownload_image);
            mDownloadImage.setText(buttonText);
        });

        mDownloadImage.setOnClickListener(v -> {
            if (ZIMKitCheckDoubleClick.isFastDoubleClick(1000)) {
                return;
            }
            if (!isSuccess) {
                mPagerAdapter.loadImage(0);
                return;
            }
            PermissionHelper.onWriteSDCardPermissionGranted(this, new RequestCallback() {
                @Override
                public void onResult(boolean allGranted, @NonNull List<String> grantedList,
                    @NonNull List<String> deniedList) {
                    if (allGranted) {
                        mPagerAdapter.downloadImage();
                    } else {
                        BaseDialog baseDialog = new BaseDialog(ZIMKitBrowserImageActivity.this);
                        baseDialog.setMsgTitle(getString(R.string.zimkit_album_dialog_tips));
                        baseDialog.setMsgContent(getString(R.string.zimkit_album_need_permission_content));
                        baseDialog.setLeftButtonContent(getString(R.string.zimkit_album_btn_cancle));
                        baseDialog.setRightButtonContent(getString(R.string.zimkit_album_go_setting));
                        baseDialog.setSureListener(v -> {
                            baseDialog.dismiss();
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                .setData(Uri.fromParts("package", getPackageName(), null));
                            startActivityForResult(intent, 666);
                        });
                        baseDialog.setCancelListener(v -> {
                            baseDialog.dismiss();
                        });
                    }
                }
            });
        });

    }

    public static void startActivity(Context mContext, String imageLocalPath, String imageLargePath, String fileName) {
        Intent intent = new Intent(mContext, ZIMKitBrowserImageActivity.class);
        intent.putExtra(KEY_PIC_LOCAL_PATH, imageLocalPath);
        intent.putExtra(KEY_PIC_LARGE_PATH, imageLargePath);
        intent.putExtra(KEY_FILE_NAME, fileName);
        mContext.startActivity(intent);
    }

}
