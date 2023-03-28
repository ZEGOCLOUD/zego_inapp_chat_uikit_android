package com.zegocloud.zimkit.common.components.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.zegocloud.zimkit.R;
import com.zegocloud.zimkit.databinding.ZimkitLayoutProcessBinding;

public class ProcessView extends FrameLayout {

    private ZimkitLayoutProcessBinding mBinding;
    protected ViewGroup mParentView;
    private boolean mIsShowShade = true;
    private boolean mCancelable = false;
    private float mBgAlpha = 0.2f;

    public ProcessView(Context context) {
        super(context);
        initView(context);
    }

    public ProcessView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public ProcessView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        mBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.zimkit_layout_process, this, true);
    }

    public ProcessView setShadeAlpha(float alpha) {
        mBgAlpha = alpha;
        return this;
    }

    public ProcessView setIsShowShade(boolean isShowShade) {
        mIsShowShade = isShowShade;
        return this;
    }

    public ProcessView setCancelable(boolean isShowShade) {
        mCancelable = isShowShade;
        return this;
    }

    public ProcessView show(ViewGroup view) {
        mParentView = view;

        final ViewParent viewParent = getParent();

        if (viewParent instanceof ViewGroup) {
            final ViewGroup viewGroup = (ViewGroup) viewParent;
            viewGroup.removeView(this);
        }

        int color = 0;
        if (mIsShowShade) {
            color = ((int) (mBgAlpha * 255.0f + 0.5f) << 24);
        }

        mBinding.vPbBg.setBackgroundColor(color);

        if (mCancelable) {
            mBinding.vPbBg.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                    dismiss();
                }
            });
            mBinding.vPbBg.setOnTouchListener(null);
        } else {
            mBinding.vPbBg.setOnClickListener(null);
            mBinding.vPbBg.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return true;
                }
            });
        }

        mParentView.addView(this);

        return this;
    }

    public void dismiss() {
        final ViewParent viewParent = getParent();
        if (viewParent instanceof ViewGroup) {
            final ViewGroup viewGroup = (ViewGroup) viewParent;
            viewGroup.removeView(this);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(
                MeasureSpec.makeMeasureSpec(
                        MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(
                        MeasureSpec.getSize(heightMeasureSpec), MeasureSpec.EXACTLY));
    }

}
