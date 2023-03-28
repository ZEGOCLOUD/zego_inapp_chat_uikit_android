package com.zegocloud.zimkit.components.message.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EmojiUtils {

    private static String[] emoji = new String[]{
            "😀", "😃", "😄", "😁", "😆", "😅", "😂",
            "😇", "😉", "😊", "😋", "😌", "😍", "😘",
            "😗", "😙", "😚", "😜", "😝", "😛", "😎",
            "😏", "😶", "😐", "😑", "😒", "😳", "😞",
            "😟", "😤", "😠", "😡", "😔", "😕", "😬",
            "😣", "😖", "😫", "😩", "😪", "😮", "😱",
            "😨", "😰", "😥", "😓", "😯", "😦", "😧",
            "😢", "😭", "😵", "😲", "😷", "😴", "💤",
            "😈", "👿", "👹", "👺", "💩", "👻", "💀",
            "👽", "🎃", "😺", "😸", "😹", "😻", "😼",
            "😽", "🙀", "😿", "😾", "👐", "🙌", "👏",
            "🙏", "👍", "👎", "👊", "✊", "👌", "👈",
            "👉", "👆", "👇", "✋", "👋", "💪", "💅",
            "👄", "👅", "👂", "👃", "👀", "👶", "👧",
            "👦", "👩", "👨", "👱", "👵", "👴", "👲",
            "👳‍", "👼", "👸", "👰", "🙇", "💁", "🙅‍",
            "🙆", "🙋", "🙎", "🙍", "💇", "💆", "💃",
            "👫", "👭", "👬", "💛", "💚", "💙", "💜",
            "💔", "💕", "💞", "💓", "💗", "💖", "💘",
            "💝", "💟"
    };

    public static List<String> createEmojiData() {
        List<String> list = new ArrayList<>();
        list.addAll(Arrays.asList(emoji));
        return list;
    }

}
