package com.zxn.netease.nimsdk.common.ui.recyclerview.loadmore

import com.zxn.netease.nimsdk.R

class SimpleLoadMoreView : LoadMoreView() {
    override val layoutId: Int
        = R.layout.nim_simple_load_more
    override val loadingViewId: Int
         = R.id.load_more_loading_view
    override val loadFailViewId: Int
         = R.id.load_more_load_fail_view
    override val loadEndViewId: Int = R.id.load_more_load_end_view
}