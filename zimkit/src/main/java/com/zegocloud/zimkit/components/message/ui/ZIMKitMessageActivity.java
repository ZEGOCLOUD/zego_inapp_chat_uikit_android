package com.zegocloud.zimkit.components.message.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModel;
import com.zegocloud.zimkit.R;
import com.zegocloud.zimkit.common.ZIMKitConstant;
import com.zegocloud.zimkit.common.base.BaseActivity;
import com.zegocloud.zimkit.common.utils.ZIMKitActivityUtils;
import com.zegocloud.zimkit.databinding.ZimkitActivityMessageBinding;

public class ZIMKitMessageActivity extends BaseActivity<ZimkitActivityMessageBinding, ViewModel> {

    private ZIMKitMessageFragment fragment;
    private String title;
    private String type;
    private boolean isFromPush;

    @Override
    protected void initView() {
        Bundle bundle = getIntent().getBundleExtra(ZIMKitConstant.RouterConstant.KEY_BUNDLE);
        if (bundle == null) {
            finish();
            return;
        }
        title = bundle.getString(ZIMKitConstant.MessagePageConstant.KEY_TITLE);
        type = bundle.getString(ZIMKitConstant.MessagePageConstant.KEY_TYPE);
        String id = bundle.getString(ZIMKitConstant.MessagePageConstant.KEY_ID);
        String avatar = bundle.getString(ZIMKitConstant.MessagePageConstant.KEY_AVATAR);
        isFromPush = bundle.getBoolean(ZIMKitConstant.MessagePageConstant.KEY_PUSH, false);
        mBinding.titleBar.setRightImg(R.mipmap.zimkit_icon_more);
        if (type.equals(ZIMKitConstant.MessagePageConstant.TYPE_GROUP_MESSAGE)) {
            mBinding.titleBar.setTitle(!TextUtils.isEmpty(title) ? title : getString(R.string.zimkit_title_group_chat));
            mBinding.titleBar.setRightCLickListener(v -> {
                Bundle data = new Bundle();
                data.putString(ZIMKitConstant.MessagePageConstant.KEY_ID, id);
                data.putString(ZIMKitConstant.MessagePageConstant.KEY_TITLE, title);
                Intent intent = new Intent(this, ZIMKitGroupChatSettingActivity.class);
                intent.putExtra(ZIMKitConstant.RouterConstant.KEY_BUNDLE, data);
                startActivity(intent, data);
            });
        } else if (type.equals(ZIMKitConstant.MessagePageConstant.TYPE_SINGLE_MESSAGE)) {
            mBinding.titleBar.setTitle(!TextUtils.isEmpty(title) ? title : getString(R.string.zimkit_title_chat));
            mBinding.titleBar.setRightCLickListener(v -> {
                Bundle data = new Bundle();
                data.putString(ZIMKitConstant.MessagePageConstant.KEY_ID, id);
                data.putString(ZIMKitConstant.MessagePageConstant.KEY_TITLE, title);
                data.putString(ZIMKitConstant.MessagePageConstant.KEY_AVATAR, avatar);
                Intent intent = new Intent(this, ZIMKitPrivateChatSettingActivity.class);
                intent.putExtra(ZIMKitConstant.RouterConstant.KEY_BUNDLE, data);
                startActivity(intent);
            });
        }
        fragment = new ZIMKitMessageFragment();
        replaceFragment(fragment, bundle);
        if (type.equals(ZIMKitConstant.MessagePageConstant.TYPE_SINGLE_MESSAGE)) {
            fragment.getInformation(type, id);
        }else {
            if (TextUtils.isEmpty(title) || TextUtils.isEmpty(avatar)) {
                fragment.getInformation(type, id);
            }
        }
    }

    private void replaceFragment(Fragment fragment, Bundle arg) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        fragment.setArguments(arg);
        transaction.replace(R.id.fra_message, fragment);
        transaction.commit();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.zimkit_activity_message;
    }

    @Override
    protected int getViewModelId() {
        return 0;
    }

    @Override
    protected void initData() {
        if (isFromPush) {
            //Close the page between and session
            ZIMKitActivityUtils.finishActivityForMessage(getComponentName().getClassName());
        }

        if (fragment != null) {
            fragment.setOnOnTitleClickListener(new ZIMKitMessageFragment.OnTitleClickListener() {
                @Override
                public void titleMultiSelect() {
                    mBinding.titleBar.hideLeftButton();
                    mBinding.titleBar.showLeftTxtButton();
                    if (type.equals(ZIMKitConstant.MessagePageConstant.TYPE_GROUP_MESSAGE)) {
                        mBinding.titleBar.hideRightButton();
                    }
                    mBinding.titleBar.setLeftTxtCLickListener(v -> {
                        if (fragment.isMultiSelect()) {
                            fragment.hideMultiSelectMessage();
                            mBinding.titleBar.hideLeftTxtButton();
                        } else {
                            mBinding.titleBar.showLeftButton();
                            mBinding.titleBar.hideLeftTxtButton();
                            if (type.equals(ZIMKitConstant.MessagePageConstant.TYPE_GROUP_MESSAGE)) {
                                mBinding.titleBar.showRightButton();
                            }
                        }
                    });
                }

                @Override
                public void titleNormal() {
                    mBinding.titleBar.showLeftButton();
                    mBinding.titleBar.hideLeftTxtButton();
                    if (type.equals(ZIMKitConstant.MessagePageConstant.TYPE_GROUP_MESSAGE)) {
                        mBinding.titleBar.showRightButton();
                    }
                }

                @Override
                public void setSetTitle(String title) {
                    if (mBinding != null) {
                        mBinding.titleBar.setTitle(title);
                    }
                }
            });
        }
    }

}