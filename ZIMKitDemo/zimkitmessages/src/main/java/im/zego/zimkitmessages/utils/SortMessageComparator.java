package im.zego.zimkitmessages.utils;

import java.util.Comparator;

import im.zego.zim.entity.ZIMMessage;

/**
 * Sorting of messages by OrderKey
 */
public class SortMessageComparator implements Comparator<ZIMMessage> {

    @Override
    public int compare(ZIMMessage o1, ZIMMessage o2) {
        long value = o2.getOrderKey() - o1.getOrderKey();
        if (value == 0) {
            return 0;
        }
        return value > 0 ? -1 : 1;
    }
}
