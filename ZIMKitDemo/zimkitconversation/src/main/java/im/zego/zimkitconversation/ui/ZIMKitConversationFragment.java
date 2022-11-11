package im.zego.zimkitconversation.ui;

import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;

import im.zego.zim.entity.ZIMError;
import im.zego.zim.enums.ZIMConversationType;
import im.zego.zimkitcommon.ZIMKitConstant;
import im.zego.zimkitcommon.ZIMKitRouter;
import im.zego.zimkitcommon.base.BaseFragment;
import im.zego.zimkitcommon.enums.ZIMKitConversationType;
import im.zego.zimkitcommon.utils.ZIMKitToastUtils;
import im.zego.zimkitconversation.BR;
import im.zego.zimkitconversation.ZIMKitConversationListAdapter;
import im.zego.zimkitconversation.R;
import im.zego.zimkitconversation.databinding.ConversationFragmentBinding;
import im.zego.zimkitconversation.databinding.ConversationLayoutDeleteBinding;
import im.zego.zimkitconversation.databinding.ConversationLayoutSeletectChatTypeBinding;
import im.zego.zimkitconversation.model.ZIMKitConversationModel;
import im.zego.zimkitconversation.viewmodel.ZIMKitConversationVM;
import im.zego.zimkitconversation.widget.CustomBottomSheet;

public class ZIMKitConversationFragment extends BaseFragment<ConversationFragmentBinding, ZIMKitConversationVM> {
    private ZIMKitConversationListAdapter mListAdapter;
    private CustomBottomSheet<ConversationLayoutDeleteBinding> mDeleteConversationBottomSheet;
    private ZIMKitConversationModel mCurrentSelectModel;
    private CustomBottomSheet<ConversationLayoutSeletectChatTypeBinding> mSelectChatBottomSheet;

    @Override
    protected int getLayoutId() {
        return R.layout.conversation_fragment;
    }

    @Override
    protected int getViewModelId() {
        return BR.vm;
    }

    @Override
    protected void initView() {
        mListAdapter = new ZIMKitConversationListAdapter();
        mListAdapter.setLongClickListener(model -> {
            if (mDeleteConversationBottomSheet != null && mDeleteConversationBottomSheet.getDialog() != null && mDeleteConversationBottomSheet.getDialog().isShowing()) {
                mDeleteConversationBottomSheet.dismiss();
            }
            if (mDeleteConversationBottomSheet == null) {
                mDeleteConversationBottomSheet = new CustomBottomSheet<>(R.layout.conversation_layout_delete, this::setBottomSheetItemListener);
            }
            mCurrentSelectModel = model;
            mDeleteConversationBottomSheet.show(getParentFragmentManager(), "delete_conversation");
        });
        mListAdapter.setItemClickListener(model -> {
            mViewModel.clearConversationUnreadMessageCount(model.getConversationID(), model.getType());
            toMessage(model.getType(), model.getName(), model.getConversationID(), model.getAvatar());
        });

        mBinding.rvList.setLayoutManager(new LinearLayoutManager(getActivity()));
        mBinding.rvList.setAdapter(mListAdapter);
        mBinding.rvList.setItemAnimator(null);
        mBinding.refreshLayout.setEnableRefresh(false);
        mBinding.refreshLayout.setEnableLoadMoreWhenContentNotFull(false);
        mBinding.refreshLayout.setOnLoadMoreListener(refreshLayout -> {
            mViewModel.loadNextPage();
        });
        mBinding.btnReload.setOnClickListener(v -> mViewModel.loadConversation());
//        mBinding.viewStartChat.setOnClickListener(v -> showSelectChatBottomSheet());
//        mBinding.tvStartChat.setOnClickListener(v -> showSelectChatBottomSheet());
    }

    public void showSelectChatBottomSheet() {
        if (mSelectChatBottomSheet != null && mSelectChatBottomSheet.getDialog() != null && mSelectChatBottomSheet.getDialog().isShowing()) {
            mSelectChatBottomSheet.dismiss();
        }
        if (mSelectChatBottomSheet == null) {
            mSelectChatBottomSheet = new CustomBottomSheet<>(R.layout.conversation_layout_seletect_chat_type, this::setSelectBottomSheetItemListener);
        }
        mSelectChatBottomSheet.show(getParentFragmentManager(), "chatType");
    }

    private void setSelectBottomSheetItemListener(ConversationLayoutSeletectChatTypeBinding binding) {
        binding.tvCreateGroupChat.setOnClickListener(v -> {
            toGroupChat(ZIMKitConstant.GroupPageConstant.TYPE_CREATE_GROUP_MESSAGE);
        });
        binding.tvCreateSingleChat.setOnClickListener(v -> {
            toSingleChat();
        });
        binding.tvJoinGroupChat.setOnClickListener(v -> {
            toGroupChat(ZIMKitConstant.GroupPageConstant.TYPE_JOIN_GROUP_MESSAGE);
        });
    }

    /**
     * Initiate a single chat
     */
    private void toSingleChat() {
        dismissBottomSheet();
        ZIMKitRouter.to(getContext(), ZIMKitConstant.RouterConstant.ROUTER_CREATE_SINGLE_CHAT, null);
    }

    /**
     * Start a group chat / Join the group chat
     * @param type
     */
    private void toGroupChat(String type) {
        dismissBottomSheet();
        Bundle bundle = new Bundle();
        bundle.putString(ZIMKitConstant.GroupPageConstant.KEY_TYPE, type);
        ZIMKitRouter.to(getContext(), ZIMKitConstant.RouterConstant.ROUTER_CREATE_AND_JOIN_GROUP, bundle);
    }

    private void setBottomSheetItemListener(ConversationLayoutDeleteBinding binding) {
        binding.teCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDeleteConversationBottomSheet.dismiss();
            }
        });
        binding.tvDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewModel.deleteConversation(mCurrentSelectModel.getConversation());
                mDeleteConversationBottomSheet.dismiss();
            }
        });
    }

    /**
     * Jump to the chat page via session
     *
     * @param type           Session type single chat or group chat
     * @param name           Session Title
     * @param conversationId
     * @param avatar
     */
    private void toMessage(ZIMConversationType type, String name, String conversationId, String avatar) {
        ZIMKitConversationType zimKitConversationType = ZIMKitConversationType.ZIMKitConversationTypePeer;
        if (type == ZIMConversationType.PEER) {
            zimKitConversationType = ZIMKitConversationType.ZIMKitConversationTypePeer;
        } else if (type == ZIMConversationType.GROUP) {
            zimKitConversationType = ZIMKitConversationType.ZIMKitConversationTypeGroup;
        }
        ZIMKitRouter.toMessageActivity(this.getContext(), conversationId, name,avatar,zimKitConversationType);
        dismissBottomSheet();
    }

    @Override
    public void onDestroy() {
        dismissBottomSheet();
        super.onDestroy();
    }

    private void dismissBottomSheet() {
        if (mDeleteConversationBottomSheet != null) {
            mDeleteConversationBottomSheet.dismiss();
            mDeleteConversationBottomSheet = null;
        }
        if (mSelectChatBottomSheet != null) {
            mSelectChatBottomSheet.dismiss();
            mSelectChatBottomSheet = null;
        }
    }

    @Override
    protected void initData() {
        mViewModel.setOnLoadConversationListener(new ZIMKitConversationVM.OnLoadConversationListener() {
            @Override
            public void onSuccess(ZIMKitConversationVM.LoadData loadData) {
                if (loadData.currentLoadIsEmpty) {
                    mBinding.refreshLayout.finishLoadMoreWithNoMoreData();
                } else {
                    mListAdapter.submitList(new ArrayList<>(loadData.allList));
                    if (loadData.state == ZIMKitConversationVM.LoadData.DATA_STATE_CHANGE) {
//                        mBinding.rvList.post(() -> mBinding.rvList.smoothScrollToPosition(0));
                    } else {
                        mBinding.refreshLayout.finishLoadMore(true);
                    }
                }
            }

            @Override
            public void onFail(ZIMError error) {
                mBinding.refreshLayout.finishLoadMore(false);
                ZIMKitToastUtils.showToast(error.message);
            }
        });
        mViewModel.loadConversation(); // the first
    }
}
