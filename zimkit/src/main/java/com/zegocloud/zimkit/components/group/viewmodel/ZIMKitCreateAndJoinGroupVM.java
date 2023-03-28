package com.zegocloud.zimkit.components.group.viewmodel;

import android.app.Application;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import com.zegocloud.zimkit.R;
import com.zegocloud.zimkit.common.ZIMKitConstant;
import com.zegocloud.zimkit.services.ZIMKit;
import com.zegocloud.zimkit.services.callback.CreateGroupCallback;
import com.zegocloud.zimkit.services.callback.JoinGroupCallback;
import com.zegocloud.zimkit.services.model.ZIMKitGroupInfo;
import im.zego.zim.entity.ZIMError;
import im.zego.zim.entity.ZIMErrorUserInfo;
import im.zego.zim.enums.ZIMErrorCode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class ZIMKitCreateAndJoinGroupVM extends AndroidViewModel {

    private String mType = "";
    public ObservableField<Boolean> isCanToChat = new ObservableField<>(false);
    public ObservableField<Boolean> isShowErrorTips = new ObservableField<>(false);
    public ObservableField<String> mId = new ObservableField<>();
    public ObservableField<String> mIdInputHint = new ObservableField<>();
    public ObservableField<Boolean> mShowSecondEdittext = new ObservableField<>(false);
    public ObservableField<String> mSecondText = new ObservableField<>();
    public MutableLiveData<Pair<ZIMErrorCode, Object>> toChatLiveData = new MutableLiveData<>();

    public ZIMKitCreateAndJoinGroupVM(@NonNull Application application) {
        super(application);
    }

    public void setType(String type) {
        mType = type;
        boolean isGroup = false;
        String hint = "";
        switch (type) {
            case ZIMKitConstant.GroupPageConstant.TYPE_CREATE_GROUP_MESSAGE:
                isGroup = true;
                hint = getApplication().getString(R.string.zimkit_input_user_id_of_group);
                break;
            case ZIMKitConstant.GroupPageConstant.TYPE_JOIN_GROUP_MESSAGE:
                hint = getApplication().getString(R.string.zimkit_input_group_id);
                break;
        }
        mIdInputHint.set(hint);
        mShowSecondEdittext.set(isGroup);
    }

    public String getType() {
        return mType;
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
                boolean isLengthCorrect = true;
                if (mType.equals(ZIMKitConstant.GroupPageConstant.TYPE_CREATE_GROUP_MESSAGE)) {
                    List<String> strings = Arrays.asList(id.split(";"));
                    for (String string : strings) {
                        if (string.length() < 6 || string.length() > 12) {
                            isLengthCorrect = false;
                            break;
                        }
                    }
                    isShowErrorTips.set(id.isEmpty() || !isLengthCorrect);
                }
                isCanToChat.set(!id.isEmpty() && isLengthCorrect);
            }
        };
    }

    public void createGroupChat(List<String> ids, String groupName) {
        if (ids == null || ids.isEmpty()) {
            toChat(ZIMErrorCode.PARAM_INVALID, "id is null or empty ");
            return;
        }
        ZIMKit.createGroup(groupName, ids, new CreateGroupCallback() {
            @Override
            public void onCreateGroup(ZIMKitGroupInfo groupInfo, ArrayList<ZIMErrorUserInfo> inviteUserErrors, ZIMError error) {
                if (error.code == ZIMErrorCode.SUCCESS) {
                    if (!inviteUserErrors.isEmpty()) {
                        StringBuilder errorUserStr = new StringBuilder();
                        Iterator<ZIMErrorUserInfo> iterator = inviteUserErrors.iterator();
                        while (iterator.hasNext()) {
                            errorUserStr.append(iterator.next().userID);
                            if (iterator.hasNext()) {
                                errorUserStr.append(",");
                            }
                        }
                        error.setCode(ZIMErrorCode.DOES_NOT_EXIST);
                        toChat(error.code, getApplication().getString(R.string.zimkit_group_user_id_not_exit, errorUserStr.toString()));
                    } else {
                        Bundle bundle = new Bundle();
                        bundle.putString(ZIMKitConstant.GroupPageConstant.KEY_TITLE, groupInfo.getName());
                        bundle.putString(ZIMKitConstant.GroupPageConstant.KEY_ID, groupInfo.getId());
                        toChat(error.code, bundle);
                    }
                } else {
                    toChat(error.code, error.message);
                }
            }
        });
    }

    public void joinGroupChat(String groupId) {
        ZIMKit.joinGroup(groupId, new JoinGroupCallback() {
            @Override
            public void onJoinGroup(ZIMKitGroupInfo groupInfo, ZIMError error) {
                if (error.code == ZIMErrorCode.SUCCESS) {
                    Bundle bundle = new Bundle();
                    bundle.putString(ZIMKitConstant.GroupPageConstant.KEY_TITLE, groupInfo.getName());
                    bundle.putString(ZIMKitConstant.GroupPageConstant.KEY_ID, groupInfo.getId());
                    toChat(error.code, bundle);
                } else {
                    if (error.code == ZIMErrorCode.DOES_NOT_EXIST) {
                        error.message = getApplication().getString(R.string.zimkit_group_id_not_exit, groupId);
                    }
                    toChat(error.code, error.message);
                }
            }
        });
    }

    private void toChat(ZIMErrorCode errorCode, Object data) {
        toChatLiveData.postValue(new Pair<>(errorCode, data));
    }

    public void startChat() {
        switch (mType) {
            case ZIMKitConstant.GroupPageConstant.TYPE_CREATE_GROUP_MESSAGE:
                String idText = mId.get();
                if (idText == null || idText.isEmpty()) {
                    return;
                }
                List<String> strings = Arrays.asList(idText.split(";"));
                createGroupChat(strings, mSecondText.get());
                break;
            case ZIMKitConstant.GroupPageConstant.TYPE_JOIN_GROUP_MESSAGE:
                String groupId = mId.get();
                joinGroupChat(groupId);
                break;
        }
    }

}
