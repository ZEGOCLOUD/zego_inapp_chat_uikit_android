package com.zegocloud.zimkit.components.message.widget.interfaces;


import com.zegocloud.zimkit.components.message.adapter.ZIMKitMessageAdapter;

public interface IMessageLayout {

    /**
     * Set the adapter for the message list {@link ZIMKitMessageAdapter}
     *
     * @param adapter
     */
    void setAdapter(ZIMKitMessageAdapter adapter);

    /**
     * Get the click event of the message list
     *
     * @return
     */
    OnItemClickListener getOnItemClickListener();

    /**
     * Setting the event listener for the message list {@link OnItemClickListener}
     *
     * @param listener
     */
    void setOnItemClickListener(OnItemClickListener listener);

}
