package im.zego.zimkitcommon.components.widget;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import im.zego.zimkitcommon.R;
import im.zego.zimkitcommon.databinding.CommonLayoutTitleBarBinding;
import im.zego.zimkitcommon.model.TitleBarModel;

public class TitleBar extends LinearLayout {
    private CommonLayoutTitleBarBinding mBinding;
    private TitleBarModel mModel;

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
        mBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.common_layout_title_bar, this, true);
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
