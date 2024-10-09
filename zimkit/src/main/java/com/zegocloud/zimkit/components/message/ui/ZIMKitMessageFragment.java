package com.zegocloud.zimkit.components.message.ui;

import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts.TakePicture;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import com.permissionx.guolindev.callback.RequestCallback;
import com.zegocloud.uikit.plugin.adapter.ZegoPluginAdapter;
import com.zegocloud.uikit.plugin.adapter.plugins.call.ZegoCallPluginProtocol;
import com.zegocloud.zimkit.BR;
import com.zegocloud.zimkit.R;
import com.zegocloud.zimkit.common.ZIMKitConstant;
import com.zegocloud.zimkit.common.base.BaseDialog;
import com.zegocloud.zimkit.common.base.BaseFragment;
import com.zegocloud.zimkit.common.utils.PermissionHelper;
import com.zegocloud.zimkit.common.utils.ZIMKitFileUtils;
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
import com.zegocloud.zimkit.components.message.model.ZIMKitInputButtonModel;
import com.zegocloud.zimkit.components.message.model.ZIMKitMessageModel;
import com.zegocloud.zimkit.components.message.utils.AudioSensorBinder;
import com.zegocloud.zimkit.components.message.utils.ChatMessageBuilder;
import com.zegocloud.zimkit.components.message.utils.ChatMessageParser;
import com.zegocloud.zimkit.components.message.utils.image.HEIFImageHelper;
import com.zegocloud.zimkit.components.message.viewmodel.ZIMKitGroupMessageVM;
import com.zegocloud.zimkit.components.message.viewmodel.ZIMKitMessageVM;
import com.zegocloud.zimkit.components.message.viewmodel.ZIMKitMessageVM.MessageRevokeListener;
import com.zegocloud.zimkit.components.message.viewmodel.ZIMKitSingleMessageVM;
import com.zegocloud.zimkit.components.message.widget.BottomCallDialog;
import com.zegocloud.zimkit.components.message.widget.ZIMKitAudioPlayer;
import com.zegocloud.zimkit.components.message.widget.input.InputCallback;
import com.zegocloud.zimkit.components.message.widget.interfaces.OnPopActionClickListener;
import com.zegocloud.zimkit.databinding.ZimkitFragmentMessageBinding;
import com.zegocloud.zimkit.services.ZIMKit;
import com.zegocloud.zimkit.services.ZIMKitConfig;
import com.zegocloud.zimkit.services.callback.QueryGroupInfoCallback;
import com.zegocloud.zimkit.services.callback.QueryUserCallback;
import com.zegocloud.zimkit.services.config.ZIMKitInputButtonName;
import com.zegocloud.zimkit.services.internal.ZIMKitCore;
import com.zegocloud.zimkit.services.model.ZIMKitGroupInfo;
import com.zegocloud.zimkit.services.model.ZIMKitMessage;
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
import java.util.stream.Collectors;

public class ZIMKitMessageFragment extends BaseFragment<ZimkitFragmentMessageBinding, ZIMKitMessageVM> {

    private ZIMKitMessageAdapter mAdapter;
    private ZIMConversationType conversationType = null;
    private String conversationID;
    private String conversationName;
    private OnTitleClickListener mOnTitleClickListener;

    private ZIMKitMessagesListListener messagesListListener;
    private static final int REQUEST_CODE_FILE = 1011;
    private static final int REQUEST_CODE_PHOTO = 1012;
    private Uri takePicUri;
    private ActivityResultLauncher<Uri> takePicLauncher = registerForActivityResult(new TakePicture(),
        new ActivityResultCallback<Boolean>() {

            @Override
            public void onActivityResult(Boolean result) {
                if (result) {
                    String filePath = ZIMKitFileUtils.getPathFromUri(takePicUri);
                    if (!TextUtils.isEmpty(filePath)) {
                        ZIMImageMessage message = new ZIMImageMessage(filePath);
                        ImageMessageModel messageModel = new ImageMessageModel();
                        messageModel.setCommonAttribute(message);
                        messageModel.onProcessMessage(message);
                        messageModel.setFileLocalPath(filePath);
                        mBinding.getRoot().post(new Runnable() {
                            @Override
                            public void run() {
                                mViewModel.sendMediaMessage(messageModel);
                                scrollToMessageEnd();
                            }
                        });
                    }
                    takePicUri = null;
                }
            }
        });
    private BottomCallDialog bottomCallDialog;
    private OnBackPressedCallback onBackPressedCallback;

    @Override
    protected void initView() {
        initRv();
    }

    private void initRv() {
        mAdapter = new ZIMKitMessageAdapter(getActivity());
        mBinding.rvMessage.setAdapter(mAdapter);

        onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isMultiSelect()) {
                    hideMultiSelectMessage();
                    if (mOnTitleClickListener != null) {
                        mOnTitleClickListener.titleNormal();
                    }
                    setEnabled(false);
                } else {
                    setEnabled(false);
                    requireActivity().finish();
                }
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
        //        mBinding.refreshLayout.setOnRefreshListener(refreshLayout -> {
        //            mViewModel.loadNextPage();
        //        });
        //        mBinding.refreshLayout.setEnableScrollContentWhenRefreshed(true);
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
            conversationType =
                type.equals(ZIMKitConstant.MessagePageConstant.TYPE_SINGLE_MESSAGE) ? ZIMConversationType.PEER
                    : ZIMConversationType.GROUP;
            //Eliminate the number of unread messages
            mViewModel.clearUnreadCount(conversationType);

            mBinding.rvMessage.setContent(conversationType, mViewModel);
        }

        mViewModel.setMessageRevokeListener(new MessageRevokeListener() {
            @Override
            public void onMessageRevokeReceived(ArrayList<ZIMKitMessage> messageList) {
                List<ZIMKitMessageModel> collect = messageList.stream()
                    .map(zimKitMessage -> ChatMessageParser.parseMessage(zimKitMessage.zim))
                    .collect(Collectors.toList());
                mAdapter.updateMessageInfo(collect);
            }
        });

        mViewModel.setReceiveMessageListener(new ZIMKitMessageVM.OnReceiveMessageListener() {
            @Override
            public void onSuccess(ZIMKitMessageVM.LoadData loadData) {
                if (loadData.data.isEmpty()) {
                    //                    mBinding.refreshLayout.finishRefreshWithNoMoreData();
                    return;
                }
                if (loadData.state == ZIMKitMessageVM.LoadData.DATA_STATE_HISTORY_FIRST) {
                    mAdapter.setNewList(loadData.data);
                } else if (loadData.state == ZIMKitMessageVM.LoadData.DATA_STATE_HISTORY_NEXT) {
                    mAdapter.addListToTop(loadData.data);
                } else if (loadData.state == ZIMKitMessageVM.LoadData.DATA_STATE_NEW) {
                    mAdapter.addListToBottom(loadData.data);
                } else if (loadData.state == ZIMKitMessageVM.LoadData.DATA_STATE_NEW_UPDATE
                    || loadData.state == ZIMKitMessageVM.LoadData.DATA_STATE_UPDATE_AVATAR) {
                    mAdapter.updateMessageInfo(loadData.data);
                }
                if (loadData.state != ZIMKitMessageVM.LoadData.DATA_STATE_NEW
                    && loadData.state != ZIMKitMessageVM.LoadData.DATA_STATE_NEW_UPDATE) {
                    if (loadData.data.size() < ZIMKitMessageVM.QUERY_HISTORY_MESSAGE_COUNT) {
                        //                        mBinding.refreshLayout.finishRefresh();
                    } else {
                        //                        mBinding.refreshLayout.finishRefreshWithNoMoreData();
                    }
                }
                if (loadData.state != ZIMKitMessageVM.LoadData.DATA_STATE_HISTORY_NEXT
                    && loadData.state != ZIMKitMessageVM.LoadData.DATA_STATE_UPDATE_AVATAR) {
                    scrollToMessageEnd();
                }
            }

            @Override
            public void onFail(ZIMError error) {
                //                mBinding.refreshLayout.finishRefresh(false);
                if (error.code != ZIMErrorCode.SUCCESS && getContext() != null) {
                    if (error.code == ZIMErrorCode.NETWORK_ERROR) {
                        ZIMKitToastUtils.showErrorMessageIfNeeded(error.code.value(),
                            getString(R.string.zimkit_network_anomaly));
                    } else if (error.code == ZIMErrorCode.FILE_SIZE_INVALID) {
                        ZIMKitToastUtils.showErrorMessageIfNeeded(error.code.value(),
                            getString(R.string.zimkit_file_size_err_tips));
                    } else {
                        ZIMKitToastUtils.showErrorMessageIfNeeded(error.code.value(), error.message);
                    }
                }
            }
        });

        initInputView();

        mBinding.multiSelectDelete.setOnClickListener(view -> {
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
                hideMultiSelectMessage();
            });
            baseDialog.setCancelListener(v -> {
                baseDialog.dismiss();
            });
        });

        //        //Multi-select after long press
        mBinding.rvMessage.setPopActionClickListener(new OnPopActionClickListener() {
            @Override
            public void onMultiSelectMessageClick(ZIMKitMessageModel messageModel) {
                showMultiSelectMessage(messageModel);
            }
        });
        //
        //        //Playback form of the player (handset, speaker)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            AudioSensorBinder audioSensorBinder = new AudioSensorBinder((AppCompatActivity) getActivity());
        }
        //
        //        // The message being sent exits back to be listened to
        ZIMKitMessageManager.share().setMessageStateListener(new ZIMKitMessageManager.IMessageListener() {
            @Override
            public void onSendState(List<ZIMKitMessageModel> data) {
                mAdapter.updateMessageInfo(data);
            }
        });
    }

    private void initInputView() {
        bottomCallDialog = new BottomCallDialog(conversationID);
        mBinding.inputViewLayout.setInputMoreItems(generateInputMoreItems());
        mBinding.inputViewLayout.attach(mBinding.rvMessage, mBinding.clContain);
        mBinding.inputViewLayout.setCallback(new InputCallback() {
            @Override
            public void onSendTextMessage(ZIMKitMessageModel model) {
                mViewModel.sendTextMessage(model);
                scrollToMessageEnd();
            }

            @Override
            public void onSendAudioMessage(ZIMKitMessageModel model) {
                mViewModel.sendMediaMessage(model);
                scrollToMessageEnd();
            }

            @Override
            public void onClickSmallItem(int position, ZIMKitInputButtonModel itemModel) {
                if (itemModel.getButtonName() == ZIMKitInputButtonName.PICTURE) {
                    requestPermission(REQUEST_CODE_PHOTO);
                } else if (itemModel.getButtonName() == ZIMKitInputButtonName.TAKE_PHOTO) {
                    takePhoto();
                } else if (itemModel.getButtonName() == ZIMKitInputButtonName.VIDEO_CALL
                    || itemModel.getButtonName() == ZIMKitInputButtonName.VOICE_CALL) {
                    showBottomCallDialog(false);
                } else if (itemModel.getButtonName() == ZIMKitInputButtonName.FILE) {
                    requestPermission(REQUEST_CODE_FILE);
                }
            }

            @Override
            public void onClickExpandItem(int position, ZIMKitInputButtonModel itemModel) {
                if (itemModel.getButtonName() == ZIMKitInputButtonName.PICTURE) {
                    requestPermission(REQUEST_CODE_PHOTO);
                } else if (itemModel.getButtonName() == ZIMKitInputButtonName.TAKE_PHOTO) {
                    takePhoto();
                } else if (itemModel.getButtonName() == ZIMKitInputButtonName.VIDEO_CALL
                    || itemModel.getButtonName() == ZIMKitInputButtonName.VOICE_CALL) {
                    showBottomCallDialog(true);
                } else if (itemModel.getButtonName() == ZIMKitInputButtonName.FILE) {
                    requestPermission(REQUEST_CODE_FILE);
                }
            }

        });
    }

    private void showBottomCallDialog(boolean fromExpand) {
        if (bottomCallDialog.getDialog() != null && bottomCallDialog.getDialog().isShowing()) {
            bottomCallDialog.dismiss();
        }
        ZIMKitConfig zimKitConfig = ZIMKitCore.getInstance().getZimKitConfig();
        ZegoCallPluginProtocol callkitPlugin = ZegoPluginAdapter.callkitPlugin();
        if (callkitPlugin == null) {
            ZIMKitToastUtils.showToast(getString(R.string.zimkit_call_kit_plugin_not_existed));
        } else if (zimKitConfig == null || zimKitConfig.callPluginConfig == null) {
            ZIMKitToastUtils.showToast(getString(R.string.zimkit_callkit_plugin_not_enabled));
        } else {
            AppCompatActivity activityCompat = (AppCompatActivity) getContext();
            bottomCallDialog.showDialog(activityCompat.getSupportFragmentManager(), fromExpand);
        }
    }

    private void takePhoto() {
        PermissionHelper.requestCameraPermissionIfNeed(requireActivity(), new RequestCallback() {
            @Override
            public void onResult(boolean allGranted, @NonNull List<String> grantedList,
                @NonNull List<String> deniedList) {
                if (allGranted) {
                    ContentValues values = new ContentValues();
                    String displayName = "zimkit_" + System.currentTimeMillis() + ".jpg";
                    values.put(MediaStore.MediaColumns.DISPLAY_NAME, displayName);
                    values.put(MediaStore.MediaColumns.MIME_TYPE, MimeType.JPEG.toString());
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM);
                    } else {
                        values.put(MediaStore.MediaColumns.DATA,
                            Environment.getExternalStorageDirectory().getPath() + "/" + Environment.DIRECTORY_DCIM + "/"
                                + displayName);
                    }
                    takePicUri = requireActivity().getContentResolver()
                        .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                    takePicLauncher.launch(takePicUri);
                }
            }
        });
    }

    private @NonNull List<ZIMKitInputButtonModel> generateInputMoreItems() {
        int maxButtons = 8;
        ZIMKitConfig zimKitConfig = ZIMKitCore.getInstance().getZimKitConfig();
        List<ZIMKitInputButtonModel> buttonModels = new ArrayList<>();
        if (zimKitConfig.inputConfig != null) {
            List<ZIMKitInputButtonName> buttonNames = new ArrayList<>(zimKitConfig.inputConfig.expandButtons);
            // expand filter audio and emoji and expand button
            buttonNames.remove(ZIMKitInputButtonName.EXPAND);
            buttonNames.remove(ZIMKitInputButtonName.AUDIO);
            buttonNames.remove(ZIMKitInputButtonName.EMOJI);

            // group call not support yet
            if (conversationType == ZIMConversationType.GROUP) {
                buttonNames.remove(ZIMKitInputButtonName.VOICE_CALL);
                buttonNames.remove(ZIMKitInputButtonName.VIDEO_CALL);
            }
            // VIDEO_CALL and VOICE_CALL only need one button
            if (buttonNames.contains(ZIMKitInputButtonName.VOICE_CALL) && buttonNames.contains(
                ZIMKitInputButtonName.VIDEO_CALL)) {
                buttonNames.remove(ZIMKitInputButtonName.VOICE_CALL);
            }

            if (buttonNames.size() > maxButtons) {
                buttonNames = buttonNames.subList(0, maxButtons);
            }
            for (ZIMKitInputButtonName buttonName : buttonNames) {
                ZIMKitInputButtonModel inputButtonModel = ZIMKitCore.getInstance().getInputButtonModel(buttonName);
                buttonModels.add(inputButtonModel);
            }
        }
        return buttonModels;
    }

    private static final String TAG = "ZIMKitMessageFragment";

    /**
     * Delete Message
     *
     * @param messageList
     */
    private void deleteMessage(ArrayList<ZIMMessage> messageList) {
        mViewModel.deleteMessage(messageList, conversationType, new ZIMMessageDeletedCallback() {
            @Override
            public void onMessageDeleted(String conversationID, ZIMConversationType conversationType,
                ZIMError errorInfo) {
                if (errorInfo.code == ZIMErrorCode.SUCCESS) {
                    mAdapter.setShowMultiSelectCheckBox(false);
                    mAdapter.deleteMultiMessages();
                    if (mOnTitleClickListener != null) {
                        mOnTitleClickListener.titleNormal();
                    }
                } else if (errorInfo.code == ZIMErrorCode.NETWORK_ERROR) {
                    ZIMKitToastUtils.showErrorMessageIfNeeded(errorInfo.code.value(),
                        getString(R.string.zimkit_network_anomaly));
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
            mBinding.inputViewLayout.hide();
            mBinding.multiSelectOperate.setVisibility(View.VISIBLE);
            mAdapter.notifyDataSetChanged();
            if (mOnTitleClickListener != null) {
                mOnTitleClickListener.titleMultiSelect();
            }
        }
    }

    public boolean isMultiSelect() {
        return mBinding.multiSelectOperate.getVisibility() == View.VISIBLE;
    }

    /**
     * Hide Multiple Choice
     */
    public void hideMultiSelectMessage() {
        mAdapter.setShowMultiSelectCheckBox(false);
        mBinding.multiSelectOperate.setVisibility(View.GONE);
        mAdapter.notifyDataSetChanged();
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
        mViewModel.setMessageRevokeListener(null);
        ZIMKitMessageManager.share().setMessageStateListener(null);
        if (ZIMKitCore.getInstance().isKickedOutAccount()) {
            transparentActivity();
        }
        mBinding.inputViewLayout.setCallback(null);
        ZIMKitAudioPlayer.getInstance().clearAudioRecordCallbacks();
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_FILE && resultCode == RESULT_OK) {
            //Get uri, followed by the process of converting uri to file.
            Uri uri = data.getData();
            onFileSelected(uri);

        } else if (requestCode == REQUEST_CODE_PHOTO && resultCode == RESULT_OK) {
            //Send picture message
            ArrayList<Item> selected = Matisse.obtainItemResult(data);
            if (selected != null && !selected.isEmpty()) {
                ZIMKitThreadHelper.INST.execute(new Runnable() {
                    @Override
                    public void run() {
                        onPicsSelected(selected);
                    }
                });
            }
        }
    }

    private void onPicsSelected(List<Item> itemList) {
        for (Item mItem : itemList) {
            String fileLocalPath = PathUtils.getPath(ZIMKitCore.getInstance().getApplication(), mItem.getContentUri());
            if (mItem.isImage()) {
                String path =
                    HEIFImageHelper.isHeif(fileLocalPath) ? HEIFImageHelper.heifToJpg(fileLocalPath) : fileLocalPath;
                ZIMImageMessage message = new ZIMImageMessage(path);
                ImageMessageModel messageModel = new ImageMessageModel();
                messageModel.setCommonAttribute(message);
                messageModel.onProcessMessage(message);
                messageModel.setFileLocalPath(path);
                mBinding.getRoot().post(new Runnable() {
                    @Override
                    public void run() {
                        mViewModel.sendMediaMessage(messageModel);
                        scrollToMessageEnd();
                    }
                });
            } else if (mItem.isVideo()) {
                long duration = mItem.duration / 1000;
                ZIMVideoMessage message = new ZIMVideoMessage(fileLocalPath, duration);
                VideoMessageModel messageModel = new VideoMessageModel();
                messageModel.setCommonAttribute(message);
                messageModel.onProcessMessage(message);
                messageModel.setFileLocalPath(fileLocalPath);
                mBinding.getRoot().post(new Runnable() {
                    @Override
                    public void run() {
                        mViewModel.sendMediaMessage(messageModel);
                        scrollToMessageEnd();
                    }
                });
            }
        }
    }

    private void onFileSelected(Uri uri) {
        ZIMKitMessageModel fileMessage = ChatMessageBuilder.buildFileMessage(uri);
        mViewModel.sendMediaMessage(fileMessage);
        scrollToMessageEnd();
    }


    // Checks if a volume containing external storage is available
    // for read and write.
    private boolean isExternalStorageWritable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    // Checks if a volume containing external storage is available to at least read.
    private boolean isExternalStorageReadable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)
            || Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED_READ_ONLY);
    }


    /**
     * Get permission judgment
     */
    private void requestPermission(int fromType) {
        PermissionHelper.requestReadSDCardPermissionIfNeed((FragmentActivity) getActivity(), new RequestCallback() {
            @Override
            public void onResult(boolean allGranted, @NonNull List<String> grantedList,
                @NonNull List<String> deniedList) {
                if (allGranted) {
                    if (fromType == REQUEST_CODE_PHOTO) {
                        startSendPhoto();
                    } else if (fromType == REQUEST_CODE_FILE) {
                        startSendFile();
                    }
                } else {
                    BaseDialog baseDialog = new BaseDialog(getActivity());
                    baseDialog.setMsgTitle(getActivity().getString(R.string.zimkit_storage_permissions_tip));
                    baseDialog.setMsgContent(getActivity().getString(R.string.zimkit_storage_permissions_description));
                    baseDialog.setLeftButtonContent(getActivity().getString(R.string.zimkit_access_later));
                    baseDialog.setRightButtonContent(getActivity().getString(R.string.zimkit_go_setting));
                    baseDialog.setSureListener(v -> {
                        baseDialog.dismiss();
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).setData(
                            Uri.fromParts("package", getActivity().getPackageName(), null));
                        ((Activity) getActivity()).startActivityForResult(intent, 666);
                    });
                    baseDialog.setCancelListener(v -> {
                        baseDialog.dismiss();
                    });
                }
            }
        });
    }

    private void startSendFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, REQUEST_CODE_FILE);
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
            .thumbnailScale(0.85f).maxImageSize(10).maxVideoSize(100)
            //Glide loading method
            .imageEngine(new GlideEngine()).setOnSelectedListener((uriList, pathList) -> {

            })
            //Whether to show only the thumbnails of the selected type, it will not put all
            // the pictures and videos together, but what is needed to show what
            .showSingleMediaType(true)
            //Whether to support the original image
            .originalEnable(true).spanCount(4)
            //Whether to automatically hide the Toolbar
            .autoHideToolbarOnSingleTap(true).showPreview(false).setOnCheckedListener(isChecked -> {

            }).forResult(REQUEST_CODE_PHOTO);
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
