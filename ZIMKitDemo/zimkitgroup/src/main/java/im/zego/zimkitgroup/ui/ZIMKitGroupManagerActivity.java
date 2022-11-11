package im.zego.zimkitgroup.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.view.View;

import im.zego.zimkitcommon.ZIMKitConstant;
import im.zego.zimkitcommon.base.BaseActivity;
import im.zego.zimkitcommon.utils.ZIMKitToastUtils;
import im.zego.zimkitgroup.BR;
import im.zego.zimkitgroup.R;
import im.zego.zimkitgroup.databinding.GroupActivityManagerBinding;
import im.zego.zimkitgroup.viewmodel.ZIMKitGroupManagerVM;

public class ZIMKitGroupManagerActivity extends BaseActivity<GroupActivityManagerBinding, ZIMKitGroupManagerVM> {
    private String mId;

    @Override
    protected void initView() {

    }

    @Override
    protected int getLayoutId() {
        return R.layout.group_activity_manager;
    }

    @Override
    protected int getViewModelId() {
        return BR.vm;
    }

    public void copy(View view) {
        ClipboardManager clipboardManager = (ClipboardManager) this.getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("zimkit_group_id", mId);
        clipboardManager.setPrimaryClip(clip);
        ZIMKitToastUtils.showToast(getString(R.string.group_copy_success));
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
