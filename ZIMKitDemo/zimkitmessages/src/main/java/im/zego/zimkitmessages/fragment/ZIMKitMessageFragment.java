package im.zego.zimkitmessages.fragment;

import static android.app.Activity.RESULT_OK;
import static im.zego.zimkitmessages.fragment.ZIMKitInputMoreFragment.REQUEST_CODE_PHOTO;

import android.content.Intent;
import android.content.pm.ActivityInfo;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;

import im.zego.zim.callback.ZIMGroupInfoQueriedCallback;
import im.zego.zim.callback.ZIMMessageDeletedCallback;
import im.zego.zim.callback.ZIMUsersInfoQueriedCallback;
import im.zego.zim.entity.ZIMError;
import im.zego.zim.entity.ZIMErrorUserInfo;
import im.zego.zim.entity.ZIMGroupFullInfo;
import im.zego.zim.entity.ZIMMessage;
import im.zego.zim.entity.ZIMUserFullInfo;
import im.zego.zim.entity.ZIMUsersInfoQueryConfig;
import im.zego.zim.enums.ZIMConversationType;
import im.zego.zim.enums.ZIMErrorCode;
import im.zego.zimkitalbum.Matisse;
import im.zego.zimkitalbum.MimeType;
import im.zego.zimkitalbum.engine.impl.GlideEngine;
import im.zego.zimkitalbum.internal.entity.Item;
import im.zego.zimkitalbum.internal.utils.PathUtils;
import im.zego.zimkitcommon.ZIMKitConstant;
import im.zego.zimkitcommon.ZIMKitManager;
import im.zego.zimkitcommon.base.BaseDialog;
import im.zego.zimkitcommon.base.BaseFragment;
import im.zego.zimkitcommon.utils.ZIMKitBackgroundTasks;
import im.zego.zimkitcommon.utils.ZIMKitKeyboardUtils;
import im.zego.zimkitcommon.utils.ZIMKitThreadHelper;
import im.zego.zimkitcommon.utils.ZIMKitToastUtils;
import im.zego.zimkitcommon.utils.ZLog;
import im.zego.zimkitmessages.BR;
import im.zego.zimkitmessages.R;
import im.zego.zimkitmessages.ZIMKitMessageManager;
import im.zego.zimkitmessages.ZIMKitTransparentActivity;
import im.zego.zimkitmessages.adapter.ZIMKitMessageAdapter;
import im.zego.zimkitmessages.databinding.MessageFragmentBinding;
import im.zego.zimkitmessages.model.message.ZIMKitMessageModel;
import im.zego.zimkitmessages.utils.AudioSensorBinder;
import im.zego.zimkitmessages.utils.ChatMessageBuilder;
import im.zego.zimkitmessages.utils.image.HEIFImageHelper;
import im.zego.zimkitmessages.viewmodel.ZIMKitGroupMessageVM;
import im.zego.zimkitmessages.viewmodel.ZIMKitMessageVM;
import im.zego.zimkitmessages.viewmodel.ZIMKitSingleMessageVM;
import im.zego.zimkitmessages.widget.ZIMKitAudioPlayer;
import im.zego.zimkitmessages.widget.interfaces.OnPopActionClickListener;
import im.zego.zimkitmessages.widget.message.input.ZIMKitInputView;

public class ZIMKitMessageFragment extends BaseFragment<MessageFragmentBinding, ZIMKitMessageVM> {

    private ZIMKitMessageAdapter mAdapter;
    private ZIMConversationType conversationType = null;
    private OnTitleClickListener mOnTitleClickListener;

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
        return R.layout.message_fragment;
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

            if (type.equals(ZIMKitConstant.MessagePageConstant.TYPE_SINGLE_MESSAGE)) {
                String title = getArguments().getString(ZIMKitConstant.MessagePageConstant.KEY_TITLE);
                String avatar = getArguments().getString(ZIMKitConstant.MessagePageConstant.KEY_AVATAR);
                mViewModel = new ViewModelProvider(requireActivity()).get(ZIMKitSingleMessageVM.class);
                ((ZIMKitSingleMessageVM) mViewModel).setSingleOtherSideUserName(title);
                ((ZIMKitSingleMessageVM) mViewModel).setSingleOtherSideUserAvatar(avatar);
            } else if (type.equals(ZIMKitConstant.MessagePageConstant.TYPE_GROUP_MESSAGE)) {
                mViewModel = new ViewModelProvider(requireActivity()).get(ZIMKitGroupMessageVM.class);
            }
            mBinding.setVariable(BR.vm, mViewModel);
            mViewModel.setId(id);
            mViewModel.queryHistoryMessage();

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
                } else if (loadData.state == ZIMKitMessageVM.LoadData.DATA_STATE_NEW_UPDATE) {
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
                if (loadData.state != ZIMKitMessageVM.LoadData.DATA_STATE_HISTORY_NEXT) {
                    scrollToMessageEnd();
                }
            }

            @Override
            public void onFail(ZIMError error) {
                mBinding.refreshLayout.finishRefresh(false);
                if (error.code != ZIMErrorCode.SUCCESS && getContext() != null) {
                    if (error.code == ZIMErrorCode.NETWORK_ERROR) {
                        ZIMKitToastUtils.showToast(getString(R.string.message_network_anomaly));
                    } else if (error.code == ZIMErrorCode.FILE_SIZE_INVALID) {
                        ZIMKitToastUtils.showToast(getString(R.string.message_file_size_err_tips));
                    } else {
                        ZIMKitToastUtils.showToast(error.message);
                    }
                }
            }
        });

        mBinding.inputView.setMessageHandler(new ZIMKitInputView.MessageHandler() {
            @Override
            public void sendMessage(ZIMKitMessageModel model) {
                //Sending text messages
                mAdapter.addLocalMessageToBottom(model);
                mViewModel.send(model);
            }

            @Override
            public void sendMediaMessage(ZIMKitMessageModel model) {
                //Sending media messages
                mAdapter.addLocalMessageToBottom(model);
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
                baseDialog.setMsgContent(getContext().getString(R.string.message_delete_confirmation_desc));
                baseDialog.setLeftButtonContent(getContext().getString(R.string.message_btn_cancel));
                baseDialog.setRightButtonContent(getContext().getString(R.string.message_option_delete));
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
        mBinding.inputView.setChatRecordHandler(new ZIMKitInputView.ChatRecordHandler() {
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
                    ZIMKitToastUtils.showToast(getString(R.string.message_network_anomaly));
                } else {
                    ZIMKitToastUtils.showToast(errorInfo.message);
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
        if (requestCode == REQUEST_CODE_PHOTO && resultCode == RESULT_OK) {
            //Send picture message
            ArrayList<Item> selected = Matisse.obtainItemResult(data);
            if (selected != null && selected.size() > 0) {
                ZIMKitThreadHelper.INST.execute(new Runnable() {
                    @Override
                    public void run() {
                        List<ZIMKitMessageModel> mMessageList = new ArrayList<>();
                        for (Item mItem : selected) {
                            String fileLocalPath = PathUtils.getPath(ZIMKitManager.share().getApplication(), mItem.getContentUri());
                            if (mItem.isImage()) {
                                String path = HEIFImageHelper.isHeif(fileLocalPath) ? HEIFImageHelper.heifToJpg(fileLocalPath) : fileLocalPath;
                                mMessageList.add(ChatMessageBuilder.buildImageMessage(path));
                            } else if (mItem.isVideo()) {
                                mMessageList.add(ChatMessageBuilder.buildVideoMessage(fileLocalPath));
                            }
                        }
                        ZIMKitBackgroundTasks.getInstance().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mMessageList != null && mMessageList.size() > 0) {
                                    mAdapter.addListToBottom(mMessageList);
                                    scrollToMessageEnd();
                                    mViewModel.sendMediaMessage(mMessageList);
                                }
                            }
                        });
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
        ZIMKitMessageManager.share().removeNetworkConnection();
        ZIMKitMessageManager.share().clearNetworkListener();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mViewModel.setReceiveMessageListener(null);
        ZIMKitMessageManager.share().setMessageStateListener(null);
        if (ZIMKitManager.share().isKickedOutAccount()) {
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
            ArrayList<String> userIDs = new ArrayList<>();
            userIDs.add(id);
            ZIMUsersInfoQueryConfig config = new ZIMUsersInfoQueryConfig();
            config.isQueryFromServer = true;
            ZIMKitManager.share().zim().queryUsersInfo(userIDs, config, new ZIMUsersInfoQueriedCallback() {
                @Override
                public void onUsersInfoQueried(ArrayList<ZIMUserFullInfo> userList, ArrayList<ZIMErrorUserInfo> errorUserList, ZIMError errorInfo) {
                    if (errorInfo.code == ZIMErrorCode.SUCCESS) {
                        String title = userList.get(0).baseInfo.userName;
                        String avatar = userList.get(0).userAvatarUrl;
                        setSingleUserInfo(title, avatar);
                        if (mOnTitleClickListener != null) {
                            mOnTitleClickListener.setSetTitle(title);
                        }
                    }
                }
            });
        } else if (type.equals(ZIMKitConstant.MessagePageConstant.TYPE_GROUP_MESSAGE)) {
            ZIMKitManager.share().zim().queryGroupInfo(id, new ZIMGroupInfoQueriedCallback() {
                @Override
                public void onGroupInfoQueried(ZIMGroupFullInfo groupInfo, ZIMError errorInfo) {
                    if (errorInfo.code == ZIMErrorCode.SUCCESS) {
                        String title = groupInfo.baseInfo.groupName;
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
                .forResult(REQUEST_CODE_PHOTO);
    }

}
