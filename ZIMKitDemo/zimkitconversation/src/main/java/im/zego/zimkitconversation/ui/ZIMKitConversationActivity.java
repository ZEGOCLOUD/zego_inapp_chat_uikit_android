package im.zego.zimkitconversation.ui;

import androidx.fragment.app.Fragment;

import im.zego.zim.enums.ZIMConnectionEvent;
import im.zego.zim.enums.ZIMConnectionState;
import im.zego.zimkitcommon.base.BaseActivity;
import im.zego.zimkitconversation.BR;
import im.zego.zimkitconversation.R;
import im.zego.zimkitconversation.databinding.ConversationActivityBinding;
import im.zego.zimkitconversation.viewmodel.ZIMKitConversationVM;

public class ZIMKitConversationActivity extends BaseActivity<ConversationActivityBinding, ZIMKitConversationVM> {

    @Override
    protected int getLayoutId() {
        return R.layout.conversation_activity;
    }

    @Override
    protected int getViewModelId() {
        return BR.vm;
    }

    @Override
    protected void initView() {
        mBinding.titleBar.setLeftImg(R.mipmap.conversation_ic_logout);
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frag);
        mBinding.titleBar.setLeftCLickListener(v -> {
            mViewModel.logout();
            finish();
        });
        mBinding.titleBar.setRightCLickListener(v -> {
            if (fragment instanceof ZIMKitConversationFragment)
                ((ZIMKitConversationFragment) fragment).showSelectChatBottomSheet();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mViewModel != null) {
            mViewModel.logout();
        }
    }

    @Override
    public void onBackPressed() {
        mViewModel.logout();
        super.onBackPressed();
    }

    @Override
    protected void initData() {
        //Connection Status Listening
        mViewModel.setConnectionStateListener(new ZIMKitConversationVM.IConnectionStateListener() {
            @Override
            public void onConnectionStateChange(ZIMConnectionEvent connectionEvent, ZIMConnectionState connectionState) {
                String mTitle = getString(R.string.conversation_title);
                if (connectionState == ZIMConnectionState.DISCONNECTED) {
                    //Not connected
                    mTitle = getString(R.string.conversation_disconnected);
                } else if (connectionState == ZIMConnectionState.CONNECTING || connectionState == ZIMConnectionState.RECONNECTING) {
                    //Connecting
                    mTitle = getString(R.string.conversation_connecting);
                } else if (connectionState == ZIMConnectionState.CONNECTED) {
                    //CONNECTED
                    mTitle = getString(R.string.conversation_title);
                }
                mBinding.titleBar.setTitle(mTitle);
            }
        });
    }
}