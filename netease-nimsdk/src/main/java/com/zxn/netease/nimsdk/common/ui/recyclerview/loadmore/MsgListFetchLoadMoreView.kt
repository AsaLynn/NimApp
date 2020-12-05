package com.zxn.netease.nimsdk.common.ui.recyclerview.loadmore

import com.zxn.netease.nimsdk.R

class MsgListFetchLoadMoreView : LoadMoreView() {
    private val mLayoutId = 0
    override val layoutId: Int = R.layout.nim_msg_list_fetch_load_more
    override val loadingViewId: Int = R.id.load_more_loading_view
    override val loadFailViewId: Int = R.id.load_more_load_fail_view
    override val loadEndViewId: Int = R.id.load_more_load_end_view
}