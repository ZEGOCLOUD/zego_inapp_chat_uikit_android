package com.zegocloud.zimkit.components.message.ui;

import android.media.MediaPlayer;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;

import androidx.lifecycle.ViewModel;

import com.zegocloud.zimkit.common.ZIMKitConstant;
import com.zegocloud.zimkit.common.base.BaseActivity;
import com.zegocloud.zimkit.common.utils.ZIMKitToastUtils;
import com.zegocloud.zimkit.components.album.browserimage.view.BrowserProcessView;
import com.zegocloud.zimkit.R;
import com.zegocloud.zimkit.databinding.ZimkitActivityVideoBinding;

/**
 * Video Playback
 */
public class ZIMKitVideoViewActivity extends BaseActivity<ZimkitActivityVideoBinding, ViewModel> {

    private MediaController mediacontroller;
    // loading
    private BrowserProcessView processView;

    public static String filePath;

    @Override
    protected void initView() {

        processView = new BrowserProcessView(this);

        filePath = getIntent().getStringExtra(ZIMKitConstant.VideoPageConstant.KEY_VIDEO_PATH);
        mediacontroller = new MediaController(this);
        mediacontroller.setAnchorView(mBinding.mVideoView);

        mBinding.mVideoView.setMediaController(mediacontroller);
        mBinding.mVideoView.setVideoURI(Uri.parse(filePath));
        mBinding.mVideoView.requestFocus();
        String loadTxt = getString(R.string.zimkit_loading_txt);

        processView.setIsShowShade(false).setCancelable(true).setLoadingTxt(loadTxt).show((ViewGroup) mBinding.mVideoView.getRootView());
        mBinding.mVideoView.start();

    }

    @Override
    protected int getLayoutId() {
        return R.layout.zimkit_activity_video;
    }

    @Override
    protected int getViewModelId() {
        return 0;
    }

    @Override
    protected void initData() {

        mBinding.mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                    @Override
                    public boolean onInfo(MediaPlayer mp, int what, int extra) {
                        if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                            processView.dismiss();
                            mBinding.ivVideoStop.setVisibility(View.GONE);
                        }
                        return true;
                    }
                });
                mp.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    @Override
                    public boolean onError(MediaPlayer mp, int what, int extra) {
                        processView.dismiss();
                        ZIMKitToastUtils.showToast(R.string.zimkit_video_play_error);
                        finish();
                        return false;
                    }
                });
            }
        });

        mBinding.mVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                processView.dismiss();
                ZIMKitToastUtils.showToast(R.string.zimkit_video_play_error);
                finish();
                return false;
            }
        });

        mBinding.mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mBinding.ivVideoStop.setVisibility(View.VISIBLE);
                mBinding.mVideoView.setVideoURI(Uri.parse(filePath));
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        mBinding.mVideoView.stopPlayback();
    }

}
