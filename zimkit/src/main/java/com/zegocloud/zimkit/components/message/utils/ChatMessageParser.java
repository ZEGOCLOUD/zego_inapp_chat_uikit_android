package com.zegocloud.zimkit.components.message.utils;

import com.zegocloud.zimkit.components.message.model.SystemMessageModel;
import com.zegocloud.zimkit.components.message.model.RevokeMessageModel;
import com.zegocloud.zimkit.components.message.model.ZIMKitMessageModel;
import java.util.ArrayList;
import java.util.List;

import im.zego.zim.entity.ZIMMessage;
import im.zego.zim.enums.ZIMMessageType;
import com.zegocloud.zimkit.R;
import com.zegocloud.zimkit.components.message.model.AudioMessageModel;
import com.zegocloud.zimkit.components.message.model.FileMessageModel;
import com.zegocloud.zimkit.components.message.model.ImageMessageModel;
import com.zegocloud.zimkit.components.message.model.TextMessageModel;
import com.zegocloud.zimkit.components.message.model.VideoMessageModel;
import com.zegocloud.zimkit.services.internal.ZIMKitCore;

public class ChatMessageParser {

    /**
     * Convert to the corresponding message type
     *
     * @param zimMessage
     * @return
     */
    public static ZIMKitMessageModel parseMessage(ZIMMessage zimMessage) {
        if (zimMessage == null) {
            return null;
        }
        ZIMKitMessageModel message = null;
        ZIMMessageType msgType = zimMessage.getType();
        switch (msgType) {
            case TEXT:
                message = new TextMessageModel();
                break;
            case IMAGE:
                message = new ImageMessageModel();
                break;
            case VIDEO:
                message = new VideoMessageModel();
                break;
            case AUDIO:
                message = new AudioMessageModel();
                break;
            case FILE:
                message = new FileMessageModel();
                break;
            case SYSTEM:
                message = new SystemMessageModel();
                break;
            case REVOKE:
                message = new RevokeMessageModel();
                break;
            default:
                message = new TextMessageModel();
                ((TextMessageModel) message).setContent(
                    ZIMKitCore.getInstance().getApplication().getString(R.string.zimkit_message_unknown));
                break;
        }

        if (message != null) {
            message.setCommonAttribute(zimMessage);
            message.onProcessMessage(zimMessage);
        }
        return message;
    }

    /**
     * Convert the IMSDK message bean list to the ZIMKIT message bean list
     *
     * @param zimMessageList List of IMSDK message beans
     * @return List of converted ZIMKIT bean
     */
    public static List<ZIMKitMessageModel> parseMessageList(List<ZIMMessage> zimMessageList) {
        if (zimMessageList == null) {
            return null;
        }
        List<ZIMKitMessageModel> messageList = new ArrayList<>();
        for (int i = 0; i < zimMessageList.size(); i++) {
            ZIMMessage timMessage = zimMessageList.get(i);
            ZIMKitMessageModel message = parseMessage(timMessage);
            if (message instanceof RevokeMessageModel) {
                continue;
            }
            if (message != null) {
                messageList.add(message);
            }
        }
        return messageList;
    }

}
