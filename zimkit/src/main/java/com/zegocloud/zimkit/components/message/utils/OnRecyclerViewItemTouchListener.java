package com.zegocloud.zimkit.components.message.utils;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GestureDetectorCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * You need to make view clickable to receive click events
 */
public class OnRecyclerViewItemTouchListener extends RecyclerView.SimpleOnItemTouchListener {

    private GestureDetectorCompat mGestureDetector;
    private RecyclerView attachedRecyclerView;

    public OnRecyclerViewItemTouchListener(RecyclerView recyclerView) {
        attachedRecyclerView = recyclerView;
        mGestureDetector = new GestureDetectorCompat(recyclerView.getContext(),
            new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    View pressedChildView = attachedRecyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (pressedChildView != null) {
                        RecyclerView.ViewHolder holder = attachedRecyclerView.getChildViewHolder(pressedChildView);
                        // BFS find views
                        List<View> allViewsInThisItem = new ArrayList<>();
                        traverseViews(pressedChildView, allViewsInThisItem);

                        // reverse to find child from deep view to root view
                        List<View> clickableViewsUnderPressPosition = IntStream.range(0, allViewsInThisItem.size())
                            .mapToObj(index -> allViewsInThisItem.get(allViewsInThisItem.size() - 1 - index))
                            .filter(view -> view.isClickable() && inRangeOfView(view, e)).collect(Collectors.toList());

                        // if child click returns true,then no itemClick
                        boolean consumed = false;
                        for (int i = 0; i < clickableViewsUnderPressPosition.size(); i++) {
                            View clickableView = clickableViewsUnderPressPosition.get(i);
                            consumed = onItemChildClick(holder, clickableView);
                            if (consumed) {
                                break;
                            }
                        }
                        // no click response,item response
                        if (!consumed) {
                            onItemClick(holder);
                        }
                    } else {
                        // cannot find pressed Item
                        onNoItemClick();
                    }
                    return true;
                }

                @Override
                public boolean onDown(MotionEvent e) {
                    View pressedChildView = attachedRecyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (pressedChildView != null) {
                        RecyclerView.ViewHolder holder = attachedRecyclerView.getChildViewHolder(pressedChildView);
                        onItemDown(holder);
                    } else {
                        onNoItemDown();
                    }
                    onRecyclerViewDown();
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    super.onLongPress(e);
                    View pressedChildView = attachedRecyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (pressedChildView != null) {
                        RecyclerView.ViewHolder holder = attachedRecyclerView.getChildViewHolder(pressedChildView);
                        // BFS find views
                        List<View> allViewsInThisItem = new ArrayList<>();
                        traverseViews(pressedChildView, allViewsInThisItem);

                        // reverse to find child from deep view to root view
                        List<View> clickableViewsUnderPressPosition = IntStream.range(0, allViewsInThisItem.size())
                            .mapToObj(index -> allViewsInThisItem.get(allViewsInThisItem.size() - 1 - index))
                            .filter(view -> view.isClickable() && inRangeOfView(view, e)).collect(Collectors.toList());

                        // if child click returns true,then no itemClick
                        boolean consumed = false;
                        for (int i = 0; i < clickableViewsUnderPressPosition.size(); i++) {
                            View clickableView = clickableViewsUnderPressPosition.get(i);
                            consumed = onItemChildLongPress(holder, clickableView);
                            if (consumed) {
                                break;
                            }
                        }
                        // no click response,item response
                        if (!consumed) {
                            onItemLongPress(holder);
                        }
                    }
                }

                @Override
                public boolean onScroll(@Nullable MotionEvent e1, @NonNull MotionEvent e2, float distanceX,
                    float distanceY) {

                    onRecyclerViewScroll(e1, e2, distanceX, distanceY);

                    return super.onScroll(e1, e2, distanceX, distanceY);
                }
            });
    }

    private void traverseViews(View view, List<View> views) {
        if (view != null) {
            views.add(view);
            if (view instanceof ViewGroup) {
                ViewGroup group = (ViewGroup) view;
                for (int i = 0; i < group.getChildCount(); i++) {
                    traverseViews(group.getChildAt(i), views);
                }
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        mGestureDetector.onTouchEvent(e);
        return false;
    }

    private boolean inRangeOfView(View view, MotionEvent ev) {
        int[] location = new int[2];
        if (view.getVisibility() != View.VISIBLE) {
            return false;
        } else {
            view.getLocationOnScreen(location);
            int x = location[0];
            int y = location[1];
            return ev.getRawX() >= (float) x && ev.getRawX() <= (float) (x + view.getWidth())
                && ev.getRawY() >= (float) y && ev.getRawY() <= (float) (y + view.getHeight());
        }
    }

    public boolean onItemChildClick(RecyclerView.ViewHolder vh, View itemChild) {
        return false;
    }

    public void onItemClick(RecyclerView.ViewHolder vh) {

    }

    public void onItemDown(RecyclerView.ViewHolder vh) {

    }

    public void onRecyclerViewDown() {

    }

    public void onItemLongPress(RecyclerView.ViewHolder holder) {

    }

    public void onNoItemClick() {

    }

    public void onRecyclerViewScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

    }

    protected boolean onItemChildLongPress(ViewHolder holder, View clickableView) {
        return false;
    }

    protected void onNoItemDown() {

    }
}
