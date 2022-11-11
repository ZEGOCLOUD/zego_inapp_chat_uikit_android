package im.zego.zimkitcommon.components.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;

import im.zego.zimkitcommon.R;

public class TextAvatarView extends AppCompatTextView {
    public TextAvatarView(@NonNull Context context) {
        super(context);
        initView(context);
    }

    public TextAvatarView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public TextAvatarView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        this.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.common_shape_12dp_stroke_2a2a2a));//default
        this.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);//default
        this.setTextColor(ContextCompat.getColor(context, R.color.black));//default

        this.setGravity(Gravity.CENTER);
    }

    public void setAvatar(String avatar) {
        this.setText(avatar);
    }
}
