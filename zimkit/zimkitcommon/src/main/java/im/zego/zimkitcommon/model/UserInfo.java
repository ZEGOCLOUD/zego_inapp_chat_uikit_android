package im.zego.zimkitcommon.model;

public class UserInfo {

    /**
     * userID: 1 to 32 characters, can only contain digits, letters, and the following special characters:
     * '~', '!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '_', '+', '=', '-', '`', ';', '’', ',', '.', '<', '>', '/', '\'.
     */
    private String userID;
    /**
     * User name: 1 - 64 characters.
     */
    private String userName;
    /**
     * User avatar URL.
     */
    private String userAvatarUrl;

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserAvatarUrl() {
        return userAvatarUrl;
    }

    public void setUserAvatarUrl(String userAvatarUrl) {
        this.userAvatarUrl = userAvatarUrl;
    }
}
