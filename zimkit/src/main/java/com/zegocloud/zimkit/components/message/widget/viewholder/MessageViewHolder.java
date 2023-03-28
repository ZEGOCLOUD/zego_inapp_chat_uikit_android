package com.zegocloud.zimkit.components.message.widget.viewholder;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

import com.zegocloud.zimkit.components.message.adapter.ZIMKitMessageAdapter;
import com.zegocloud.zimkit.components.message.model.ZIMKitMessageModel;
import com.zegocloud.zimkit.components.message.widget.interfaces.OnItemClickListener;

public abstract class MessageViewHolder extends RecyclerView.ViewHolder {

    public Context context;
    protected OnItemClickListener onItemClickListener;
    private final ViewDataBinding mBinding;
    public ZIMKitMessageModel model;
    public boolean isMultiSelectMode = false;
    public CheckBox mMutiSelectCheckBox;
    public View msgContent;
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

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void initLongClickListener(View view, int position, ZIMKitMessageModel messageInfo) {
        if (isMultiSelectMode) {
            messageInfo.setCheck(!messageInfo.isCheck());
        } else {
            if (onItemClickListener != null) {
                onItemClickListener.onMessageLongClick(view, position, messageInfo);
            }
        }
    }

}
