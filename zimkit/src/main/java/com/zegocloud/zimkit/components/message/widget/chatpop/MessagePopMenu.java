package com.zegocloud.zimkit.components.message.widget.chatpop;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout.Tab;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.tabs.TabLayoutMediator.TabConfigurationStrategy;
import com.zegocloud.zimkit.R;
import com.zegocloud.zimkit.common.utils.ZIMKitScreenUtils;
import com.zegocloud.zimkit.components.message.model.ZIMKitMessageModel;
import com.zegocloud.zimkit.components.message.utils.OnRecyclerViewItemTouchListener;
import com.zegocloud.zimkit.components.message.widget.chatpop.ChatPopEmojiFragment.Callback;
import com.zegocloud.zimkit.databinding.ZimkitLayoutMessagePopMenuBinding;
import com.zegocloud.zimkit.services.ZIMKitConfig;
import com.zegocloud.zimkit.services.internal.ZIMKitCore;
import java.util.ArrayList;
import java.util.List;

public class MessagePopMenu {

    private ZimkitLayoutMessagePopMenuBinding binding;
    private final PopupWindow popupWindow;
    private final Context context;
    private ChatPopActionAdapter actionAdapter;
    private ChatPopEmojiAdapter recentEmojiAdapter;
    private int emojiWindowWidth; // with index
    private int actionWindowWidth; // with index
    private int popWindowHeight; // with index
    // Small triangle height
    private final int indicatorHeight;
    private final int indicatorWidth;
    private int parentTop;
    private View anchorView;
    private CallBack callBack;
    private ZIMKitMessageModel messageModel;

    public MessagePopMenu(Context context, ZIMKitMessageModel messageModel, List<ChatPopAction> mPopActions,
        boolean showChatPopReactionView) {
        this.context = context;
        this.messageModel = messageModel;

        binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.zimkit_layout_message_pop_menu, null,
            false);

        initChatActionButtons(context, mPopActions);

        initRecentEmojiButtons(context, showChatPopReactionView);
        initEmojiPager(context);

        if (!showChatPopReactionView) {
            binding.chatPopMenuRecentEmojiLayout.setVisibility(View.GONE);
            binding.line.setVisibility(View.GONE);
        }

        binding.chatPopMenuActionView.setVisibility(View.VISIBLE);
        binding.chatPopMenuEmojiLayout.setVisibility(View.GONE);

        popupWindow = new PopupWindow(binding.getRoot(), ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT, false);
        popupWindow.setBackgroundDrawable(new ColorDrawable());
        popupWindow.setTouchable(true);
        popupWindow.setOutsideTouchable(true);

        indicatorHeight = context.getResources().getDimensionPixelOffset(R.dimen.message_pop_menu_indicator_height);
        indicatorWidth = context.getResources().getDimensionPixelOffset(R.dimen.message_pop_menu_indicator_width);
    }

    private void initEmojiPager(Context context) {
        ZIMKitConfig zimKitConfig = ZIMKitCore.getInstance().getZimKitConfig();
        if (zimKitConfig != null && zimKitConfig.messageConfig != null) {
            List<String> emojis = new ArrayList<>(zimKitConfig.messageConfig.emojis);
            ChatPopEmojiFragmentAdapter chatPopEmojiFragmentAdapter = new ChatPopEmojiFragmentAdapter(
                (FragmentActivity) context);
            chatPopEmojiFragmentAdapter.setEmojis(emojis);
            binding.chatPopMenuEmojis.setAdapter(chatPopEmojiFragmentAdapter);
            binding.chatPopMenuEmojis.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);

            TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(binding.chatPopMenuEmojiTablayout,
                binding.chatPopMenuEmojis, new TabConfigurationStrategy() {
                @Override
                public void onConfigureTab(@NonNull Tab tab, int position) {

                }
            });
            tabLayoutMediator.attach();

            chatPopEmojiFragmentAdapter.setCallback(new Callback() {
                @Override
                public void onItemClicked(ChatPopEmojiFragment fragment, int position) {
                    int index = position + fragment.getStartOffset();
                    String emoji = emojis.get(index);
                    if (callBack != null) {
                        callBack.onPagerEmojiClick(emoji,messageModel);
                    }
                    dismissPopWindow();
                }
            });
        }
    }

    private void initRecentEmojiButtons(Context context, boolean showChatPopReactionView) {
        ZIMKitConfig zimKitConfig = ZIMKitCore.getInstance().getZimKitConfig();
        if (zimKitConfig != null && zimKitConfig.messageConfig != null) {
            List<String> emojis = zimKitConfig.messageConfig.emojis;
            List<String> recent;
            if (emojis.size() > 6) {
                recent = new ArrayList<>(emojis.subList(0, 6));
            } else {
                recent = new ArrayList<>(emojis);
            }
            recentEmojiAdapter = new ChatPopEmojiAdapter();
            recentEmojiAdapter.setEmojis(recent);
            GridLayoutManager layoutManager = new GridLayoutManager(context, 7);
            binding.chatPopMenuRecentEmoji.setLayoutManager(layoutManager);
            binding.chatPopMenuRecentEmoji.setAdapter(recentEmojiAdapter);

            emojiWindowWidth = emojiWindowWidth + dp2px(40 * recent.size(), context.getResources().getDisplayMetrics());
        }
        emojiWindowWidth = emojiWindowWidth + dp2px(36, context.getResources().getDisplayMetrics());
        if (!showChatPopReactionView) {
            emojiWindowWidth = 0;
        }

        binding.chatPopMenuRecentEmoji.addOnItemTouchListener(
            new OnRecyclerViewItemTouchListener(binding.chatPopMenuRecentEmoji) {
                @Override
                public void onItemClick(ViewHolder vh) {
                    if (vh.getAdapterPosition() == RecyclerView.NO_POSITION) {
                        return;
                    }
                    String emoji = recentEmojiAdapter.getEmoji(vh.getAdapterPosition());
                    if (callBack != null) {
                        callBack.onRecentEmojiClick(emoji,messageModel);
                    }
                    dismissPopWindow();
                }
            });

        binding.chatPopMenuRecentButton.setOnClickListener(v -> {
            boolean selected = v.isSelected();
            boolean newState = !selected;
            v.setSelected(newState);

            if (newState) {
                binding.chatPopMenuActionView.setVisibility(View.GONE);
                binding.chatPopMenuEmojiLayout.setVisibility(View.VISIBLE);
            } else {
                binding.chatPopMenuActionView.setVisibility(View.VISIBLE);
                binding.chatPopMenuEmojiLayout.setVisibility(View.GONE);
            }

            float anchorWidth = anchorView.getWidth();
            float anchorHeight = anchorView.getHeight();
            int[] anchorLocation = new int[2];
            anchorView.getLocationOnScreen(anchorLocation);
            // getMeasuredWidth cannot get right width,so only get height here
            binding.getRoot().measure(0, 0);
            popWindowHeight = binding.getRoot().getMeasuredHeight() + indicatorHeight;

            popupWindow.setWidth(getWindowWidth());
            popupWindow.setHeight(popWindowHeight);

            float indicatorX = anchorWidth / 2;
            int screenWidth = ZIMKitScreenUtils.getScreenWidth(context);

            int x = anchorLocation[0];
            // If it is on the right, both the small arrow x coordinate and the pop-up x position should change
            if (anchorLocation[0] * 2 + anchorWidth > screenWidth) {
                indicatorX = getWindowWidth() - anchorWidth / 2;
                x = (int) (anchorLocation[0] + anchorWidth - getWindowWidth());
            }

            int y = anchorLocation[1] - popWindowHeight;
            // If the height is less than the given minimum height, it is too far up and will cover the title bar, to be displayed below
            boolean showIndicatorInTop = y <= parentTop;
            if (showIndicatorInTop) {
                y = (int) (anchorLocation[1] + anchorHeight);
            }

            if (indicatorX <= 0 || indicatorX > getWindowWidth() || getWindowWidth() < anchorWidth) {
                indicatorX = getWindowWidth() * 1.0f / 2;
            }

            int paddingTop = showIndicatorInTop ? indicatorHeight : 0;
            binding.getRoot().setPadding(0, paddingTop, 0, 0);

            Drawable drawable = getBackgroundDrawable(indicatorX, showIndicatorInTop);
            binding.getRoot().setBackground(drawable);
            //            binding.getRoot().setBackground(new ColorDrawable(Color.parseColor("#444444")));

            popupWindow.update(x, y, getWindowWidth(), popWindowHeight);
        });
    }

    private void initChatActionButtons(Context context, List<ChatPopAction> mPopActions) {
        LinearLayoutManager layout = new LinearLayoutManager(context);
        layout.setOrientation(LinearLayoutManager.HORIZONTAL);
        //        GridLayoutManager layout = new GridLayoutManager(context, 6);
        binding.chatPopMenuActionView.setLayoutManager(layout);
        actionAdapter = new ChatPopActionAdapter();
        actionAdapter.setChatPopMenuActionList(mPopActions);
        binding.chatPopMenuActionView.setAdapter(actionAdapter);
        int spaceWidth = context.getResources().getDimensionPixelSize(R.dimen.message_pop_menu_item_space_width);
        int spaceHeight = context.getResources().getDimensionPixelSize(R.dimen.message_pop_menu_item_space_height);
        Drawable divider = context.getResources().getDrawable(R.drawable.zimkit_shape_pop_menu_divider);
        binding.chatPopMenuActionView.addItemDecoration(new GridDecoration(divider, 6, spaceWidth, spaceHeight));

        binding.chatPopMenuActionView.addOnItemTouchListener(
            new OnRecyclerViewItemTouchListener(binding.chatPopMenuActionView) {
                @Override
                public void onItemClick(ViewHolder vh) {
                    if (vh.getAdapterPosition() == RecyclerView.NO_POSITION) {
                        return;
                    }
                    if (callBack != null) {
                        callBack.onPopActionClick(vh.getAdapterPosition(),messageModel);
                    }
                    dismissPopWindow();
                }
            });
        binding.getRoot().measure(0, 0);
        actionWindowWidth = binding.getRoot().getMeasuredWidth();
    }

    public void show(View anchorView, int parentTop) {
        this.parentTop = parentTop;
        this.anchorView = anchorView;

        float anchorWidth = anchorView.getWidth();
        float anchorHeight = anchorView.getHeight();
        int[] anchorLocation = new int[2];
        anchorView.getLocationOnScreen(anchorLocation);

        if (popupWindow != null) {

            // getMeasuredWidth cannot get right width,so only get height here
            binding.getRoot().measure(0, 0);
            popWindowHeight = binding.getRoot().getMeasuredHeight() + indicatorHeight;

            popupWindow.setWidth(getWindowWidth());
            popupWindow.setHeight(popWindowHeight);

            float indicatorX = anchorWidth / 2;
            int screenWidth = ZIMKitScreenUtils.getScreenWidth(context);

            int x = anchorLocation[0];
            // If it is on the right, both the small arrow x coordinate and the pop-up x position should change
            if (anchorLocation[0] * 2 + anchorWidth > screenWidth) {
                indicatorX = getWindowWidth() - anchorWidth / 2;
                x = (int) (anchorLocation[0] + anchorWidth - getWindowWidth());
            }

            int y = anchorLocation[1] - popWindowHeight;
            // If the height is less than the given minimum height, it is too far up and will cover the title bar, to be displayed below
            boolean showIndicatorInTop = y <= parentTop;
            if (showIndicatorInTop) {
                y = (int) (anchorLocation[1] + anchorHeight);
            }

            if (indicatorX <= 0 || indicatorX > getWindowWidth() || getWindowWidth() < anchorWidth) {
                indicatorX = getWindowWidth() * 1.0f / 2;
            }

            int paddingTop = showIndicatorInTop ? indicatorHeight : 0;
            binding.getRoot().setPadding(0, paddingTop, 0, 0);

            Drawable drawable = getBackgroundDrawable(indicatorX, showIndicatorInTop);
            binding.getRoot().setBackground(drawable);
            //            binding.getRoot().setBackground(new ColorDrawable(Color.parseColor("#444444")));

            popupWindow.showAtLocation(anchorView, Gravity.NO_GRAVITY, x, y);
        }
    }

    public static int dp2px(float v, DisplayMetrics displayMetrics) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, displayMetrics);
    }

    public void dismissPopWindow() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
    }

    private int getWindowWidth() {
        return Math.max(emojiWindowWidth, actionWindowWidth);
    }

    /**
     * Draw popup background with small triangle
     */
    public Drawable getBackgroundDrawable(float indicatorX, boolean isTop) {

        int radius = ZIMKitScreenUtils.dip2px(8.0f);
        Path path = new Path();
        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#313233"));
        paint.setStyle(Paint.Style.FILL);

        Drawable drawable = new Drawable() {
            @Override
            public void draw(@NonNull Canvas canvas) {

                // Small triangle arrow on top
                if (isTop) {
                    // draw RoundRect
                    path.addRoundRect(new RectF(0, indicatorHeight, getWindowWidth(), popWindowHeight), radius, radius,
                        Path.Direction.CW);
                    path.moveTo(indicatorX - indicatorWidth / 2f, indicatorHeight);
                    path.lineTo(indicatorX, 0);
                    path.lineTo(indicatorX + indicatorWidth / 2f, indicatorHeight);
                } else {
                    // draw RoundRect
                    path.addRoundRect(new RectF(0, 0, getWindowWidth(), popWindowHeight - indicatorHeight), radius,
                        radius, Path.Direction.CW);
                    // move to indicator start
                    path.moveTo(indicatorX - indicatorWidth / 2f, popWindowHeight - indicatorHeight);
                    // line to indicator point
                    path.lineTo(indicatorX, popWindowHeight);
                    // move to indicator end
                    path.lineTo(indicatorX + indicatorWidth / 2f, popWindowHeight - indicatorHeight);
                }
                path.close();
                canvas.drawPath(path, paint);
            }

            @Override
            public void setAlpha(int alpha) {

            }

            @Override
            public void setColorFilter(@Nullable ColorFilter colorFilter) {

            }

            @Override
            public int getOpacity() {
                return PixelFormat.TRANSLUCENT;
            }
        };
        return drawable;
    }

    /**
     * Add spacing and split lines
     */
    static class GridDecoration extends RecyclerView.ItemDecoration {

        private final int columnNum; // Total number of columns
        private final int leftRightSpace; // Left and right interval
        private final int topBottomSpace; // Upper and lower interval
        private final Drawable divider;// Dividing Line

        public GridDecoration(Drawable divider, int columnNum, int leftRightSpace, int topBottomSpace) {
            this.divider = divider;
            this.columnNum = columnNum;
            this.leftRightSpace = leftRightSpace;
            this.topBottomSpace = topBottomSpace;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view);
            int column = position % columnNum;

            int leftRightSpacePerColumn = leftRightSpace / columnNum;
            int leftSpace = column * leftRightSpacePerColumn;
            int rightSpace = leftRightSpace - (column + 1) * leftRightSpacePerColumn;

            if (parent.getAdapter().getItemCount() == 1) {
                outRect.left = leftRightSpace / 2;
                outRect.right = leftRightSpace / 2;
            } else {
                // 其他情况，根据列数计算左右间距
                outRect.left = leftSpace;
                outRect.right = rightSpace;
            }

            // Add top spacing for multiple lines
            if (position >= columnNum) {
                outRect.top = topBottomSpace;
            }
        }

        @Override
        public void onDraw(@NonNull Canvas canvas, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            canvas.save();
            final int childCount = parent.getChildCount();
            int rowNum = (int) Math.ceil(childCount * 1.0f / columnNum);
            final int divideLine = rowNum - 1;
            for (int i = 0; i < divideLine; i++) {
                View startChild = parent.getChildAt(i * columnNum);
                View endChild = parent.getChildAt(i * columnNum + (columnNum - 1));
                final int bottom = startChild.getBottom();
                final int top = bottom - divider.getIntrinsicHeight();
                divider.setBounds(startChild.getLeft(), top + topBottomSpace / 2, endChild.getRight(),
                    bottom + topBottomSpace / 2);
                divider.draw(canvas);
            }
            canvas.restore();
        }
    }

    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }

    public interface CallBack {

        void onRecentEmojiClick(String emoji, ZIMKitMessageModel messageModel);

        void onPagerEmojiClick(String emoji, ZIMKitMessageModel messageModel);

        void onPopActionClick(int position, ZIMKitMessageModel messageModel);
    }
}
