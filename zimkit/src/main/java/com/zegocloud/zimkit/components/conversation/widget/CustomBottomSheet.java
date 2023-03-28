package com.zegocloud.zimkit.components.conversation.widget;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class CustomBottomSheet<T extends ViewDataBinding> extends BottomSheetDialogFragment {

    public int mLayoutId;
    private final IViewListener<T> mViewListener;

    public CustomBottomSheet(int layoutId, IViewListener<T> viewListener) {
        mLayoutId = layoutId;
        mViewListener = viewListener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        T mBinding = DataBindingUtil.inflate(inflater, mLayoutId, container, false);
        if (mViewListener != null) {
            mViewListener.onBinding(mBinding);
        }
        return mBinding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Get the dialog object
        if (getDialog() != null) {
            FrameLayout frameLayout = getDialog().getWindow().findViewById(com.google.android.material.R.id.design_bottom_sheet);
            frameLayout.setBackground(new ColorDrawable(Color.TRANSPARENT));

            Window dialogWindow = getDialog().getWindow();
            WindowManager.LayoutParams lp = dialogWindow.getAttributes();
            //Peripheral mask transparency 0.0f-1.0f
            lp.dimAmount = 0.2f;
            dialogWindow.setAttributes(lp);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        if (getContext() != null) {
            return new BottomSheetDialog(getContext());
        }
        return super.onCreateDialog(savedInstanceState);
    }

    public interface IViewListener<T extends ViewDataBinding> {
        void onBinding(T binding);
    }

}
