package im.zego.zimkit;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;

import androidx.databinding.ObservableField;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.Random;

import im.zego.zim.enums.ZIMErrorCode;
import im.zego.zimkit.constant.UserAvatar;
import im.zego.zimkit.constant.UserNames;
import im.zego.zimkitcommon.ZIMKitManager;
import im.zego.zimkitcommon.model.UserInfo;
import im.zego.zimkitcommon.utils.ZIMKitToastUtils;

public class LoginViewModel extends ViewModel {
    public ObservableField<String> mUserId = new ObservableField<>();
    public MutableLiveData<Boolean> mLoginButtonEnableLiveData = new MutableLiveData<>();
    public MutableLiveData<Boolean> isShowErrorTips = new MutableLiveData<>(false);
    public MutableLiveData<Pair<Boolean, String>> mLoginStateLiveData = new MutableLiveData<>();
    public ObservableField<String> mUserName = new ObservableField<>();
    private SharedPreferences sp = MyApplication.sInstance.getSharedPreferences("imkit", Context.MODE_PRIVATE);

    public LoginViewModel() {
        mUserName.set("");
    }

    public TextWatcher onEditTextWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String id = s.toString().trim();
                boolean isLengthCorrect = s.length() >= 6 && s.length() <= 12;
                mLoginButtonEnableLiveData.postValue(!id.isEmpty() && isLengthCorrect);
                isShowErrorTips.postValue(id.isEmpty() ? false : !isLengthCorrect);
                mUserName.set(getUserName(s.toString().trim()));
            }
        };
    }

    /**
     * connectUser
     */
    public void connectUser() {
        UserInfo userInfo = new UserInfo();
        userInfo.setUserID(mUserId.get());
        userInfo.setUserName(mUserName.get());
        userInfo.setUserAvatarUrl(UserAvatar.getUserAvatar(mUserId.get()));
        ZIMKitManager.share().connectUser(userInfo, errorInfo -> {
            if (errorInfo.code == ZIMErrorCode.SUCCESS) {
                saveUserName(userInfo.getUserID(), userInfo.getUserName());
            } else {
                if (errorInfo.code == ZIMErrorCode.PARAM_INVALID) {
                    isShowErrorTips.postValue(true);
                } else if (errorInfo.code == ZIMErrorCode.NETWORK_ERROR) {
                    ZIMKitToastUtils.showToast(ZIMKitManager.share().getApplication().getString(im.zego.zimkitmessages.R.string.message_network_anomaly));
                } else {
                    ZIMKitToastUtils.showToast(errorInfo.message);
                }
            }
            mLoginStateLiveData.postValue(new Pair<>(errorInfo.code == ZIMErrorCode.SUCCESS, errorInfo.message));
        });
    }

    private String getUserName(String userId) {
        String spValue = sp.getString(userId, "");
        if (spValue.isEmpty()) {
            return genRandomUserName();
        } else {
            return spValue;
        }
    }

    public void cleanUserId() {
        mUserId.set("");
    }

    private void saveUserName(String userId, String userName) {
        SharedPreferences.Editor edit = sp.edit();
        edit.putString(userId, userName);
        edit.apply();
    }

    /**
     * Generate user nicknames based on random numbers
     *
     * @return
     */
    private String genRandomUserName() {
        int length = UserNames.userNames.length;
        Random ra = new Random();
        int i = ra.nextInt(length - 1);//Generate a random number of [0-length-1)
        return UserNames.userNames[i];
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }
}
