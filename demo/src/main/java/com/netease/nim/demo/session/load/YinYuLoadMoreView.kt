package com.netease.nim.demo.session.load

import com.netease.nim.demo.R
import com.zxn.netease.nimsdk.common.ui.recyclerview.loadmore.LoadMoreView
import java.io.Serializable

class YinYuLoadMoreView : LoadMoreView(), Serializable {
    override val layoutId: Int = R.layout.yy_nim_top_load_more
    override val loadingViewId: Int = R.id.load_more_loading_view
    override val loadFailViewId: Int = R.id.load_more_load_fail_view
    override val loadEndViewId: Int = R.id.load_more_load_end_view
}