package com.zegocloud.zimkit.components.message.widget.chatpop;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import com.zegocloud.zimkit.components.message.utils.OnRecyclerViewItemTouchListener;
import java.util.List;

public class ChatPopEmojiFragment extends Fragment {

    private static final int SPAN_COUNT = 7;
    private List<String> subEmojis;
    private int pagePosition;
    private Callback callback;
    private int startOffset;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState) {
        RecyclerView recyclerView = new RecyclerView(getContext());
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), SPAN_COUNT);
        recyclerView.setLayoutManager(gridLayoutManager);
        ChatPopEmojiAdapter emojiAdapter = new ChatPopEmojiAdapter();
        emojiAdapter.setEmojis(subEmojis);
        recyclerView.setAdapter(emojiAdapter);

        recyclerView.addOnItemTouchListener(new OnRecyclerViewItemTouchListener(recyclerView) {
            @Override
            public void onItemClick(ViewHolder vh) {
                if (vh.getAdapterPosition() == RecyclerView.NO_POSITION) {
                    return;
                }
                if (callback != null) {
                    callback.onItemClicked(ChatPopEmojiFragment.this, vh.getAdapterPosition());
                }
            }
        });
        return recyclerView;
    }

    public void setSubEmojis(int position, List<String> subEmojis, int startOffset) {
        this.subEmojis = subEmojis;
        this.pagePosition = position;
        this.startOffset = startOffset;
    }

    public int getPagePosition() {
        return pagePosition;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public int getStartOffset() {
        return startOffset;
    }

    public List<String> getSubEmojis() {
        return subEmojis;
    }

    public interface Callback {

        void onItemClicked(ChatPopEmojiFragment fragment, int position);
    }
}
