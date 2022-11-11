package im.zego.zimkitmessages;

import android.os.Bundle;

import im.zego.zim.enums.ZIMErrorCode;
import im.zego.zimkitcommon.ZIMKitConstant;
import im.zego.zimkitcommon.ZIMKitRouter;
import im.zego.zimkitcommon.base.BaseActivity;
import im.zego.zimkitcommon.utils.ZIMKitToastUtils;
import im.zego.zimkitmessages.databinding.MessageActivityCreateSingleChatBinding;
import im.zego.zimkitmessages.viewmodel.ZIMKitCreateSingleChatVM;

public class ZIMKitCreateSingleChatActivity extends BaseActivity<MessageActivityCreateSingleChatBinding, ZIMKitCreateSingleChatVM> {

    @Override
    protected int getLayoutId() {
        return R.layout.message_activity_create_single_chat;
    }

    @Override
    protected int getViewModelId() {
        return BR.vm;
    }

    @Override
    protected void initView() {
        mBinding.titleBar.setTitle(getString(R.string.message_create_single_chat));
        mBinding.titleBar.setLeftImg(R.mipmap.messasge_ic_close);
        mBinding.titleBar.hideRightButton();
    }

    @Override
    protected void initData() {
        mViewModel.toChatLiveData.observe(this, pair -> {
            if (pair.first == ZIMErrorCode.SUCCESS) {
                Bundle data = (Bundle) pair.second;
                data.putString(ZIMKitConstant.MessagePageConstant.KEY_TYPE, ZIMKitConstant.MessagePageConstant.TYPE_SINGLE_MESSAGE);
                ZIMKitRouter.toAndFinish(ZIMKitCreateSingleChatActivity.this,
                        ZIMKitConstant.RouterConstant.ROUTER_MESSAGE, data);
            } else {
                String errorMsg = (String) pair.second;
                ZIMKitToastUtils.showToast(errorMsg);
            }
        });
    }

    @Override
    protected boolean interceptClickInputClose() {
        return true;
    }
}
