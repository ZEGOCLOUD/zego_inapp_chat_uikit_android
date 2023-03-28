package com.zegocloud.zimkit.components.message.ui;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import com.zegocloud.zimkit.BR;
import com.zegocloud.zimkit.R;
import com.zegocloud.zimkit.common.base.BaseFragment;
import com.zegocloud.zimkit.common.utils.ZIMKitScreenUtils;
import com.zegocloud.zimkit.components.message.adapter.ZIMKitEmojiAdapter;
import com.zegocloud.zimkit.components.message.model.ZIMKitEmojiItemModel;
import com.zegocloud.zimkit.components.message.utils.EmojiUtils;
import com.zegocloud.zimkit.components.message.viewmodel.ZIMKitEmojiVM;
import com.zegocloud.zimkit.components.message.widget.input.EmojiSpacesItemDecoration;
import com.zegocloud.zimkit.databinding.ZimkitFragmentEmojiBinding;
import java.util.ArrayList;
import java.util.List;

public class ZIMKitEmojiFragment extends BaseFragment<ZimkitFragmentEmojiBinding, ZIMKitEmojiVM> {

    private ZIMKitEmojiAdapter mEmojiAdapter;
    private OnEmojiClickListener mEmojiClickListener;

    @Override
    protected void initView() {
        initRv();
    }

    private void initRv() {
        List<ZIMKitEmojiItemModel> emojiModel = new ArrayList<>();
        List<String> emojis = EmojiUtils.createEmojiData();
        for (String emoji : emojis) {
            ZIMKitEmojiItemModel model = new ZIMKitEmojiItemModel(emoji);
            emojiModel.add(model);
        }
        mEmojiAdapter = new ZIMKitEmojiAdapter(emojiModel);
        mBinding.rvEmoji.setLayoutManager(new GridLayoutManager(getContext(), 7));
        int space = ZIMKitScreenUtils.dip2px(16);
        mBinding.rvEmoji.addItemDecoration(new EmojiSpacesItemDecoration(space));
        mBinding.rvEmoji.setPadding(space, 0, 0, 0);
        mBinding.rvEmoji.setAdapter(mEmojiAdapter);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.zimkit_fragment_emoji;
    }

    @Override
    protected int getViewModelId() {
        return 0;
    }

    @Override
    protected void initData() {
        mViewModel = new ViewModelProvider(requireActivity()).get(ZIMKitEmojiVM.class);
        mBinding.setVariable(BR.vm, mViewModel);

        mEmojiAdapter.setItemClickListener(model -> {
            if (mEmojiClickListener != null) {
                mEmojiClickListener.onEmojiClick(model);
            }
        });

        mBinding.clDeleteEmoji.setOnClickListener(v -> {
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
