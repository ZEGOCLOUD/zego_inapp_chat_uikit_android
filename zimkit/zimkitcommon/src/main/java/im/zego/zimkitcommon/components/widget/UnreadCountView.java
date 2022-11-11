package im.zego.zimkitcommon.components.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;

import im.zego.zimkitcommon.R;

public class UnreadCountView extends AppCompatTextView {

    public UnreadCountView(Context context) {
        super(context);
        initView(context);
    }

    public UnreadCountView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public UnreadCountView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        this.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.common_shape_oval_ff4a50));//default
        this.setTextSize(TypedValue.COMPLEX_UNIT_SP, 9);//default
        this.setWidth(40);//default
        this.setHeight(40);//default
        this.setTextColor(ContextCompat.getColor(context, R.color.white));//default
        this.setGravity(Gravity.CENTER);
    }
}
