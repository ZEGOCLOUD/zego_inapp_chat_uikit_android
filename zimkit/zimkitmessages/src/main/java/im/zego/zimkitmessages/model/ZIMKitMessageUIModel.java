package im.zego.zimkitmessages.model;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DimenRes;
import androidx.core.content.ContextCompat;

import im.zego.zimkitcommon.ZIMKitManager;
import im.zego.zimkitmessages.R;

public class ZIMKitMessageUIModel {
    public static
    float nameFontSP = ZIMKitManager.share().getApplication().getResources().getDimension(R.dimen.message_other_side_nick_name_size);
    public @ColorInt
    static int nameColor = ContextCompat.getColor(ZIMKitManager.share().getApplication(), im.zego.zimkitcommon.R.color.color_666666);

    public static void setNameColor(@ColorRes int color) {
        nameColor = ContextCompat.getColor(ZIMKitManager.share().getApplication(), color);
    }

    public static void setNameFontSP(@DimenRes int size) {
        nameFontSP = ZIMKitManager.share().getApplication().getResources().getDimension(size);
    }

}
