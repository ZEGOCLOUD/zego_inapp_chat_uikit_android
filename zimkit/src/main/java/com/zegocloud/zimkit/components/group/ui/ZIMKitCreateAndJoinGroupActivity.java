package com.zegocloud.zimkit.components.group.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import com.zegocloud.zimkit.BR;
import com.zegocloud.zimkit.R;
import com.zegocloud.zimkit.common.ZIMKitConstant;
import com.zegocloud.zimkit.common.base.BaseActivity;
import com.zegocloud.zimkit.common.utils.ZIMKitToastUtils;
import com.zegocloud.zimkit.components.group.viewmodel.ZIMKitCreateAndJoinGroupVM;
import com.zegocloud.zimkit.components.message.ui.ZIMKitMessageActivity;
import com.zegocloud.zimkit.databinding.ZimkitActivityGroupCreateAndJoinBinding;
import im.zego.zim.enums.ZIMErrorCode;

public class ZIMKitCreateAndJoinGroupActivity extends
    BaseActivity<ZimkitActivityGroupCreateAndJoinBinding, ZIMKitCreateAndJoinGroupVM> {

    private String mType;

    @Override
    protected int getLayoutId() {
        return R.layout.zimkit_activity_group_create_and_join;
    }

    @Override
    protected int getViewModelId() {
        return BR.vm;
    }

    @Override
    protected void initView() {
        Bundle bundle = this.getIntent().getBundleExtra(ZIMKitConstant.RouterConstant.KEY_BUNDLE);
        mType = bundle.getString(ZIMKitConstant.GroupPageConstant.KEY_TYPE);
        String title = "";
        switch (mType) {
            case ZIMKitConstant.GroupPageConstant.TYPE_CREATE_GROUP_MESSAGE:
                title = getString(R.string.zimkit_create_group_chat_title);
                break;
            case ZIMKitConstant.GroupPageConstant.TYPE_JOIN_GROUP_MESSAGE:
                title = getString(R.string.zimkit_group_join_group_chat);
                break;
        }
        mBinding.titleBar.setTitle(title);
        mBinding.titleBar.setLeftImg(R.mipmap.zimkit_icon_close);
        mBinding.titleBar.hideRightButton();
    }

    @Override
    protected void initData() {
        mViewModel.setType(mType);
        mViewModel.toChatLiveData.observe(this, pair -> {
            if (pair.first == ZIMErrorCode.SUCCESS) {
                Bundle data = (Bundle) pair.second;
                data.putString(ZIMKitConstant.MessagePageConstant.KEY_TYPE, ZIMKitConstant.MessagePageConstant.TYPE_GROUP_MESSAGE);
                Intent intent = new Intent(this, ZIMKitMessageActivity.class);
                intent.putExtra(ZIMKitConstant.RouterConstant.KEY_BUNDLE, data);
                startActivity(intent);
                finish();
            } else {
                String errorMsg = (String) pair.second;
                if (pair.first == ZIMErrorCode.DOES_NOT_EXIST && mType.equals(ZIMKitConstant.GroupPageConstant.TYPE_CREATE_GROUP_MESSAGE)) {
                    showDialog(R.string.zimkit_user_not_exit, errorMsg);
                } else if (pair.first == ZIMErrorCode.DOES_NOT_EXIST && mType.equals(ZIMKitConstant.GroupPageConstant.TYPE_JOIN_GROUP_MESSAGE)) {
                    showDialog(R.string.zimkit_group_not_exit, errorMsg);
                } else if (pair.first == ZIMErrorCode.MEMBER_IS_ALREADY_IN_THE_GROUP && mType.equals(ZIMKitConstant.GroupPageConstant.TYPE_JOIN_GROUP_MESSAGE)) {
                    ZIMKitToastUtils.showErrorMessageIfNeeded(pair.first.value(), getString(R.string.zimkit_repeat_join_group_chat));
                } else {
                    ZIMKitToastUtils.showErrorMessageIfNeeded(pair.first.value(), errorMsg);
                }
            }
        });
    }

    private void showDialog(int titleRes, String msg) {
        new AlertDialog.Builder(this).setTitle(titleRes)
                .setPositiveButton(R.string.zimkit_confirm, (dialog, id) -> dialog.cancel())
                .setMessage(msg).show();
    }

    @Override
    protected boolean interceptClickInputClose() {
        return true;
    }

}
