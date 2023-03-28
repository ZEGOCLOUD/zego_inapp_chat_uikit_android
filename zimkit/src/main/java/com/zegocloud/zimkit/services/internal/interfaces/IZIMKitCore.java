package com.zegocloud.zimkit.services.internal.interfaces;

import android.app.Application;

import com.zegocloud.zimkit.components.conversation.interfaces.ZIMKitConversationListListener;
import com.zegocloud.zimkit.components.message.interfaces.ZIMKitMessagesListListener;
import com.zegocloud.zimkit.services.ZIMKitDelegate;

public interface IZIMKitCore extends IUserService,IConversationService,IGroupService,IMessageService{

    void initWith(Application application, Long appID, String appSign);

    void initNotifications();
    void unInitNotifications();

    void registerZIMKitDelegate(ZIMKitDelegate delegate);

    void unRegisterZIMKitDelegate(ZIMKitDelegate delegate);

    void registerConversationListListener(ZIMKitConversationListListener listener);

    void unRegisterConversationListListener();

    void registerMessageListListener(ZIMKitMessagesListListener listener);

    void unRegisterMessageListListener();

}
