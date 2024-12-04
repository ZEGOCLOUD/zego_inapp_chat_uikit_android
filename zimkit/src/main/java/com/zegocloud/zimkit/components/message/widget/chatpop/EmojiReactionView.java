package com.zegocloud.zimkit.components.message.widget.chatpop;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.zegocloud.zimkit.R;
import com.zegocloud.zimkit.components.message.model.ZIMKitMessageModel;
import com.zegocloud.zimkit.databinding.ZimkitLayoutEmojiReactionBinding;
import im.zego.zim.entity.ZIMMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EmojiReactionView extends FrameLayout {

    private ZimkitLayoutEmojiReactionBinding binding;
    private String emoji;
    private List<String> names = new ArrayList<>();
    private ZIMKitMessageModel messageModel;

    public EmojiReactionView(@NonNull Context context) {
        super(context);
        initView();
    }

    public EmojiReactionView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public EmojiReactionView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public EmojiReactionView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr,
        int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView();
    }

    private void initView() {
        binding = ZimkitLayoutEmojiReactionBinding.inflate(LayoutInflater.from(getContext()), this, true);
    }

    public void setSendStyle() {
        binding.emojiLine.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.color_33ffffff));
        binding.emojiNames.setTextColor(ContextCompat.getColor(getContext(), R.color.color_b3ffffff));
        binding.getRoot().setBackgroundResource(R.drawable.zimkit_shape_40dp_1a63f1);
    }

    public void setRecvStyle() {
        binding.emojiLine.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.color_cacbce));
        binding.emojiNames.setTextColor(ContextCompat.getColor(getContext(), R.color.color_646A73));
        binding.getRoot().setBackgroundResource(R.drawable.zimkit_shape_40dp_eff0f2);
    }

    public void setEmoji(String emoji) {
        this.emoji = emoji;
        binding.emojiTextview.setText(emoji);
    }

    public void setMessageModel(ZIMKitMessageModel messageModel) {
        this.messageModel = messageModel;
    }

    public void setNames(List<String> names) {
        this.names.clear();
        this.names.addAll(names);
        Optional<String> reduce = names.stream().reduce((s, s2) -> s + "," + s2);
        String targetNames = reduce.orElse("");
        binding.emojiNames.setText(targetNames);
    }

    public ZIMKitMessageModel getMessageModel() {
        return messageModel;
    }

    public String getEmoji() {
        return emoji;
    }

    public static int dp2px(float v, DisplayMetrics displayMetrics) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, displayMetrics);
    }
}
