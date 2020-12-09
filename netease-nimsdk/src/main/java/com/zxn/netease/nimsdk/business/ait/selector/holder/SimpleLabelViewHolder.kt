package com.zxn.netease.nimsdk.business.ait.selector.holder

import android.widget.TextView
import com.zxn.netease.nimsdk.R
import com.zxn.netease.nimsdk.business.ait.selector.model.AitContactItem
import com.zxn.netease.nimsdk.common.ui.recyclerview.adapter.BaseQuickAdapter
import com.zxn.netease.nimsdk.common.ui.recyclerview.holder.BaseViewHolder
import com.zxn.netease.nimsdk.common.ui.recyclerview.holder.RecyclerViewHolder

class SimpleLabelViewHolder(adapter: BaseQuickAdapter<*, *>) :
    RecyclerViewHolder<BaseQuickAdapter<*, *>, BaseViewHolder, AitContactItem<String>>(adapter) {

    private var textView: TextView? = null

    override fun convert(
        holder: BaseViewHolder,
        data: AitContactItem<String>,
        position: Int,
        isScrolling: Boolean
    ) {
        inflate(holder)
        refresh(data.model)
    }

    fun inflate(holder: BaseViewHolder) {
        textView = holder.getView<TextView>(R.id.tv_label)
    }

    fun refresh(label: String?) {
        textView!!.text = label
    }
}