package com.zegocloud.zimkit.components.message.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import com.zegocloud.zimkit.common.base.BaseDialog;
import com.zegocloud.zimkit.common.base.BaseFragment;
import com.zegocloud.zimkit.common.utils.PermissionHelper;
import com.zegocloud.zimkit.components.message.viewmodel.ZIMKitInputMoreVM;
import com.zegocloud.zimkit.BR;
import com.zegocloud.zimkit.R;
import com.zegocloud.zimkit.databinding.ZimkitFragmentInputMoreBinding;

public class ZIMKitInputMoreFragment extends BaseFragment<ZimkitFragmentInputMoreBinding, ZIMKitInputMoreVM> {

    public static final int REQUEST_CODE_FILE = 1011;
    public static final int REQUEST_CODE_PHOTO = 1012;

    private InputMoreCallback mCallback;

    @Override
    protected void initView() {
    }

    @Override
    protected int getLayoutId() {
        return R.layout.zimkit_fragment_input_more;
    }

    @Override
    protected int getViewModelId() {
        return 0;
    }

    @Override
    protected void initData() {
        mViewModel = new ViewModelProvider(requireActivity()).get(ZIMKitInputMoreVM.class);
        mBinding.setVariable(BR.vm, mViewModel);

        mBinding.clAlbum.setOnClickListener(v -> {
            requestPermission(REQUEST_CODE_PHOTO);
        });

        mBinding.clFile.setOnClickListener(v -> {
            requestPermission(REQUEST_CODE_FILE);
        });
    }

    /**
     * Get permission judgment
     */
    private void requestPermission(int fromType) {
        PermissionHelper.onWriteSDCardPermissionGranted((FragmentActivity) getActivity(), new PermissionHelper.GrantResult() {
            @Override
            public void onGrantResult(boolean allGranted) {
                if (allGranted) {
                    if (fromType == REQUEST_CODE_PHOTO) {
                        if (mCallback != null) {
                            mCallback.selectPhoto();
                        }
                    } else if (fromType == REQUEST_CODE_FILE) {
                        startSendFile();
                    }
                } else {
                    BaseDialog baseDialog = new BaseDialog(getActivity());
                    baseDialog.setMsgTitle(getActivity().getString(R.string.zimkit_storage_permissions_tip));
                    baseDialog.setMsgContent(getActivity().getString(R.string.zimkit_storage_permissions_description));
                    baseDialog.setLeftButtonContent(getActivity().getString(R.string.zimkit_access_later));
                    baseDialog.setRightButtonContent(getActivity().getString(R.string.zimkit_go_setting));
                    baseDialog.setSureListener(v -> {
                        baseDialog.dismiss();
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                .setData(Uri.fromParts("package", getActivity().getPackageName(), null));
                        ((Activity) getActivity()).startActivityForResult(intent, 666);
                    });
                    baseDialog.setCancelListener(v -> {
                        baseDialog.dismiss();
                    });
                }
            }
        });
    }

    private void startSendFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, REQUEST_CODE_FILE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_FILE) {
            if (resultCode != -1) {
                return;
            }
            //Get uri, followed by the process of converting uri to file.
            Uri uri = data.getData();
            if (mCallback != null) {
                mCallback.selectFile(uri);
            }
        }
    }

    public void setInputMoreCallback(InputMoreCallback callback) {
        mCallback = callback;
    }

    public interface InputMoreCallback {
        void selectFile(Uri uri);

        void selectPhoto();
    }

}
