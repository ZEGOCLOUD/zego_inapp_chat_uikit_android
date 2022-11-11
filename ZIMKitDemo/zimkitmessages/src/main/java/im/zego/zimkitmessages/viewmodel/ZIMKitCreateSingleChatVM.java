package im.zego.zimkitmessages.viewmodel;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;

import androidx.databinding.ObservableField;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import im.zego.zim.enums.ZIMErrorCode;
import im.zego.zimkitcommon.ZIMKitConstant;

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

    public void createSingleChat() {
        Bundle bundle = new Bundle();
        bundle.putString(ZIMKitConstant.GroupPageConstant.KEY_TITLE, mId.get());
        bundle.putString(ZIMKitConstant.GroupPageConstant.KEY_ID, mId.get());
        toChat(ZIMErrorCode.SUCCESS, bundle);
    }

    private void toChat(ZIMErrorCode errorCode, Object data) {
        toChatLiveData.postValue(new Pair<>(errorCode, data));
    }
}
