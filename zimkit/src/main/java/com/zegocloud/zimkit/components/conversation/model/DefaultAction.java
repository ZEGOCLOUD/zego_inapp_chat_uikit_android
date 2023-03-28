package com.zegocloud.zimkit.components.conversation.model;

public class DefaultAction {

    private ZIMKitDefaultActionListener defaultAction;
    private ZIMKitConversationModel model;

    public DefaultAction(ZIMKitConversationModel model, ZIMKitDefaultActionListener defaultAction) {
        this.defaultAction = defaultAction;
        this.model = model;
    }

    public interface ZIMKitDefaultActionListener {
        void onDefaultAction(ZIMKitConversationModel model);
    }

    public void toMessage() {
        if (defaultAction != null) {
            defaultAction.onDefaultAction(model);
        }
    }

}
