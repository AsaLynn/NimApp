package com.zxn.netease.nimsdk.business.session.viewholder.media

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.zxn.netease.nimsdk.R

/**
 * 日期.
 */
class DateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    @JvmField
    var dateText: TextView

    init {
        dateText = itemView.findViewById(R.id.date_tip)
    }
}