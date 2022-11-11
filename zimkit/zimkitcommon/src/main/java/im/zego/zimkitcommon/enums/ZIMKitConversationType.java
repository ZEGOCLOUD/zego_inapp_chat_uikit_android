package im.zego.zimkitcommon.enums;

/**
 * Session type
 */
public enum ZIMKitConversationType {

    ZIMKitConversationTypePeer(0),  //One-on-one chat
    ZIMKitConversationTypeGroup(2); //Group chat

    private int value;

    private ZIMKitConversationType(int value) {
        this.value = value;
    }

    public int value() {
        return this.value;
    }

}
