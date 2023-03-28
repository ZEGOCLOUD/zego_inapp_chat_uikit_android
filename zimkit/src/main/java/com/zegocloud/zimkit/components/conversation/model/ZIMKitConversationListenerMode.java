package com.zegocloud.zimkit.components.conversation.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.zegocloud.zimkit.components.conversation.interfaces.ZIMKitConversationListListener;

public class ZIMKitConversationListenerMode implements Parcelable {

    private ZIMKitConversationListListener listener;


    public ZIMKitConversationListenerMode() {

    }
    public ZIMKitConversationListenerMode(Parcel in) {
        this.listener = in.readParcelable(ZIMKitConversationListListener.class.getClassLoader());
    }

    public static final Creator<ZIMKitConversationListenerMode> CREATOR = new Creator<ZIMKitConversationListenerMode>() {
        @Override
        public ZIMKitConversationListenerMode createFromParcel(Parcel in) {
            return new ZIMKitConversationListenerMode(in);
        }

        @Override
        public ZIMKitConversationListenerMode[] newArray(int size) {
            return new ZIMKitConversationListenerMode[size];
        }
    };

    public ZIMKitConversationListListener getListener() {
        return listener;
    }

    public void setListener(ZIMKitConversationListListener listener) {
        this.listener = listener;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable((Parcelable) listener,0);
    }
}
