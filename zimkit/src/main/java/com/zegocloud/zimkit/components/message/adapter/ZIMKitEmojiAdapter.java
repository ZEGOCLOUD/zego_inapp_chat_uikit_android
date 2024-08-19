package com.zegocloud.zimkit.components.message.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;
import com.zegocloud.zimkit.BR;
import com.zegocloud.zimkit.R;
import com.zegocloud.zimkit.components.message.model.ZIMKitEmojiItemModel;
import com.zegocloud.zimkit.components.message.utils.EmojiUtils;
import java.util.ArrayList;
import java.util.List;

public class ZIMKitEmojiAdapter extends RecyclerView.Adapter<ZIMKitEmojiAdapter.EmojiItemViewHolder> {

    private List<ZIMKitEmojiItemModel> mList;

    public ZIMKitEmojiAdapter() {
        List<ZIMKitEmojiItemModel> emojiModel = new ArrayList<>();
        List<String> emojis = EmojiUtils.createEmojiData();
        for (String emoji : emojis) {
            ZIMKitEmojiItemModel model = new ZIMKitEmojiItemModel(emoji);
            emojiModel.add(model);
        }
        this.mList = emojiModel;
    }

    @NonNull
    @Override
    public EmojiItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewDataBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
            R.layout.zimkit_item_emoji, parent, false);
        return new EmojiItemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull EmojiItemViewHolder holder, int position) {
        holder.bind(BR.model, mList.get(position));

        holder.mBinding.getRoot().setOnClickListener(v -> {
            if (mOnClickListener != null) {
                mOnClickListener.onClick(mList.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public interface IOnItemClickListener {

        void onClick(ZIMKitEmojiItemModel model);
    }

    private IOnItemClickListener mOnClickListener;

    public void setItemClickListener(IOnItemClickListener itemClickListener) {
        mOnClickListener = itemClickListener;
    }

    public static class EmojiItemViewHolder extends RecyclerView.ViewHolder {

        private final ViewDataBinding mBinding;

        public EmojiItemViewHolder(ViewDataBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }

        public void bind(int id, ZIMKitEmojiItemModel model) {
            if (mBinding != null) {
                mBinding.setVariable(id, model);
                mBinding.executePendingBindings();
            }
        }
    }

}
