package com.netease.nim.demo.session.viewholder

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.netease.nim.demo.R
import com.netease.nim.demo.session.extension.StickerAttachment
import com.zxn.netease.nimsdk.business.session.emoji.StickerManager
import com.zxn.netease.nimsdk.business.session.viewholder.MsgViewHolderBase
import com.zxn.netease.nimsdk.business.session.viewholder.MsgViewHolderThumbBase
import com.zxn.netease.nimsdk.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter

class MsgViewHolderSticker(adapter: BaseMultiItemFetchLoadAdapter<*, *>) :
    MsgViewHolderBase(adapter) {
    private var baseView: ImageView? = null

    override val contentResId: Int = R.layout.nim_message_item_sticker


    override fun inflateContentView() {
        baseView = findViewById(R.id.message_item_sticker_image)
        baseView?.setMaxWidth(MsgViewHolderThumbBase.imageMaxEdge)
    }

    override fun bindContentView() {
        message?.let { message ->
            val attachment = message.attachment as StickerAttachment ?: return
            Glide.with(context!!)
                .load(
                    StickerManager.getInstance()
                        .getStickerUri(attachment.catalog, attachment.chartlet)
                )
                .apply(
                    RequestOptions()
                        .error(R.drawable.nim_default_img_failed)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                )
                .into(baseView!!)
        }
    }

    override fun leftBackground(): Int {
        return R.drawable.nim_message_left_white_bg
    }

    override fun rightBackground(): Int {
        return R.drawable.nim_message_right_blue_bg
    }
}