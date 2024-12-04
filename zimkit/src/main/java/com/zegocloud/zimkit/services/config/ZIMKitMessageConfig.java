package com.zegocloud.zimkit.services.config;

import com.zegocloud.zimkit.components.message.utils.EmojiUtils;
import com.zegocloud.zimkit.services.config.message.ZIMKitAudioMessageConfig;
import com.zegocloud.zimkit.services.config.message.ZIMKitCombineMessageConfig;
import com.zegocloud.zimkit.services.config.message.ZIMKitFileMessageConfig;
import com.zegocloud.zimkit.services.config.message.ZIMKitImageMessageConfig;
import com.zegocloud.zimkit.services.config.message.ZIMKitTextMessageConfig;
import com.zegocloud.zimkit.services.config.message.ZIMKitVideoMessageConfig;
import java.util.ArrayList;
import java.util.List;

public class ZIMKitMessageConfig {

    public List<String> emojis;
    public ZIMKitTextMessageConfig textMessageConfig = new ZIMKitTextMessageConfig();
    public ZIMKitImageMessageConfig imageMessageConfig = new ZIMKitImageMessageConfig();
    public ZIMKitAudioMessageConfig audioMessageConfig = new ZIMKitAudioMessageConfig();
    public ZIMKitVideoMessageConfig videoMessageConfig = new ZIMKitVideoMessageConfig();
    public ZIMKitFileMessageConfig fileMessageConfig = new ZIMKitFileMessageConfig();
    public ZIMKitCombineMessageConfig combineMessageConfig = new ZIMKitCombineMessageConfig();

    public ZIMKitMessageConfig() {
        emojis = new ArrayList<>(EmojiUtils.createEmojiData());
    }
}
