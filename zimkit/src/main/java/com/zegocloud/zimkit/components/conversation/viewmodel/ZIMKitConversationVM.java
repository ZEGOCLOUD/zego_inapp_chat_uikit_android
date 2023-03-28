package com.zegocloud.zimkit.components.conversation.viewmodel;

import androidx.databinding.ObservableField;
import androidx.lifecycle.ViewModel;

import com.zegocloud.zimkit.services.ZIMKit;
import com.zegocloud.zimkit.services.ZIMKitDelegate;
import com.zegocloud.zimkit.services.callback.LoadMoreConversationCallback;
import com.zegocloud.zimkit.services.model.ZIMKitConversation;
import java.util.ArrayList;
import java.util.TreeSet;

import im.zego.zim.entity.ZIMConversation;
import im.zego.zim.entity.ZIMError;
import im.zego.zim.enums.ZIMConnectionEvent;
import im.zego.zim.enums.ZIMConnectionState;
import im.zego.zim.enums.ZIMConversationEvent;
import im.zego.zim.enums.ZIMConversationType;
import im.zego.zim.enums.ZIMErrorCode;
import com.zegocloud.zimkit.components.conversation.model.ZIMKitConversationModel;
import com.zegocloud.zimkit.services.callback.GetConversationListCallback;
import com.zegocloud.zimkit.services.internal.ZIMKitCore;

public class ZIMKitConversationVM extends ViewModel {

    private static final int MAX_PAGE_COUNT = 100;

    private final ObservableField<Boolean> isListEmpty = new ObservableField<>(true);
    public final ObservableField<Boolean> isLoadFirstFail = new ObservableField<>(false);

    private final TreeSet<ZIMKitConversationModel> mItemModelCacheTreeSet = new TreeSet<>((model1, model2) -> {
        //Sort by orderKey
        long value = model2.getConversation().orderKey - model1.getConversation().orderKey;
        if (value == 0) {
            return 0;
        }
        return value > 0 ? 1 : -1;
    });
    public final ObservableField<Integer> totalUnReadCount = new ObservableField<>();

    private OnLoadConversationListener mLoadConversationListener;
    //Connection Status Listening
    private IConnectionStateListener mStateListener;

    public ZIMKitConversationVM() {
        ZIMKit.registerZIMKitDelegate(eventCallBack);
        int totalUnreadMessageCount = ZIMKitCore.getInstance().getTotalUnreadMessageCount();
        if (totalUnreadMessageCount > 0) {
            totalUnReadCount.set(totalUnreadMessageCount);
        }
    }

    private final ZIMKitDelegate eventCallBack = new ZIMKitDelegate() {
        @Override
        public void onConnectionStateChange(ZIMConnectionState state, ZIMConnectionEvent event) {
            if (mStateListener != null) {
                mStateListener.onConnectionStateChange(event, state);
            }
        }

        @Override
        public void onTotalUnreadMessageCountChange(int totalCount) {
            totalUnReadCount.set(totalCount);
        }

        @Override
        public void onConversationListChanged(ArrayList<ZIMKitConversation> conversations) {

            if (conversations.isEmpty()) {
                return;
            }
            mItemModelCacheTreeSet.clear();
            for (ZIMKitConversation info : conversations) {
                ZIMKitConversationModel viewModel = new ZIMKitConversationModel(info.getZim(), ZIMConversationEvent.ADDED);
                mItemModelCacheTreeSet.add(viewModel);
            }

            postList(conversations.isEmpty(), true, null, LoadData.DATA_STATE_CHANGE);

        }
    };

    public void loadConversation() {
        ZIMKit.getConversationList(new GetConversationListCallback() {
            @Override
            public void onGetConversationList(ArrayList<ZIMKitConversation> conversations, ZIMError error) {
                int state = LoadData.DATA_STATE_LOAD_FIRST;
                if (error.code == ZIMErrorCode.SUCCESS) {
                    handleConversationListLoad(conversations, state);
                } else {
                    postList(true, false, error, state);
                }
            }
        });
    }

    public void loadNextPage() {
        ZIMKit.loadMoreConversation(new LoadMoreConversationCallback() {
            @Override
            public void onLoadMoreConversation(ZIMError error) {
                int state = LoadData.DATA_STATE_LOAD_NEXT;
                if (error.code != ZIMErrorCode.SUCCESS) {
                    postList(true, false, error, state);
                }
            }
        });
    }

    public void clearConversationUnreadMessageCount(String conversationID, ZIMConversationType type) {
        ZIMKit.clearUnreadCount(conversationID, type, null);
    }

    private void handleConversationListLoad(ArrayList<ZIMKitConversation> newConversationList, int state) {
        if (newConversationList.isEmpty()) {
            postList(true, true, null, state);
            return;
        }
        ArrayList<ZIMKitConversationModel> newViewModels = new ArrayList<>();
        for (ZIMKitConversation zimConversation : newConversationList) {
            newViewModels.add(new ZIMKitConversationModel(zimConversation.getZim(), ZIMConversationEvent.ADDED));
        }
        mItemModelCacheTreeSet.addAll(newViewModels);
        postList(false, true, null, state);
    }

    public void deleteConversation(ZIMConversation conversation) {
        ZIMKit.deleteConversation(conversation.conversationID, conversation.type, null);
    }

    public ObservableField<Boolean> isListEmpty() {
        return isListEmpty;
    }

    public void postList(boolean isEmpty, boolean isSuccess, ZIMError zimError, int state) {
        LoadData loadData = new LoadData(isEmpty, mItemModelCacheTreeSet, state);
        if (mLoadConversationListener != null) {
            if (isSuccess) {
                mLoadConversationListener.onSuccess(loadData);
            } else {
                mLoadConversationListener.onFail(zimError);
            }
        }
        if (state == LoadData.DATA_STATE_LOAD_FIRST) {
            isLoadFirstFail.set(!isSuccess);
        }
        isListEmpty.set(mItemModelCacheTreeSet.isEmpty());
    }

    public void logout() {
        if (mItemModelCacheTreeSet != null) {
            mItemModelCacheTreeSet.clear();
        }
        ZIMKit.disconnectUser();
    }

    public static class LoadData {
        public final static int DATA_STATE_CHANGE = 0;
        public final static int DATA_STATE_LOAD_FIRST = 1;
        public final static int DATA_STATE_LOAD_NEXT = 2;
        public final static int DATA_STATE_LOAD_DELETE = 3;

        public boolean currentLoadIsEmpty;
        public TreeSet<ZIMKitConversationModel> allList;
        public int state;

        public LoadData(boolean isEmpty, TreeSet<ZIMKitConversationModel> allList, int state) {
            this.currentLoadIsEmpty = isEmpty;
            this.allList = allList;
            this.state = state;
        }

    }

    public void setOnLoadConversationListener(OnLoadConversationListener listener) {
        mLoadConversationListener = listener;
    }

    public interface OnLoadConversationListener {
        void onSuccess(LoadData data);

        void onFail(ZIMError error);
    }

    public void setConnectionStateListener(IConnectionStateListener mStateListener) {
        this.mStateListener = mStateListener;
    }

    public interface IConnectionStateListener {
        void onConnectionStateChange(ZIMConnectionEvent connectionEvent, ZIMConnectionState connectionState);
    }

    @Override
    protected void onCleared() {
        if (mItemModelCacheTreeSet != null) {
            mItemModelCacheTreeSet.clear();
        }
        ZIMKit.unRegisterZIMKitDelegate(eventCallBack);
        super.onCleared();
    }

}
