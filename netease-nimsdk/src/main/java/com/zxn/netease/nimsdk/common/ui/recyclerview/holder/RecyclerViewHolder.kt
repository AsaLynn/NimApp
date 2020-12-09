package com.zxn.netease.nimsdk.common.ui.recyclerview.holder

import androidx.recyclerview.widget.RecyclerView

abstract class RecyclerViewHolder<T : RecyclerView.Adapter<*>, V : BaseViewHolder, K>(open val adapter: T) {

    abstract fun convert(holder: V, data: K, position: Int, isScrolling: Boolean)

}