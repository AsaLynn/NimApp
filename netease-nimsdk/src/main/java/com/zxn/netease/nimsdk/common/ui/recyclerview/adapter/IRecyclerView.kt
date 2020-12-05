package com.zxn.netease.nimsdk.common.ui.recyclerview.adapter

/**
 * 定义条目视图类型.
 */
interface IRecyclerView {
    /**
     * 获取Header item的数量（包含FetchItem）
     */
    val headerLayoutCount: Int

    /**
     * 获取Item视图类型
     *
     * @param position Item位置
     * @return
     */
    fun getItemViewType(position: Int): Int

    companion object {
        /**
         * special view type
         */
        const val FETCHING_VIEW = 0x00001000
        const val HEADER_VIEW = 0x00001001
        const val LOADING_VIEW = 0x00001002
        const val FOOTER_VIEW = 0x00001003
        const val EMPTY_VIEW = 0x00001004
    }
}