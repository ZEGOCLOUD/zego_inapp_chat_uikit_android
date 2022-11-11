package im.zego.zimkitconversation.viewmodel;

import androidx.databinding.ObservableField;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.TreeSet;

import im.zego.zim.entity.ZIMConversation;
import im.zego.zim.entity.ZIMConversationChangeInfo;
import im.zego.zim.entity.ZIMConversationDeleteConfig;
import im.zego.zim.entity.ZIMConversationQueryConfig;
import im.zego.zim.entity.ZIMError;
import im.zego.zim.enums.ZIMConnectionEvent;
import im.zego.zim.enums.ZIMConnectionState;
import im.zego.zim.enums.ZIMConversationEvent;
import im.zego.zim.enums.ZIMConversationType;
import im.zego.zim.enums.ZIMErrorCode;
import im.zego.zimkitcommon.ZIMKitConstant;
import im.zego.zimkitcommon.ZIMKitManager;
import im.zego.zimkitcommon.event.IZIMKitEventCallBack;
import im.zego.zimkitcommon.event.ZIMKitEventHandler;
import im.zego.zimkitconversation.model.ZIMKitConversationModel;

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
    public final ObservableField<Integer> totalUnReadCount = new ObservableField<>(0);

    private OnLoadConversationListener mLoadConversationListener;
    //Connection Status Listening
    private IConnectionStateListener mStateListener;

    public ZIMKitConversationVM() {
        ZIMKitEventHandler.share().addEventListener(ZIMKitConstant.EventConstant.KEY_CONVERSATION_TOTAL_UNREAD_MESSAGE_COUNT_UPDATED, this, eventCallBack);
        ZIMKitEventHandler.share().addEventListener(ZIMKitConstant.EventConstant.KEY_CONVERSATION_CHANGED, this, eventCallBack);
        ZIMKitEventHandler.share().addEventListener(ZIMKitConstant.EventConstant.KEY_CONNECTION_STATE_CHANGED, this, eventCallBack);
    }

    private final IZIMKitEventCallBack eventCallBack = (key, event) -> {
        if (key.equals(ZIMKitConstant.EventConstant.KEY_CONVERSATION_CHANGED)) {
            List<ZIMConversationChangeInfo> info = (List<ZIMConversationChangeInfo>) event.get(ZIMKitConstant.EventConstant.PARAM_CONVERSATION_CHANGE_INFO_LIST);
            if (info == null) {
                return;
            }
            handlerConversationChange(info);
        } else if (key.equals(ZIMKitConstant.EventConstant.KEY_CONVERSATION_TOTAL_UNREAD_MESSAGE_COUNT_UPDATED)) {
            int totalCount = (int) event.get(ZIMKitConstant.EventConstant.PARAM_TOTAL_UNREAD_MESSAGE_COUNT);
            totalUnReadCount.set(totalCount);
        } else if (key.equals(ZIMKitConstant.EventConstant.KEY_CONNECTION_STATE_CHANGED)) {
            if (mStateListener != null) {
                ZIMConnectionEvent connectionEvent = (ZIMConnectionEvent) event.get(ZIMKitConstant.EventConstant.PARAM_EVENT);
                ZIMConnectionState connectionState = (ZIMConnectionState) event.get(ZIMKitConstant.EventConstant.PARAM_STATE);
                mStateListener.onConnectionStateChange(connectionEvent, connectionState);
            }
        }
    };

    private void handlerConversationChange(List<ZIMConversationChangeInfo> infos) {
        for (ZIMConversationChangeInfo info : infos) {
            if (info.event == ZIMConversationEvent.ADDED) {
                ZIMKitConversationModel viewModel = new ZIMKitConversationModel(info.conversation, ZIMConversationEvent.ADDED);
                mItemModelCacheTreeSet.add(viewModel);
            } else if (info.event == ZIMConversationEvent.UPDATED) {
                // Incremental Updates
                ZIMKitConversationModel oldModel = null;
                for (ZIMKitConversationModel model : mItemModelCacheTreeSet) {
                    if (model.getConversation().conversationID.equals(info.conversation.conversationID)) {
                        oldModel = model;
                        break;
                    }
                }
                if (oldModel != null) {
                    mItemModelCacheTreeSet.remove(oldModel);
                    mItemModelCacheTreeSet.add(new ZIMKitConversationModel(info.conversation, ZIMConversationEvent.UPDATED));
                }
            } else if (info.event == ZIMConversationEvent.DISABLED) {
                ZIMKitConversationModel oldModel = null;
                for (ZIMKitConversationModel model : mItemModelCacheTreeSet) {
                    if (model.getConversation().conversationID.equals(info.conversation.conversationID)) {
                        oldModel = model;
                        break;
                    }
                }
                if (oldModel != null) {
                    mItemModelCacheTreeSet.remove(oldModel);
                    mItemModelCacheTreeSet.add(new ZIMKitConversationModel(info.conversation, ZIMConversationEvent.DISABLED));
                }
            }
        }
        postList(infos.isEmpty(), true, null, LoadData.DATA_STATE_CHANGE);
    }

    public void loadConversation() {
        loadConversation(null);
    }

    private void loadConversation(ZIMConversation conversation) {
        ZIMConversationQueryConfig config = new ZIMConversationQueryConfig();
        config.count = MAX_PAGE_COUNT;
        config.nextConversation = conversation;
        ZIMKitManager.share().zim().queryConversationList(config, (conversationList, errorInfo) -> {
            int state = conversation == null ? LoadData.DATA_STATE_LOAD_FIRST : LoadData.DATA_STATE_LOAD_NEXT;
            if (errorInfo.code == ZIMErrorCode.SUCCESS) {
                handleConversationListLoad(conversationList, state);
            } else {
                postList(true, false, errorInfo, state);
            }
        });
    }

    public void loadNextPage() {
        try {
            ZIMKitConversationModel lastModel = mItemModelCacheTreeSet.last();
            if (lastModel != null) {
                ZIMConversation conversation = lastModel.getConversation();
                loadConversation(conversation);
            } else {
                postLoadNextPageCustomError();
            }
        } catch (NoSuchElementException e) {
            e.printStackTrace();
            postLoadNextPageCustomError();
        }
    }

    public void clearConversationUnreadMessageCount(String conversationID, ZIMConversationType type) {
        ZIMKitManager.share().zim().clearConversationUnreadMessageCount(conversationID, type, null);
    }

    private void postLoadNextPageCustomError() {
        ZIMError error = new ZIMError();
        error.code = ZIMErrorCode.FAILED;
        error.message = "not next page";
        postList(true, false, error, LoadData.DATA_STATE_LOAD_NEXT);
    }

    private void handleConversationListLoad(ArrayList<ZIMConversation> newConversationList, int state) {
        if (newConversationList.isEmpty()) {
            postList(true, true, null, state);
            return;
        }
        ArrayList<ZIMKitConversationModel> newViewModels = new ArrayList<>();
        for (ZIMConversation zimConversation : newConversationList) {
            newViewModels.add(new ZIMKitConversationModel(zimConversation, ZIMConversationEvent.ADDED));
        }
        mItemModelCacheTreeSet.addAll(newViewModels);
        postList(false, true, null, state);
    }

    public void deleteConversation(ZIMConversation conversation) {
        ZIMKitManager.share().zim().deleteConversation(conversation.conversationID, conversation.type
                , new ZIMConversationDeleteConfig(), (conversationID, conversationType, errorInfo) -> {
                    if (errorInfo.code == ZIMErrorCode.SUCCESS) {
                        Iterator<ZIMKitConversationModel> iterator = mItemModelCacheTreeSet.iterator();
                        while (iterator.hasNext()) {
                            ZIMKitConversationModel model = iterator.next();
                            if (model.getConversationID().equals(conversationID) && conversationType == model.getType()) {
                                iterator.remove();
                                break;
                            }
                        }
                    }
                    postList(mItemModelCacheTreeSet.isEmpty(), errorInfo.code == ZIMErrorCode.SUCCESS, errorInfo, LoadData.DATA_STATE_LOAD_DELETE);
                });
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
        ZIMKitManager.share().disconnectUser();
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
        ZIMKitEventHandler.share().removeEventListener(ZIMKitConstant.EventConstant.KEY_CONVERSATION_TOTAL_UNREAD_MESSAGE_COUNT_UPDATED, this);
        ZIMKitEventHandler.share().removeEventListener(ZIMKitConstant.EventConstant.KEY_CONVERSATION_CHANGED, this);
        ZIMKitEventHandler.share().removeEventListener(ZIMKitConstant.EventConstant.KEY_CONNECTION_STATE_CHANGED, this);
        super.onCleared();
    }

}
