package com.zegocloud.zimkit.components.message.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.zegocloud.zimkit.R;

public class ZIMKitMessageTimeLineDecoration extends RecyclerView.ItemDecoration {
    private final DecorationCallback mCallBack;
    private final TextPaint mTextPaint;
    private final float topGap;
    private final int paddingBottom;

    public ZIMKitMessageTimeLineDecoration(Context context, DecorationCallback decorationCallback) {
        mCallBack = decorationCallback;
        Resources res = context.getResources();
        mTextPaint = new TextPaint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(getSPSize(context, 12));
        mTextPaint.setColor(res.getColor(R.color.color_b8b8b8));
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        Rect rect = new Rect();
        mTextPaint.getTextBounds(res.getString(R.string.zimkit_test_time), 0, res.getString(R.string.zimkit_test_time).length(), rect);
        int h = rect.height();

        int paddingTop = res.getDimensionPixelSize(R.dimen.message_time_decoration_top_padding);
        paddingBottom = res.getDimensionPixelSize(R.dimen.message_time_decoration_bottom_padding);
        topGap = h + paddingBottom + paddingTop;// measure height
    }

    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.onDraw(c, parent, state);
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();
        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = parent.getChildAt(i);
            int position = parent.getChildAdapterPosition(view);
            boolean needAddTimeLine = mCallBack.needAddTimeLine(position);
            if (needAddTimeLine) {
                String time = mCallBack.getTimeLine(position);
                if (TextUtils.isEmpty(time)) return;
                float top = view.getTop() - paddingBottom;
                c.drawText(time, (left + right) >> 1, top, mTextPaint);//绘制文本
            }
        }
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        int pos = parent.getChildAdapterPosition(view);
        boolean needAddTimeLine = mCallBack.needAddTimeLine(pos);
        if (needAddTimeLine) {
            outRect.top = (int) topGap;
        } else {
            outRect.top = 0;
        }
    }

    /**
     * Convert px values to sp values, ensuring that the text size remains unchanged
     */
    public static float getSPSize(Context context, float size) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, size, context.getResources().getDisplayMetrics());
    }

    public interface DecorationCallback {
        boolean needAddTimeLine(int position);

        String getTimeLine(int position);
    }
}
