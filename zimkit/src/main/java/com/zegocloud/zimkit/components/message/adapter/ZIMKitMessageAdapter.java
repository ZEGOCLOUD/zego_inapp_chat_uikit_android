package com.zegocloud.zimkit.components.message.adapter;

import android.content.Context;
import android.media.AudioManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;
import com.zegocloud.zimkit.BR;
import com.zegocloud.zimkit.R;
import com.zegocloud.zimkit.components.message.ZIMKitMessageManager;
import com.zegocloud.zimkit.components.message.model.AudioMessageModel;
import com.zegocloud.zimkit.components.message.model.FileMessageModel;
import com.zegocloud.zimkit.components.message.model.VideoMessageModel;
import com.zegocloud.zimkit.components.message.model.ZIMKitMessageModel;
import com.zegocloud.zimkit.components.message.ui.ZIMKitVideoViewActivity;
import com.zegocloud.zimkit.components.message.widget.ZIMKitAudioPlayer;
import com.zegocloud.zimkit.components.message.widget.viewholder.AudioMessageHolder;
import com.zegocloud.zimkit.components.message.widget.viewholder.CombineMessageHolder;
import com.zegocloud.zimkit.components.message.widget.viewholder.CustomMessageHolder;
import com.zegocloud.zimkit.components.message.widget.viewholder.FileMessageHolder;
import com.zegocloud.zimkit.components.message.widget.viewholder.ImageMessageHolder;
import com.zegocloud.zimkit.components.message.widget.viewholder.MessageSystemHolder;
import com.zegocloud.zimkit.components.message.widget.viewholder.MessageViewHolder;
import com.zegocloud.zimkit.components.message.widget.viewholder.RevokeMessageHolder;
import com.zegocloud.zimkit.components.message.widget.viewholder.TextMessageHolder;
import com.zegocloud.zimkit.components.message.widget.viewholder.TipsMessageHolder;
import com.zegocloud.zimkit.components.message.widget.viewholder.VideoMessageHolder;
import com.zegocloud.zimkit.services.ZIMKit;
import com.zegocloud.zimkit.services.ZIMKitDelegate;
import com.zegocloud.zimkit.services.internal.ZIMKitCore;
import com.zegocloud.zimkit.services.model.ZIMKitMessage;
import im.zego.zim.enums.ZIMMessageDirection;
import im.zego.zim.enums.ZIMMessageType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ZIMKitMessageAdapter extends RecyclerView.Adapter<MessageViewHolder> {

    private final List<ZIMKitMessageModel> mList = new ArrayList<>();

    protected boolean isMultiSelectMode = false;
    private boolean forwardMode;
    private boolean oneSideForwardMode;

    public ZIMKitMessageAdapter() {
        this(false, false);
    }

    public ZIMKitMessageAdapter(boolean forwardMode, boolean oneSideForwardMode) {
        if (oneSideForwardMode) {
            this.forwardMode = true;
        } else {
            this.forwardMode = forwardMode;
        }
        this.oneSideForwardMode = oneSideForwardMode;
        ZIMKit.registerZIMKitDelegate(eventCallBack);
    }

    public void setNewList(List<ZIMKitMessageModel> list) {
        List<ZIMKitMessageModel> newList = new ArrayList<>(list);
        //        if (forwardMode) {
        //            newList = new ArrayList<>();
        //            for (ZIMKitMessageModel messageModel : list) {
        //                if (messageModel.getMessage().getType() == ZIMMessageType.AUDIO) {
        //                    String content = ZIMMessageUtil.simplifyZIMMessageContent(messageModel.getMessage());
        //                    ZIMKitMessageModel model = ChatMessageBuilder.buildTextMessage(content);
        //                    model.setSentStatus(ZIMMessageSentStatus.SUCCESS);
        //                    newList.add(model);
        //                } else {
        //                    newList.add(messageModel);
        //                }
        //            }
        //        } else {
        //            newList = new ArrayList<>(list);
        //        }

        if (ZIMKitCore.getInstance().isSendMessageByServer()) {
            mList.clear();
            mList.addAll(newList);
            sortAndUpdateListInner();
        } else {
            if (mList.size() == newList.size()) {
                mList.clear();
                mList.addAll(newList);
                this.notifyItemRangeChanged(0, newList.size());
                return;
            }
            if (mList.size() > 0) {
                int count = mList.size();
                mList.clear();
                this.notifyItemRangeRemoved(0, count);
            }
            if (newList.size() > 0) {
                mList.addAll(newList);
                this.notifyItemRangeInserted(0, newList.size());
            }
        }
    }

    public void addListToTop(List<ZIMKitMessageModel> list) {
        deleteLoadingMessage();
        mList.addAll(0, list);
        if (ZIMKitCore.getInstance().isSendMessageByServer()) {
            sortAndUpdateListInner();
        } else {
            this.notifyItemRangeInserted(0, list.size());
        }
    }

    public void addListToBottom(List<ZIMKitMessageModel> list) {
        deleteLoadingMessage();

        int oldCount = mList.size();
        mList.addAll(list);
        if (ZIMKitCore.getInstance().isSendMessageByServer()) {
            sortAndUpdateListInner();
        } else {
            this.notifyItemRangeInserted(oldCount, list.size());
        }

    }

    public void addMessageToBottom(ZIMKitMessageModel model) {
        if (model == null) {
            return;
        }
        deleteLoadingMessage();
        int oldCount = mList.size();
        mList.add(model);
        if (ZIMKitCore.getInstance().isSendMessageByServer()) {
            sortAndUpdateListInner();
        } else {
            this.notifyItemRangeInserted(oldCount, 1);
        }

    }

    /**
     * Delete radio message
     *
     * @param model
     */
    public void deleteMessages(ZIMKitMessageModel model) {
        if (model instanceof AudioMessageModel) {
            stopPlayAudio(model);
        }
        int index = mList.indexOf(model);
        mList.remove(model);

        if (ZIMKitCore.getInstance().isSendMessageByServer()) {
            sortAndUpdateListInner();
        } else {
            this.notifyItemRemoved(index);
        }
    }

    /**
     * Delete multi-select messages
     */
    public void deleteMultiMessages() {
        Iterator<ZIMKitMessageModel> it = mList.iterator();
        while (it.hasNext()) {
            ZIMKitMessageModel model = it.next();
            if (model.isCheck()) {
                if (model instanceof AudioMessageModel) {
                    stopPlayAudio(model);
                }
                it.remove();
            }
        }
        if (ZIMKitCore.getInstance().isSendMessageByServer()) {
            sortAndUpdateListInner();
        } else {
            this.notifyDataSetChanged();
        }
    }

    /**
     * Pause playback when deleting a message containing a audio that is playing
     *
     * @param model
     */
    public void stopPlayAudio(ZIMKitMessageModel model) {
        if (ZIMKitAudioPlayer.getInstance().isPlaying()) {
            if (TextUtils.equals(ZIMKitAudioPlayer.getInstance().getPath(),
                ((AudioMessageModel) model).getFileLocalPath())) {
                ZIMKitAudioPlayer.getInstance().stopPlay();
            }
        }
    }

    /**
     * Add system prompt ；Update the delivery status of messages
     *
     * @param list
     */
    public void updateMessageInfo(List<ZIMKitMessageModel> list) {
        if (mList.isEmpty()) {
            mList.addAll(list);
            if (ZIMKitCore.getInstance().isSendMessageByServer()) {
                sortAndUpdateListInner();
            } else {
                notifyDataSetChanged();
            }
        } else {

            deleteLoadingMessage();

            for (int j = 0; j < list.size(); j++) {
                ZIMKitMessageModel model = list.get(j);
                for (int i = 0; i < mList.size(); i++) {
                    ZIMKitMessageModel messageLocalModel = mList.get(i);
                    if (model.getMessage() != null && messageLocalModel.getMessage() != null) {
                        if (model.getMessage().equals(messageLocalModel.getMessage())
                            || model.getMessage().getLocalMessageID() == messageLocalModel.getMessage()
                            .getLocalMessageID()) {
                            mList.set(i, model);
                            this.notifyItemChanged(i);
                            break;
                        }
                    }
                }
            }
            if (ZIMKitCore.getInstance().isSendMessageByServer()) {
                sortAndUpdateListInner();
            }
        }
    }

    public boolean isOneSideForwardMode() {
        return oneSideForwardMode;
    }

    public boolean isForwardMode() {
        return forwardMode;
    }

    private void deleteLoadingMessage() {
        if (ZIMKitCore.getInstance().isShowLoadingWhenSend()) {
            List<ZIMKitMessageModel> collect = mList.stream()
                .filter(messageModel -> Objects.equals(messageModel.getMessage().localExtendedData, "loading"))
                .collect(Collectors.toList());
            if (!collect.isEmpty()) {
                mList.removeAll(collect);
                notifyDataSetChanged();
            }
        }
        //        List<ZIMMessage> messageList = collect.stream().map(zimKitMessageModel -> zimKitMessageModel.getMessage())
        //            .collect(Collectors.toList());
        //        if (!messageList.isEmpty()) {
        //            ZIMMessage zimMessage = messageList.get(0);
        //            if (zimMessage.getConversationID() != null) {
        //                ZIM.getInstance()
        //                    .deleteMessages(messageList, zimMessage.getConversationID(), zimMessage.getConversationType(),
        //                        new ZIMMessageDeleteConfig(), null);
        //            }
        //        }

    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewDataBinding binding;
        MessageViewHolder viewHolder;
        if (viewType == 999) { // temp error message type
            binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                R.layout.zimkit_item_message_system, parent, false);
            viewHolder = new MessageSystemHolder(binding);
        } else {
            boolean isSend;
            if (oneSideForwardMode) {
                isSend = false;
            } else {
                isSend = (viewType / 1000) == 0; // because send =0,RECEIVE = 1
            }
            int type = (viewType % 1000);
            if (type == ZIMMessageType.TIPS.value()) {
                binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                    R.layout.zimkit_item_message_tips, parent, false);
                viewHolder = new TipsMessageHolder(binding);
            } else if (type == ZIMMessageType.REVOKE.value()) {
                binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                    R.layout.zimkit_item_message_revoke, parent, false);
                viewHolder = new RevokeMessageHolder(binding);
            } else if (type == ZIMMessageType.CUSTOM.value()) {
                binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                    R.layout.zimkit_item_message_custom, parent, false);
                viewHolder = new CustomMessageHolder(binding);
            } else {
                if (isSend) {
                    if (type == ZIMMessageType.TEXT.value()) {
                        binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                            R.layout.zimkit_item_message_send_text, parent, false);
                        viewHolder = new TextMessageHolder(binding);
                    } else if (type == ZIMMessageType.IMAGE.value()) {
                        binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                            R.layout.zimkit_item_message_send_photo, parent, false);
                        viewHolder = new ImageMessageHolder(binding);
                    } else if (type == ZIMMessageType.VIDEO.value()) {
                        binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                            R.layout.zimkit_item_message_send_video, parent, false);
                        viewHolder = new VideoMessageHolder(binding);
                    } else if (type == ZIMMessageType.AUDIO.value()) {
                        //                        if (forwardMode) {
                        //                            binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                        //                                R.layout.zimkit_item_message_send_text, parent, false);
                        //                            viewHolder = new TextMessageHolder(binding);
                        //                        } else {
                        binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                            R.layout.zimkit_item_message_send_audio, parent, false);
                        viewHolder = new AudioMessageHolder(binding);
                        //                        }

                    } else if (type == ZIMMessageType.FILE.value()) {
                        binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                            R.layout.zimkit_item_message_send_file, parent, false);
                        viewHolder = new FileMessageHolder(binding);
                    } else if (type == ZIMMessageType.COMBINE.value()) {
                        binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                            R.layout.zimkit_item_message_send_combine, parent, false);
                        viewHolder = new CombineMessageHolder(binding);
                    } else {
                        binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                            R.layout.zimkit_item_message_send_text, parent, false);
                        viewHolder = new TextMessageHolder(binding);
                    }
                } else {
                    if (type == ZIMMessageType.TEXT.value()) {
                        binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                            R.layout.zimkit_item_message_receive_text, parent, false);
                        viewHolder = new TextMessageHolder(binding);
                    } else if (type == ZIMMessageType.IMAGE.value()) {
                        binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                            R.layout.zimkit_item_message_receive_photo, parent, false);
                        viewHolder = new ImageMessageHolder(binding);
                    } else if (type == ZIMMessageType.VIDEO.value()) {
                        binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                            R.layout.zimkit_item_message_receive_video, parent, false);
                        viewHolder = new VideoMessageHolder(binding);
                    } else if (type == ZIMMessageType.AUDIO.value()) {
                        //                        if (forwardMode) {
                        //                            binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                        //                                R.layout.zimkit_item_message_receive_text, parent, false);
                        //                            viewHolder = new TextMessageHolder(binding);
                        //                        } else {
                        binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                            R.layout.zimkit_item_message_receive_audio, parent, false);
                        viewHolder = new AudioMessageHolder(binding);
                        //                        }
                    } else if (type == ZIMMessageType.FILE.value()) {
                        binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                            R.layout.zimkit_item_message_receive_file, parent, false);
                        viewHolder = new FileMessageHolder(binding);
                    } else if (type == ZIMMessageType.COMBINE.value()) {
                        binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                            R.layout.zimkit_item_message_receive_combine, parent, false);
                        viewHolder = new CombineMessageHolder(binding);
                    } else {
                        binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()),
                            R.layout.zimkit_item_message_receive_text, parent, false);
                        viewHolder = new TextMessageHolder(binding);
                    }
                }
            }
        }
        return viewHolder;
    }

    private static final String TAG = "ZIMKitMessageAdapter";

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ZIMKitMessageModel model = mList.get(holder.getAdapterPosition());
        holder.mAdapter = this;
        holder.bind(BR.model, holder.getAdapterPosition(), model);
        ViewGroup reactionLayout = holder.itemView.findViewById(R.id.msg_reaction_layout);
        AppCompatCheckBox checkBox = holder.itemView.findViewById(R.id.select_checkbox);
        ViewGroup replyLayout = holder.itemView.findViewById(R.id.item_message_reply_layout);
        if (isForwardMode()) {
            if (checkBox != null) {
                checkBox.setVisibility(View.GONE);
            }
            if (reactionLayout != null) {
                reactionLayout.setVisibility(View.GONE);
            }
            if (replyLayout != null) {
                replyLayout.setVisibility(View.GONE);
            }
        } else {
            if (checkBox != null) {
                checkBox.setVisibility(isMultiSelectMode() ? View.VISIBLE : View.GONE);
            }
            if (!isMultiSelectMode()) {
                model.setCheck(false);
            }
        }

    }

    @Override
    public int getItemViewType(int position) {
        ZIMKitMessageModel messageModel = mList.get(position);
        int type = messageModel.getType();
        if (type == 999) { // temp error message type
            return type;
        } else {
            int direction = messageModel.getDirection().value();
            boolean loading = Objects.equals(messageModel.getMessage().localExtendedData, "loading");
            if (loading) {
                direction = ZIMMessageDirection.RECEIVE.value();
            }
            return (direction * 1000) + type;
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }


    public void setMultiSelectMode(boolean show) {
        this.isMultiSelectMode = show;
        notifyDataSetChanged();
    }

    public boolean isMultiSelectMode() {
        return isMultiSelectMode;
    }

    /**
     * Get results for selected items
     *
     * @return
     */
    public ArrayList<ZIMKitMessageModel> getSelectedItem() {
        if (mList == null || mList.size() == 0) {
            return null;
        }
        ArrayList<ZIMKitMessageModel> selectList = new ArrayList<>();
        for (int i = 0; i < mList.size(); i++) {
            if (mList.get(i).isCheck()) {
                selectList.add(mList.get(i));
            }
        }

        return selectList;
    }

    public List<ZIMKitMessageModel> getItemDataList() {
        return mList;
    }

    public ZIMKitMessageModel getItemData(int position) {
        return mList.get(position);
    }

    /**
     * Set the audio playback mode
     *
     * @param isSpeaker
     */
    public static void setAudioPlayByEarPhone(Context context, boolean isSpeaker) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (isSpeaker) {
            // Outgoing mode
            audioManager.setSpeakerphoneOn(true);
            audioManager.setMode(AudioManager.MODE_NORMAL);
        } else {
            // Earpiece mode
            audioManager.setSpeakerphoneOn(false);
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        }
    }

    private final ZIMKitDelegate eventCallBack = new ZIMKitDelegate() {
        @Override
        public void onMediaMessageDownloadingProgressUpdated(ZIMKitMessage message, boolean isFinished) {
            if (isFinished) {
                if (mList.size() > 0) {
                    for (int i = 0; i < mList.size(); i++) {
                        ZIMKitMessageModel messageLocalModel = mList.get(i);
                        if (message.zim.getMessageID() == messageLocalModel.getMessage().getMessageID()) {
                            if (messageLocalModel instanceof AudioMessageModel) {
                                ((AudioMessageModel) messageLocalModel).setFileLocalPath(
                                    message.audioContent.fileLocalPath);
                            } else if (messageLocalModel instanceof VideoMessageModel) {
                                ZIMKitVideoViewActivity.filePath = message.videoContent.fileLocalPath;
                                ((VideoMessageModel) messageLocalModel).setFileLocalPath(
                                    message.videoContent.fileLocalPath);
                            } else if (messageLocalModel instanceof FileMessageModel) {
                                FileMessageModel fileMessageModel = (FileMessageModel) messageLocalModel;
                                fileMessageModel.setFileLocalPath(message.fileContent.fileLocalPath);
                                if (fileMessageModel.isSizeLimit() && fileMessageModel.getMessage() != null) {
                                    ZIMKitMessageManager.share()
                                        .removeLimitFile(fileMessageModel.getMessage().getMessageID());
                                }
                            }
                        }
                    }
                }
            }
        }
    };

    private void sortAndUpdateListInner() {
        Collections.sort(mList, new Comparator<ZIMKitMessageModel>() {
            @Override
            public int compare(ZIMKitMessageModel o1, ZIMKitMessageModel o2) {
                if (Objects.equals(o1.getMessage().localExtendedData, "loading")) {
                    return 1;
                }
                if (Objects.equals(o2.getMessage().localExtendedData, "loading")) {
                    return 1;
                }
                return (int) (o1.getMessage().getTimestamp() - o2.getMessage().getTimestamp());
            }
        });
        notifyDataSetChanged();
    }

    public void clear() {
        ZIMKit.unRegisterZIMKitDelegate(eventCallBack);
    }

}
