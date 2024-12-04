package com.zegocloud.zimkit.components.message.widget.chatpop;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.zegocloud.zimkit.R;
import java.util.ArrayList;
import java.util.List;

public class ChatPopActionAdapter extends RecyclerView.Adapter<ChatPopActionAdapter.MenuItemViewHolder> {

    List<ChatPopAction> chatPopMenuActionList = new ArrayList<>();

    @NonNull
    @Override
    public MenuItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.zimkit_layout_message_pop_menu_item, null);
        return new MenuItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuItemViewHolder holder, int position) {
        ChatPopAction chatPopMenuAction = chatPopMenuActionList.get(position);
        holder.title.setText(chatPopMenuAction.getActionName());
        Drawable drawable = ResourcesCompat.getDrawable(holder.itemView.getResources(),
            chatPopMenuAction.getActionIcon(), null);
        holder.icon.setImageDrawable(drawable);
    }

    @Override
    public int getItemCount() {
        return chatPopMenuActionList.size();
    }

    public ChatPopAction getAction(int index) {
        return chatPopMenuActionList.get(index);
    }

    public void setChatPopMenuActionList(List<ChatPopAction> chatPopMenuActionList) {
        this.chatPopMenuActionList = chatPopMenuActionList;
        notifyDataSetChanged();
    }

    static class MenuItemViewHolder extends RecyclerView.ViewHolder {

        public TextView title;
        public ImageView icon;

        public MenuItemViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.menu_title);
            icon = itemView.findViewById(R.id.menu_icon);
        }
    }
}
