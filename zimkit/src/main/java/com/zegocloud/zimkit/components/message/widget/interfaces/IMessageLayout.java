package com.zegocloud.zimkit.components.message.widget.interfaces;


import com.zegocloud.zimkit.components.message.adapter.ZIMKitMessageAdapter;

public interface IMessageLayout {

    /**
     * Set the adapter for the message list {@link ZIMKitMessageAdapter}
     *
     * @param adapter
     */
    void setAdapter(ZIMKitMessageAdapter adapter);

}
