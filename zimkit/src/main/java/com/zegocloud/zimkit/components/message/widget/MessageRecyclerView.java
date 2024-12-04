package com.zegocloud.zimkit.components.message.widget;

import static android.content.Context.CLIPBOARD_SERVICE;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AlertDialog.Builder;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import com.zegocloud.zimkit.R;
import com.zegocloud.zimkit.common.adapter.ZIMKitBindingAdapter;
import com.zegocloud.zimkit.common.base.BaseDialog;
import com.zegocloud.zimkit.common.components.CustomLinearLayoutManager;
import com.zegocloud.zimkit.common.utils.ZIMKitCustomToastUtil;
import com.zegocloud.zimkit.common.utils.ZIMKitDateUtils;
import com.zegocloud.zimkit.common.utils.ZIMKitSPUtils;
import com.zegocloud.zimkit.common.utils.ZIMKitToastUtils;
import com.zegocloud.zimkit.common.utils.ZLog;
import com.zegocloud.zimkit.components.message.adapter.ZIMKitMessageAdapter;
import com.zegocloud.zimkit.components.message.model.FileMessageModel;
import com.zegocloud.zimkit.components.message.model.TextMessageModel;
import com.zegocloud.zimkit.components.message.model.ZIMKitMessageModel;
import com.zegocloud.zimkit.components.message.viewmodel.ZIMKitMessageVM;
import com.zegocloud.zimkit.components.message.widget.chatpop.ChatPopAction;
import com.zegocloud.zimkit.components.message.widget.chatpop.MessagePopMenu;
import com.zegocloud.zimkit.components.message.widget.chatpop.MessagePopMenu.CallBack;
import com.zegocloud.zimkit.components.message.widget.interfaces.IMessageLayout;
import com.zegocloud.zimkit.components.message.widget.interfaces.OnItemClickListener;
import com.zegocloud.zimkit.components.message.widget.interfaces.OnPopActionClickListener;
import com.zegocloud.zimkit.services.ZIMKit;
import com.zegocloud.zimkit.services.ZIMKitConfig;
import com.zegocloud.zimkit.services.config.ZIMKitMessageConfig;
import com.zegocloud.zimkit.services.config.message.ZIMKitMessageOperationName;
import com.zegocloud.zimkit.services.internal.ZIMKitCore;
import com.zegocloud.zimkit.services.model.ZIMKitUser;
import com.zegocloud.zimkit.services.utils.ZIMMessageUtil;
import im.zego.zim.callback.ZIMMessageDeletedCallback;
import im.zego.zim.callback.ZIMMessageReactionAddedCallback;
import im.zego.zim.callback.ZIMMessageReactionDeletedCallback;
import im.zego.zim.callback.ZIMMessageRevokedCallback;
import im.zego.zim.entity.ZIMError;
import im.zego.zim.entity.ZIMMessage;
import im.zego.zim.entity.ZIMMessageReaction;
import im.zego.zim.enums.ZIMConversationType;
import im.zego.zim.enums.ZIMErrorCode;
import im.zego.zim.enums.ZIMMessageType;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;


public class MessageRecyclerView extends RecyclerView implements IMessageLayout {

    public static final String rootPath =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/ZIMKit";

    // Take a large enough offset to ensure that you can scroll to the bottom in one go
    private static final int SCROLL_TO_END_OFFSET = -999999;

    //Timestamp interval,5min
    private final long mTimeLineInterval = 1000 * 60 * 5;

    private ZIMKitMessageAdapter mAdapter;

    protected List<ChatPopAction> mPopActions = new ArrayList<>();
    protected List<ChatPopAction> mMorePopActions = new ArrayList<>();
    private MessagePopMenu mMessagePopMenu;
    protected OnItemClickListener mOnItemClickListener;
    private ZIMConversationType conversationType;
    private ZIMKitMessageVM mViewModel;
    //    private boolean isSpeaker = true;
    protected OnPopActionClickListener mOnPopActionClickListener;

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
        //        setLayoutFrozen(false);
        setItemViewCacheSize(0);
        //        setHasFixedSize(true);
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
            addItemDecoration(new ZIMKitMessageTimeLineDecoration(getContext(),
                new ZIMKitMessageTimeLineDecoration.DecorationCallback() {
                    @Override
                    public boolean needAddTimeLine(int position) {
                        if (position < 0) {
                            return false;
                        }
                        if (position == 0) {
                            return mAdapter.getItemDataList().get(position).getMessage() != null;
                        }
                        ZIMMessage nowMessage = mAdapter.getItemDataList().get(position).getMessage();
                        ZIMMessage lastMessage = mAdapter.getItemDataList().get(position - 1).getMessage();
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
                        return ZIMKitDateUtils.getMessageDate(
                            mAdapter.getItemDataList().get(position).getMessage().getTimestamp(), true);
                    }
                }));
        }

        setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (mMessagePopMenu != null) {
                    mMessagePopMenu.dismissPopWindow();
                    mMessagePopMenu = null;
                }
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
    }

    public void setContent(ZIMConversationType conversationType, ZIMKitMessageVM mViewModel) {
        this.conversationType = conversationType;
        this.mViewModel = mViewModel;
    }

    public void showItemPopMenu(final ZIMKitMessageModel messageInfo, View view) {
        List<ChatPopAction> chatPopActions = initPopActions(view.getContext(), messageInfo);
        mPopActions.clear();
        mPopActions.addAll(chatPopActions);
        mPopActions.addAll(mMorePopActions);

        if (mPopActions.isEmpty()) {
            return;
        }

        if (mMessagePopMenu != null) {
            mMessagePopMenu.dismissPopWindow();
            mMessagePopMenu = null;
        }

        boolean showChatPopReactionView = shouldShowChatPopReactionView(messageInfo);
        mMessagePopMenu = new MessagePopMenu(getContext(), messageInfo, mPopActions, showChatPopReactionView);
        int[] location = new int[2];
        getLocationOnScreen(location);
        mMessagePopMenu.show(view, location[1]);

        mMessagePopMenu.setCallBack(new CallBack() {
            @Override
            public void onRecentEmojiClick(String emoji, ZIMKitMessageModel messageModel) {
                onEmojiIconClicked(emoji, messageModel);
            }

            @Override
            public void onPagerEmojiClick(String emoji, ZIMKitMessageModel messageModel) {
                onEmojiIconClicked(emoji, messageModel);
            }

            @Override
            public void onPopActionClick(int position, ZIMKitMessageModel messageModel) {
                ChatPopAction chatPopAction = mPopActions.get(position);
                chatPopAction.getActionClickListener().onClick();
            }
        });
    }

    private void onEmojiIconClicked(String emoji, ZIMKitMessageModel messageModel) {
        Optional<ZIMMessageReaction> any = messageModel.getReactions().stream()
            .filter(messageReaction -> Objects.equals(messageReaction.reactionType, emoji)).findAny();
        if (any.isPresent()) {
            ZIMMessageReaction reaction = any.get();
            List<String> collect = reaction.userList.stream()
                .map(zimMessageReactionUserInfo -> zimMessageReactionUserInfo.userID).collect(Collectors.toList());
            ZIMKitUser localUser = ZIMKitCore.getInstance().getLocalUser();
            if (collect.contains(localUser.getId())) {
                // if the emoji existed in the message,and contains me,then remove
                removeMessageEmojiReaction(emoji, messageModel);
                return;
            }
        }

        addMessageEmojiReaction(emoji, messageModel);
    }

    private void addMessageEmojiReaction(String emoji, ZIMKitMessageModel messageModel) {
        ZIMKitCore.getInstance()
            .addMessageReaction(emoji, messageModel.getMessage(), new ZIMMessageReactionAddedCallback() {
                @Override
                public void onMessageReactionAdded(ZIMMessageReaction reaction, ZIMError error) {
                    if (error.code == ZIMErrorCode.SUCCESS) {
                        ArrayList<ZIMMessageReaction> zimReactions = messageModel.getMessage().getReactions();
                        Optional<ZIMMessageReaction> any = zimReactions.stream().filter(
                                messageReaction -> Objects.equals(messageReaction.reactionType, reaction.reactionType))
                            .findAny();
                        if (any.isPresent()) {
                            any.get().userList = reaction.userList;
                        } else {
                            zimReactions.add(reaction);
                        }
                        messageModel.setReactions(zimReactions);
                        mAdapter.updateMessageInfo(Collections.singletonList(messageModel));
                    }
                }
            });
    }

    public void onMessageReactionClicked(String emoji, ZIMKitMessageModel messageModel) {
        Optional<ZIMMessageReaction> any = messageModel.getReactions().stream()
            .filter(messageReaction -> Objects.equals(messageReaction.reactionType, emoji)).findAny();
        if (any.isPresent()) {
            ZIMMessageReaction reaction = any.get();
            List<String> collect = reaction.userList.stream()
                .map(zimMessageReactionUserInfo -> zimMessageReactionUserInfo.userID).collect(Collectors.toList());
            ZIMKitUser localUser = ZIMKitCore.getInstance().getLocalUser();
            // if the emoji existed in the message,and contains me,then remove
            if (collect.contains(localUser.getId())) {
                removeMessageEmojiReaction(emoji, messageModel);
                return;
            }
        }

        // else add
        addMessageEmojiReaction(emoji, messageModel);
    }

    private void removeMessageEmojiReaction(String emoji, ZIMKitMessageModel messageModel) {
        ZIMKitCore.getInstance()
            .deleteMessageReaction(emoji, messageModel.getMessage(), new ZIMMessageReactionDeletedCallback() {
                @Override
                public void onMessageReactionDeleted(ZIMMessageReaction reaction, ZIMError error) {
                    if (error.code == ZIMErrorCode.SUCCESS) {
                        ArrayList<ZIMMessageReaction> zimReactions = messageModel.getMessage().getReactions();
                        Optional<ZIMMessageReaction> any = zimReactions.stream().filter(
                                messageReaction -> Objects.equals(messageReaction.reactionType, reaction.reactionType))
                            .findAny();
                        if (any.isPresent()) {
                            any.get().userList = reaction.userList;
                            if (reaction.userList.isEmpty()) {
                                zimReactions.remove(any.get());
                            }
                        }
                        messageModel.setReactions(zimReactions);
                        mAdapter.updateMessageInfo(Collections.singletonList(messageModel));
                    }
                }
            });
    }

    /**
     * Long press to multi-select
     *
     * @param context
     * @param messageModel
     */
    private List<ChatPopAction> initPopActions(Context context, final ZIMKitMessageModel messageModel) {
        List<ChatPopAction> collect = getChatPopOperationNames(messageModel).stream()
            .map(zimKitMessageOperationName -> getChatPopAction(context, zimKitMessageOperationName, messageModel))
            .filter(Objects::nonNull).collect(Collectors.toList());
        if (messageModel.getMessage().getType() == ZIMMessageType.FILE) {
            ChatPopAction chatPopSaveAction = getChatPopSaveAction((FileMessageModel) messageModel);
            collect.add(chatPopSaveAction);
        }
        return collect;
    }

    private @NonNull List<ZIMKitMessageOperationName> getChatPopOperationNames(ZIMKitMessageModel messageModel) {
        if (messageModel == null) {
            return new ArrayList<>();
        }
        ZIMKitConfig zimKitConfig = ZIMKitCore.getInstance().getZimKitConfig();
        if (zimKitConfig == null || zimKitConfig.messageConfig == null) {
            return new ArrayList<>();
        }
        ZIMKitMessageConfig messageConfig = zimKitConfig.messageConfig;

        List<ZIMKitMessageOperationName> operationNameList = new ArrayList<>();
        if (messageModel.getMessage().getType() == ZIMMessageType.TEXT) {
            if (messageConfig.textMessageConfig != null && messageConfig.textMessageConfig.operations != null) {
                operationNameList = new ArrayList<>(messageConfig.textMessageConfig.operations);
            }
        } else if (messageModel.getMessage().getType() == ZIMMessageType.AUDIO) {
            if (messageConfig.audioMessageConfig != null && messageConfig.audioMessageConfig.operations != null) {
                operationNameList = new ArrayList<>(messageConfig.audioMessageConfig.operations);
            }
        } else if (messageModel.getMessage().getType() == ZIMMessageType.IMAGE) {
            if (messageConfig.imageMessageConfig != null && messageConfig.imageMessageConfig.operations != null) {
                operationNameList = new ArrayList<>(messageConfig.imageMessageConfig.operations);
            }
        } else if (messageModel.getMessage().getType() == ZIMMessageType.VIDEO) {
            if (messageConfig.videoMessageConfig != null && messageConfig.videoMessageConfig.operations != null) {
                operationNameList = new ArrayList<>(messageConfig.videoMessageConfig.operations);
            }
        } else if (messageModel.getMessage().getType() == ZIMMessageType.FILE) {
            if (messageConfig.fileMessageConfig != null && messageConfig.fileMessageConfig.operations != null) {
                operationNameList = new ArrayList<>(messageConfig.fileMessageConfig.operations);
            }
        } else if (messageModel.getMessage().getType() == ZIMMessageType.COMBINE) {
            if (messageConfig.combineMessageConfig != null && messageConfig.combineMessageConfig.operations != null) {
                operationNameList = new ArrayList<>(messageConfig.combineMessageConfig.operations);
            }
        }
        return operationNameList;
    }

    private boolean shouldShowChatPopReactionView(ZIMKitMessageModel messageModel) {
        ZIMKitConfig zimKitConfig = ZIMKitCore.getInstance().getZimKitConfig();
        if (zimKitConfig == null || zimKitConfig.messageConfig == null) {
            return false;
        }
        ZIMKitMessageConfig messageConfig = zimKitConfig.messageConfig;
        if (messageModel.getMessage().getType() == ZIMMessageType.TEXT) {
            if (messageConfig.textMessageConfig != null && messageConfig.textMessageConfig.operations != null) {
                return messageConfig.textMessageConfig.operations.contains(ZIMKitMessageOperationName.REACTION);
            }
        } else if (messageModel.getMessage().getType() == ZIMMessageType.AUDIO) {
            if (messageConfig.audioMessageConfig != null && messageConfig.audioMessageConfig.operations != null) {
                return messageConfig.audioMessageConfig.operations.contains(ZIMKitMessageOperationName.REACTION);
            }
        } else if (messageModel.getMessage().getType() == ZIMMessageType.IMAGE) {
            if (messageConfig.imageMessageConfig != null && messageConfig.imageMessageConfig.operations != null) {
                return messageConfig.imageMessageConfig.operations.contains(ZIMKitMessageOperationName.REACTION);
            }
        } else if (messageModel.getMessage().getType() == ZIMMessageType.VIDEO) {
            if (messageConfig.videoMessageConfig != null && messageConfig.videoMessageConfig.operations != null) {
                return messageConfig.videoMessageConfig.operations.contains(ZIMKitMessageOperationName.REACTION);
            }
        } else if (messageModel.getMessage().getType() == ZIMMessageType.FILE) {
            if (messageConfig.fileMessageConfig != null && messageConfig.fileMessageConfig.operations != null) {
                return messageConfig.fileMessageConfig.operations.contains(ZIMKitMessageOperationName.REACTION);
            }
        } else if (messageModel.getMessage().getType() == ZIMMessageType.COMBINE) {
            if (messageConfig.combineMessageConfig != null && messageConfig.combineMessageConfig.operations != null) {
                return messageConfig.combineMessageConfig.operations.contains(ZIMKitMessageOperationName.REACTION);
            }
        }
        return false;
    }

    private ChatPopAction getChatPopAction(Context context, ZIMKitMessageOperationName operationName,
        ZIMKitMessageModel messageModel) {
        ChatPopAction chatPopAction = null;
        switch (operationName) {
            case COPY:
                chatPopAction = getChatPopCopyAction(messageModel);
                break;
            case SPEAKER:
                chatPopAction = getChatPopSpeakerAction(context, messageModel);
                break;
            case REPLY:
                chatPopAction = getChatPopReplyAction(messageModel);
                break;
            case FORWARD:
                chatPopAction = getChatPopForwardAction(messageModel);
                break;
            case MULTIPLE_CHOICE:
                chatPopAction = getChatPopMultipleAction(messageModel);
                break;
            case DELETE:
                chatPopAction = getChatPopDeleteAction(messageModel);
                break;
            case REVOKE:
                chatPopAction = getChatPopRevokeAction(messageModel);
                break;
            case REACTION:
                break;
            default:
                break;
        }
        return chatPopAction;
    }

    private ChatPopAction getChatPopRevokeAction(ZIMKitMessageModel messageModel) {
        if (messageModel.getMessage().getSenderUserID().equals(ZIMKit.getLocalUser().getId())) {
            long duration = System.currentTimeMillis() - messageModel.getMessage().getTimestamp();
            //TODO should be server time.
            if (duration > 2 * 60 * 1000) {
                return null;
            }
            ChatPopAction action = new ChatPopAction();
            action.setActionName(getContext().getString(R.string.zimkit_message_withdraw));
            action.setActionIcon(R.drawable.zimkit_icon_reaction_withdraw);
            action.setActionClickListener(() -> {
                Builder builder = new Builder(getContext());
                ViewGroup viewGroup = (ViewGroup) View.inflate(getContext(), R.layout.zimkit_dialog_confirm_content,
                    null);
                TextView title = viewGroup.findViewById(R.id.title);
                title.setText(R.string.zimkit_chat_message_revoke);
                viewGroup.findViewById(R.id.content_edit_text).setVisibility(View.GONE);
                TextView textView = viewGroup.findViewById(R.id.content_text_view);
                textView.setText(R.string.zimkit_chat_retract_confirm);
                builder.setView(viewGroup);
                AlertDialog dialog = builder.create();
                viewGroup.findViewById(R.id.confirm).setOnClickListener(v -> {
                    withDrawMessage(messageModel);
                    dialog.dismiss();
                });
                viewGroup.findViewById(R.id.cancel).setOnClickListener(v -> {
                    dialog.dismiss();
                });
                dialog.show();
                Window window = dialog.getWindow();
                window.setBackgroundDrawableResource(R.drawable.zimkit_shape_12dp_white);
                WindowManager.LayoutParams lp = window.getAttributes();
                DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                lp.width = ZIMKitBindingAdapter.dp2px(270, displayMetrics);
                lp.height = ZIMKitBindingAdapter.dp2px(165, displayMetrics);
                window.setAttributes(lp);

            });
            return action;
        } else {
            return null;
        }
    }

    private @NonNull ChatPopAction getChatPopDeleteAction(ZIMKitMessageModel model) {
        ChatPopAction action = new ChatPopAction();
        action.setActionName(getContext().getString(R.string.zimkit_option_delete));
        action.setActionIcon(R.drawable.zimkit_icon_reaction_delete);
        action.setActionClickListener(() -> {
            if (mMessagePopMenu != null) {
                mMessagePopMenu.dismissPopWindow();
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
        return action;
    }

    private @NonNull ChatPopAction getChatPopMultipleAction(ZIMKitMessageModel model) {
        ChatPopAction action = new ChatPopAction();
        action.setActionName(getContext().getString(R.string.zimkit_multi_select));
        action.setActionIcon(R.drawable.zimkit_icon_reaction_multi_select);
        action.setActionClickListener(() -> {
            if (mOnPopActionClickListener != null) {
                mOnPopActionClickListener.onActionMultiSelectClick(model);
            }
        });
        return action;
    }

    private @NonNull ChatPopAction getChatPopSaveAction(FileMessageModel model) {
        ChatPopAction action = new ChatPopAction();
        action.setActionName(getContext().getString(R.string.zimkit_option_save));
        action.setActionIcon(R.drawable.zimkit_icon_reaction_save);
        action.setActionClickListener(() -> downloadFile(model.getFileLocalPath()));
        return action;
    }

    private ChatPopAction getChatPopForwardAction(ZIMKitMessageModel model) {
        if (model.getMessage().getType() == ZIMMessageType.AUDIO) {
            return null;
        }
        ChatPopAction action = new ChatPopAction();
        action.setActionName(getContext().getString(R.string.zimkit_option_forward));
        action.setActionIcon(R.drawable.zimkit_icon_reaction_forward);
        action.setActionClickListener(() -> {
            if (mOnPopActionClickListener != null) {
                mOnPopActionClickListener.onActionForwardMessageClick(model);
            }
        });
        return action;
    }

    private ChatPopAction getChatPopReplyAction(ZIMKitMessageModel model) {
        ChatPopAction action = new ChatPopAction();
        action.setActionName(getContext().getString(R.string.zimkit_option_reply));
        action.setActionIcon(R.drawable.zimkit_icon_reaction_reply);
        action.setActionClickListener(() -> {
            if (mOnPopActionClickListener != null) {
                mOnPopActionClickListener.onActionReplyMessageClick(model);
            }
        });
        return action;
    }

    private ChatPopAction getChatPopCopyAction(ZIMKitMessageModel model) {
        if (model.getMessage().getType() != ZIMMessageType.TEXT) {
            return null;
        }
        ChatPopAction action = new ChatPopAction();
        action.setActionName(getContext().getString(R.string.zimkit_option_copy));
        action.setActionIcon(R.drawable.zimkit_icon_reaction_copy);
        action.setActionClickListener(() -> copy(((TextMessageModel) model).getContent()));
        return action;
    }

    private ChatPopAction getChatPopSpeakerAction(Context context, ZIMKitMessageModel model) {
        if (model.getMessage().getType() != ZIMMessageType.AUDIO) {
            return null;
        }
        boolean isSpeaker = ZIMKitSPUtils.getBoolean(ZIMKitSPUtils.KEY_AUDIO_PLAY_MODE, true);
        ChatPopAction action = new ChatPopAction();
        action.setActionName(
            getContext().getString(isSpeaker ? R.string.zimkit_option_speaker_off : R.string.zimkit_option_speaker_on));
        action.setActionIcon(
            isSpeaker ? R.drawable.zimkit_icon_reaction_earpiece : R.drawable.zimkit_icon_reaction_speaker);
        action.setActionClickListener(() -> {
            mAdapter.setAudioPlayByEarPhone(context, !isSpeaker);
            ZIMKitSPUtils.putBoolean(ZIMKitSPUtils.KEY_AUDIO_PLAY_MODE, !isSpeaker);
            if (isSpeaker) {
                ZIMKitCustomToastUtil.showToast(getContext(), getContext().getString(R.string.zimkit_speaker_off_tip),
                    R.drawable.zimkit_icon_reaction_earpiece);
            } else {
                ZIMKitCustomToastUtil.showToast(getContext(), getContext().getString(R.string.zimkit_speaker_on_tip),
                    R.drawable.zimkit_icon_reaction_speaker);
            }

        });
        return action;
    }

    private static final String TAG = "MessageRecyclerView";

    private void withDrawMessage(ZIMKitMessageModel model) {
        mViewModel.withDrawMessage(model, new ZIMMessageRevokedCallback() {
            @Override
            public void onMessageRevoked(ZIMMessage message, ZIMError errorInfo) {
                if (errorInfo.code == ZIMErrorCode.SUCCESS) {
                    ZIMKitMessageModel messageModel = ZIMMessageUtil.parseZIMMessageToModel(message);
                    mAdapter.updateMessageInfo(Collections.singletonList(messageModel));
                }
            }
        });
    }

    @Override
    public void setAdapter(ZIMKitMessageAdapter adapter) {
        super.setAdapter(adapter);
        this.mAdapter = adapter;
    }

    public void setPopActionClickListener(OnPopActionClickListener listener) {
        mOnPopActionClickListener = listener;
    }

    public void deleteMessage(ZIMKitMessageModel model) {
        ArrayList<ZIMKitMessageModel> messageList = new ArrayList<>();
        messageList.add(model);
        mViewModel.deleteMessage(messageList, conversationType, new ZIMMessageDeletedCallback() {
            @Override
            public void onMessageDeleted(String conversationID, ZIMConversationType conversationType,
                ZIMError errorInfo) {
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
                ZIMKitToastUtils.showToast(String.format(
                    ZIMKitCore.getInstance().getApplication().getString(R.string.zimkit_file_save_path_tip),
                    dest.getName()));
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
