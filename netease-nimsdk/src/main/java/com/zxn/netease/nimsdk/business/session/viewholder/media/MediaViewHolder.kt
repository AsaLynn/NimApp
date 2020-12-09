package com.zxn.netease.nimsdk.business.session.viewholder.media

import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.zxn.netease.nimsdk.R

/**
 * 媒体
 */
class MediaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    @JvmField
    var mediaImage: ImageView
    @JvmField
    var playImage: ImageView

    init {
        mediaImage = itemView.findViewById(R.id.media_image)
        playImage = itemView.findViewById(R.id.play_image)
    }
}