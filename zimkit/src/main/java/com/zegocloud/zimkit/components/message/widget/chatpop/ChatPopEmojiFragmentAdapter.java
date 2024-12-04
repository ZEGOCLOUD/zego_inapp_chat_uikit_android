package com.zegocloud.zimkit.components.message.widget.chatpop;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.zegocloud.zimkit.components.message.widget.chatpop.ChatPopEmojiFragment.Callback;
import java.util.ArrayList;
import java.util.List;

public class ChatPopEmojiFragmentAdapter extends FragmentStateAdapter {

    private List<String> emojis;
    private int pageEmojiCount = 14;
    private Callback callback;

    public ChatPopEmojiFragmentAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    public ChatPopEmojiFragmentAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    public ChatPopEmojiFragmentAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        int start = position * pageEmojiCount;
        int end = (position + 1) * pageEmojiCount;
        if (end > emojis.size() - 1) {
            end = emojis.size() - 1;
        }
        List<String> subEmojis = new ArrayList<>(emojis.subList(start, end));
        ChatPopEmojiFragment chatPopEmojiFragment = new ChatPopEmojiFragment();
        chatPopEmojiFragment.setSubEmojis(position, subEmojis, start);
        chatPopEmojiFragment.setCallback(callback);
        return chatPopEmojiFragment;
    }

    @Override
    public int getItemCount() {
        double ceil = Math.ceil(emojis.size() * 1f / pageEmojiCount);
        return (int) ceil;
    }

    public void setEmojis(List<String> emojis) {
        this.emojis = emojis;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }
}
