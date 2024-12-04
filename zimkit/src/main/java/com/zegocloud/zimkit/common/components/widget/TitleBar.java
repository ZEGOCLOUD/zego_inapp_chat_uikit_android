package com.zegocloud.zimkit.common.components.widget;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.zegocloud.zimkit.common.model.TitleBarModel;
import com.zegocloud.zimkit.common.utils.ZIMKitScreenUtils;
import com.zegocloud.zimkit.components.message.model.ZIMKitHeaderBar;
import com.zegocloud.zimkit.R;
import com.zegocloud.zimkit.databinding.ZimkitLayoutTitleBarBinding;
import com.zegocloud.zimkit.services.ZIMKitConfig;
import com.zegocloud.zimkit.services.internal.ZIMKitAdvancedKey;
import com.zegocloud.zimkit.services.internal.ZIMKitCore;

public class TitleBar extends LinearLayout {

    private ZimkitLayoutTitleBarBinding mBinding;
    private TitleBarModel mModel;
    private ZIMKitHeaderBar headerBar;

    public TitleBar(Context context) {
        super(context);
        initView(context);
    }

    public TitleBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public TitleBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        mBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.zimkit_layout_title_bar, this, true);
        mModel = new TitleBarModel();
        mBinding.setModel(mModel);
        mBinding.imLeft.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (context instanceof Activity) {
                    ((Activity) context).finish();
                }
            }
        });

        ZIMKitConfig zimKitConfig = ZIMKitCore.getInstance().getZimKitConfig();
        if (zimKitConfig != null && zimKitConfig.advancedConfig != null) {
            if (zimKitConfig.advancedConfig.containsKey(ZIMKitAdvancedKey.max_title_width)) {
                String content = zimKitConfig.advancedConfig.get(ZIMKitAdvancedKey.max_title_width);
                try {
                    int maxTitleWidth = Integer.parseInt(content);
                    mBinding.tvTitle.setMaxWidth(maxTitleWidth);
                } catch (Exception e) {

                }

            }
        }
    }

    private void cusLayoutTitleBar() {

        if (headerBar == null) {
            return;
        }

        if (headerBar.getLeftView() != null) {
            hideLeftButton();
            hideLeftTxtButton();
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.CENTER_VERTICAL | Gravity.START;
            int marginStart = ZIMKitScreenUtils.dip2px(4f);
            layoutParams.setMarginStart(marginStart);
            mBinding.titleStartLayout.addView(headerBar.getLeftView(), layoutParams);
        }

        if (headerBar.getTitleView() != null) {
            mBinding.tvTitle.setVisibility(GONE);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.CENTER_VERTICAL | Gravity.CENTER;
            mBinding.titleCenterLayout.addView(headerBar.getTitleView(), layoutParams);
        }

        if (headerBar.getRightView() != null) {
            hideRightButton();
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.CENTER_VERTICAL | Gravity.END;
            int marginEnd = ZIMKitScreenUtils.dip2px(4f);
            layoutParams.setMarginEnd(marginEnd);
            mBinding.titleEndLayout.addView(headerBar.getRightView(), layoutParams);
        }

    }

    public ZIMKitHeaderBar getHeaderBar() {
        return headerBar;
    }

    public void setHeaderBar(ZIMKitHeaderBar headerBar) {
        this.headerBar = headerBar;
        cusLayoutTitleBar();
    }

    public void setTitle(String title) {
        mModel.setTitle(title);
    }

    public void setLeftCLickListener(OnClickListener leftCLickListener) {
        mBinding.imLeft.setOnClickListener(leftCLickListener);
    }

    public void setLeftTxtCLickListener(OnClickListener leftCLickListener) {
        mBinding.tvLeft.setOnClickListener(leftCLickListener);
    }

    public void setRightCLickListener(OnClickListener rightCLickListener) {
        mBinding.imRight.setOnClickListener(rightCLickListener);
    }

    public void setLeftImg(int id) {
        mBinding.imLeft.setImageResource(id);
    }

    public void setRightImg(int id) {
        mBinding.imRight.setImageResource(id);
    }

    public void hideLeftButton() {
        mBinding.imLeft.setVisibility(View.GONE);
    }

    public void showLeftButton() {
        mBinding.imLeft.setVisibility(View.VISIBLE);
    }

    public void hideLeftTxtButton() {
        mBinding.tvLeft.setVisibility(View.GONE);
    }

    public void showLeftTxtButton() {
        mBinding.tvLeft.setVisibility(View.VISIBLE);
    }

    public void hideRightButton() {
        mBinding.imRight.setVisibility(View.GONE);
    }

    public void showRightButton() {
        mBinding.imRight.setVisibility(View.VISIBLE);
    }

    public void setMaxTitleWidth(int maxTitleWidth) {
        if (mBinding != null) {
            mBinding.tvTitle.setMaxWidth(dp2px(maxTitleWidth, getResources().getDisplayMetrics()));
        }
    }

    private int dp2px(float v, DisplayMetrics displayMetrics) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, displayMetrics);
    }
}
