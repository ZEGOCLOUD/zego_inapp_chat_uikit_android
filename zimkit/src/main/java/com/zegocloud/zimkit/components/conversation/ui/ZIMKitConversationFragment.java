package com.zegocloud.zimkit.components.conversation.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.zegocloud.zimkit.common.ZIMKitConstant;
import com.zegocloud.zimkit.common.ZIMKitRouter;
import com.zegocloud.zimkit.common.base.BaseFragment;
import com.zegocloud.zimkit.common.enums.ZIMKitConversationType;
import com.zegocloud.zimkit.common.utils.ZIMKitToastUtils;
import com.zegocloud.zimkit.components.conversation.interfaces.ZIMKitConversationListListener;
import com.zegocloud.zimkit.components.conversation.model.DefaultAction;
import com.zegocloud.zimkit.components.conversation.model.ZIMKitConversationModel;
import com.zegocloud.zimkit.components.conversation.viewmodel.ZIMKitConversationVM;
import com.zegocloud.zimkit.components.conversation.widget.CustomBottomSheet;
import com.zegocloud.zimkit.services.model.ZIMKitConversation;
import com.zegocloud.zimkit.BR;
import java.util.ArrayList;

import im.zego.zim.entity.ZIMError;
import im.zego.zim.enums.ZIMConversationType;
import com.zegocloud.zimkit.R;
import com.zegocloud.zimkit.components.group.ui.ZIMKitCreateAndJoinGroupActivity;
import com.zegocloud.zimkit.components.message.ui.ZIMKitCreateSingleChatActivity;
import com.zegocloud.zimkit.databinding.ZimkitFragmentConversationBinding;
import com.zegocloud.zimkit.databinding.ZimkitLayoutConversationDeleteBinding;
import com.zegocloud.zimkit.databinding.ZimkitLayoutSeletectChatTypeBinding;
import com.zegocloud.zimkit.services.internal.ZIMKitCore;

public class ZIMKitConversationFragment extends BaseFragment<ZimkitFragmentConversationBinding, ZIMKitConversationVM> {

    private ZIMKitConversationListAdapter mListAdapter;
    private CustomBottomSheet<ZimkitLayoutConversationDeleteBinding> mDeleteConversationBottomSheet;
    private ZIMKitConversationModel mCurrentSelectModel;
    private CustomBottomSheet<ZimkitLayoutSeletectChatTypeBinding> mSelectChatBottomSheet;

    private ZIMKitConversationListListener conversationListListener;

    @Override
    protected int getLayoutId() {
        return R.layout.zimkit_fragment_conversation;
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
                mDeleteConversationBottomSheet = new CustomBottomSheet<>(R.layout.zimkit_layout_conversation_delete, this::setBottomSheetItemListener);
            }
            mCurrentSelectModel = model;
            mDeleteConversationBottomSheet.show(getParentFragmentManager(), "delete_conversation");
        });
        mListAdapter.setItemClickListener(model -> {
            if (conversationListListener != null) {
                ZIMKitConversation conversation = new ZIMKitConversation(model.getConversation());
                DefaultAction defaultAction = new DefaultAction(model, defaultActionListener);
                conversationListListener.onConversationListClick(this,conversation, defaultAction);
            } else {
                if (ZIMKitCore.getInstance().getConversationListListener() != null) {
                    ZIMKitConversation conversation = new ZIMKitConversation(model.getConversation());
                    DefaultAction defaultAction = new DefaultAction(model, defaultActionListener);
                    ZIMKitCore.getInstance().getConversationListListener().onConversationListClick(this,conversation, defaultAction);
                } else {
                    mViewModel.clearConversationUnreadMessageCount(model.getConversationID(), model.getType());
                    toMessage(model.getType(),model.getConversationID(), model.getName(), model.getAvatar());
                }
            }
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

    private DefaultAction.ZIMKitDefaultActionListener defaultActionListener = new DefaultAction.ZIMKitDefaultActionListener() {
        @Override
        public void onDefaultAction(ZIMKitConversationModel model) {
            mViewModel.clearConversationUnreadMessageCount(model.getConversationID(), model.getType());
            toMessage(model.getType(),model.getConversationID(), model.getName(), model.getAvatar());
        }
    };

    public void showSelectChatBottomSheet() {
        if (mSelectChatBottomSheet != null && mSelectChatBottomSheet.getDialog() != null && mSelectChatBottomSheet.getDialog().isShowing()) {
            mSelectChatBottomSheet.dismiss();
        }
        if (mSelectChatBottomSheet == null) {
            mSelectChatBottomSheet = new CustomBottomSheet<>(R.layout.zimkit_layout_seletect_chat_type, this::setSelectBottomSheetItemListener);
        }
        mSelectChatBottomSheet.show(getParentFragmentManager(), "chatType");
    }

    private void setSelectBottomSheetItemListener(ZimkitLayoutSeletectChatTypeBinding binding) {
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
        Intent intent = new Intent(getActivity(), ZIMKitCreateSingleChatActivity.class);
        intent.putExtra(ZIMKitConstant.RouterConstant.KEY_BUNDLE, new Bundle());
        startActivity(intent);
    }

    /**
     * Start a group chat / Join the group chat
     *
     * @param type
     */
    private void toGroupChat(String type) {
        dismissBottomSheet();
        Bundle bundle = new Bundle();
        bundle.putString(ZIMKitConstant.GroupPageConstant.KEY_TYPE, type);
        Intent intent = new Intent(getActivity(), ZIMKitCreateAndJoinGroupActivity.class);
        intent.putExtra(ZIMKitConstant.RouterConstant.KEY_BUNDLE, bundle);
        startActivity(intent);
    }

    private void setBottomSheetItemListener(ZimkitLayoutConversationDeleteBinding binding) {
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
    private void toMessage(ZIMConversationType type, String conversationId, String name, String avatar) {
        ZIMKitConversationType zimKitConversationType = ZIMKitConversationType.ZIMKitConversationTypePeer;
        if (type == ZIMConversationType.GROUP) {
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
                    mBinding.refreshLayout.finishLoadMore(true);
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

    public void registerConversationListListener(ZIMKitConversationListListener listener) {
        this.conversationListListener = listener;
    }

    public void unRegisterConversationListListener() {
        this.conversationListListener = null;
    }

}
