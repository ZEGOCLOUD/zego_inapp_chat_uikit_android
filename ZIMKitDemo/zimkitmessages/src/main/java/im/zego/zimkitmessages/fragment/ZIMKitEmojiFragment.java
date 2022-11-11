package im.zego.zimkitmessages.fragment;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import java.util.ArrayList;
import java.util.List;

import im.zego.zimkitcommon.base.BaseFragment;
import im.zego.zimkitmessages.widget.message.input.EmojiSpacesItemDecoration;
import im.zego.zimkitcommon.utils.ZIMKitScreenUtils;
import im.zego.zimkitmessages.BR;
import im.zego.zimkitmessages.R;
import im.zego.zimkitmessages.adapter.ZIMKitEmojiAdapter;
import im.zego.zimkitmessages.databinding.MessageFragmentEmojiBinding;
import im.zego.zimkitmessages.model.ZIMKitEmojiItemModel;
import im.zego.zimkitmessages.utils.EmojiUtils;
import im.zego.zimkitmessages.viewmodel.ZIMKitEmojiVM;

public class ZIMKitEmojiFragment extends BaseFragment<MessageFragmentEmojiBinding, ZIMKitEmojiVM> {

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
        return R.layout.message_fragment_emoji;
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
