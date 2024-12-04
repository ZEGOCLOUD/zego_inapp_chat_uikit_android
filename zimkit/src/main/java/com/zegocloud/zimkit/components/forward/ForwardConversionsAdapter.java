package com.zegocloud.zimkit.components.forward;

import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import com.zegocloud.zimkit.R;
import com.zegocloud.zimkit.common.glide.ZIMKitGlideLoader;
import com.zegocloud.zimkit.services.model.ZIMKitConversation;
import java.util.ArrayList;
import java.util.List;

public class ForwardConversionsAdapter extends RecyclerView.Adapter<ViewHolder> {

    private List<ZIMKitConversation> conversationList = new ArrayList<>();


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = View.inflate(parent.getContext(), R.layout.zimkit_item_group_member, null);
        DisplayMetrics displayMetrics = parent.getResources().getDisplayMetrics();
        inflate.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, dp2px(68, displayMetrics)));
        return new ViewHolder(inflate) {
        };
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ZIMKitConversation conversation = conversationList.get(position);
        ImageView memberIcon = holder.itemView.findViewById(R.id.member_icon);
        TextView memberName = holder.itemView.findViewById(R.id.member_name);
        ZIMKitGlideLoader.displayMessageAvatarImage(memberIcon, conversation.getAvatarUrl());
        memberName.setText(conversation.getName());
        ZIMKitGlideLoader.displayConversationAvatarImage(memberIcon, conversation.getAvatarUrl(), conversation.getType());
    }

    @Override
    public int getItemCount() {
        return conversationList.size();
    }

    public void setConversationList(List<ZIMKitConversation> conversationList) {
        this.conversationList.clear();
        this.conversationList.addAll(conversationList);
        notifyDataSetChanged();
    }

    public ZIMKitConversation getItemData(int position) {
        return conversationList.get(position);
    }

    public static int dp2px(float v, DisplayMetrics displayMetrics) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, displayMetrics);
    }
}
