package com.zegocloud.zimkit.components.message.ui;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.zegocloud.zimkit.BR;
import com.zegocloud.zimkit.R;
import com.zegocloud.zimkit.common.ZIMKitConstant;
import com.zegocloud.zimkit.common.base.BaseDialog;
import com.zegocloud.zimkit.common.base.BaseFragment;
import com.zegocloud.zimkit.common.utils.ZIMKitKeyboardUtils;
import com.zegocloud.zimkit.common.utils.ZIMKitThreadHelper;
import com.zegocloud.zimkit.common.utils.ZIMKitToastUtils;
import com.zegocloud.zimkit.components.album.Matisse;
import com.zegocloud.zimkit.components.album.MimeType;
import com.zegocloud.zimkit.components.album.engine.impl.GlideEngine;
import com.zegocloud.zimkit.components.album.internal.entity.Item;
import com.zegocloud.zimkit.components.album.internal.utils.PathUtils;
import com.zegocloud.zimkit.components.message.ZIMKitMessageManager;
import com.zegocloud.zimkit.components.message.adapter.ZIMKitMessageAdapter;
import com.zegocloud.zimkit.components.message.interfaces.ZIMKitMessagesListListener;
import com.zegocloud.zimkit.components.message.model.ImageMessageModel;
import com.zegocloud.zimkit.components.message.model.VideoMessageModel;
import com.zegocloud.zimkit.components.message.model.ZIMKitMessageModel;
import com.zegocloud.zimkit.components.message.utils.AudioSensorBinder;
import com.zegocloud.zimkit.components.message.utils.image.HEIFImageHelper;
import com.zegocloud.zimkit.components.message.viewmodel.ZIMKitGroupMessageVM;
import com.zegocloud.zimkit.components.message.viewmodel.ZIMKitMessageVM;
import com.zegocloud.zimkit.components.message.viewmodel.ZIMKitSingleMessageVM;
import com.zegocloud.zimkit.components.message.widget.ZIMKitAudioPlayer;
import com.zegocloud.zimkit.components.message.widget.input.ZIMKitInputView.ChatRecordHandler;
import com.zegocloud.zimkit.components.message.widget.input.ZIMKitInputView.MessageHandler;
import com.zegocloud.zimkit.components.message.widget.interfaces.OnPopActionClickListener;
import com.zegocloud.zimkit.databinding.ZimkitFragmentMessageBinding;
import com.zegocloud.zimkit.services.ZIMKit;
import com.zegocloud.zimkit.services.callback.QueryGroupInfoCallback;
import com.zegocloud.zimkit.services.callback.QueryUserCallback;
import com.zegocloud.zimkit.services.internal.ZIMKitCore;
import com.zegocloud.zimkit.services.model.ZIMKitGroupInfo;
import com.zegocloud.zimkit.services.model.ZIMKitUser;
import im.zego.zim.callback.ZIMMessageDeletedCallback;
import im.zego.zim.entity.ZIMError;
import im.zego.zim.entity.ZIMImageMessage;
import im.zego.zim.entity.ZIMMessage;
import im.zego.zim.entity.ZIMVideoMessage;
import im.zego.zim.enums.ZIMConversationType;
import im.zego.zim.enums.ZIMErrorCode;
import java.util.ArrayList;
import java.util.List;

public class ZIMKitMessageFragment extends BaseFragment<ZimkitFragmentMessageBinding, ZIMKitMessageVM> {

    private ZIMKitMessageAdapter mAdapter;
    private ZIMConversationType conversationType = null;
    private String conversationID;
    private String conversationName;
    private OnTitleClickListener mOnTitleClickListener;

    private ZIMKitMessagesListListener messagesListListener;

    @Override
    protected void initView() {
        initRv();
    }

    private void initRv() {
        mAdapter = new ZIMKitMessageAdapter(getActivity());
        mBinding.rvMessage.setAdapter(mAdapter);
        mBinding.refreshLayout.setOnRefreshListener(refreshLayout -> {
            mViewModel.loadNextPage();
        });
        mBinding.refreshLayout.setEnableScrollContentWhenRefreshed(true);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.zimkit_fragment_message;
    }

    @Override
    protected int getViewModelId() {
        return 0;
    }

    @Override
    protected void initData() {
        ZIMKitMessageManager.share().initNetworkConnection();
        if (getArguments() != null) {
            String type = getArguments().getString(ZIMKitConstant.MessagePageConstant.KEY_TYPE);
            String id = getArguments().getString(ZIMKitConstant.MessagePageConstant.KEY_ID);
            String title = getArguments().getString(ZIMKitConstant.MessagePageConstant.KEY_TITLE);
            if (type.equals(ZIMKitConstant.MessagePageConstant.TYPE_SINGLE_MESSAGE)) {
                String avatar = getArguments().getString(ZIMKitConstant.MessagePageConstant.KEY_AVATAR);
                mViewModel = new ViewModelProvider(requireActivity()).get(ZIMKitSingleMessageVM.class);
                ((ZIMKitSingleMessageVM) mViewModel).setSingleOtherSideUserName(title);
                ((ZIMKitSingleMessageVM) mViewModel).setSingleOtherSideUserAvatar(avatar);
            } else if (type.equals(ZIMKitConstant.MessagePageConstant.TYPE_GROUP_MESSAGE)) {
                mViewModel = new ViewModelProvider(requireActivity()).get(ZIMKitGroupMessageVM.class);
                ((ZIMKitGroupMessageVM) mViewModel).setGroupTitle(title);
            }
            mBinding.setVariable(BR.vm, mViewModel);
            mViewModel.setId(id);
            mViewModel.queryHistoryMessage();

            conversationID = id;
            conversationName = title;
            conversationType = type.equals(ZIMKitConstant.MessagePageConstant.TYPE_SINGLE_MESSAGE) ? ZIMConversationType.PEER : ZIMConversationType.GROUP;
            //Eliminate the number of unread messages
            mViewModel.clearUnreadCount(conversationType);

            mBinding.rvMessage.setContent(conversationType, mViewModel);
        }

        mViewModel.setReceiveMessageListener(new ZIMKitMessageVM.OnReceiveMessageListener() {
            @Override
            public void onSuccess(ZIMKitMessageVM.LoadData loadData) {
                if (loadData.data.isEmpty()) {
                    mBinding.refreshLayout.finishRefreshWithNoMoreData();
                    return;
                }
                if (loadData.state == ZIMKitMessageVM.LoadData.DATA_STATE_HISTORY_FIRST) {
                    mAdapter.setNewList(loadData.data);
                } else if (loadData.state == ZIMKitMessageVM.LoadData.DATA_STATE_HISTORY_NEXT) {
                    mAdapter.addListToTop(loadData.data);
                } else if (loadData.state == ZIMKitMessageVM.LoadData.DATA_STATE_NEW) {
                    mAdapter.addListToBottom(loadData.data);
                } else if (loadData.state == ZIMKitMessageVM.LoadData.DATA_STATE_NEW_UPDATE || loadData.state == ZIMKitMessageVM.LoadData.DATA_STATE_UPDATE_AVATAR) {
                    mAdapter.updateMessageInfo(loadData.data);
                }
                if (loadData.state != ZIMKitMessageVM.LoadData.DATA_STATE_NEW &&
                        loadData.state != ZIMKitMessageVM.LoadData.DATA_STATE_NEW_UPDATE) {
                    if (loadData.data.size() < ZIMKitMessageVM.QUERY_HISTORY_MESSAGE_COUNT) {
                        mBinding.refreshLayout.finishRefresh();
                    } else {
                        mBinding.refreshLayout.finishRefreshWithNoMoreData();
                    }
                }
                if (loadData.state != ZIMKitMessageVM.LoadData.DATA_STATE_HISTORY_NEXT && loadData.state != ZIMKitMessageVM.LoadData.DATA_STATE_UPDATE_AVATAR) {
                    scrollToMessageEnd();
                }
            }

            @Override
            public void onFail(ZIMError error) {
                mBinding.refreshLayout.finishRefresh(false);
                if (error.code != ZIMErrorCode.SUCCESS && getContext() != null) {
                    if (error.code == ZIMErrorCode.NETWORK_ERROR) {
                        ZIMKitToastUtils.showErrorMessageIfNeeded(error.code.value(), getString(R.string.zimkit_network_anomaly));
                    } else if (error.code == ZIMErrorCode.FILE_SIZE_INVALID) {
                        ZIMKitToastUtils.showErrorMessageIfNeeded(error.code.value(), getString(R.string.zimkit_file_size_err_tips));
                    } else {
                        ZIMKitToastUtils.showErrorMessageIfNeeded(error.code.value(), error.message);
                    }
                }
            }
        });

        mBinding.inputView.setMessageHandler(new MessageHandler() {
            @Override
            public void sendMessage(ZIMKitMessageModel model) {
                //Sending text messages
                mViewModel.send(model);
            }

            @Override
            public void sendMediaMessage(ZIMKitMessageModel model) {
                //Sending media messages
                scrollToMessageEnd();
                mViewModel.sendMediaMessage(model);
            }

            @Override
            public void scrollToEnd() {
                //Slide to the bottom of the page
                mBinding.inputView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        scrollToMessageEnd();
                    }
                }, 150);
            }

            @Override
            public void deleteMultiSelect() {
                //Multiple choice deletion
                ArrayList<ZIMMessage> messageList = mAdapter.getSelectedItem();
                if (messageList == null || messageList.size() == 0) {
                    return;
                }
                BaseDialog baseDialog = new BaseDialog(getContext());
                baseDialog.setMsgTitle("");
                baseDialog.setMsgContent(getContext().getString(R.string.zimkit_delete_confirmation_desc));
                baseDialog.setLeftButtonContent(getContext().getString(R.string.zimkit_btn_cancel));
                baseDialog.setRightButtonContent(getContext().getString(R.string.zimkit_option_delete));
                baseDialog.setSureListener(v -> {
                    baseDialog.dismiss();
                    deleteMessage(messageList);
                });
                baseDialog.setCancelListener(v -> {
                    baseDialog.dismiss();
                });
            }

            @Override
            public void openPhoto() {
                startSendPhoto();
            }
        });

        //Click on the blank area
        mBinding.rvMessage.setEmptySpaceClickListener(() -> mBinding.inputView.onEmptyClick());
        //Recording status callback
        mBinding.inputView.setChatRecordHandler(new ChatRecordHandler() {
            @Override
            public void onRecordStatusChanged(int status) {
                mBinding.mRecordAudioView.recordStatus(status);
            }

            @Override
            public void onRecordCountDownTimer(long recordTime) {
                mBinding.mRecordAudioView.onRecordCountDownTimer(recordTime);
            }
        });

        //Multi-select after long press
        mBinding.rvMessage.setPopActionClickListener(new OnPopActionClickListener() {
            @Override
            public void onMultiSelectMessageClick(ZIMKitMessageModel messageModel) {
                showMultiSelectMessage(messageModel);
            }
        });

        //Playback form of the player (handset, speaker)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            AudioSensorBinder audioSensorBinder = new AudioSensorBinder((AppCompatActivity) getActivity());
        }

        // The message being sent exits back to be listened to
        ZIMKitMessageManager.share().setMessageStateListener(new ZIMKitMessageManager.IMessageListener() {
            @Override
            public void onSendState(List<ZIMKitMessageModel> data) {
                mAdapter.updateMessageInfo(data);
            }
        });

        if (messagesListListener != null) {
            messagesListListener.getMessageListHeaderBar(this);
        }

    }

    /**
     * Delete Message
     *
     * @param messageList
     */
    private void deleteMessage(ArrayList<ZIMMessage> messageList) {
        mViewModel.deleteMessage(messageList, conversationType, new ZIMMessageDeletedCallback() {
            @Override
            public void onMessageDeleted(String conversationID, ZIMConversationType conversationType, ZIMError errorInfo) {
                if (errorInfo.code == ZIMErrorCode.SUCCESS) {
                    mAdapter.setShowMultiSelectCheckBox(false);
                    mAdapter.deleteMultiMessages();
                    mBinding.inputView.hideMultiSelectDelete();
                    if (mOnTitleClickListener != null) {
                        mOnTitleClickListener.titleNormal();
                    }
                } else if (errorInfo.code == ZIMErrorCode.NETWORK_ERROR) {
                    ZIMKitToastUtils.showErrorMessageIfNeeded(errorInfo.code.value(), getString(R.string.zimkit_network_anomaly));
                } else {
                    ZIMKitToastUtils.showErrorMessageIfNeeded(errorInfo.code.value(), errorInfo.message);
                }
            }
        });
    }

    /**
     * Show Multiple Choice
     *
     * @param messageModel
     */
    protected void showMultiSelectMessage(ZIMKitMessageModel messageModel) {
        if (mAdapter != null) {
            mAdapter.setShowMultiSelectCheckBox(true);
            messageModel.setCheck(true);
            mBinding.inputView.hideSoftInput();
            mBinding.inputView.showMultiSelectDelete();
            mAdapter.notifyDataSetChanged();
            if (mOnTitleClickListener != null) {
                mOnTitleClickListener.titleMultiSelect();
            }
        }
    }

    /**
     * Hide Multiple Choice
     */
    public void hideMultiSelectMessage() {
        mAdapter.setShowMultiSelectCheckBox(false);
        mBinding.inputView.hideMultiSelectDelete();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ZIMKitInputMoreFragment.REQUEST_CODE_PHOTO && resultCode == RESULT_OK) {
            //Send picture message
            ArrayList<Item> selected = Matisse.obtainItemResult(data);
            if (selected != null && selected.size() > 0) {
                ZIMKitThreadHelper.INST.execute(new Runnable() {
                    @Override
                    public void run() {
                        for (Item mItem : selected) {
                            String fileLocalPath = PathUtils.getPath(ZIMKitCore.getInstance().getApplication(), mItem.getContentUri());
                            if (mItem.isImage()) {
                                String path = HEIFImageHelper.isHeif(fileLocalPath) ? HEIFImageHelper.heifToJpg(fileLocalPath) : fileLocalPath;
                                ZIMImageMessage message = new ZIMImageMessage(path);
                                ImageMessageModel messageModel = new ImageMessageModel();
                                messageModel.setCommonAttribute(message);
                                messageModel.onProcessMessage(message);
                                messageModel.setFileLocalPath(path);
                                mViewModel.sendMediaMessage(messageModel);
                            } else if (mItem.isVideo()) {
                                long duration = mItem.duration / 1000;
                                ZIMVideoMessage message = new ZIMVideoMessage(fileLocalPath, duration);
                                VideoMessageModel messageModel = new VideoMessageModel();
                                messageModel.setCommonAttribute(message);
                                messageModel.onProcessMessage(message);
                                messageModel.setFileLocalPath(fileLocalPath);
                                mViewModel.sendMediaMessage(messageModel);
                            }
                        }
                    }
                });
            }
        }
    }

    /**
     * Slide to the bottom
     */
    public void scrollToMessageEnd() {
        mBinding.rvMessage.scrollToEnd();
    }

    public void setSingleUserInfo(String title, String avatar) {
        ((ZIMKitSingleMessageVM) mViewModel).setSingleOtherSideUserName(title);
        ((ZIMKitSingleMessageVM) mViewModel).setSingleOtherSideUserAvatar(avatar);
    }

    @Override
    public void onPause() {
        super.onPause();
        //Pause audio playback
        ZIMKitAudioPlayer.getInstance().stopPlay();
        ZIMKitKeyboardUtils.closeSoftKeyboard(getActivity());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ZIMKitMessageManager.share().removeNetworkConnection();
        ZIMKitMessageManager.share().clearNetworkListener();
        if (mAdapter != null) {
            mAdapter.clear();
        }
        mViewModel.setReceiveMessageListener(null);
        ZIMKitMessageManager.share().setMessageStateListener(null);
        if (ZIMKitCore.getInstance().isKickedOutAccount()) {
            transparentActivity();
        }
    }

    public void setOnOnTitleClickListener(OnTitleClickListener listener) {
        this.mOnTitleClickListener = listener;
    }

    /**
     * Need to get information when message notifications come through
     */
    public void getInformation(String type, String id) {
        if (type.equals(ZIMKitConstant.MessagePageConstant.TYPE_SINGLE_MESSAGE)) {
            ZIMKit.queryUserInfo(id, new QueryUserCallback() {
                @Override
                public void onQueryUser(ZIMKitUser userInfo, ZIMError error) {
                    if (error.code == ZIMErrorCode.SUCCESS) {
                        String title = userInfo.getName();
                        String avatar = userInfo.getAvatarUrl();
                        setSingleUserInfo(title, avatar);
                        conversationName = title;
                        if (mOnTitleClickListener != null) {
                            mOnTitleClickListener.setSetTitle(title);
                        }
                        ((ZIMKitSingleMessageVM) mViewModel).updateHistoryMessage(title, avatar);
                    }
                }
            });
        } else if (type.equals(ZIMKitConstant.MessagePageConstant.TYPE_GROUP_MESSAGE)) {
            ZIMKit.queryGroupInfo(id, new QueryGroupInfoCallback() {
                @Override
                public void onQueryGroupInfo(ZIMKitGroupInfo info, ZIMError error) {
                    if (error.code == ZIMErrorCode.SUCCESS) {
                        String title = info.getName();
                        conversationName = title;
                        if (mOnTitleClickListener != null) {
                            mOnTitleClickListener.setSetTitle(title);
                        }
                    }
                }
            });
        }
    }

    public interface OnTitleClickListener {
        void titleMultiSelect();

        void titleNormal();

        void setSetTitle(String title);
    }

    /**
     * Micro spicy solution, open the file pop-up window, squeeze the number of the time to close the problem
     */
    private void transparentActivity() {
        startActivity(new Intent(getActivity(), ZIMKitTransparentActivity.class));
    }

    private void startSendPhoto() {
        Matisse.from(this)
                //Select image
                .choose(MimeType.ofAll(), false)
                //Orderly selection of pictures 123456...
                .countable(true)
                //Maximum number of selections is 9
                .maxSelectable(9)
                //Set column width
                .gridExpectedSize(getResources().getDimensionPixelSize(R.dimen.message_grid_expected_size))
                //Select Direction
                .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                //Quality of thumbnails in the interface
                .thumbnailScale(0.85f)
                .maxImageSize(10)
                .maxVideoSize(100)
                //Glide loading method
                .imageEngine(new GlideEngine())
                .setOnSelectedListener((uriList, pathList) -> {

                })
                //Whether to show only the thumbnails of the selected type, it will not put all
                // the pictures and videos together, but what is needed to show what
                .showSingleMediaType(true)
                //Whether to support the original image
                .originalEnable(true)
                .spanCount(4)
                //Whether to automatically hide the Toolbar
                .autoHideToolbarOnSingleTap(true)
                .showPreview(false)
                .setOnCheckedListener(isChecked -> {

                })
                .forResult(ZIMKitInputMoreFragment.REQUEST_CODE_PHOTO);
    }

    public void registerMessageListListener(ZIMKitMessagesListListener listener) {
        this.messagesListListener = listener;
    }

    public void unRegisterMessageListListener() {
        this.messagesListListener = null;
    }

    public ZIMConversationType getConversationType() {
        return conversationType;
    }

    public String getConversationID() {
        return conversationID;
    }

    public String getConversationName() {
        return conversationName;
    }
}
