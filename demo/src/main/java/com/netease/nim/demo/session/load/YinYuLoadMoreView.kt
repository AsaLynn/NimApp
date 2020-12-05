package com.netease.nim.demo.session.load;

import com.netease.nim.demo.R;
import com.zxn.netease.nimsdk.common.ui.recyclerview.loadmore.LoadMoreView;

import java.io.Serializable;

public final class YinYuLoadMoreView extends LoadMoreView implements Serializable {

    @Override
    public int getLayoutId() {
        return R.layout.yy_nim_top_load_more;
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
