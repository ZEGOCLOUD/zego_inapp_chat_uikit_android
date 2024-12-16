package com.zegocloud.zimkit.components.message.widget.viewholder;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils.TruncateAt;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.databinding.ViewDataBinding;
import com.zegocloud.zimkit.R;
import com.zegocloud.zimkit.components.forward.ForwardDetailsActivity;
import com.zegocloud.zimkit.components.message.model.CombineMessageModel;
import com.zegocloud.zimkit.components.message.model.ZIMKitMessageModel;
import com.zegocloud.zimkit.databinding.ZimkitItemMessageReceiveCombineBinding;
import com.zegocloud.zimkit.databinding.ZimkitItemMessageSendCombineBinding;
import com.zegocloud.zimkit.services.internal.ZIMKitCore;
import com.zegocloud.zimkit.services.model.ZIMKitUser;
import com.zegocloud.zimkit.services.utils.ZIMMessageUtil;
import im.zego.zim.callback.ZIMCombineMessageDetailQueriedCallback;
import im.zego.zim.entity.ZIMCombineMessage;
import im.zego.zim.entity.ZIMError;
import im.zego.zim.entity.ZIMMessage;
import im.zego.zim.entity.ZIMUserFullInfo;
import im.zego.zim.entity.ZIMUsersInfoQueryConfig;
import im.zego.zim.enums.ZIMErrorCode;
import im.zego.zim.enums.ZIMMessageDirection;
import im.zego.zim.enums.ZIMMessageType;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CombineMessageHolder extends MessageViewHolder {

    private ZimkitItemMessageSendCombineBinding sendTextBinding;
    private ZimkitItemMessageReceiveCombineBinding receiveTextBinding;
    private String emojiRegex = "[" + "\\ud83d\\ude00-\\ud83d\\ude4f" // Emoticons
        + "\\ud83d\\ude80-\\ud83d\\udeff" // Transport and map symbols
        + "\\ud83d\\udf00-\\ud83d\\udfff" // Household items
        + "\\ud83c\\udf00-\\ud83c\\udfff" // Miscellaneous symbols and pictographs
        + "\\u2600-\\u26ff" // Miscellaneous symbols
        + "\\u2700-\\u27bf" // Dingbats
        + "]+";
    private Pattern emojiPattern = Pattern.compile(emojiRegex);

    public CombineMessageHolder(ViewDataBinding binding) {
        super(binding);
        if (binding instanceof ZimkitItemMessageSendCombineBinding) {
            sendTextBinding = (ZimkitItemMessageSendCombineBinding) binding;
        } else if (binding instanceof ZimkitItemMessageReceiveCombineBinding) {
            receiveTextBinding = (ZimkitItemMessageReceiveCombineBinding) binding;
        }
    }

    @Override
    public void bind(int id, int position, ZIMKitMessageModel model) {
        super.bind(id, position, model);

        if (model.getMessage().getType() == ZIMMessageType.COMBINE) {
            CombineMessageModel combineMessageModel = (CombineMessageModel) model;
            boolean isSend;
            if (mAdapter.isOneSideForwardMode()) {
                isSend = false;
            } else {
                isSend = model.getDirection() == ZIMMessageDirection.SEND;
            }

            LinearLayout combineSummery = itemView.findViewById(R.id.combine_message_text);

            String[] split = combineMessageModel.getSummary().split("\n");

            combineSummery.removeAllViews();
            for (String content : split) {
                TextView textView = generateSummeryTextView(itemView.getContext(), isSend);

                Matcher matcher = emojiPattern.matcher(content);

                SpannableString spannableString = new SpannableString(content);
                while (matcher.find()) {
                    int start = matcher.start();
                    int end = matcher.end();
                    spannableString.setSpan(new ForegroundColorSpan(Color.BLACK), start, end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                textView.setText(spannableString);
                combineSummery.addView(textView);
            }
        }
    }

    private TextView generateSummeryTextView(Context context, boolean isSend) {
        TextView textView = new TextView(context);
        textView.setMaxLines(2);
        textView.setEllipsize(TruncateAt.END);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        if (isSend) {
            textView.setTextColor(context.getColor(R.color.color_b3ffffff));
        } else {
            textView.setTextColor(context.getColor(R.color.color_b32a2a2a));
        }
        return textView;
    }

    public void onMessageLayoutClicked(Context context, CombineMessageModel combineMessageModel) {
        ZIMMessage message = combineMessageModel.getMessage();
        if (message.getType() != ZIMMessageType.COMBINE) {
            return;
        }
        // ZIMCombineMessage need to query to fill messageList
        ZIMKitCore.getInstance()
            .queryCombineMessageDetail((ZIMCombineMessage) message, new ZIMCombineMessageDetailQueriedCallback() {
                @Override
                public void onCombineMessageDetailQueried(ZIMCombineMessage message, ZIMError error) {
                    if (error.code == ZIMErrorCode.SUCCESS) {
                        ArrayList<String> combineSubSenderList = message.messageList.stream()
                            .map(ZIMMessage::getSenderUserID).collect(Collectors.toCollection(ArrayList::new));
                        ZIMKitCore.getInstance().queryUserInfo(combineSubSenderList, new ZIMUsersInfoQueryConfig(),
                            (userList, errorUserList, errorInfo) -> {
                                if (errorInfo.code == ZIMErrorCode.SUCCESS) {
                                    combineMessageModel.messageList = message.messageList.stream().map(zimMessage -> {
                                        ZIMKitMessageModel messageModel = ZIMMessageUtil.parseZIMMessageToModel(
                                            zimMessage);
                                        String senderUserID = zimMessage.getSenderUserID();
                                        ZIMUserFullInfo memoryUserInfo = ZIMKitCore.getInstance()
                                            .getMemoryUserInfo(senderUserID);
                                        if (memoryUserInfo != null) {
                                            messageModel.setNickName(memoryUserInfo.baseInfo.userName);
                                            messageModel.setAvatar(memoryUserInfo.baseInfo.userAvatarUrl);
                                        }
                                        ZIMKitUser localUser = ZIMKitCore.getInstance().getLocalUser();
                                        if (Objects.equals(localUser.getId(), senderUserID)) {
                                            messageModel.setDirection(ZIMMessageDirection.SEND);
                                        } else {
                                            messageModel.setDirection(ZIMMessageDirection.RECEIVE);
                                        }
                                        return messageModel;
                                    }).collect(Collectors.toList());
                                    List<ZIMKitMessageModel> forwardMessages = combineMessageModel.messageList;
                                    ZIMKitCore.getInstance().setForwardMessages(null, forwardMessages);

                                    Intent intent = new Intent(context, ForwardDetailsActivity.class);
                                    intent.putExtra("title", combineMessageModel.getTitle());
                                    intent.putExtra("one_side", true);
                                    context.startActivity(intent);
                                }
                            });
                    }
                }
            });
    }

}
