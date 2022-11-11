package im.zego.zimkit.constant;

import androidx.annotation.NonNull;

public class UserAvatar {

    public static final String AVATAR_URL_FORMAT = "https://storage.zego.im/IMKit/avatar/avatar-%d.png";

    /**
     * According to the user id to get the first character into ASCII code to get the remainder of
     * the subscript corresponding to the avatar link
     *
     * @param userId User id
     * @return
     */
    @NonNull
    public static String getUserAvatar(String userId) {
        char charIndex = userId.charAt(0);
        int remainderIndex = charIndex;
        int urlIndex = Math.abs(remainderIndex % 9);
        String headPath = String.format(UserAvatar.AVATAR_URL_FORMAT, urlIndex);
        return headPath;
    }

}
