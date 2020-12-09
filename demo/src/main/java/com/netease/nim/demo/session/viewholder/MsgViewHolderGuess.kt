package com.netease.nim.demo.session.viewholder

import android.widget.ImageView
import com.netease.nim.demo.R
import com.netease.nim.demo.session.extension.GuessAttachment
import com.zxn.netease.nimsdk.business.session.viewholder.MsgViewHolderBase
import com.zxn.netease.nimsdk.common.ui.recyclerview.adapter.BaseMultiItemFetchLoadAdapter

class MsgViewHolderGuess(adapter: BaseMultiItemFetchLoadAdapter<*, *>?) :
    MsgViewHolderBase(adapter) {
    private var guessAttachment: GuessAttachment? = null
    private var imageView: ImageView? = null
    override fun getContentResId(): Int {
        return R.layout.rock_paper_scissors
    }

    override fun inflateContentView() {
        imageView = view.findViewById(R.id.rock_paper_scissors_text)
    }

    override fun bindContentView() {
        if (message.attachment == null) {
            return
        }
        guessAttachment = message.attachment as GuessAttachment
        when (guessAttachment!!.value.desc) {
            "石头" -> imageView!!.setImageResource(R.drawable.message_view_rock)
            "剪刀" -> imageView!!.setImageResource(R.drawable.message_view_scissors)
            "布" -> imageView!!.setImageResource(R.drawable.message_view_paper)
            else -> {
            }
        }
    }
}