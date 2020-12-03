package com.zxn.netease.nimsdk.common.ui.recyclerview.loadmore;

import com.zxn.netease.nimsdk.R;

public final class MsgListFetchLoadMoreView extends LoadMoreView {

    private int mLayoutId = 0;

    public MsgListFetchLoadMoreView() {

    }

    public MsgListFetchLoadMoreView(int layoutId) {
        mLayoutId = layoutId;
    }

    @Override
    public int getLayoutId() {
        if (mLayoutId != 0){
            return mLayoutId;
        }
        return R.layout.nim_msg_list_fetch_load_more;
    }

    @Override
    protected int getLoadingViewId() {
        return R.id.load_more_loading_view;
    }

    @Override
    protected int getLoadFailViewId() {
        return R.id.load_more_load_fail_view;
    }

    @Override
    protected int getLoadEndViewId() {
        return R.id.load_more_load_end_view;
    }
}
