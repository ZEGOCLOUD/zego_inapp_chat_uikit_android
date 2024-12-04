package com.zegocloud.zimkit.services.config.message;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ZIMKitAudioMessageConfig {

    public List<ZIMKitMessageOperationName> operations = new ArrayList<>(
        Arrays.asList(ZIMKitMessageOperationName.SPEAKER, ZIMKitMessageOperationName.REPLY,
            ZIMKitMessageOperationName.MULTIPLE_CHOICE, ZIMKitMessageOperationName.DELETE,
            ZIMKitMessageOperationName.REVOKE,
            ZIMKitMessageOperationName.REACTION));
}
