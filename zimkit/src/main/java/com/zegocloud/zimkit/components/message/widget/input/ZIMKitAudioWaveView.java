package com.zegocloud.zimkit.components.message.widget.input;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ZIMKitAudioWaveView extends View {

    private Paint paint;

    public ZIMKitAudioWaveView(Context context) {
        super(context);
        initView();
    }

    public ZIMKitAudioWaveView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public ZIMKitAudioWaveView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initView() {
        paint = new Paint();
        paint.setColor(Color.BLUE); // 设置波形图的颜色
        paint.setStyle(Paint.Style.FILL); // 设置填充样式

        setWillNotDraw(false);

        volumeBarWidth = dp2px(2, getResources().getDisplayMetrics());
        volumeBarMargin = dp2px(2, getResources().getDisplayMetrics());
    }


    private int volumeBarWidth;
    private int volumeBarMargin;
    private int volumeBarHeight;
    private int volumeBarCount;
    private int volumePointCount;
    private List<Integer> volumes = new ArrayList<>();

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        volumeBarCount =
            (getMeasuredWidth() - getPaddingStart() - getPaddingEnd()) / (volumeBarWidth + volumeBarMargin);

        volumePointCount = volumeBarCount;
    }

    private static final String TAG = "ZIMKitAudioWaveView";

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int i = 0; i < volumes.size(); i++) {
            Log.d(TAG, "onDraw() called volumes: i = [" + i + ",values: " + volumes.get(i) + "]");
        }
        int left;
        int right;
        for (int i = 0; i < volumeBarCount; i++) {
            left = getPaddingStart() + i * (volumeBarWidth + volumeBarMargin);
            right = left + volumeBarWidth;
            if (i < volumePointCount) {
                canvas.drawOval(left, getHeight() / 2 - volumeBarWidth / 2, right, getHeight() / 2 + volumeBarWidth / 2,
                    paint);
            } else {
                int startIndex = i - volumePointCount;
                volumeBarHeight = getHeight();
                int volume = volumes.get(startIndex);
                float ratio = Math.max(volume / 100f, 0.1f);
                int height = (int) (ratio * volumeBarHeight);
                canvas.drawRect(left, getHeight() / 2 - height / 2, right, getHeight() / 2 + height / 2, paint);
            }
        }
    }

    public void updateVolume(int volume) {
        Log.d(TAG, "updateVolume() called with: volume = [" + volume + "]");
        if (volumes.size() > volumeBarCount) {
            volumes.remove(0);
        }
        volumes.add(volume);
        if (volumePointCount > 0) {
            volumePointCount = volumePointCount - 1;
        }
        invalidate();
    }

    public static int dp2px(float v, DisplayMetrics displayMetrics) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, displayMetrics);
    }

    public void reset() {
        volumePointCount = volumeBarCount;
        volumes.clear();
        invalidate();
    }
}
