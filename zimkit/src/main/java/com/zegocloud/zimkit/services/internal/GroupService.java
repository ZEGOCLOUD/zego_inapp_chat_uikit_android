package com.zegocloud.zimkit.services.internal;

import android.text.TextUtils;
import com.zegocloud.zimkit.components.group.bean.ZIMKitGroupMemberInfo;
import com.zegocloud.zimkit.services.callback.CreateGroupCallback;
import com.zegocloud.zimkit.services.callback.InviteUsersToJoinGroupCallback;
import com.zegocloud.zimkit.services.callback.JoinGroupCallback;
import com.zegocloud.zimkit.services.callback.LeaveGroupCallback;
import com.zegocloud.zimkit.services.callback.QueryGroupInfoCallback;
import com.zegocloud.zimkit.services.callback.QueryGroupMemberInfoCallback;
import com.zegocloud.zimkit.services.callback.QueryGroupMemberListCallback;
import com.zegocloud.zimkit.services.model.GroupMemberRole;
import com.zegocloud.zimkit.services.model.ZIMKitGroupInfo;
import im.zego.zim.callback.ZIMGroupMemberListQueriedCallback;
import im.zego.zim.entity.ZIMError;
import im.zego.zim.entity.ZIMGroupFullInfo;
import im.zego.zim.entity.ZIMGroupInfo;
import im.zego.zim.entity.ZIMGroupMemberInfo;
import im.zego.zim.entity.ZIMGroupMemberQueryConfig;
import java.util.ArrayList;
import java.util.List;

public class GroupService {

    public void createGroup(String groupName, List<String> inviteUserIDs, CreateGroupCallback callback) {
        createGroup(groupName, "", inviteUserIDs, callback);
    }

    public void createGroup(String groupName, String groupId, List<String> inviteUserIDs,
        CreateGroupCallback callback) {
        ZIMGroupInfo info = new ZIMGroupInfo();
        info.groupName = groupName;
        if (!TextUtils.isEmpty(groupId)) {
            info.groupID = groupId;
        }
        ZIMKitCore.getInstance().zim()
            .createGroup(info, inviteUserIDs, (groupInfo, userList, errorUserList, errorInfo) -> {
                if (callback != null) {
                    callback.onCreateGroup(transGroupInfo(groupInfo), errorUserList, errorInfo);
                }
            });
    }

    public void joinGroup(String groupID, JoinGroupCallback callback) {
        ZIMKitCore.getInstance().zim().joinGroup(groupID, (groupInfo, errorInfo) -> {
            if (callback != null) {
                callback.onJoinGroup(transGroupInfo(groupInfo), errorInfo);
            }
        });
    }

    public void leaveGroup(String groupID, LeaveGroupCallback callback) {
        ZIMKitCore.getInstance().zim().leaveGroup(groupID, (groupID1, errorInfo) -> {
            if (callback != null) {
                callback.onLeaveGroup(errorInfo);
            }
        });
    }

    public void inviteUsersToJoinGroup(List<String> userIDs, String groupID, InviteUsersToJoinGroupCallback callback) {
        ZIMKitCore.getInstance().zim()
            .inviteUsersIntoGroup(userIDs, groupID, (groupID1, userList, errorUserList, errorInfo) -> {
                if (callback != null) {
                    ArrayList<ZIMKitGroupMemberInfo> groupMembers = new ArrayList<>();
                    for (ZIMGroupMemberInfo info : userList) {
                        groupMembers.add(transGroupMember(info));
                    }
                    callback.onInviteUsersToJoinGroup(groupMembers, errorUserList, errorInfo);
                }
            });
    }

    public void queryGroupInfo(String groupID, QueryGroupInfoCallback callback) {
        ZIMKitCore.getInstance().zim().queryGroupInfo(groupID, (groupInfo, errorInfo) -> {
            if (callback != null) {
                callback.onQueryGroupInfo(transGroupInfo(groupInfo), errorInfo);
            }
        });
    }

    public void queryGroupMemberInfo(String userID, String groupID, QueryGroupMemberInfoCallback callback) {
        ZIMKitCore.getInstance().zim().queryGroupMemberInfo(userID, groupID, (groupID1, userInfo, errorInfo) -> {
            if (callback != null) {
                callback.onQueryGroupMemberInfo(transGroupMember(userInfo), errorInfo);
            }
        });
    }

    public void queryGroupMemberList(String groupID, ZIMGroupMemberQueryConfig config,
        QueryGroupMemberListCallback callback) {
        ZIMKitCore.getInstance().zim().queryGroupMemberList(groupID, config, new ZIMGroupMemberListQueriedCallback() {
            @Override
            public void onGroupMemberListQueried(String groupID, ArrayList<ZIMGroupMemberInfo> userList, int nextFlag,
                ZIMError errorInfo) {
                if (callback != null) {
                    ArrayList<ZIMKitGroupMemberInfo> list = new ArrayList<>();
                    for (ZIMGroupMemberInfo zimGroupMemberInfo : userList) {
                        list.add(transGroupMember(zimGroupMemberInfo));
                    }
                    callback.onGroupMemberListQueried(groupID, list, nextFlag, errorInfo);
                }
            }
        });
    }

    public static ZIMKitGroupInfo transGroupInfo(ZIMGroupFullInfo groupInfo) {
        ZIMKitGroupInfo zimKitGroupInfo = new ZIMKitGroupInfo();
        if (groupInfo == null || groupInfo.baseInfo == null) {
            return zimKitGroupInfo;
        }
        zimKitGroupInfo.setId(groupInfo.baseInfo.groupID);
        zimKitGroupInfo.setName(groupInfo.baseInfo.groupName);
        zimKitGroupInfo.setAvatarUrl(groupInfo.baseInfo.groupAvatarUrl);
        return zimKitGroupInfo;
    }

    public static ZIMKitGroupMemberInfo transGroupMember(ZIMGroupMemberInfo info) {
        ZIMKitGroupMemberInfo member = new ZIMKitGroupMemberInfo();
        if (info == null) {
            return member;
        }
        member.setId(info.userID);
        member.setName(info.userName);
        member.setNickName(info.memberNickname);
        member.setAvatarUrl(info.userAvatarUrl);
        member.setRole(info.memberRole == 1 ? GroupMemberRole.OWNER : GroupMemberRole.MEMBER);
        return member;
    }
}
