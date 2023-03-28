package com.zegocloud.zimkit.components.message.utils;

import com.zegocloud.zimkit.services.model.ZIMKitMessage;
import java.util.Comparator;

public class SortZIMKitMessageComparator implements Comparator<ZIMKitMessage> {
    @Override
    public int compare(ZIMKitMessage o1, ZIMKitMessage o2) {
        long value = o2.info.orderKey - o1.info.orderKey;
        if (value == 0) {
            return 0;
        }
        return value > 0 ? -1 : 1;
    }
}
