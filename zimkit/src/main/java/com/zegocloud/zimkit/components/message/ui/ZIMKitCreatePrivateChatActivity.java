package com.zegocloud.zimkit.components.message.ui;

import android.content.Intent;
import android.os.Bundle;

import com.zegocloud.zimkit.common.ZIMKitConstant;
import com.zegocloud.zimkit.common.base.BaseActivity;
import com.zegocloud.zimkit.common.utils.ZIMKitToastUtils;
import com.zegocloud.zimkit.components.message.viewmodel.ZIMKitCreateSingleChatVM;
import com.zegocloud.zimkit.BR;
import im.zego.zim.enums.ZIMErrorCode;
import com.zegocloud.zimkit.R;
import com.zegocloud.zimkit.databinding.ZimkitActivityCreateSingleChatBinding;

public class ZIMKitCreatePrivateChatActivity extends
    BaseActivity<ZimkitActivityCreateSingleChatBinding, ZIMKitCreateSingleChatVM> {

    @Override
    protected int getLayoutId() {
        return R.layout.zimkit_activity_create_single_chat;
    }

    @Override
    protected int getViewModelId() {
        return BR.vm;
    }

    @Override
    protected void initView() {
        mBinding.titleBar.setTitle(getString(R.string.zimkit_create_single_chat));
        mBinding.titleBar.setLeftImg(R.mipmap.zimkit_icon_close);
        mBinding.titleBar.hideRightButton();
    }

    @Override
    protected void initData() {
        mViewModel.toChatLiveData.observe(this, pair -> {
            if (pair.first == ZIMErrorCode.SUCCESS) {
                Bundle data = (Bundle) pair.second;
                data.putString(ZIMKitConstant.MessagePageConstant.KEY_TYPE, ZIMKitConstant.MessagePageConstant.TYPE_SINGLE_MESSAGE);
                Intent intent = new Intent(this, ZIMKitMessageActivity.class);
                intent.putExtra(ZIMKitConstant.RouterConstant.KEY_BUNDLE, data);
                startActivity(intent, data);
                finish();
            } else {
                String errorMsg = (String) pair.second;
                ZIMKitToastUtils.showErrorMessageIfNeeded(pair.first.value(), errorMsg);
            }
        });
    }

    @Override
    protected boolean interceptClickInputClose() {
        return true;
    }
}
