package com.zegocloud.zimkit.components.message.widget;

import static android.content.Context.CLIPBOARD_SERVICE;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.zegocloud.zimkit.common.base.BaseDialog;
import com.zegocloud.zimkit.common.components.CustomLinearLayoutManager;
import com.zegocloud.zimkit.common.utils.PermissionHelper;
import com.zegocloud.zimkit.common.utils.ZIMKitCustomToastUtil;
import com.zegocloud.zimkit.common.utils.ZIMKitDateUtils;
import com.zegocloud.zimkit.common.utils.ZIMKitSPUtils;
import com.zegocloud.zimkit.common.utils.ZIMKitToastUtils;
import com.zegocloud.zimkit.common.utils.ZLog;
import com.zegocloud.zimkit.components.message.adapter.ZIMKitMessageAdapter;
import com.zegocloud.zimkit.components.message.viewmodel.ZIMKitMessageVM;
import com.zegocloud.zimkit.components.message.widget.interfaces.IMessageLayout;
import com.zegocloud.zimkit.components.message.widget.interfaces.OnItemClickListener;
import com.zegocloud.zimkit.components.message.widget.interfaces.OnPopActionClickListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import im.zego.zim.callback.ZIMMessageDeletedCallback;
import im.zego.zim.entity.ZIMError;
import im.zego.zim.entity.ZIMMessage;
import im.zego.zim.enums.ZIMConversationType;
import im.zego.zim.enums.ZIMErrorCode;
import com.zegocloud.zimkit.R;
import com.zegocloud.zimkit.components.message.model.AudioMessageModel;
import com.zegocloud.zimkit.components.message.model.FileMessageModel;
import com.zegocloud.zimkit.components.message.model.TextMessageModel;
import com.zegocloud.zimkit.components.message.model.ZIMKitMessageModel;
import com.zegocloud.zimkit.services.internal.ZIMKitCore;


public class MessageRecyclerView extends RecyclerView implements IMessageLayout {

    public static final String rootPath =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/ZIMKit";

    // Take a large enough offset to ensure that you can scroll to the bottom in one go
    private static final int SCROLL_TO_END_OFFSET = -999999;

    //Timestamp interval,5min
    private final long mTimeLineInterval = 1000 * 60 * 5;

    private ZIMKitMessageAdapter mAdapter;

    protected List<MessagePopMenu.ChatPopMenuAction> mPopActions = new ArrayList<>();
    protected List<MessagePopMenu.ChatPopMenuAction> mMorePopActions = new ArrayList<>();
    private MessagePopMenu mMessagePopMenu;
    protected OnItemClickListener mOnItemClickListener;
    private ZIMConversationType conversationType;
    private ZIMKitMessageVM mViewModel;
    //    private boolean isSpeaker = true;
    protected OnPopActionClickListener mOnPopActionClickListener;
    protected OnEmptySpaceClickListener mEmptySpaceClickListener;

    public MessageRecyclerView(Context context) {
        super(context);
        init();
    }

    public MessageRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MessageRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        setLayoutFrozen(false);
        setItemViewCacheSize(0);
        setHasFixedSize(true);
        setFocusableInTouchMode(false);
        setFocusable(true);
        setClickable(true);
        LinearLayoutManager linearLayoutManager = new CustomLinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        setLayoutManager(linearLayoutManager);
        SimpleItemAnimator animator = (SimpleItemAnimator) getItemAnimator();
        if (animator != null) {
            animator.setSupportsChangeAnimations(false);
        }

        if (getContext() != null) {
            addItemDecoration(new ZIMKitMessageTimeLineDecoration(getContext(), new ZIMKitMessageTimeLineDecoration.DecorationCallback() {
                @Override
                public boolean needAddTimeLine(int position) {
                    if (position < 0) {
                        return false;
                    }
                    if (position == 0) {
                        return mAdapter.getData().get(position).getMessage() != null;
                    }
                    ZIMMessage nowMessage = mAdapter.getData().get(position).getMessage();
                    ZIMMessage lastMessage = mAdapter.getData().get(position - 1).getMessage();
                    if (nowMessage == null || lastMessage == null) {
                        return false;
                    } else {
                        return (nowMessage.getTimestamp() - lastMessage.getTimestamp()) > mTimeLineInterval;
                    }
                }

                @Override
                public String getTimeLine(int position) {
                    if (position < 0) {
                        return "";
                    }
                    return ZIMKitDateUtils.getMessageDate(mAdapter.getData().get(position).getMessage().getTimestamp(), true);
                }
            }));
        }

        setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (mMessagePopMenu != null) {
                    mMessagePopMenu.hide();
                    mMessagePopMenu = null;
                }
                super.onScrollStateChanged(recyclerView, newState);
            }
        });

        setClickEmptySpaceEvent();
    }

    public void setContent(ZIMConversationType conversationType, ZIMKitMessageVM mViewModel) {
        this.conversationType = conversationType;
        this.mViewModel = mViewModel;
    }

    public void showItemPopMenu(final ZIMKitMessageModel messageInfo, View view) {
        initPopActions(messageInfo);
        if (mPopActions.size() == 0) {
            return;
        }

        if (mMessagePopMenu != null) {
            mMessagePopMenu.hide();
            mMessagePopMenu = null;
        }
        mMessagePopMenu = new MessagePopMenu(getContext(), mPopActions.size());
        mMessagePopMenu.setChatPopMenuActionList(mPopActions);
        int[] location = new int[2];
        getLocationOnScreen(location);
        mMessagePopMenu.show(view, location[1]);
//        mMessagePopMenu.setEmptySpaceClickListener(new MessageRecyclerView.OnEmptySpaceClickListener() {
//            @Override
//            public void onClick() {
//                if (mEmptySpaceClickListener != null) {
//                    mEmptySpaceClickListener.onClick();
//                }
//            }
//        });
    }

    /**
     * Long press to multi-select
     *
     * @param model
     */
    private void initPopActions(final ZIMKitMessageModel model) {
        if (model == null) {
            return;
        }

        List<MessagePopMenu.ChatPopMenuAction> actions = new ArrayList<>();
        actions.clear();
        MessagePopMenu.ChatPopMenuAction action = null;

        //Earpiece
        if (model instanceof AudioMessageModel) {
            boolean isSpeaker = ZIMKitSPUtils.getBoolean(ZIMKitSPUtils.KEY_AUDIO_PLAY_MODE, true);
            action = new MessagePopMenu.ChatPopMenuAction();
            action.setActionName(getContext().getString(isSpeaker ? R.string.zimkit_option_speaker_off : R.string.zimkit_option_speaker_on));
            action.setActionIcon(isSpeaker ? R.mipmap.zimkit_icon_pop_menu_earpiece : R.mipmap.zimkit_icon_pop_menu_speakers);
            action.setActionClickListener(() -> {
                mAdapter.setAudioPlayByEarPhone(!isSpeaker);
                ZIMKitSPUtils.putBoolean(ZIMKitSPUtils.KEY_AUDIO_PLAY_MODE, !isSpeaker);
                if (isSpeaker) {
                    ZIMKitCustomToastUtil.showToast(getContext(), getContext().getString(R.string.zimkit_speaker_off_tip), R.mipmap.zimkit_icon_pop_menu_earpiece);
                } else {
                    ZIMKitCustomToastUtil.showToast(getContext(), getContext().getString(R.string.zimkit_speaker_on_tip), R.mipmap.zimkit_icon_pop_menu_speakers);
                }

            });
            actions.add(action);
        }

        //Delete
        action = new MessagePopMenu.ChatPopMenuAction();
        action.setActionName(getContext().getString(R.string.zimkit_option_delete));
        action.setActionIcon(R.mipmap.zimkit_icon_pop_menu_delete);
        action.setActionClickListener(() -> {
            if (mMessagePopMenu != null) {
                mMessagePopMenu.hide();
                mMessagePopMenu = null;
            }
            BaseDialog baseDialog = new BaseDialog(getContext());
            baseDialog.setMsgTitle("");
            baseDialog.setMsgContent(getContext().getString(R.string.zimkit_delete_confirmation_desc));
            baseDialog.setLeftButtonContent(getContext().getString(R.string.zimkit_btn_cancel));
            baseDialog.setRightButtonContent(getContext().getString(R.string.zimkit_option_delete));
            baseDialog.setSureListener(v -> {
                baseDialog.dismiss();
                deleteMessage(model);
            });
            baseDialog.setCancelListener(v -> {
                baseDialog.dismiss();
            });
        });
        actions.add(action);

        //Text Copy
        if (model instanceof TextMessageModel) {
            action = new MessagePopMenu.ChatPopMenuAction();
            action.setActionName(getContext().getString(R.string.zimkit_option_copy));
            action.setActionIcon(R.mipmap.zimkit_icon_pop_menu_copy);
            action.setActionClickListener(() -> copy(((TextMessageModel) model).getContent()));
            actions.add(action);
        }

        //File Saving
        if (model instanceof FileMessageModel) {
            if (!TextUtils.isEmpty(((FileMessageModel) model).getFileLocalPath())) {
                action = new MessagePopMenu.ChatPopMenuAction();
                action.setActionName(getContext().getString(R.string.zimkit_option_save));
                action.setActionIcon(R.mipmap.zimkit_icon_pop_menu_save);
                action.setActionClickListener(() -> downloadFile(((FileMessageModel) model).getFileLocalPath()));
                actions.add(action);
            }
        }

        //Multiple Choice
        action = new MessagePopMenu.ChatPopMenuAction();
        action.setActionName(getContext().getString(R.string.zimkit_multi_select));
        action.setActionIcon(R.mipmap.zimkit_icon_pop_menu_multiselect);
        action.setActionClickListener(() -> {
            if (mOnPopActionClickListener != null) {
                mOnPopActionClickListener.onMultiSelectMessageClick(model);
            }
        });
        actions.add(action);

        mPopActions.clear();
        mPopActions.addAll(actions);
        mPopActions.addAll(mMorePopActions);
    }

    public void setAdapterListener() {
        mAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onMessageLongClick(View view, int position, ZIMKitMessageModel messageInfo) {
                showItemPopMenu(messageInfo, view);
            }
        });
    }

    /**
     * Click on the blank area
     */
    private void setClickEmptySpaceEvent() {
        GestureDetector.OnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                if (mEmptySpaceClickListener != null) {
                    mEmptySpaceClickListener.onClick();
                    return true;
                }
                return false;
            }
        };

        GestureDetector gestureDetector = new GestureDetector(getContext(), gestureListener);
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (v instanceof RecyclerView) {
                    gestureDetector.onTouchEvent(event);
                }
                return false;
            }
        });
    }

    @Override
    public void setAdapter(ZIMKitMessageAdapter adapter) {
        super.setAdapter(adapter);
        this.mAdapter = adapter;
        setAdapterListener();
    }

    @Override
    public OnItemClickListener getOnItemClickListener() {
        return mAdapter.getOnItemClickListener();
    }

    @Override
    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public void setPopActionClickListener(OnPopActionClickListener listener) {
        mOnPopActionClickListener = listener;
    }

    public void setEmptySpaceClickListener(OnEmptySpaceClickListener mEmptySpaceClickListener) {
        this.mEmptySpaceClickListener = mEmptySpaceClickListener;
    }

    public interface OnEmptySpaceClickListener {
        void onClick();
    }

    public void deleteMessage(ZIMKitMessageModel model) {
        ArrayList<ZIMMessage> messageList = new ArrayList<>();
        messageList.add(model.getMessage());
        mViewModel.deleteMessage(messageList, conversationType, new ZIMMessageDeletedCallback() {
            @Override
            public void onMessageDeleted(String conversationID, ZIMConversationType conversationType, ZIMError errorInfo) {
                if (errorInfo.code == ZIMErrorCode.SUCCESS) {
                    mAdapter.deleteMessages(model);
                }
            }
        });
    }

    /**
     * Slide down to the bottom
     */
    public void scrollToEnd() {
        if (getAdapter() != null) {
            LayoutManager layoutManager = getLayoutManager();
            int itemCount = getAdapter().getItemCount();
            if (layoutManager instanceof LinearLayoutManager && itemCount > 0) {
                ((LinearLayoutManager) layoutManager).scrollToPositionWithOffset(itemCount - 1, SCROLL_TO_END_OFFSET);
            }
        }
    }

    private void requestPermission(String fileName, String filePath) {
        PermissionHelper.onWriteSDCardPermissionGranted((FragmentActivity) getContext(), new PermissionHelper.GrantResult() {
            @Override
            public void onGrantResult(boolean allGranted) {
                if (allGranted) {
                    downloadFile(filePath);
                } else {
                    BaseDialog baseDialog = new BaseDialog(getContext());
                    baseDialog.setMsgTitle(getContext().getString(R.string.zimkit_storage_permissions_tip));
                    baseDialog.setMsgContent(getContext().getString(R.string.zimkit_storage_permissions_description));
                    baseDialog.setLeftButtonContent(getContext().getString(R.string.zimkit_access_later));
                    baseDialog.setRightButtonContent(getContext().getString(R.string.zimkit_go_setting));
                    baseDialog.setSureListener(v -> {
                        baseDialog.dismiss();
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                .setData(Uri.fromParts("package", getContext().getPackageName(), null));
                        ((Activity) getContext()).startActivityForResult(intent, 666);
                    });
                    baseDialog.setCancelListener(v -> {
                        baseDialog.dismiss();
                    });
                }
            }
        });
    }

    public static void downloadFile(String srcString) {
        if (TextUtils.isEmpty(srcString)) {
            ZLog.d("save file", "srcString isEmpty");
            return;
        }
        File srcFile = new File(srcString);
        if (!srcFile.exists()) {
            ZLog.d("save file", "srcFile is null");
            return;
        }
        createDirIfNotExist();
        File destFile = new File(rootPath + "/" + srcFile.getName());
        copyFile(srcFile, destFile);
    }

    public static void createDirIfNotExist() {
        File file = new File(rootPath);
        if (!file.exists()) {
            try {
                file.mkdirs();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (!file.isDirectory() && file.canWrite()) {
            try {
                file.delete();
                file.mkdirs();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Save file
     *
     * @param src
     * @param dest
     */
    public static void copyFile(File src, File dest) {
        if (!src.getAbsolutePath().equals(dest.getAbsolutePath())) {
            try {
                InputStream in = new FileInputStream(src);
                FileOutputStream out = new FileOutputStream(dest);
                byte[] buf = new byte[1024];

                int len;
                while ((len = in.read(buf)) >= 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();
                ZIMKitToastUtils.showToast(String.format(ZIMKitCore.getInstance().getApplication().getString(R.string.zimkit_file_save_path_tip), dest.getName()));
            } catch (IOException e) {
                e.printStackTrace();
                ZIMKitToastUtils.showToast(R.string.zimkit_file_save_fail);
            }
        }
    }

    /**
     * Copy of text messages
     *
     * @param copyText
     */
    public void copy(String copyText) {
        ClipboardManager clipboardManager = (ClipboardManager) getContext().getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("zimkit_text_content", copyText);
        clipboardManager.setPrimaryClip(clip);
        ZIMKitToastUtils.showToast(ZIMKitCore.getInstance().getApplication().getString(R.string.zimkit_copy_success));
    }

}
