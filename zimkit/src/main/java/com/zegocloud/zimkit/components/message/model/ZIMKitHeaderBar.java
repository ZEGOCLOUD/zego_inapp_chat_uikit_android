package com.zegocloud.zimkit.components.message.model;

import android.view.View;

public class ZIMKitHeaderBar {

    private View titleView;
    private View leftView;
    private View rightView;

    public View getTitleView() {
        return titleView;
    }

    public void setTitleView(View titleView) {
        this.titleView = titleView;
    }

    public View getLeftView() {
        return leftView;
    }

    public void setLeftView(View leftView) {
        this.leftView = leftView;
    }

    public View getRightView() {
        return rightView;
    }

    public void setRightView(View rightView) {
        this.rightView = rightView;
    }
}
