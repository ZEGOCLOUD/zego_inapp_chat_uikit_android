package im.zego.zimkitcommon.model;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import im.zego.zimkitcommon.BR;

public class BaseDialogModel extends BaseObservable {

    private String mTitle;
    private String mContent;
    private String mLeftButtonContent;
    private String mRightButtonContent;

    @Bindable
    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
        notifyPropertyChanged(BR.title);
    }

    @Bindable
    public String getContent() {
        return mContent;
    }

    public void setContent(String mContent) {
        this.mContent = mContent;
        notifyPropertyChanged(BR.content);
    }

    @Bindable
    public String getLeftButtonContent() {
        return mLeftButtonContent;
    }

    public void setLeftButtonContent(String mLeftButtonContent) {
        this.mLeftButtonContent = mLeftButtonContent;
        notifyPropertyChanged(BR.leftButtonContent);
    }

    @Bindable
    public String getRightButtonContent() {
        return mRightButtonContent;
    }

    public void setRightButtonContent(String mRightButtonContent) {
        this.mRightButtonContent = mRightButtonContent;
        notifyPropertyChanged(BR.rightButtonContent);
    }
}
