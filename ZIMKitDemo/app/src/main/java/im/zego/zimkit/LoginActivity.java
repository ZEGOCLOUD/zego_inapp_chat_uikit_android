package im.zego.zimkit;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import im.zego.zim.enums.ZIMConnectionEvent;
import im.zego.zim.enums.ZIMConnectionState;
import im.zego.zimkit.databinding.ActivityLoginBinding;
import im.zego.zimkitcommon.ZIMKitConstant;
import im.zego.zimkitcommon.ZIMKitManager;
import im.zego.zimkitcommon.ZIMKitRouter;
import im.zego.zimkitcommon.components.widget.ProcessView;
import im.zego.zimkitcommon.interfaces.ZIMKitEventListener;
import im.zego.zimkitcommon.utils.ZIMKitActivityUtils;
import im.zego.zimkitcommon.utils.ZIMKitCheckDoubleClick;
import im.zego.zimkitcommon.utils.ZIMKitKeyboardUtils;
import im.zego.zimkitcommon.utils.ZIMKitToastUtils;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding viewDataBinding;
    private LoginViewModel mViewModel;
    // loading
    private ProcessView processView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewDataBinding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        mViewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        viewDataBinding.setLoginRoomVM(mViewModel);
        viewDataBinding.setLifecycleOwner(this);
        initData();
    }

    private void initData() {
        processView = new ProcessView(this);
        viewDataBinding.btnLogin.setOnClickListener(v -> {
            if (ZIMKitCheckDoubleClick.isFastDoubleClick(1000)) {
                return;
            }
            processView.setIsShowShade(true).setCancelable(false).show((ViewGroup) viewDataBinding.btnLogin.getRootView());
            mViewModel.connectUser();
        });

        mViewModel.mLoginStateLiveData.observe(this, booleanStringPair -> {
            if (processView != null) {
                processView.dismiss();
            }
            if (booleanStringPair.first) {
                ZIMKitRouter.to(this, ZIMKitConstant.RouterConstant.ROUTER_CONVERSATION, null);
            }
            initListener();
        });
    }

    private void initListener() {

        ZIMKitManager.share().addZIMKitEventListener(new ZIMKitEventListener() {
            @Override
            public void onTotalUnreadMessageCountChange(int totalCount) {

            }

            @Override
            public void onConnectionStateChange(ZIMConnectionEvent connectionEvent, ZIMConnectionState connectionState) {
                if (connectionState == ZIMConnectionState.DISCONNECTED && connectionEvent == ZIMConnectionEvent.KICKED_OUT) {
                    ZIMKitToastUtils.showToast(getString(im.zego.zimkitcommon.R.string.common_user_kick_out));
                    ZIMKitActivityUtils.onlyExitActivity(getComponentName().getClassName());
                }
            }
        });

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        mViewModel.cleanUserId();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            //Click on the outer area keyboard to put away
            new ZIMKitKeyboardUtils.ActivityDispatchTouchEvent().dispatchTouchEventCloseInput(ev, this);
        }
        return super.dispatchTouchEvent(ev);
    }
}