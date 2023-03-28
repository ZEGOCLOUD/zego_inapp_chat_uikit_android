package com.zegocloud.zimkit.services.internal.interfaces;

import com.zegocloud.zimkit.services.callback.InviteUsersToJoinGroupCallback;
import com.zegocloud.zimkit.services.callback.LeaveGroupCallback;
import com.zegocloud.zimkit.services.callback.QueryGroupInfoCallback;
import java.util.List;

import com.zegocloud.zimkit.services.callback.CreateGroupCallback;
import com.zegocloud.zimkit.services.callback.JoinGroupCallback;
import com.zegocloud.zimkit.services.callback.QueryGroupMemberInfoCallback;

public interface IGroupService {

    void createGroup(String groupName, List<String> inviteUserIDs, CreateGroupCallback callback);

    void createGroup(String groupName,String groupId, List<String> inviteUserIDs, CreateGroupCallback callback);

    void joinGroup(String groupID, JoinGroupCallback callback);

    void leaveGroup(String groupID, LeaveGroupCallback callback);

    void inviteUsersToJoinGroup(List<String> userIDs, String groupID, InviteUsersToJoinGroupCallback callback);

    void queryGroupInfo(String groupID, QueryGroupInfoCallback callback);

    void queryGroupMemberInfo(String userID, String groupID, QueryGroupMemberInfoCallback callback);

}
