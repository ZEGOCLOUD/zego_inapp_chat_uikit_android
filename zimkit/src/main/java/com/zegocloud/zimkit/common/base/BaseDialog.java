package com.zegocloud.zimkit.common.base;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import com.zegocloud.zimkit.common.model.BaseDialogModel;
import com.zegocloud.zimkit.R;
import com.zegocloud.zimkit.databinding.ZimkitLayoutDialogBinding;

public class BaseDialog extends Dialog {

    private ZimkitLayoutDialogBinding mBinding;
    private BaseDialogModel mModel;

    private boolean mCancelable = false;

    public BaseDialog(@NonNull Context context) {
        this(context, false);
    }

    public BaseDialog(@NonNull Context context, boolean cancelable) {
        this(context, R.style.BaseDialogStyle, cancelable);
    }

    private BaseDialog(@NonNull Context context, int themeResId, boolean cancelable) {
        super(context, themeResId);
        mCancelable = cancelable;
        init(context);
    }

    private void init(Context context) {

        mBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.zimkit_layout_dialog, null, true);

        Window window = getWindow();
        if (window != null) {
            window.setContentView(mBinding.getRoot());
        }

        mModel = new BaseDialogModel();
        mBinding.setModel(mModel);

        setCancelable(mCancelable);
        setCanceledOnTouchOutside(mCancelable);

        if (context instanceof Activity) {
            if (((Activity) context).isDestroyed() || ((Activity) context).isFinishing()) {
                return;
            }
        }
        show();
    }

    public void setMsgTitle(String title) {
        mModel.setTitle(title);
    }

    public void setMsgContent(String content) {
        mModel.setContent(content);
    }

    public void setLeftButtonContent(String content) {
        mModel.setLeftButtonContent(content);
    }

    public void setRightButtonContent(String content) {
        mModel.setRightButtonContent(content);
    }

    public void setSureListener(View.OnClickListener listener) {
        mBinding.dialogConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onClick(view);
            }
        });
    }

    public void setCancelListener(View.OnClickListener listener) {
        mBinding.dialogCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onClick(view);
            }
        });
    }

}
