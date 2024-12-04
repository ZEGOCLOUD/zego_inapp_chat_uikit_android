package com.zegocloud.zimkit.components.message.widget.chatpop;

public class ChatPopAction {

    private String actionName;
    private int actionIcon;
    private OnClickListener actionClickListener;

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    public String getActionName() {
        return actionName;
    }

    public void setActionIcon(int actionIcon) {
        this.actionIcon = actionIcon;
    }

    public int getActionIcon() {
        return actionIcon;
    }

    public void setActionClickListener(OnClickListener actionClickListener) {
        this.actionClickListener = actionClickListener;
    }

    public OnClickListener getActionClickListener() {
        return actionClickListener;
    }

    @FunctionalInterface
    public interface OnClickListener {

        void onClick();
    }
}
