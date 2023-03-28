package com.zegocloud.zimkit.common.components.widget;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
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
            mBinding.clTitleBar.addView(headerBar.getLeftView(), layoutParams);
        }

        if (headerBar.getTitleView() != null) {
            mBinding.tvTitle.setVisibility(GONE);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.CENTER_VERTICAL | Gravity.CENTER;
            mBinding.clTitleBar.addView(headerBar.getTitleView(), layoutParams);
        }

        if (headerBar.getRightView() != null) {
            hideRightButton();
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.CENTER_VERTICAL | Gravity.END;
            int marginEnd = ZIMKitScreenUtils.dip2px(4f);
            layoutParams.setMarginEnd(marginEnd);
            mBinding.clTitleBar.addView(headerBar.getRightView(), layoutParams);
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

}
