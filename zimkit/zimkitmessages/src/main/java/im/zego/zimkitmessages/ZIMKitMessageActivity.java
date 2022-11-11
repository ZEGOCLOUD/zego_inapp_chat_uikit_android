package im.zego.zimkitmessages;

import android.os.Bundle;
import android.text.TextUtils;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModel;

import im.zego.zimkitcommon.ZIMKitConstant;
import im.zego.zimkitcommon.ZIMKitRouter;
import im.zego.zimkitcommon.base.BaseActivity;
import im.zego.zimkitcommon.utils.ZIMKitActivityUtils;
import im.zego.zimkitmessages.databinding.MessageActivityBinding;
import im.zego.zimkitmessages.fragment.ZIMKitMessageFragment;

public class ZIMKitMessageActivity extends BaseActivity<MessageActivityBinding, ViewModel> {
    private ZIMKitMessageFragment fragment;
    private String title;
    private String type;
    private boolean isFromPush;

    @Override
    protected void initView() {
        Bundle bundle = getIntent().getBundleExtra(ZIMKitConstant.RouterConstant.KEY_BUNDLE);
        title = bundle.getString(ZIMKitConstant.MessagePageConstant.KEY_TITLE);
        type = bundle.getString(ZIMKitConstant.MessagePageConstant.KEY_TYPE);
        String id = bundle.getString(ZIMKitConstant.MessagePageConstant.KEY_ID);
        String avatar = bundle.getString(ZIMKitConstant.MessagePageConstant.KEY_AVATAR);
        isFromPush = bundle.getBoolean(ZIMKitConstant.MessagePageConstant.KEY_PUSH, false);
        if (type.equals(ZIMKitConstant.MessagePageConstant.TYPE_GROUP_MESSAGE)) {
            mBinding.titleBar.setTitle(!TextUtils.isEmpty(title) ? title : getString(R.string.message_title_group_chat));
            mBinding.titleBar.setRightImg(R.mipmap.message_ic_more);
            mBinding.titleBar.setRightCLickListener(v -> {
                Bundle data = new Bundle();
                data.putString(ZIMKitConstant.MessagePageConstant.KEY_ID, id);
                data.putString(ZIMKitConstant.MessagePageConstant.KEY_TITLE, title);
                ZIMKitRouter.to(ZIMKitMessageActivity.this, ZIMKitConstant.RouterConstant.ROUTER_GROUP_MANAGER, data);
            });
        } else if (type.equals(ZIMKitConstant.MessagePageConstant.TYPE_SINGLE_MESSAGE)) {
            mBinding.titleBar.setTitle(!TextUtils.isEmpty(title) ? title : getString(R.string.message_title_chat));
            mBinding.titleBar.hideRightButton();
        }
        fragment = new ZIMKitMessageFragment();
        replaceFragment(fragment, bundle);
        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(avatar)) {
            fragment.getInformation(type, id);
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
        return R.layout.message_activity;
    }

    @Override
    protected int getViewModelId() {
        return 0;
    }

    @Override
    protected void initData() {
        if (isFromPush) {
            //Close the page between and session
            ZIMKitActivityUtils.finishActivityForMessage();
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
                        fragment.hideMultiSelectMessage();
                        mBinding.titleBar.showLeftButton();
                        mBinding.titleBar.hideLeftTxtButton();
                        if (type.equals(ZIMKitConstant.MessagePageConstant.TYPE_GROUP_MESSAGE)) {
                            mBinding.titleBar.showRightButton();
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