package com.zegocloud.zimkit.components.conversation.ui;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;
import android.view.animation.DecelerateInterpolator;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArraySet;
import androidx.core.view.GestureDetectorCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.OnChildAttachStateChangeListener;
import androidx.recyclerview.widget.RecyclerView.OnItemTouchListener;
import androidx.recyclerview.widget.RecyclerView.State;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SlideButtonDecor extends RecyclerView.ItemDecoration implements OnChildAttachStateChangeListener {

    private static final String TAG = "SlideButtonDecor";
    private static final int MIN_EXPAND_DISTANCE = 150;
    private List<SwipeButton> buttons = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private int mTouchSlop;
    private GestureDetectorCompat mDecorGestureDetector;
    private float mDownTouchX;
    private float mDownTranslationX;
    private float mDownTouchY;
    private ViewHolder mSelectedHolder;
    private Set<ViewHolder> mChangedViewHolderList = new ArraySet<>();
    private SwipeButtonProvider mSwipeButtonProvider;
    private Map<ViewHolder, ObjectAnimator> mAnimatorMap = new HashMap<>();
    private int allButtonWidth;
    private int touchEventTarget;
    private static final int TARGET_UNSELECT = 0;
    private static final int TARGET_ITEM = 1;
    private static final int TARGET_RECYCLERVIEW = 2;
    private OnItemTouchListener mOnItemTouchListener = new OnItemTouchListener() {

        @Override
        public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
            // always has MotionEvent.ACTION_DOWN in onInterceptTouchEvent.
            // if return true, onTouchEvent will be called ,and event will passed to it,too.
            // if return false, then ACTION_DOWN,MOVE,UP will received
            if (mDecorGestureDetector != null) {
                mDecorGestureDetector.onTouchEvent(e);
            }
            int actionMasked = e.getActionMasked();
            if (actionMasked == MotionEvent.ACTION_DOWN) {
                mDownTouchX = e.getX();
                mDownTouchY = e.getY();
                View child = mRecyclerView.findChildViewUnder(e.getX(), e.getY());
                if (child != null) {
                    mSelectedHolder = mRecyclerView.getChildViewHolder(child);
                    mDownTranslationX = mSelectedHolder.itemView.getTranslationX();
                    if (mDownTranslationX == 0) {
                        // is closed item.then prepare to show open menu.calc width first
                        buttons.clear();
                        if (mSwipeButtonProvider != null) {
                            List<SwipeButton> swipeButtons = mSwipeButtonProvider.onSwipeButtonRequired(
                                mSelectedHolder);
                            buttons.addAll(swipeButtons);
                        }
                        allButtonWidth = 0;
                        for (SwipeButton button : buttons) {
                            allButtonWidth = allButtonWidth + button.buttonWidth;
                        }
                    }

                    // close other opened item
                    for (ViewHolder viewHolder : mChangedViewHolderList) {
                        if (viewHolder != mSelectedHolder) {
                            collapseSlideMenu(viewHolder);
                        }
                    }
                    mChangedViewHolderList.clear();
                    // add now or item will tinkle
                    addToChangeList(mSelectedHolder);
                }
                requestDisallowIntercept();
            } else if (actionMasked == MotionEvent.ACTION_MOVE) {
                float currentX = e.getX();
                float currentY = e.getY();
                float dx = currentX - mDownTouchX;
                float dy = currentY - mDownTouchY;
                if (touchEventTarget == TARGET_UNSELECT) {
                    if (mSelectedHolder != null) {
                        if (Math.abs(dy) > mTouchSlop) {
                            touchEventTarget = TARGET_RECYCLERVIEW;
                            return false;
                        } else if (Math.abs(dx) > mTouchSlop) {
                            touchEventTarget = TARGET_ITEM;
                            return true;
                        }
                    }
                }
            } else if (actionMasked == MotionEvent.ACTION_CANCEL || actionMasked == MotionEvent.ACTION_UP) {
                touchEventTarget = TARGET_UNSELECT;
                unselectPressedHolder();
                requestDisallowIntercept();
            }
            return false;
        }

        @Override
        public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
            //            Log.d(TAG, "onTouchEvent() called with e = [" + e.getActionMasked() + "]");
            int actionMasked = e.getActionMasked();
            if (actionMasked == MotionEvent.ACTION_DOWN) {
                mDownTouchX = e.getX();
                mDownTouchY = e.getY();
                mDownTranslationX = mSelectedHolder.itemView.getTranslationX();

                if (mDownTranslationX == 0) {
                    // is closed item.then prepare to show open menu.calc width first
                    buttons.clear();
                    if (mSwipeButtonProvider != null) {
                        List<SwipeButton> swipeButtons = mSwipeButtonProvider.onSwipeButtonRequired(mSelectedHolder);
                        buttons.addAll(swipeButtons);
                    }
                    allButtonWidth = 0;
                    for (SwipeButton button : buttons) {
                        allButtonWidth = allButtonWidth + button.buttonWidth;
                    }
                }

                // close other opened item
                for (ViewHolder viewHolder : mChangedViewHolderList) {
                    if (viewHolder != mSelectedHolder) {
                        collapseSlideMenu(viewHolder);
                    }
                }
                mChangedViewHolderList.clear();
                // add now or item will tinkle
                addToChangeList(mSelectedHolder);

            } else if (actionMasked == MotionEvent.ACTION_MOVE) {
                float currentX = e.getX();
                float currentY = e.getY();
                float dx = currentX - mDownTouchX;
                float dy = currentY - mDownTouchY;
                if (mSelectedHolder != null) {
                    int max = 0;  // can only slide right to original position
                    float translationX = mDownTranslationX + dx;
                    if (translationX > max || Math.abs(translationX) > allButtonWidth) {
                        return;
                    }
                    mSelectedHolder.itemView.setTranslationX(translationX);
                    addToChangeList(mSelectedHolder);
                }
            } else if (actionMasked == MotionEvent.ACTION_CANCEL || actionMasked == MotionEvent.ACTION_UP) {
                if (allButtonWidth > 0) {
                    // if from expand to close,
                    if (mDownTranslationX < 0) {
                        if (mSelectedHolder != null) {
                            if (e.getX() - mDownTouchX > mTouchSlop) {
                                collapseSlideMenu(mSelectedHolder);
                                removeFromChanged(mSelectedHolder);
                            } else {
                                expandSlideMenu(mSelectedHolder, allButtonWidth);
                            }
                        }
                    } else {
                        // from close to expand
                        if (mSelectedHolder != null) {
                            float translationX = mSelectedHolder.itemView.getTranslationX();
                            if (Math.abs(translationX) > Math.min(MIN_EXPAND_DISTANCE, allButtonWidth / 2)) {
                                expandSlideMenu(mSelectedHolder, allButtonWidth);
                            } else {
                                collapseSlideMenu(mSelectedHolder);
                                removeFromChanged(mSelectedHolder);
                            }
                        }
                    }
                }
                touchEventTarget = TARGET_UNSELECT;
                unselectPressedHolder();
                requestDisallowIntercept();
            }
            mRecyclerView.invalidate();
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    };

    private void addToChangeList(ViewHolder holder) {
        if (mSelectedHolder != null) {
            mChangedViewHolderList.add(holder);
        }
    }

    private void removeFromChanged(ViewHolder holder) {
        if (mSelectedHolder != null) {
            mChangedViewHolderList.remove(holder);
        }
    }

    // decor and blank in recycler view
    private SimpleOnGestureListener mDecorGestureListener = new SimpleOnGestureListener() {
        @Override
        public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {
            View view = findChildViewUnderWithoutTranslation(e.getX(), e.getY());
            if (view != null) {
                ViewHolder touchedHolder = mRecyclerView.getChildViewHolder(view);
                boolean inRangeOfButton = false;
                for (SwipeButton button : buttons) {
                    inRangeOfButton = button.inRangeOfButton(e.getX(), e.getY());
                    if (inRangeOfButton) {
                        if (button.getClickListener() != null) {
                            button.getClickListener().onSingleTapConfirmed(touchedHolder.getAdapterPosition(), button,
                                SlideButtonDecor.this);
                        }
                        collapseSlideMenu(touchedHolder);
                        break;
                    }
                }
                if (!inRangeOfButton) {
                    // click original item range
                    collapseSlideMenu(touchedHolder);
                }
            } else {
                // click in blank point of recyclerview
                for (ViewHolder viewHolder : mChangedViewHolderList) {
                    collapseSlideMenu(viewHolder);
                }
            }

            return super.onSingleTapConfirmed(e);
        }
    };

    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull State state) {
        super.onDraw(c, parent, state);
        //        for (int i = 0; i < mRecyclerView.getChildCount(); i++) {
        //            View itemView = mRecyclerView.getChildAt(i);
        //            extracted(c, itemView, itemView.getTranslationX());
        //        }
        //        Log.d(TAG, "onDraw: start");
        for (ViewHolder viewHolder : mChangedViewHolderList) {
            View itemView = viewHolder.itemView;
            drawButtonList(c, itemView, itemView.getTranslationX());
        }
        //        Log.d(TAG, "onDraw: end");
    }

    private void requestDisallowIntercept() {
        final ViewParent rvParent = mRecyclerView.getParent();
        if (rvParent != null) {
            rvParent.requestDisallowInterceptTouchEvent(mSelectedHolder != null);
        }
    }

    private void drawButtonList(@NonNull Canvas c, View itemView, float translationX) {
        float right = itemView.getRight();

        for (SwipeButton button : buttons) {
            float currentButtonWidth = button.buttonWidth * (Math.abs(translationX) / allButtonWidth);
            float left = right - currentButtonWidth;
            //            Log.d(TAG, "drawButtonList: " + button.getText());
            button.onDraw(c, new RectF(left, itemView.getTop(), right, itemView.getBottom()));
            right = left;
        }
    }

    public void attachToRecyclerView(@Nullable RecyclerView recyclerView) {
        if (mRecyclerView == recyclerView) {
            return; // nothing to do
        }
        if (mRecyclerView != null) {
            destroyCallbacks();
        }
        mRecyclerView = recyclerView;
        if (recyclerView != null) {
            final Resources resources = recyclerView.getResources();
            setupCallbacks();
        }
    }

    private void setupCallbacks() {
        ViewConfiguration vc = ViewConfiguration.get(mRecyclerView.getContext());
        mTouchSlop = vc.getScaledTouchSlop();
        mRecyclerView.addItemDecoration(this);
        mRecyclerView.addOnItemTouchListener(mOnItemTouchListener);
        mRecyclerView.addOnChildAttachStateChangeListener(this);

        mDecorGestureDetector = new GestureDetectorCompat(mRecyclerView.getContext(), mDecorGestureListener);

    }

    private void destroyCallbacks() {
        mRecyclerView.removeItemDecoration(this);
        mRecyclerView.removeOnItemTouchListener(mOnItemTouchListener);
        mRecyclerView.removeOnChildAttachStateChangeListener(this);

        for (ObjectAnimator animator : mAnimatorMap.values()) {
            animator.cancel();
        }
        mAnimatorMap.clear();
        mChangedViewHolderList.clear();

        if (mDecorGestureListener != null) {
            mDecorGestureListener = null;
        }
        if (mDecorGestureDetector != null) {
            mDecorGestureDetector = null;
        }
    }

    /**
     * expand to show swipe button
     */
    private void expandSlideMenu(ViewHolder viewHolder, int allButtonWidth) {
        if (viewHolder != null) {
            float startX = viewHolder.itemView.getTranslationX();
            float endX = -allButtonWidth;
            long duration = 200;

            ObjectAnimator animator = ObjectAnimator.ofFloat(viewHolder.itemView, "translationX", startX, endX);
            animator.setDuration(duration);
            animator.setInterpolator(new DecelerateInterpolator());
            mAnimatorMap.put(viewHolder, animator);
            animator.start();
            animator.addUpdateListener(new AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(@NonNull ValueAnimator animation) {
                    if (mRecyclerView != null) {
                        mRecyclerView.invalidate();
                    }
                    if (animation.getAnimatedFraction() == 1) {
                        mAnimatorMap.remove(viewHolder);
                    }
                }
            });
        }
    }


    public void invalidate() {
        if (mRecyclerView != null) {
            mRecyclerView.invalidate();
        }
    }


    /**
     * collapse to default
     *
     * @param viewHolder
     */
    private void collapseSlideMenu(ViewHolder viewHolder) {
        if (viewHolder != null) {
            float startX = viewHolder.itemView.getTranslationX();
            float endX = 0;
            long duration = 200;

            ObjectAnimator animator = ObjectAnimator.ofFloat(viewHolder.itemView, "translationX", startX, endX);
            animator.setDuration(duration);
            animator.setInterpolator(new DecelerateInterpolator());
            mAnimatorMap.put(viewHolder, animator);
            animator.start();
            animator.addUpdateListener(new AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(@NonNull ValueAnimator animation) {
                    if (mRecyclerView != null) {
                        mRecyclerView.invalidate();
                    }
                    if (animation.getAnimatedFraction() == 1) {
                        mAnimatorMap.remove(viewHolder);
                    }
                }
            });
        }
    }

    private void unselectPressedHolder() {
        if (mSelectedHolder != null) {
            mSelectedHolder = null;
        }
    }

    public void setSwipeButtonProvider(SwipeButtonProvider swipeButtonProvider) {
        this.mSwipeButtonProvider = swipeButtonProvider;
    }

    public View findChildViewUnderWithoutTranslation(float x, float y) {
        final int count = mRecyclerView.getChildCount();
        for (int i = count - 1; i >= 0; i--) {
            final View child = mRecyclerView.getChildAt(i);
            final float translationX = child.getTranslationX();
            final float translationY = child.getTranslationY();
            if (x >= child.getLeft() && x <= child.getRight() && y >= child.getTop() + translationY
                && y <= child.getBottom() + translationY) {
                return child;
            }
        }
        return null;
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

    @Override
    public void onChildViewAttachedToWindow(@NonNull View view) {
    }

    @Override
    public void onChildViewDetachedFromWindow(@NonNull View view) {
        final ViewHolder holder = mRecyclerView.getChildViewHolder(view);
        if (holder == null) {
            return;
        }
        if (mSelectedHolder != null && holder == mSelectedHolder) {
            unselectPressedHolder();
        }
        ObjectAnimator objectAnimator = mAnimatorMap.get(holder);
        if (objectAnimator != null) {
            objectAnimator.cancel();
        }
        view.setTranslationX(0);
        mChangedViewHolderList.remove(holder);
    }

    public static class SwipeButton {

        private int backgroundColor;
        private String text;
        private final int textSize;
        private final int textColor;
        // pre designed width
        private int buttonWidth;
        private final Paint p;
        private RectF rectF; // real size
        private ClickListener clickListener;
        private boolean activated;

        public SwipeButton(String text, int textColor, int textSize, int backgroundColor, int buttonWidth,
            ClickListener clickListener) {
            this.text = text;
            this.backgroundColor = backgroundColor;
            this.textSize = textSize;
            this.textColor = textColor;
            this.buttonWidth = buttonWidth;
            p = new Paint();
            this.clickListener = clickListener;
        }

        public void onDraw(Canvas c, RectF rect) {
            // Draw background
            p.setColor(backgroundColor);
            c.drawRect(rect, p);

            // Draw Text
            p.setColor(textColor);
            p.setTextSize(textSize);

            Rect r = new Rect();
            float cHeight = rect.height();
            float cWidth = rect.width();
            p.setTextAlign(Paint.Align.LEFT);
            p.getTextBounds(text, 0, text.length(), r);
            float x = cWidth / 2f - r.width() / 2f - r.left;
            float y = cHeight / 2f + r.height() / 2f - r.bottom;
            c.drawText(text, rect.left + x, rect.top + y, p);
            rectF = rect;
        }

        boolean inRangeOfButton(float x, float y) {
            if (rectF != null && rectF.contains(x, y)) {
                return true;
            }
            return false;
        }

        ClickListener getClickListener() {
            return clickListener;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public void setActivated(boolean activated) {
            this.activated = activated;
        }

        public boolean isActivated() {
            return activated;
        }

        public void setBackgroundColor(int backgroundColor) {
            this.backgroundColor = backgroundColor;
        }

        public void setButtonWidth(int buttonWidth) {
            this.buttonWidth = buttonWidth;
        }
    }

    public interface ClickListener {

        void onSingleTapConfirmed(int position, SwipeButton button, SlideButtonDecor slideButtonDecor);
    }

    public interface SwipeButtonProvider {

        List<SwipeButton> onSwipeButtonRequired(ViewHolder viewHolder);
    }
}
