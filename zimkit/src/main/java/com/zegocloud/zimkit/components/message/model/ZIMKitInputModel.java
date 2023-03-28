package com.zegocloud.zimkit.components.message.model;

import android.text.Editable;
import android.text.TextWatcher;

import androidx.databinding.BaseObservable;
import androidx.databinding.ObservableField;

import java.util.concurrent.atomic.AtomicBoolean;

public class ZIMKitInputModel extends BaseObservable {

    public ObservableField<String> inputMessage = new ObservableField<>();
    public ObservableField<Boolean> audioBtn = new ObservableField<>(false);
    public ObservableField<Boolean> audioRecordBtn = new ObservableField<>(false);
    public ObservableField<Boolean> isShowWhite = new ObservableField<>(true);

    public AtomicBoolean isShowEmoji = new AtomicBoolean(false);
    public AtomicBoolean isShowFile = new AtomicBoolean(false);
    public AtomicBoolean isShowAudio = new AtomicBoolean(false);

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
                String content = s.toString();
                inputMessage.set(content);
            }
        };
    }

    public void emojiClick(boolean emojiStatus) {
        isShowEmoji.set(emojiStatus);
    }

    public void fileClick(boolean moreStatus) {
        isShowFile.set(moreStatus);
    }

    public void audioClick(boolean audioStatus) {
        isShowAudio.set(audioStatus);
        audioBtn.set(audioStatus);
        setBackgroundColorIsWhite(!audioStatus);
    }

    public void setBackgroundColorIsWhite(boolean backgroundColor){
        isShowWhite.set(backgroundColor);
    }

    public void setAudioRecordBtn(boolean audioRecordBtn) {
        this.audioRecordBtn.set(audioRecordBtn);
    }
}
