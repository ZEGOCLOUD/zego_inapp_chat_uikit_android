package com.zegocloud.zimkit.common.base;

import android.os.Bundle;
import android.view.MotionEvent;
import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.zegocloud.zimkit.common.utils.ZIMKitActivityUtils;
import com.zegocloud.zimkit.common.utils.ZIMKitKeyboardUtils.ActivityDispatchTouchEvent;
import java.lang.reflect.ParameterizedType;

public abstract class BaseActivity<T extends ViewDataBinding, VM extends ViewModel> extends AppCompatActivity {

    protected T mBinding;
    protected VM mViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        mBinding = DataBindingUtil.setContentView(this, getLayoutId());
        ViewCompat.setOnApplyWindowInsetsListener(mBinding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (getViewModelId() != 0) {
            Class<VM> entityClass = (Class<VM>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[1];
            mViewModel = new ViewModelProvider(this).get(entityClass);
            mBinding.setLifecycleOwner(this);
            mBinding.setVariable(getViewModelId(), mViewModel);
        }

        initView();
        initData();
    }

    protected abstract void initView();

    protected abstract int getLayoutId();

    protected abstract int getViewModelId();

    protected abstract void initData();

    @Override
    protected void onResume() {
        super.onResume();
        //Notification bar to cancel message notifications
        ZIMKitActivityUtils.clearAllNotifications();
    }

    /**
     * Whether to intercept clicks outside of EditText, close the input method
     *
     * @return false No treatment
     */
    protected boolean interceptClickInputClose() {
        return false;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            if (interceptClickInputClose()) {
                new ActivityDispatchTouchEvent().dispatchTouchEventCloseInput(ev, this);
            }
        }
        return super.dispatchTouchEvent(ev);
    }

}
