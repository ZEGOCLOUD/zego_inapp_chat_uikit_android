package com.zegocloud.zimkit.components.conversation.ui;

import android.Manifest.permission;
import android.app.Application;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import com.zegocloud.uikit.plugin.adapter.ZegoPluginAdapter;
import com.zegocloud.uikit.plugin.adapter.plugins.call.ZegoCallPluginProtocol;
import com.zegocloud.uikit.plugin.signaling.ZegoSignalingPlugin;
import com.zegocloud.zimkit.BR;
import com.zegocloud.zimkit.R;
import com.zegocloud.zimkit.common.ZIMKitConstant;
import com.zegocloud.zimkit.common.ZIMKitRouter;
import com.zegocloud.zimkit.common.base.BaseFragment;
import com.zegocloud.zimkit.common.enums.ZIMKitConversationType;
import com.zegocloud.zimkit.common.utils.ZIMKitToastUtils;
import com.zegocloud.zimkit.components.conversation.interfaces.ZIMKitConversationListListener;
import com.zegocloud.zimkit.components.conversation.model.DefaultAction;
import com.zegocloud.zimkit.components.conversation.model.ZIMKitConversationModel;
import com.zegocloud.zimkit.components.conversation.ui.SlideButtonDecor.ClickListener;
import com.zegocloud.zimkit.components.conversation.ui.SlideButtonDecor.SwipeButton;
import com.zegocloud.zimkit.components.conversation.ui.SlideButtonDecor.SwipeButtonProvider;
import com.zegocloud.zimkit.components.conversation.viewmodel.ZIMKitConversationVM;
import com.zegocloud.zimkit.components.conversation.widget.CustomBottomSheet;
import com.zegocloud.zimkit.components.group.ui.ZIMKitCreateAndJoinGroupActivity;
import com.zegocloud.zimkit.components.message.ui.ZIMKitCreatePrivateChatActivity;
import com.zegocloud.zimkit.databinding.ZimkitFragmentConversationBinding;
import com.zegocloud.zimkit.databinding.ZimkitLayoutConversationDeleteBinding;
import com.zegocloud.zimkit.databinding.ZimkitLayoutSeletectChatTypeBinding;
import com.zegocloud.zimkit.services.ZIMKit;
import com.zegocloud.zimkit.services.ZIMKitConfig;
import com.zegocloud.zimkit.services.ZIMKitDelegate;
import com.zegocloud.zimkit.services.callback.DeleteConversationCallback;
import com.zegocloud.zimkit.services.internal.ZIMKitCore;
import com.zegocloud.zimkit.services.model.ZIMKitConversation;
import im.zego.zim.entity.ZIMConversation;
import im.zego.zim.entity.ZIMError;
import im.zego.zim.enums.ZIMConnectionEvent;
import im.zego.zim.enums.ZIMConnectionState;
import im.zego.zim.enums.ZIMConversationType;
import java.util.ArrayList;
import java.util.List;

public class ZIMKitConversationFragment extends BaseFragment<ZimkitFragmentConversationBinding, ZIMKitConversationVM> {

    private ZIMKitConversationListAdapter mListAdapter;
    private ZIMKitConversationModel mCurrentSelectModel;
    private CustomBottomSheet<ZimkitLayoutConversationDeleteBinding> mDeleteConversationBottomSheet;
    private CustomBottomSheet<ZimkitLayoutSeletectChatTypeBinding> mSelectChatBottomSheet;

    private ZIMKitConversationListListener conversationListListener;
    private List<SwipeButton> swipeButtons = new ArrayList<>();
    private ZIMKitDelegate zimKitDelegate;
    private ActivityResultLauncher<String> permissionRequest = registerForActivityResult(new RequestPermission(),
        result -> {
            if (result) {
            }
        });

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
//            if (mDeleteConversationBottomSheet != null && mDeleteConversationBottomSheet.getDialog() != null
//                && mDeleteConversationBottomSheet.getDialog().isShowing()) {
//                mDeleteConversationBottomSheet.dismiss();
//            }
//            if (mDeleteConversationBottomSheet == null) {
//                mDeleteConversationBottomSheet = new CustomBottomSheet<>(R.layout.zimkit_layout_conversation_delete,
//                    this::setBottomSheetItemListener);
//            }
//            mCurrentSelectModel = model;
//            mDeleteConversationBottomSheet.show(getParentFragmentManager(), "delete_conversation");
        });
        mListAdapter.setItemClickListener(model -> {
            if (conversationListListener != null) {
                ZIMKitConversation conversation = new ZIMKitConversation(model.getConversation());
                DefaultAction defaultAction = new DefaultAction(model, defaultActionListener);
                conversationListListener.onConversationListClick(this, conversation, defaultAction);
            } else {
                if (ZIMKitCore.getInstance().getConversationListListener() != null) {
                    ZIMKitConversation conversation = new ZIMKitConversation(model.getConversation());
                    DefaultAction defaultAction = new DefaultAction(model, defaultActionListener);
                    ZIMKitCore.getInstance().getConversationListListener()
                        .onConversationListClick(this, conversation, defaultAction);
                } else {
                    mViewModel.clearConversationUnreadMessageCount(model.getConversationID(), model.getType());
                    toMessage(model.getType(), model.getConversationID(), model.getName(), model.getAvatar());
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

        SlideButtonDecor decor = new SlideButtonDecor();

        decor.setSwipeButtonProvider(new SwipeButtonProvider() {
            @Override
            public List<SwipeButton> onSwipeButtonRequired(ViewHolder viewHolder) {
                int position = viewHolder.getAdapterPosition();
                if (position == RecyclerView.NO_POSITION) {
                    return new ArrayList<>();
                }

                ZIMKitConversationModel model = mListAdapter.getModel(position);
                ZIMConversation conversation = model.getConversation();
                swipeButtons.clear();

                boolean addDeleteButton = true;
                boolean addPinButton = true;
                ZIMKitConversationListListener listener = ZIMKitCore.getInstance().getConversationListListener();
                if (listener != null) {
                    addDeleteButton = !listener.shouldHideSwipeDeleteItem(conversation, position);
                    addPinButton = !listener.shouldHideSwipePinnedItem(conversation, position);
                }
                if (addDeleteButton) {
                    String delete = getString(R.string.zimkit_delete);
                    SwipeButton deleteButton = new SwipeButton(delete, Color.WHITE, spToPx(15),
                        ContextCompat.getColor(getContext(), R.color.color_ff3c48), dpToPx(80), new ClickListener() {
                        @Override
                        public void onSingleTapConfirmed(int position, SwipeButton button, SlideButtonDecor slideButtonDecor) {
                            ZIMKit.deleteConversation(conversation.conversationID, conversation.type,
                                new DeleteConversationCallback() {
                                    @Override
                                    public void onDeleteConversation(ZIMError error) {
                                        if (ZIMKitCore.getInstance().getConversationListListener() != null) {
                                            ZIMKitCore.getInstance().getConversationListListener()
                                                .onConversationDeleted(conversation, position);
                                        }
                                    }
                                });
                        }
                    });
                    swipeButtons.add(deleteButton);
                }
                if (addPinButton) {
                    String pin = getString(R.string.zimkit_pin_conversation);
                    SwipeButton pinButton = new SwipeButton(pin, Color.WHITE, spToPx(15),
                        ContextCompat.getColor(getContext(), R.color.conversation_item_make_top), dpToPx(80),
                        new ClickListener() {
                            @Override
                            public void onSingleTapConfirmed(int position, SwipeButton button,
                                SlideButtonDecor slideButtonDecor) {
                                boolean activated = button.isActivated();
                                ZIMKitCore.getInstance()
                                    .setConversationPinnedState(!activated, conversation.conversationID,
                                        conversation.type, null);
                            }
                        });
                    boolean isPinned = false;
                    if (conversation != null) {
                        isPinned = conversation.isPinned;
                    }

                    updatePinButtonUI(pinButton, isPinned);
                    swipeButtons.add(pinButton);
                }
                return swipeButtons;
            }
        });
        decor.attachToRecyclerView(mBinding.rvList);

        mBinding.btnReload.setOnClickListener(v -> mViewModel.loadConversation());

        initCallKitPlugin();
        zimKitDelegate = new ZIMKitDelegate() {
            @Override
            public void onConnectionStateChange(ZIMConnectionState state, ZIMConnectionEvent event) {
                if (state == ZIMConnectionState.DISCONNECTED && event == ZIMConnectionEvent.SUCCESS) {
                    ZegoCallPluginProtocol callkitPlugin = ZegoPluginAdapter.callkitPlugin();
                    ZIMKitConfig zimKitConfig = ZIMKitCore.getInstance().getZimKitConfig();
                    if (zimKitConfig.callPluginConfig != null) {
                        if (callkitPlugin != null) {
                            callkitPlugin.logoutUser();
                        }
                    }
                }
            }
        };
        ZIMKit.registerZIMKitDelegate(zimKitDelegate);
    }

    private void initCallKitPlugin() {
        ZIMKitConfig zimKitConfig = ZIMKitCore.getInstance().getZimKitConfig();
        ZegoCallPluginProtocol callkitPlugin = ZegoPluginAdapter.callkitPlugin();
        if (zimKitConfig.callPluginConfig != null && callkitPlugin != null) {
            Application application = requireActivity().getApplication();
            String userID = ZIMKit.getLocalUser().getId();
            String userName = ZIMKit.getLocalUser().getName();
            callkitPlugin.init(application, ZIMKitCore.getInstance().appID, ZIMKitCore.getInstance().appSign, userID,
                userName, zimKitConfig.callPluginConfig);
            ZIMKitCore.getInstance().appID = 0;
            ZIMKitCore.getInstance().appSign = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ZegoSignalingPlugin.getInstance().getUserInfo() != null) {
            mViewModel.loadConversation();
        }
    }

    private void updatePinButtonUI(SwipeButton pinButton, boolean isPinned) {
        pinButton.setActivated(isPinned);
        if (isPinned) {
            pinButton.setText(getString(R.string.zimkit_cancel_pin_conversation));
            pinButton.setButtonWidth(dpToPx(94));
            int removeTopColor = ContextCompat.getColor(getContext(), R.color.conversation_item_remove_top);
            pinButton.setBackgroundColor(removeTopColor);
        } else {
            pinButton.setText(getString(R.string.zimkit_pin_conversation));
            pinButton.setButtonWidth(dpToPx(80));
            int makeTopColor = ContextCompat.getColor(getContext(), R.color.conversation_item_make_top);
            pinButton.setBackgroundColor(makeTopColor);
        }
    }

    private DefaultAction.ZIMKitDefaultActionListener defaultActionListener = new DefaultAction.ZIMKitDefaultActionListener() {
        @Override
        public void onDefaultAction(ZIMKitConversationModel model) {
            mViewModel.clearConversationUnreadMessageCount(model.getConversationID(), model.getType());
            toMessage(model.getType(), model.getConversationID(), model.getName(), model.getAvatar());
        }
    };

    public void showSelectChatBottomSheet() {
        if (mSelectChatBottomSheet != null && mSelectChatBottomSheet.getDialog() != null
            && mSelectChatBottomSheet.getDialog().isShowing()) {
            mSelectChatBottomSheet.dismiss();
        }
        if (mSelectChatBottomSheet == null) {
            mSelectChatBottomSheet = new CustomBottomSheet<>(R.layout.zimkit_layout_seletect_chat_type,
                this::setSelectBottomSheetItemListener);
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

    private int dpToPx(float dpValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue,
            getContext().getResources().getDisplayMetrics());
    }

    private int spToPx(float dpValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, dpValue,
            getContext().getResources().getDisplayMetrics());
    }

    /**
     * Initiate a single chat
     */
    private void toSingleChat() {
        dismissBottomSheet();
        Intent intent = new Intent(getActivity(), ZIMKitCreatePrivateChatActivity.class);
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
        ZIMKitRouter.toMessageActivity(this.getContext(), conversationId, name, avatar, zimKitConversationType);

        dismissBottomSheet();
    }

    @Override
    public void onDestroy() {
        dismissBottomSheet();
        ZIMKit.unRegisterZIMKitDelegate(zimKitDelegate);
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

        if (VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) {
            permissionRequest.launch(permission.POST_NOTIFICATIONS);
        }

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
                    mListAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFail(ZIMError error) {
                mBinding.refreshLayout.finishLoadMore(false);
                ZIMKitToastUtils.showErrorMessageIfNeeded(error.code.value(), error.message);
            }
        });
        if (ZegoSignalingPlugin.getInstance().getUserInfo() != null) {
            mViewModel.loadConversation(); // the first
        }
    }

    public void registerConversationListListener(ZIMKitConversationListListener listener) {
        this.conversationListListener = listener;
    }

    public void unRegisterConversationListListener() {
        this.conversationListListener = null;
    }

}
