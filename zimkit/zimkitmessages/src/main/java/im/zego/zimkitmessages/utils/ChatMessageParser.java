package im.zego.zimkitmessages.utils;

import java.util.ArrayList;
import java.util.List;

import im.zego.zim.entity.ZIMMessage;
import im.zego.zim.enums.ZIMMessageType;
import im.zego.zimkitcommon.ZIMKitManager;
import im.zego.zimkitmessages.model.message.AudioMessageModel;
import im.zego.zimkitmessages.model.message.FileMessageModel;
import im.zego.zimkitmessages.model.message.ImageMessageModel;
import im.zego.zimkitmessages.model.message.TextMessageModel;
import im.zego.zimkitmessages.model.message.VideoMessageModel;
import im.zego.zimkitmessages.model.message.ZIMKitMessageModel;

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
            default:
                message = new TextMessageModel();
                ((TextMessageModel) message).setContent(ZIMKitManager.share().getApplication().getString(im.zego.zimkitcommon.R.string.common_message_unknown));
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
            if (message != null) {
                messageList.add(message);
            }
        }
        return messageList;
    }

}
