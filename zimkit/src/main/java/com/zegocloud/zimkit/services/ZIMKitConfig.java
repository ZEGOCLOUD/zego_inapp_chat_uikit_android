package com.zegocloud.zimkit.services;

import com.zegocloud.uikit.plugin.adapter.plugins.call.ZegoCallPluginConfig;
import com.zegocloud.zimkit.services.config.ZIMKitInputConfig;
import com.zegocloud.zimkit.services.config.ZIMKitMessageConfig;
import com.zegocloud.zimkit.services.config.conversation.ZIMKitConversationConfig;
import java.util.HashMap;
import java.util.Map;

public class ZIMKitConfig {

    public String resourceID;
    public ZegoCallPluginConfig callPluginConfig;
    public ZIMKitInputConfig inputConfig = new ZIMKitInputConfig();
    public ZIMKitConversationConfig conversationConfig = new ZIMKitConversationConfig();
    public ZIMKitMessageConfig messageConfig = new ZIMKitMessageConfig();
    public Map<String, String> advancedConfig = new HashMap<>();
}
