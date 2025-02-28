package com.zegocloud.zimkit.services.config.conversation;

import android.view.ViewGroup;
import androidx.annotation.NonNull;
import im.zego.zim.entity.ZIMConversation;

public interface ConversationItemDecor {

    void onCreateViewHolder(@NonNull ViewGroup itemView, int viewType);

    void onBindViewHolder(@NonNull ViewGroup itemView, ZIMConversation conversation, int position);
}
