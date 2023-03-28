package com.zegocloud.zimkit.components.conversation.ui;

import androidx.fragment.app.Fragment;
import com.zegocloud.zimkit.BR;
import com.zegocloud.zimkit.R;
import com.zegocloud.zimkit.common.base.BaseActivity;
import com.zegocloud.zimkit.components.conversation.viewmodel.ZIMKitConversationVM;
import com.zegocloud.zimkit.databinding.ZimkitActivityConversationBinding;
import im.zego.zim.enums.ZIMConnectionEvent;
import im.zego.zim.enums.ZIMConnectionState;

public class ZIMKitConversationActivity extends BaseActivity<ZimkitActivityConversationBinding, ZIMKitConversationVM> {

    @Override
    protected int getLayoutId() {
        return R.layout.zimkit_activity_conversation;
    }

    @Override
    protected int getViewModelId() {
        return BR.vm;
    }

    @Override
    protected void initView() {
        mBinding.titleBar.setLeftImg(R.mipmap.zimkit_icon_logout);
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
                String mTitle = getString(R.string.zimkit_title);
                if (connectionState == ZIMConnectionState.DISCONNECTED) {
                    //Not connected
                    mTitle = getString(R.string.zimkit_disconnected);
                } else if (connectionState == ZIMConnectionState.CONNECTING || connectionState == ZIMConnectionState.RECONNECTING) {
                    //Connecting
                    mTitle = getString(R.string.zimkit_connecting);
                } else if (connectionState == ZIMConnectionState.CONNECTED) {
                    //CONNECTED
                    mTitle = getString(R.string.zimkit_title);
                }
                mBinding.titleBar.setTitle(mTitle);
            }
        });
    }

}
