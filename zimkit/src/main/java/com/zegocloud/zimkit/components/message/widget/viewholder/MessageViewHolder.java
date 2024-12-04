package com.zegocloud.zimkit.components.message.widget.viewholder;

import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;
import com.zegocloud.zimkit.components.message.adapter.ZIMKitMessageAdapter;
import com.zegocloud.zimkit.components.message.model.ZIMKitMessageModel;

public abstract class MessageViewHolder extends RecyclerView.ViewHolder {

    private final ViewDataBinding mBinding;
    public ZIMKitMessageModel model;
    public ZIMKitMessageAdapter mAdapter;

    public MessageViewHolder(ViewDataBinding binding) {
        super(binding.getRoot());
        mBinding = binding;
    }

    public void bind(int id, int position, ZIMKitMessageModel model) {
        this.model = model;
        if (mBinding != null) {
            mBinding.setVariable(id, model);
            mBinding.executePendingBindings();
        }
    }

    /**
     * Set the height and width of the control
     *
     * @param width
     * @param height
     * @param views
     */
    public static void setLayoutParams(int width, int height, View... views) {
        for (View view : views) {
            ViewGroup.LayoutParams maskParams = view.getLayoutParams();
            maskParams.width = width;
            maskParams.height = height;
            view.setLayoutParams(maskParams);
        }
    }

    public static int dp2px(float v, DisplayMetrics displayMetrics) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, displayMetrics);
    }
}
