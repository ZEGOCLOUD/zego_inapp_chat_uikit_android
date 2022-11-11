package im.zego.zimkitgroup.viewmodel;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import im.zego.zim.entity.ZIMErrorUserInfo;
import im.zego.zim.entity.ZIMGroupInfo;
import im.zego.zim.enums.ZIMErrorCode;
import im.zego.zimkitcommon.ZIMKitConstant;
import im.zego.zimkitcommon.ZIMKitManager;
import im.zego.zimkitgroup.R;

import android.app.Application;

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
                hint = getApplication().getString(R.string.group_input_user_id_of_group);
                break;
            case ZIMKitConstant.GroupPageConstant.TYPE_JOIN_GROUP_MESSAGE:
                hint = getApplication().getString(R.string.group_input_group_id);
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
        ZIMGroupInfo groupInfo = new ZIMGroupInfo();
        groupInfo.groupName = groupName;
        ZIMKitManager.share().zim().createGroup(groupInfo, ids, (groupInfo1, userList, errorUserList, errorInfo) -> {
            if (errorInfo.code == ZIMErrorCode.SUCCESS) {
                if (!errorUserList.isEmpty()) {
                    StringBuilder errorUserStr = new StringBuilder();
                    Iterator<ZIMErrorUserInfo> iterator = errorUserList.iterator();
                    while (iterator.hasNext()) {
                        errorUserStr.append(iterator.next().userID);
                        if (iterator.hasNext()) {
                            errorUserStr.append(",");
                        }
                    }
                    errorInfo.setCode(ZIMErrorCode.DOES_NOT_EXIST);
                    toChat(errorInfo.code, getApplication().getString(R.string.group_group_user_id_not_exit, errorUserStr.toString()));
                } else {
                    Bundle bundle = new Bundle();
                    bundle.putString(ZIMKitConstant.GroupPageConstant.KEY_TITLE, groupInfo1.baseInfo.groupName);
                    bundle.putString(ZIMKitConstant.GroupPageConstant.KEY_ID, groupInfo1.baseInfo.groupID);
                    toChat(errorInfo.code, bundle);
                }
            } else {
                toChat(errorInfo.code, errorInfo.message);
            }
        });
    }

    public void joinGroupChat(String groupId) {
        ZIMKitManager.share().zim().joinGroup(groupId, (groupInfo, errorInfo) -> {
            if (errorInfo.code == ZIMErrorCode.SUCCESS) {
                Bundle bundle = new Bundle();
                bundle.putString(ZIMKitConstant.GroupPageConstant.KEY_TITLE, groupInfo.baseInfo.groupName);
                bundle.putString(ZIMKitConstant.GroupPageConstant.KEY_ID, groupInfo.baseInfo.groupID);
                toChat(errorInfo.code, bundle);
            } else {
                if (errorInfo.code == ZIMErrorCode.DOES_NOT_EXIST) {
                    errorInfo.message = getApplication().getString(R.string.group_group_id_not_exit, groupId);
                }
                toChat(errorInfo.code, errorInfo.message);
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
