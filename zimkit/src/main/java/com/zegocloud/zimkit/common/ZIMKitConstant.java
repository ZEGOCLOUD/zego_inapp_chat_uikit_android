package com.zegocloud.zimkit.common;

public class ZIMKitConstant {

    public static class EventConstant {
        /**
         * event key
         */
        public static final String KEY_RECEIVE_PEER_MESSAGE = "receivePeerMessage";
        public static final String KEY_RECEIVE_GROUP_MESSAGE = "receiveGroupMessage";
        public static final String KEY_CONNECTION_STATE_CHANGED = "connectionStateChanged";
        public static final String KEY_CONVERSATION_CHANGED = "conversationChanged";
        public static final String KEY_CONVERSATION_TOTAL_UNREAD_MESSAGE_COUNT_UPDATED = "conversationTotalUnreadMessageCountUpdated";
        public static final String KEY_GROUP_MEMBER_STATE_CHANGED = "groupMemberStateChanged";

        /**
         * event param
         */
        public static final String PARAM_MESSAGE_LIST = "messageList";
        public static final String PARAM_FROM_USER_ID = "fromUserID";
        public static final String PARAM_FROM_GROUP_ID = "fromGroupID";
        public static final String PARAM_USER_LIST = "userList";
        public static final String PARAM_GROUP_OPERATED_INFO = "groupOperatedInfo";
        public static final String PARAM_GROUP_ID = "groupId";
        public static final String PARAM_STATE = "state";
        public static final String PARAM_EVENT = "event";
        public static final String PARAM_EXTENDED_DATA = "extendedData";
        public static final String PARAM_CONVERSATION_CHANGE_INFO_LIST = "conversationChangeInfoList";
        public static final String PARAM_TOTAL_UNREAD_MESSAGE_COUNT = "totalUnreadMessageCount";
    }

    public static class RouterConstant {
        public static final String KEY_BUNDLE = "key_bundle";
        public static final String ROUTER_CONVERSATION = "ZIMKitConversationActivity";
        public static final String ROUTER_CREATE_AND_JOIN_GROUP = "ZIMKitCreateAndJoinGroupActivity";
        public static final String ROUTER_MESSAGE = "ZIMKitMessageActivity";
        public static final String ROUTER_GROUP_MANAGER = "ZIMKitGroupManagerActivity";
        public static final String ROUTER_CREATE_SINGLE_CHAT = "ZIMKitCreateSingleChatActivity";
    }

    public static class MessagePageConstant {
        public static final String KEY_TITLE = "key_title";
        public static final String KEY_TYPE = "key_type";
        public static final String KEY_ID = "key_id";
        public static final String KEY_AVATAR = "key_avatar";
        public static final String KEY_PUSH = "key_push";
        public static final String TYPE_SINGLE_MESSAGE = "single_message";
        public static final String TYPE_GROUP_MESSAGE = "group_message";
    }

    public static class GroupPageConstant {
        public static final String KEY_TITLE = "key_title";
        public static final String KEY_ID = "key_id";
        public static final String KEY_TYPE = "key_type";
        public static final String TYPE_CREATE_GROUP_MESSAGE = "create_group_message";
        public static final String TYPE_JOIN_GROUP_MESSAGE = "join_group_message";
    }

    public static class VideoPageConstant {
        public static final String KEY_VIDEO_PATH = "key_video_path";
    }

}
