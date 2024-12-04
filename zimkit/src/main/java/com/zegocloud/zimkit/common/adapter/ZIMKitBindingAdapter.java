package com.zegocloud.zimkit.common.adapter;

import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.databinding.BindingAdapter;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.flexbox.FlexboxLayout.LayoutParams;
import com.zegocloud.zimkit.R;
import com.zegocloud.zimkit.common.glide.ZIMKitGlideLoader;
import com.zegocloud.zimkit.common.utils.ZIMKitFileUtils;
import com.zegocloud.zimkit.components.message.model.ZIMKitMessageModel;
import com.zegocloud.zimkit.components.message.widget.chatpop.EmojiReactionView;
import com.zegocloud.zimkit.services.internal.ZIMKitCore;
import com.zegocloud.zimkit.services.utils.ZIMMessageUtil;
import im.zego.zim.entity.ZIMMessage;
import im.zego.zim.entity.ZIMMessageReaction;
import im.zego.zim.entity.ZIMMessageRepliedInfo;
import im.zego.zim.entity.ZIMTextMessage;
import im.zego.zim.entity.ZIMUserFullInfo;
import im.zego.zim.enums.ZIMConversationNotificationStatus;
import im.zego.zim.enums.ZIMConversationType;
import im.zego.zim.enums.ZIMMessageDirection;
import im.zego.zim.enums.ZIMMessageType;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ZIMKitBindingAdapter {

    @BindingAdapter("unReadCount")
    public static void setCount(TextView view, int count) {
        if (count <= 0) {
            view.setVisibility(View.GONE);
        } else {
            view.setVisibility(View.VISIBLE);
            String strCount = count + "";
            if (count > 99) {
                strCount = "99+";
            }
            view.setText(strCount);
        }
    }

    @BindingAdapter("unReadCountBackground")
    public static void setUnReadCountBackground(View view, ZIMConversationNotificationStatus notificationStatus) {
        if (notificationStatus == ZIMConversationNotificationStatus.NOTIFY) {
            view.setBackgroundResource(R.drawable.zimkit_shape_oval_ff4a50);
        } else if (notificationStatus == ZIMConversationNotificationStatus.DO_NOT_DISTURB) {
            view.setBackgroundResource(R.drawable.zimkit_shape_oval_babbc0);
        }
    }

    @BindingAdapter("conversationItemBackground")
    public static void setConversationItemBackground(View view, boolean isPinned) {
        if (isPinned) {
            view.setBackgroundColor(view.getResources().getColor(R.color.color_f8f8f8));
        } else {
            view.setBackgroundColor(view.getResources().getColor(R.color.color_ffffff));
        }
    }

    @BindingAdapter("showWhiteColor")
    public static void setBackgroundColor(View view, boolean isShowWhite) {
        if (isShowWhite) {
            view.setBackgroundColor(ZIMKitCore.getInstance().getApplication().getResources().getColor(R.color.white));
        } else {
            view.setBackgroundColor(
                ZIMKitCore.getInstance().getApplication().getResources().getColor(R.color.color_eff0f2));
        }
    }

    private static final String TAG = "ZIMKitBindingAdapter";
    /**
     * Load message image
     *
     * @param imageView
     * @param url
     * @param width
     * @param height
     * @param fileName
     */
    @BindingAdapter(value = {"messageImageUrl", "fileLocalPath", "imgWidth", "imgHeight",
        "fileName"}, requireAll = false)
    public static void loadMessageImage(ImageView imageView, String url, String fileLocalPath, int width, int height,
        String fileName) {
        String suffixStr = ZIMKitFileUtils.getFileSuffix(fileName);
        if (!TextUtils.isEmpty(fileName) && suffixStr.equals("gif")) {
            ZIMKitGlideLoader.displayMessageGifImage(imageView, url, fileLocalPath, width, height);
        } else {
            ZIMKitGlideLoader.displayMessageImage(imageView, url, fileLocalPath, width, height);
        }
    }

    /**
     * Loading session list avatars
     *
     * @param imageView
     * @param url
     * @param type
     */
    @BindingAdapter(value = {"avatarConversationUrl", "conversationType"}, requireAll = false)
    public static void loadConversationAvatar(ImageView imageView, String url, ZIMConversationType type) {
        ZIMKitGlideLoader.displayConversationAvatarImage(imageView, url, type);
    }

    /**
     * Loading message content avatar
     *
     * @param imageView
     * @param url
     */
    @BindingAdapter(value = {"avatarMessageUrl"}, requireAll = false)
    public static void loadMessageAvatar(ImageView imageView, String url) {
        ZIMKitGlideLoader.displayMessageAvatarImage(imageView, url);
    }

    @BindingAdapter(value = {"fileIcon"}, requireAll = false)
    public static void loadFileIcon(ImageView imageView, int url) {
        ZIMKitGlideLoader.displayLocalImage(imageView, url);
    }

    private static String emojiRegex = "[" + "\\ud83d\\ude00-\\ud83d\\ude4f" // Emoticons
        + "\\ud83d\\ude80-\\ud83d\\udeff" // Transport and map symbols
        + "\\ud83d\\udf00-\\ud83d\\udfff" // Household items
        + "\\ud83c\\udf00-\\ud83c\\udfff" // Miscellaneous symbols and pictographs
        + "\\u2600-\\u26ff" // Miscellaneous symbols
        + "\\u2700-\\u27bf" // Dingbats
        + "]+";
    private static Pattern emojiPattern = Pattern.compile(emojiRegex);

    @BindingAdapter(value = {"replyMessageContent"}, requireAll = false)
    public static void replyMessageContent(TextView textView, ZIMMessage zimMessage) {
        if (zimMessage != null && zimMessage.getRepliedInfo() != null) {
            ZIMMessageRepliedInfo repliedInfo = zimMessage.getRepliedInfo();
            String content = ZIMMessageUtil.simplifyZIMMessageRepliedContent(zimMessage);
            ZIMUserFullInfo memoryUserInfo = ZIMKitCore.getInstance().getMemoryUserInfo(repliedInfo.senderUserID);
            String nickName = memoryUserInfo == null ? repliedInfo.senderUserID : memoryUserInfo.baseInfo.userName;
            String string = textView.getContext().getString(R.string.zimkit_reply_content, nickName, content);

            Matcher matcher = emojiPattern.matcher(string);

            SpannableString spannableString = new SpannableString(string);
            while (matcher.find()) {
                String emoji = matcher.group();
                int start = matcher.start();
                int end = matcher.end();
                spannableString.setSpan(new ForegroundColorSpan(Color.BLACK), start, end,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            textView.setText(spannableString);
        } else {
            textView.setText("");
        }
    }

    @BindingAdapter(value = {"applyReactions"}, requireAll = false)
    public static void applyReactions(FlexboxLayout flexboxLayout, ZIMKitMessageModel kitMessageModel) {
        flexboxLayout.removeAllViews();
        flexboxLayout.setVisibility(kitMessageModel.getReactions().isEmpty() ? View.GONE : View.VISIBLE);
        if (!kitMessageModel.getReactions().isEmpty()) {
            for (int i = 0; i < kitMessageModel.getReactions().size(); i++) {
                ZIMMessageReaction reaction = kitMessageModel.getReactions().get(i);
                EmojiReactionView reactionView = new EmojiReactionView(flexboxLayout.getContext());
                reactionView.setEmoji(reaction.reactionType);

                ArrayList<String> reactionUserIDList = reaction.userList.stream()
                    .map(zimMessageReactionUserInfo -> zimMessageReactionUserInfo.userID)
                    .collect(Collectors.toCollection(ArrayList::new));
                List<String> collect = reactionUserIDList.stream().map(userID -> {
                    ZIMUserFullInfo memoryUserInfo = ZIMKitCore.getInstance().getMemoryUserInfo(userID);
                    if (memoryUserInfo == null) {
                        return userID;
                    } else {
                        return memoryUserInfo.baseInfo.userName;
                    }
                }).collect(Collectors.toList());
                reactionView.setNames(collect);
                reactionView.setMessageModel(kitMessageModel);

                if (kitMessageModel.getDirection() == ZIMMessageDirection.RECEIVE) {
                    reactionView.setRecvStyle();
                } else {
                    reactionView.setSendStyle();
                }
                FlexboxLayout.LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
                DisplayMetrics metrics = flexboxLayout.getContext().getResources().getDisplayMetrics();
                params.topMargin = dp2px(2, metrics);
                params.bottomMargin = dp2px(2, metrics);
                params.rightMargin = dp2px(5, metrics);
                flexboxLayout.addView(reactionView, params);
            }
        }
    }

    @BindingAdapter(value = {"applyTextMessage"}, requireAll = false)
    public static void applyTextMessage(TextView textView, ZIMMessage zimMessage) {
        if (zimMessage.getType() == ZIMMessageType.TEXT) {
            String content = ((ZIMTextMessage) zimMessage).message;
            textView.setText(content);
        } else {
            // audio 类型，转发的时候改成显示 text 类型
            String content = ZIMMessageUtil.simplifyZIMMessageContent(zimMessage);
            textView.setText(content);
        }

    }

    public static int dp2px(float v, DisplayMetrics displayMetrics) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, displayMetrics);
    }
}
