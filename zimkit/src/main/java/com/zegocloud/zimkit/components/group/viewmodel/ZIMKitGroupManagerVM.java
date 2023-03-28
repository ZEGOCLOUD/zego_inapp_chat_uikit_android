package com.zegocloud.zimkit.components.group.viewmodel;

import androidx.databinding.ObservableField;
import androidx.lifecycle.ViewModel;

public class ZIMKitGroupManagerVM extends ViewModel {

    private final ObservableField<String> mGroupId = new ObservableField<>();

    public void setGroupId(String groupId) {
        mGroupId.set(groupId);
    }

    public ObservableField<String> getGroupId() {
        return mGroupId;
    }

}
