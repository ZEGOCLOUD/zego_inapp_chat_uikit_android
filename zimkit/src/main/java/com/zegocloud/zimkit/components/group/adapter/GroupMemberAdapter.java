package com.zegocloud.zimkit.components.group.adapter;

import android.text.TextUtils;
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
import com.zegocloud.zimkit.components.group.bean.ZIMKitGroupMemberInfo;
import java.util.ArrayList;
import java.util.List;

public class GroupMemberAdapter extends RecyclerView.Adapter<ViewHolder> {

    private List<ZIMKitGroupMemberInfo> memberList = new ArrayList<>();


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
        ZIMKitGroupMemberInfo groupMember = memberList.get(position);
        ImageView memberIcon = holder.itemView.findViewById(R.id.member_icon);
        TextView memberName = holder.itemView.findViewById(R.id.member_name);
        ZIMKitGlideLoader.displayMessageAvatarImage(memberIcon, groupMember.getAvatarUrl());
        if (TextUtils.isEmpty(groupMember.getNickName())) {
            memberName.setText(groupMember.getName());
        } else {
            memberName.setText(groupMember.getNickName());
        }
    }

    @Override
    public int getItemCount() {
        return memberList.size();
    }

    public void setMemberList(List<ZIMKitGroupMemberInfo> memberList) {
        this.memberList.clear();
        this.memberList.addAll(memberList);
        notifyDataSetChanged();
    }

    public ZIMKitGroupMemberInfo getItemData(int position) {
        return memberList.get(position);
    }

    public static int dp2px(float v, DisplayMetrics displayMetrics) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, displayMetrics);
    }
}
