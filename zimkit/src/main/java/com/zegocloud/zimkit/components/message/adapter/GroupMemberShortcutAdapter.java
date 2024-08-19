package com.zegocloud.zimkit.components.message.adapter;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import com.zegocloud.zimkit.R;
import com.zegocloud.zimkit.common.glide.ZIMKitGlideLoader;
import com.zegocloud.zimkit.components.group.bean.ZIMKitGroupMemberInfo;
import java.util.ArrayList;
import java.util.List;

public class GroupMemberShortcutAdapter extends RecyclerView.Adapter<ViewHolder> {

    private List<ZIMKitGroupMemberInfo> memberList = new ArrayList<>();
    private static final ZIMKitGroupMemberInfo ADD = new ZIMKitGroupMemberInfo();


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = View.inflate(parent.getContext(), R.layout.item_small_group_member, null);
        return new ViewHolder(inflate) {
        };
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ZIMKitGroupMemberInfo groupMember = memberList.get(position);
        ImageView memberIcon = holder.itemView.findViewById(R.id.member_icon);
        TextView memberName = holder.itemView.findViewById(R.id.member_name);
        if (groupMember != ADD) {
            ZIMKitGlideLoader.displayMessageAvatarImage(memberIcon, groupMember.getAvatarUrl());
            if (TextUtils.isEmpty(groupMember.getNickName())) {
                memberName.setText(groupMember.getName());
            } else {
                memberName.setText(groupMember.getNickName());
            }
        } else {
            memberIcon.setImageResource(R.drawable.zimkit_icon_member_add);
            memberName.setText(R.string.invite);
        }
    }

    @Override
    public int getItemCount() {
        return memberList.size();
    }

    public void setMemberList(List<ZIMKitGroupMemberInfo> memberList) {
        this.memberList.clear();
        this.memberList.addAll(memberList);
        if (this.memberList.size() > 9) {
            this.memberList = this.memberList.subList(0, 9);
        }
        this.memberList.add(ADD);
        notifyDataSetChanged();
    }

    public ZIMKitGroupMemberInfo getItemData(int position) {
        return memberList.get(position);
    }
}
