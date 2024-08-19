package com.zegocloud.zimkit.components.message.utils;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import androidx.core.view.GestureDetectorCompat;
import androidx.recyclerview.widget.RecyclerView;

public class OnRecyclerViewItemTouchListener extends RecyclerView.SimpleOnItemTouchListener {
    private GestureDetectorCompat mGestureDetector;
    private RecyclerView attachedRecyclerView;

    public OnRecyclerViewItemTouchListener(RecyclerView recyclerView) {
        attachedRecyclerView = recyclerView;
        mGestureDetector = new GestureDetectorCompat(recyclerView.getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                View pressedChildView = attachedRecyclerView.findChildViewUnder(e.getX(), e.getY());
                if (pressedChildView != null) {
                    RecyclerView.ViewHolder holder = attachedRecyclerView.getChildViewHolder(pressedChildView);
                    if (findCallbackChild(e, pressedChildView, holder)) {
                        return true;
                    }
                    if (pressedChildView.isClickable()) {
                        View.OnClickListener onClickListener = view -> {
                        };
                        onClickListener.onClick(pressedChildView);
                        onItemClick(holder);
                    }
                } else {
                    onNoChildClicked();
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
                    View.OnLongClickListener onClickListener = view -> true;
                    onClickListener.onLongClick(pressedChildView);
                    onItemLongPress(holder);
                }
            }
        });
    }

    private boolean findCallbackChild(MotionEvent e, View pressedChildView, RecyclerView.ViewHolder holder) {
        if (pressedChildView instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) pressedChildView).getChildCount(); i++) {
                View child = ((ViewGroup) pressedChildView).getChildAt(i);
                if (child instanceof ViewGroup) {
                    boolean result = findCallbackChild(e, child, holder);
                    if (result) {
                        return true;
                    } else {
                        if (child.isClickable() && inRangeOfView(child, e)) {
                            return true;
                        }
                    }
                } else {
                    if (child.isClickable() && inRangeOfView(child, e)) {
                        View.OnClickListener onClickListener = view -> {
                        };
                        onClickListener.onClick(child);
                        onItemChildClick(holder, child);
                        return true;
                    }
                }
            }
        }
        return false;
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
            return ev.getRawX() >= (float) x && ev.getRawX() <= (float) (x + view.getWidth()) && ev.getRawY() >= (float) y && ev.getRawY() <= (float) (y + view.getHeight());
        }
    }

    public void onItemChildClick(RecyclerView.ViewHolder vh, View itemChild) {

    }

    public void onItemClick(RecyclerView.ViewHolder vh) {

    }

    public void onItemDown(RecyclerView.ViewHolder vh) {

    }

    public void onRecyclerViewDown() {

    }

    public void onItemLongPress(RecyclerView.ViewHolder holder) {

    }

    public void onNoChildClicked() {

    }
}
