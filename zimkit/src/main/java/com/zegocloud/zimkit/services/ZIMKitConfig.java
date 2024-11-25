package com.zegocloud.zimkit.services;

import com.zegocloud.uikit.plugin.adapter.plugins.call.ZegoCallPluginConfig;
import com.zegocloud.zimkit.services.config.ZIMKitInputConfig;
import com.zegocloud.zimkit.services.config.ZIMKitMessageConfig;
import java.util.HashMap;
import java.util.Map;

public class ZIMKitConfig {

    public String resourceID;
    public ZegoCallPluginConfig callPluginConfig;
    public ZIMKitMessageConfig messageConfig = new ZIMKitMessageConfig();
    public ZIMKitInputConfig inputConfig = new ZIMKitInputConfig();
    public Map<String, String> advancedConfig = new HashMap<>();
}
