package com.zegocloud.zimkit.components.message.widget.chatpop;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import com.zegocloud.zimkit.R;
import java.util.List;

public class ChatPopEmojiAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<String> emojis;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.zimkit_layout_message_pop_emoji_item, null);
        return new ViewHolder(view) {
        };
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TextView textView = holder.itemView.findViewById(R.id.chat_pop_emoji);
        textView.setText(emojis.get(position));
    }

    @Override
    public int getItemCount() {
        return emojis.size();
    }

    public void setEmojis(List<String> emojis) {
        this.emojis = emojis;
    }

    public String getEmoji(int position) {
        return emojis.get(position);
    }
}
