package com.zegocloud.zimkit.components.group.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.view.View;

import com.zegocloud.zimkit.R;
import com.zegocloud.zimkit.common.ZIMKitConstant;
import com.zegocloud.zimkit.common.base.BaseActivity;
import com.zegocloud.zimkit.common.utils.ZIMKitToastUtils;
import com.zegocloud.zimkit.components.group.viewmodel.ZIMKitGroupManagerVM;
import com.zegocloud.zimkit.BR;
import com.zegocloud.zimkit.databinding.ZimkitActivityGroupManagerBinding;

public class ZIMKitGroupManagerActivity extends BaseActivity<ZimkitActivityGroupManagerBinding, ZIMKitGroupManagerVM> {

    private String mId;

    @Override
    protected void initView() {
    }

    @Override
    protected int getLayoutId() {
        return R.layout.zimkit_activity_group_manager;
    }

    @Override
    protected int getViewModelId() {
        return BR.vm;
    }

    public void copy(View view) {
        ClipboardManager clipboardManager = (ClipboardManager) this.getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("zimkit_group_id", mId);
        clipboardManager.setPrimaryClip(clip);
        ZIMKitToastUtils.showToast(getString(R.string.zimkit_copy_success));
    }

    @Override
    protected void initData() {
        Bundle bundle = getIntent().getBundleExtra(ZIMKitConstant.RouterConstant.KEY_BUNDLE);
        String title = bundle.getString(ZIMKitConstant.MessagePageConstant.KEY_TITLE);
        mId = bundle.getString(ZIMKitConstant.MessagePageConstant.KEY_ID);
        mBinding.titleBar.setTitle(title);
        mBinding.titleBar.hideRightButton();
        mViewModel.setGroupId(mId);
    }

}
