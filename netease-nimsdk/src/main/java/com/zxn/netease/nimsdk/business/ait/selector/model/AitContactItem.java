package com.zxn.netease.nimsdk.business.ait.selector.model;

/**
 * Created by hzchenkang on 2017/6/21.
 */

public class AitContactItem<T> {

    // view type
    private final int viewType;

    // data
    private final T model;

    public AitContactItem(int viewType, T model) {
        this.viewType = viewType;
        this.model = model;
    }

    public T getModel() {
        return model;
    }

    public int getViewType() {
        return viewType;
    }
}
