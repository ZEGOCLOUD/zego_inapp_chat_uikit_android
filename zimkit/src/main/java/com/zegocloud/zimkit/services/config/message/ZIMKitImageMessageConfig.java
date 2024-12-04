package com.zegocloud.zimkit.services.config.message;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ZIMKitImageMessageConfig {

    public List<ZIMKitMessageOperationName> operations = new ArrayList<>(
        Arrays.asList(ZIMKitMessageOperationName.REPLY, ZIMKitMessageOperationName.FORWARD,
            ZIMKitMessageOperationName.MULTIPLE_CHOICE, ZIMKitMessageOperationName.DELETE, ZIMKitMessageOperationName.REVOKE,
            ZIMKitMessageOperationName.REACTION));
}
