package im.zego.zimkitmessages.widget.interfaces;

import im.zego.zimkitmessages.adapter.ZIMKitMessageAdapter;

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
