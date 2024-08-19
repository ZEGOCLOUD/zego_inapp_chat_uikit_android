package com.zegocloud.zimkit.components.message.ui;

import android.content.Context;
import android.util.AttributeSet;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.switchmaterial.SwitchMaterial;

/**
 * suspend setChecked method,and send a request to server to update state, <br/> suspend until we get the operate
 * result.
 */
public class AsynchronousSwitch extends SwitchMaterial {

    private Asynchronous asynchronous;

    public AsynchronousSwitch(@NonNull Context context) {
        super(context);
    }

    public AsynchronousSwitch(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AsynchronousSwitch(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setChecked(boolean checked) {
        if (asynchronous != null) {
            asynchronous.beforeApplyState(this, checked);
        } else {
            realSetChecked(checked);
        }
    }

    public void realSetChecked(boolean checked) {
        super.setChecked(checked);
    }

    public void setAsynchronous(Asynchronous asynchronous) {
        this.asynchronous = asynchronous;
    }

    public interface Asynchronous {

        void beforeApplyState(AsynchronousSwitch aSwitch, boolean originalCheck);
    }
}
