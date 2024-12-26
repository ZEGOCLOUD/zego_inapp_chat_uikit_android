package com.zegocloud.zimkit.components.message.viewmodel;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.View;
import androidx.databinding.ObservableField;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.zegocloud.uikit.plugin.signaling.ZegoSignalingPlugin;
import com.zegocloud.zimkit.R;
import com.zegocloud.zimkit.common.ZIMKitConstant;
import com.zegocloud.zimkit.services.ZIMKit;
import im.zego.zim.callback.ZIMUsersInfoQueriedCallback;
import im.zego.zim.entity.ZIMError;
import im.zego.zim.entity.ZIMErrorUserInfo;
import im.zego.zim.entity.ZIMUserFullInfo;
import im.zego.zim.entity.ZIMUsersInfoQueryConfig;
import im.zego.zim.enums.ZIMErrorCode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

public class ZIMKitCreateSingleChatVM extends ViewModel {

    public ObservableField<Boolean> mButtonState = new ObservableField<>(false);
    public ObservableField<String> mId = new ObservableField<>();
    public MutableLiveData<Pair<ZIMErrorCode, Object>> toChatLiveData = new MutableLiveData<>();

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
                mButtonState.set(!id.isEmpty() && isLengthCorrect);
            }
        };
    }

    private static final String TAG = "ZIMKitCreateSingleChatV";

    public void createSingleChat(View view) {
        if (Objects.equals(ZIMKit.getLocalUser().getId(), mId.get())) {
            toChat(ZIMErrorCode.FAILED, view.getContext().getString(R.string.zimkit_cannot_chat_self));
        } else {
            ZegoSignalingPlugin.getInstance()
                .queryUserInfo(Collections.singletonList(mId.get()), new ZIMUsersInfoQueryConfig(),
                    new ZIMUsersInfoQueriedCallback() {
                        @Override
                        public void onUsersInfoQueried(ArrayList<ZIMUserFullInfo> userList,
                            ArrayList<ZIMErrorUserInfo> errorUserList, ZIMError errorInfo) {
                            if (errorInfo.code == ZIMErrorCode.SUCCESS) {
                                Bundle bundle = new Bundle();
                                bundle.putString(ZIMKitConstant.GroupPageConstant.KEY_TITLE, mId.get());
                                bundle.putString(ZIMKitConstant.GroupPageConstant.KEY_ID, mId.get());
                                toChat(ZIMErrorCode.SUCCESS, bundle);
                            } else {
                                if (errorInfo.code == ZIMErrorCode.USER_NOT_EXIST) {
                                    toChat(ZIMErrorCode.FAILED,
                                        view.getContext().getString(R.string.zimkit_user_id_not_exist,mId.get()));
                                } else {
                                    toChat(ZIMErrorCode.FAILED, errorInfo.message);
                                }
                            }
                        }
                    });

        }
    }

    private void toChat(ZIMErrorCode errorCode, Object data) {
        toChatLiveData.postValue(new Pair<>(errorCode, data));
    }

}
