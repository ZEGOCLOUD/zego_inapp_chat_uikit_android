package com.zegocloud.zimkit.components.message.adapter;

import android.content.Context;
import android.media.AudioManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

import com.zegocloud.zimkit.R;
import com.zegocloud.zimkit.components.message.widget.interfaces.OnItemClickListener;
import com.zegocloud.zimkit.components.message.widget.viewholder.AudioMessageHolder;
import com.zegocloud.zimkit.components.message.widget.viewholder.FileMessageHolder;
import com.zegocloud.zimkit.components.message.widget.viewholder.ImageMessageHolder;
import com.zegocloud.zimkit.components.message.widget.viewholder.MessageSystemHolder;
import com.zegocloud.zimkit.components.message.widget.viewholder.MessageViewHolder;
import com.zegocloud.zimkit.components.message.widget.viewholder.TextMessageHolder;
import com.zegocloud.zimkit.components.message.widget.viewholder.VideoMessageHolder;
import com.zegocloud.zimkit.services.ZIMKit;
import com.zegocloud.zimkit.services.ZIMKitDelegate;
import com.zegocloud.zimkit.services.model.ZIMKitMessage;
import com.zegocloud.zimkit.BR;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import im.zego.zim.entity.ZIMMessage;
import im.zego.zim.enums.ZIMMessageType;
import com.zegocloud.zimkit.components.message.ZIMKitMessageManager;
import com.zegocloud.zimkit.components.message.model.AudioMessageModel;
import com.zegocloud.zimkit.components.message.model.FileMessageModel;
import com.zegocloud.zimkit.components.message.model.SystemMessageModel;
import com.zegocloud.zimkit.components.message.model.VideoMessageModel;
import com.zegocloud.zimkit.components.message.model.ZIMKitMessageModel;
import com.zegocloud.zimkit.components.message.ui.ZIMKitVideoViewActivity;
import com.zegocloud.zimkit.components.message.widget.ZIMKitAudioPlayer;

public class ZIMKitMessageAdapter extends RecyclerView.Adapter<MessageViewHolder> {

    private OnItemClickListener mOnItemClickListener;

    private final List<ZIMKitMessageModel> mList = new ArrayList<>();

    public List<ZIMKitMessageModel> getData() {
        return mList;
    }

    protected boolean isShowMultiSelectCheckBox = false;
    private Context context;

    public ZIMKitMessageAdapter(Context context) {
        this.context = context;
        ZIMKit.registerZIMKitDelegate(eventCallBack);
    }

    public void setNewList(List<ZIMKitMessageModel> list) {
        if (mList.size() == list.size()) {
            mList.clear();
            mList.addAll(list);
            this.notifyItemRangeChanged(0, list.size());
            return;
        }
        if (mList.size() > 0) {
            int count = mList.size();
            mList.clear();
            this.notifyItemRangeRemoved(0, count);
        }
        if (list.size() > 0) {
            mList.addAll(list);
            this.notifyItemRangeInserted(0, list.size());
        }
    }

    public void addListToTop(List<ZIMKitMessageModel> list) {
        mList.addAll(0, list);
        this.notifyItemRangeInserted(0, list.size());
    }

    public void addListToBottom(List<ZIMKitMessageModel> list) {
        int oldCount = mList.size();
        mList.addAll(list);
        this.notifyItemRangeInserted(oldCount, list.size());
    }

    public void addLocalMessageToBottom(ZIMKitMessageModel model) {
        if (model == null) {
            return;
        }
        int oldCount = mList.size();
        mList.add(model);
        this.notifyItemRangeInserted(oldCount, 1);
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
        this.notifyItemRemoved(index);
        this.notifyDataSetChanged();
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
        this.notifyDataSetChanged();
    }

    /**
     * Pause playback when deleting a message containing a audio that is playing
     *
     * @param model
     */
    public void stopPlayAudio(ZIMKitMessageModel model) {
        if (ZIMKitAudioPlayer.getInstance().isPlaying()) {
            if (TextUtils.equals(ZIMKitAudioPlayer.getInstance().getPath(), ((AudioMessageModel) model).getFileLocalPath())) {
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
        for (int j = 0; j < list.size(); j++) {
            ZIMKitMessageModel model = list.get(j);
            if (model instanceof SystemMessageModel) {
                addLocalMessageToBottom(model);
            } else {
                if (mList.size() > 0) {
                    for (int i = 0; i < mList.size(); i++) {
                        ZIMKitMessageModel messageLocalModel = mList.get(i);
                        if (model.getMessage() != null && messageLocalModel.getMessage() != null) {
                            if (model.getMessage().equals(messageLocalModel.getMessage()) || model.getMessage().getLocalMessageID() == messageLocalModel.getMessage().getLocalMessageID()) {
                                mList.set(i, model);
                                this.notifyItemChanged(i);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewDataBinding binding;
        MessageViewHolder viewHolder;
        if (viewType == 99) { // temp error message type
            binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.zimkit_item_message_system, parent, false);
            viewHolder = new MessageSystemHolder(binding);
        } else {
            boolean isSend = (viewType / 100) == 0; // because send =0,RECEIVE = 1
            int type = (viewType % 100);
            if (isSend) {
                if (type == ZIMMessageType.TEXT.value()) {
                    binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.zimkit_item_message_send_text, parent, false);
                    viewHolder = new TextMessageHolder(binding);
                } else if (type == ZIMMessageType.IMAGE.value()) {
                    binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.zimkit_item_message_send_photo, parent, false);
                    viewHolder = new ImageMessageHolder(binding);
                } else if (type == ZIMMessageType.VIDEO.value()) {
                    binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.zimkit_item_message_send_video, parent, false);
                    viewHolder = new VideoMessageHolder(binding);
                } else if (type == ZIMMessageType.AUDIO.value()) {
                    binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.zimkit_item_message_send_audio, parent, false);
                    viewHolder = new AudioMessageHolder(binding);
                } else if (type == ZIMMessageType.FILE.value()) {
                    binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.zimkit_item_message_send_file, parent, false);
                    viewHolder = new FileMessageHolder(binding);
                } else {
                    binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.zimkit_item_message_send_text, parent, false);
                    viewHolder = new TextMessageHolder(binding);
                }
            } else {
                if (type == ZIMMessageType.TEXT.value()) {
                    binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.zimkit_item_message_receive_text, parent, false);
                    viewHolder = new TextMessageHolder(binding);
                } else if (type == ZIMMessageType.IMAGE.value()) {
                    binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.zimkit_item_message_receive_photo, parent, false);
                    viewHolder = new ImageMessageHolder(binding);
                } else if (type == ZIMMessageType.VIDEO.value()) {
                    binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.zimkit_item_message_receive_video, parent, false);
                    viewHolder = new VideoMessageHolder(binding);
                } else if (type == ZIMMessageType.AUDIO.value()) {
                    binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.zimkit_item_message_receive_audio, parent, false);
                    viewHolder = new AudioMessageHolder(binding);
                } else if (type == ZIMMessageType.FILE.value()) {
                    binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.zimkit_item_message_receive_file, parent, false);
                    viewHolder = new FileMessageHolder(binding);
                } else {
                    binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.zimkit_item_message_receive_text, parent, false);
                    viewHolder = new TextMessageHolder(binding);
                }
            }
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ZIMKitMessageModel model = mList.get(holder.getAdapterPosition());
        model.setShowMultiSelectCheckBox(isShowMultiSelectCheckBox);
        if (!isShowMultiSelectCheckBox && model.isCheck()) {
            model.setCheck(false);
        }
        holder.setContext(context);
        holder.isMultiSelectMode = isShowMultiSelectCheckBox;
        holder.mAdapter = this;
        holder.bind(BR.model, holder.getAdapterPosition(), model);
        holder.setOnItemClickListener(mOnItemClickListener);

        setCheckBoxStatus(position, model, holder);

    }

    @Override
    public int getItemViewType(int position) {
        int type = mList.get(position).getType();
        if (type == 99) { // temp error message type
            return type;
        } else {
            int direction = mList.get(position).getDirection().value();
            return (direction * 100) + type;
        }
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    private void setCheckBoxStatus(final int position, ZIMKitMessageModel model, MessageViewHolder holder) {
        if (isShowMultiSelectCheckBox) {
            //The checkBox listener
            if (holder.mMutiSelectCheckBox != null) {
                holder.mMutiSelectCheckBox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        model.setCheck(!model.isCheck());
                    }
                });
            }

            //Listening to the view of an entry
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    model.setCheck(!model.isCheck());
                }
            });

            if (holder.msgContent != null) {
                holder.msgContent.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        model.setCheck(!model.isCheck());
                    }
                });
            }

        }
    }

    public void setShowMultiSelectCheckBox(boolean show) {
        this.isShowMultiSelectCheckBox = show;
    }

    public OnItemClickListener getOnItemClickListener() {
        return this.mOnItemClickListener;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    /**
     * Get results for selected items
     *
     * @return
     */
    public ArrayList<ZIMMessage> getSelectedItem() {
        if (mList == null || mList.size() == 0) {
            return null;
        }
        ArrayList<ZIMMessage> selectList = new ArrayList<>();
        for (int i = 0; i < mList.size(); i++) {
            if (mList.get(i).isCheck()) {
                selectList.add(mList.get(i).getMessage());
            }
        }

        return selectList;
    }

    /**
     * Set the audio playback mode
     *
     * @param isSpeaker
     */
    public void setAudioPlayByEarPhone(boolean isSpeaker) {
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
                                ((AudioMessageModel) messageLocalModel).setFileLocalPath(message.audioContent.fileLocalPath);
                            } else if (messageLocalModel instanceof VideoMessageModel) {
                                ZIMKitVideoViewActivity.filePath = message.videoContent.fileLocalPath;
                                ((VideoMessageModel) messageLocalModel).setFileLocalPath(message.videoContent.fileLocalPath);
                            } else if (messageLocalModel instanceof FileMessageModel) {
                                FileMessageModel fileMessageModel = (FileMessageModel) messageLocalModel;
                                fileMessageModel.setFileLocalPath(message.fileContent.fileLocalPath);
                                if (fileMessageModel.isSizeLimit() && fileMessageModel.getMessage() != null) {
                                    ZIMKitMessageManager.share().removeLimitFile(fileMessageModel.getMessage().getMessageID());
                                }
                            }
                        }
                    }
                }
            }
        }
    };

    public void clear() {
        ZIMKit.unRegisterZIMKitDelegate(eventCallBack);
    }

}
