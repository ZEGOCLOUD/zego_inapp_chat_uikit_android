package im.zego.zimkitmessages.component.popmenu;

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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import im.zego.zimkitcommon.utils.ZIMKitScreenUtils;
import im.zego.zimkitmessages.R;
import im.zego.zimkitmessages.widget.message.MessageRecyclerView;

public class MessagePopMenu {

    // Number of columns
    private int COLUMN_NUM = 2;
    // Shade width
    private static final int SHADOW_WIDTH = 10;
    // Pop-up window display height offset (slightly upward display)
    private static final int Y_OFFSET = 8;

    private final PopupWindow popupWindow;
    private final Context context;
    private RecyclerView recyclerView;
    private View popupView;
    private final MenuAdapter adapter;
    private final List<ChatPopMenuAction> chatPopMenuActionList = new ArrayList<>();
    private MessagePopMenu messagePopMenu;
    private MessageRecyclerView.OnEmptySpaceClickListener mEmptySpaceClickListener;

    public MessagePopMenu(Context context, int columnNum) {
        messagePopMenu = this;
        this.context = context;
        this.COLUMN_NUM = columnNum >= 4 ? 4 : columnNum;
        popupView = LayoutInflater.from(context).inflate(R.layout.message_layout_pop_menu, null);
        recyclerView = popupView.findViewById(R.id.chat_pop_menu_content_view);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, COLUMN_NUM);
        recyclerView.setLayoutManager(gridLayoutManager);
        adapter = new MenuAdapter();
        recyclerView.setAdapter(adapter);

        int spaceWidth = context.getResources().getDimensionPixelSize(R.dimen.message_pop_menu_item_space_width);
        int spaceHeight = context.getResources().getDimensionPixelSize(R.dimen.message_pop_menu_item_space_height);
        Drawable divider = context.getResources().getDrawable(R.drawable.message_shape_pop_menu_divider);
        recyclerView.addItemDecoration(new GridDecoration(divider, COLUMN_NUM, spaceWidth, spaceHeight));
        popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, false);
        popupWindow.setBackgroundDrawable(new ColorDrawable());
        popupWindow.setTouchable(true);
        popupWindow.setOutsideTouchable(true);
    }

    public void show(View anchorView, int minY) {
        if (chatPopMenuActionList.size() == 0) {
            return;
        }
        float anchorWidth = anchorView.getWidth();
        float anchorHeight = anchorView.getHeight();
        int[] location = new int[2];
        anchorView.getLocationOnScreen(location);
        // Small triangle height
        int indicatorHeight = context.getResources().getDimensionPixelOffset(R.dimen.message_pop_menu_indicator_height);
        int rowCount = (int) Math.ceil(chatPopMenuActionList.size() * 1.0f / COLUMN_NUM);
        if (popupWindow != null) {
            int itemSpaceWidth = context.getResources().getDimensionPixelSize(R.dimen.message_pop_menu_item_space_width);
            int itemSpaceHeight = context.getResources().getDimensionPixelSize(R.dimen.message_pop_menu_item_space_height);

            int itemWidth = ZIMKitScreenUtils.dip2px(60f);
            int itemHeight = ZIMKitScreenUtils.dip2px(60f);

            int paddingLeftRight = ZIMKitScreenUtils.dip2px(18.0f);
            int paddingTopBottom = ZIMKitScreenUtils.dip2px(12.5f);

            int columnNum = Math.min(chatPopMenuActionList.size(), COLUMN_NUM);

            int popWidth = itemWidth * columnNum + paddingLeftRight * 2 + (columnNum - 1) * itemSpaceWidth - SHADOW_WIDTH;
            int popHeight = itemHeight * rowCount + paddingTopBottom * 2 + (rowCount - 1) * itemSpaceHeight - SHADOW_WIDTH;

            float indicatorX = anchorWidth / 2;
            int screenWidth = ZIMKitScreenUtils.getScreenWidth(context);
            int x = location[0];
            int y = location[1] - popHeight - indicatorHeight - Y_OFFSET;
            // If it is on the right, both the small arrow x coordinate and the pop-up x position should change
            if (location[0] * 2 + anchorWidth > screenWidth) {
                indicatorX = popWidth - anchorWidth / 2;
                x = (int) (location[0] + anchorWidth - popWidth);
            }
            // If the height is less than the given minimum height, it is too far up and will cover the title bar, to be displayed below
            boolean isTop = y <= minY;
            if (isTop) {
                y = (int) (location[1] + anchorHeight) + Y_OFFSET;
                popHeight = popHeight - indicatorHeight;
            }

            if (indicatorX <= 0 || indicatorX > popWidth || popWidth < anchorWidth) {
                indicatorX = popWidth * 1.0f / 2;
            }

            int radius = ZIMKitScreenUtils.dip2px(8.0f);
            Drawable drawable = getBackgroundDrawable(popWidth, popHeight, indicatorX, indicatorHeight, isTop, radius);
            popupView.setBackground(drawable);
            popupWindow.showAtLocation(anchorView, Gravity.NO_GRAVITY, x, y);
        }
    }

    public void hide() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
    }

    public void setChatPopMenuActionList(List<ChatPopMenuAction> actionList) {
        chatPopMenuActionList.clear();
        chatPopMenuActionList.addAll(actionList);
        adapter.notifyDataSetChanged();
    }

    public void setEmptySpaceClickListener(MessageRecyclerView.OnEmptySpaceClickListener mEmptySpaceClickListener) {
        this.mEmptySpaceClickListener = mEmptySpaceClickListener;
    }

    private ChatPopMenuAction getChatPopMenuAction(int position) {
        return chatPopMenuActionList.get(position);
    }

    /**
     * Draw popup background with small triangle
     */
    public Drawable getBackgroundDrawable(final float widthPixel, final float heightPixel, float indicatorX, float indicatorHeight, boolean isTop, float radius) {
        int borderWidth = SHADOW_WIDTH;

        Path path = new Path();
        Drawable drawable = new Drawable() {
            @Override
            public void draw(@NonNull Canvas canvas) {
                Paint paint = new Paint();
                paint.setColor(Color.parseColor("#cc000000"));
                paint.setStyle(Paint.Style.FILL);

//                paint.setShadowLayer(borderWidth, 0, 0, 0xFFAAAAAA);

                // Small triangle arrow on top
                if (isTop) {
                    path.addRoundRect(new RectF(borderWidth, indicatorHeight + borderWidth, widthPixel - borderWidth, heightPixel + indicatorHeight - borderWidth), radius, radius, Path.Direction.CW);
                    path.moveTo(indicatorX - indicatorHeight, indicatorHeight + borderWidth);
                    path.lineTo(indicatorX, borderWidth);
                    path.lineTo(indicatorX + indicatorHeight, indicatorHeight + borderWidth);
                } else {
                    path.addRoundRect(new RectF(borderWidth, borderWidth, widthPixel - borderWidth, heightPixel - borderWidth), radius, radius, Path.Direction.CW);
                    path.moveTo(indicatorX - indicatorHeight, heightPixel - borderWidth);
                    path.lineTo(indicatorX, heightPixel + indicatorHeight - borderWidth);
                    path.lineTo(indicatorX + indicatorHeight, heightPixel - borderWidth);
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

    class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.MenuItemViewHolder> {
        @NonNull
        @Override
        public MenuAdapter.MenuItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.message_layout_pop_menu_item, null);
            return new MenuItemViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MenuAdapter.MenuItemViewHolder holder, int position) {
            ChatPopMenuAction chatPopMenuAction = getChatPopMenuAction(position);
            holder.title.setText(chatPopMenuAction.actionName);
            Drawable drawable = ResourcesCompat.getDrawable(context.getResources(), chatPopMenuAction.actionIcon, null);
            holder.icon.setImageDrawable(drawable);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    chatPopMenuAction.actionClickListener.onClick();
                    messagePopMenu.hide();

                    if (mEmptySpaceClickListener != null) {
                        mEmptySpaceClickListener.onClick();
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return chatPopMenuActionList.size();
        }

        class MenuItemViewHolder extends RecyclerView.ViewHolder {
            public TextView title;
            public ImageView icon;

            public MenuItemViewHolder(@NonNull View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.menu_title);
                icon = itemView.findViewById(R.id.menu_icon);
            }
        }
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

            outRect.left = column * leftRightSpace / columnNum;
            outRect.right = leftRightSpace - (column + 1) * leftRightSpace / columnNum;

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
                divider.setBounds(startChild.getLeft(), top + topBottomSpace / 2, endChild.getRight(), bottom + topBottomSpace / 2);
                divider.draw(canvas);
            }
            canvas.restore();
        }
    }

    public static class ChatPopMenuAction {
        private String actionName;
        private int actionIcon;
        private OnClickListener actionClickListener;

        public void setActionName(String actionName) {
            this.actionName = actionName;
        }

        public String getActionName() {
            return actionName;
        }

        public void setActionIcon(int actionIcon) {
            this.actionIcon = actionIcon;
        }

        public int getActionIcon() {
            return actionIcon;
        }

        public void setActionClickListener(OnClickListener actionClickListener) {
            this.actionClickListener = actionClickListener;
        }

        public OnClickListener getActionClickListener() {
            return actionClickListener;
        }

        @FunctionalInterface
        public interface OnClickListener {
            void onClick();
        }
    }

}
