package com.zegocloud.zimkit.components.message.adapter;

import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;
import com.zegocloud.zimkit.BR;
import com.zegocloud.zimkit.R;
import com.zegocloud.zimkit.components.message.adapter.ZIMKitInputMoreAdapter.InputMoreItemViewHolder;
import com.zegocloud.zimkit.components.message.model.ZIMKitInputButtonModel;
import java.util.ArrayList;
import java.util.List;

public class ZIMKitInputMoreAdapter extends RecyclerView.Adapter<InputMoreItemViewHolder> {

    private List<ZIMKitInputButtonModel> itemModels = new ArrayList<>();

    @NonNull
    @Override
    public InputMoreItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewDataBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
            R.layout.zimkit_item_input_more, parent, false);
        return new InputMoreItemViewHolder(binding);
    }


    @Override
    public void onBindViewHolder(@NonNull InputMoreItemViewHolder holder, int position) {
        holder.bind(BR.model, itemModels.get(position));
    }

    @Override
    public int getItemCount() {
        return itemModels.size();
    }

    public void setItemModels(List<ZIMKitInputButtonModel> itemModels) {
        this.itemModels.clear();
        this.itemModels.addAll(itemModels);
        notifyDataSetChanged();
    }

    public ZIMKitInputButtonModel getItemModel(int position) {
        return itemModels.get(position);
    }

    public static class InputMoreItemViewHolder extends RecyclerView.ViewHolder {

        private final ViewDataBinding mBinding;

        public InputMoreItemViewHolder(ViewDataBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }

        public void bind(int id, ZIMKitInputButtonModel model) {
            if (mBinding != null) {
                mBinding.setVariable(id, model);
                mBinding.executePendingBindings();
            }
        }
    }

    public static int dp2px(float v, DisplayMetrics displayMetrics) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, displayMetrics);
    }
}
