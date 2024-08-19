package com.zegocloud.zimkit.components.message.widget.input;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import com.zegocloud.zimkit.R;
import com.zegocloud.zimkit.common.utils.ZIMKitScreenUtils;
import com.zegocloud.zimkit.components.message.adapter.ZIMKitEmojiAdapter;
import com.zegocloud.zimkit.components.message.model.ZIMKitEmojiItemModel;
import com.zegocloud.zimkit.databinding.ZimkitLayoutInputEmojiBinding;

public class ZIMKitEmojiView extends FrameLayout {

    private ZimkitLayoutInputEmojiBinding binding;
    private ZIMKitEmojiAdapter mEmojiAdapter;
    private OnEmojiClickListener mEmojiClickListener;


    public ZIMKitEmojiView(@NonNull Context context) {
        super(context);
        initView();
    }

    public ZIMKitEmojiView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public ZIMKitEmojiView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.zimkit_layout_input_emoji, this,
            true);

        mEmojiAdapter = new ZIMKitEmojiAdapter();
        binding.rvEmoji.setLayoutManager(new GridLayoutManager(getContext(), 7));
        int space = ZIMKitScreenUtils.dip2px(16);
        binding.rvEmoji.addItemDecoration(new EmojiSpacesItemDecoration(space));
        binding.rvEmoji.setPadding(space, 0, 0, 0);
        binding.rvEmoji.setAdapter(mEmojiAdapter);

        mEmojiAdapter.setItemClickListener(model -> {
            if (mEmojiClickListener != null) {
                mEmojiClickListener.onEmojiClick(model);
            }
        });

        binding.clDeleteEmoji.setOnClickListener(v -> {
            if (mEmojiClickListener != null) {
                mEmojiClickListener.onEmojiDelete();
            }
        });
    }


    public void setEmojiListener(OnEmojiClickListener listener) {
        this.mEmojiClickListener = listener;
    }

    public interface OnEmojiClickListener {

        void onEmojiDelete();

        void onEmojiClick(ZIMKitEmojiItemModel emoji);
    }
}
