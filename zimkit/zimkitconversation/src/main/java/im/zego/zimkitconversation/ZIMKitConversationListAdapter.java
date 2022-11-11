package im.zego.zimkitconversation;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import im.zego.zimkitcommon.adapter.BaseDifferRvAdapter;
import im.zego.zimkitconversation.databinding.ConversationItemBinding;
import im.zego.zimkitconversation.model.ZIMKitConversationModel;

public class ZIMKitConversationListAdapter extends BaseDifferRvAdapter<ZIMKitConversationListAdapter.ConversationItemViewHolder, ZIMKitConversationModel> {
    @Override
    protected boolean itemsTheSame(@NonNull ZIMKitConversationModel oldItem, @NonNull ZIMKitConversationModel newItem) {
        return oldItem.getConversation().conversationID.equals(newItem.getConversation().conversationID);
    }

    @Override
    protected boolean contentsTheSame(@NonNull ZIMKitConversationModel oldItem, @NonNull ZIMKitConversationModel newItem) {
        return oldItem.getConversation().unreadMessageCount == newItem.getConversation().unreadMessageCount
                && oldItem.getConversation().conversationName.equals(newItem.getConversation().conversationName)
                && (oldItem.getConversation().lastMessage == null ? 0 : oldItem.getConversation().lastMessage.getTimestamp())
                == (newItem.getConversation().lastMessage == null ? 0 : newItem.getConversation().lastMessage.getTimestamp())
                && oldItem.getSendState() == newItem.getSendState();
    }

    @Override
    protected void onBind(ConversationItemViewHolder holder, ZIMKitConversationModel model, int position) {
        holder.bind(model);
        holder.mBinding.getRoot().setOnLongClickListener(v -> {
            if (mLongClickListener != null) {
                mLongClickListener.onLongClick(model);
            }
            return true;
        });
        holder.mBinding.getRoot().setOnClickListener(v -> {
            if (mOnClickListener != null) {
                mOnClickListener.onClick(model);
            }
        });
    }

    @Override
    protected ConversationItemViewHolder getHolder(@NonNull ViewGroup parent, int position) {
        ConversationItemBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.conversation_item, parent, false);
        return new ConversationItemViewHolder(binding);
    }

    public interface ILongClickListener {
        void onLongClick(ZIMKitConversationModel model);
    }

    public interface IOnItemClickListener {
        void onClick(ZIMKitConversationModel model);
    }

    private ILongClickListener mLongClickListener;
    private IOnItemClickListener mOnClickListener;

    public void setLongClickListener(ILongClickListener listener) {
        mLongClickListener = listener;
    }

    public void setItemClickListener(IOnItemClickListener itemClickListener) {
        mOnClickListener = itemClickListener;
    }

    public static class ConversationItemViewHolder extends RecyclerView.ViewHolder {
        private final ConversationItemBinding mBinding;

        public ConversationItemViewHolder(ConversationItemBinding binding) {
            super(binding.getRoot());
            mBinding = binding;
        }

        public void bind(ZIMKitConversationModel model) {
            mBinding.setItemModel(model);
            mBinding.executePendingBindings();
        }
    }
}
