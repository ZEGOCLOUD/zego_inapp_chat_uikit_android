package com.zegocloud.zimkit.services.config;

import com.zegocloud.zimkit.components.message.utils.EmojiUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ZIMKitInputConfig {

    public List<ZIMKitInputButtonName> smallButtons;
    public List<ZIMKitInputButtonName> expandButtons;
    public List<String> emojis;

    public ZIMKitInputConfig() {
        smallButtons = new ArrayList<>(
            Arrays.asList(ZIMKitInputButtonName.AUDIO, ZIMKitInputButtonName.EMOJI, ZIMKitInputButtonName.PICTURE,
                ZIMKitInputButtonName.EXPAND));
        expandButtons = new ArrayList<>(Arrays.asList(ZIMKitInputButtonName.TAKE_PHOTO, ZIMKitInputButtonName.FILE));
        emojis = new ArrayList<>(EmojiUtils.createEmojiData());
    }
}
